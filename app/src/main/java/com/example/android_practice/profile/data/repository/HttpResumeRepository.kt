package com.example.android_practice.profile.data.repository

import com.example.android_practice.profile.domain.model.ResumeDocument
import com.example.android_practice.profile.domain.model.ResumeError
import com.example.android_practice.profile.domain.model.ResumeException
import com.example.android_practice.profile.domain.repository.ResumeRepository
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HttpResumeRepository(
    private val directory: File,
    private val client: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ResumeRepository {
    private val mutex = Mutex()

    override suspend fun download(url: String): ResumeDocument = withContext(ioDispatcher) {
        val parsed = url.trim().toHttpUrlOrNull() ?: throw ResumeException(ResumeError.INVALID_URL)
        mutex.withLock {
            try {
                if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Cannot create resume directory")
                val hash = MessageDigest.getInstance("SHA-256").digest(parsed.toString().toByteArray())
                    .joinToString("") { "%02x".format(it) }
                val target = File(directory, "resume-$hash.pdf")
                if (!isPdf(target)) {
                    if (target.exists() && !target.delete()) throw IOException("Cannot replace resume")
                    downloadFile(Request.Builder().url(parsed).build(), target)
                }
                ResumeDocument(target.absolutePath)
            } catch (error: IOException) {
                throw ResumeException(ResumeError.STORAGE, error)
            }
        }
    }

    private suspend fun downloadFile(request: Request, target: File) = suspendCancellableCoroutine<Unit> { continuation ->
        val call = client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(ResumeException(ResumeError.NETWORK, e))
            }

            override fun onResponse(call: Call, response: Response) {
                var temporary: File? = null
                var failure: Exception? = null
                try {
                    response.use {
                        if (!it.isSuccessful) throw ResumeException(ResumeError.SERVER)
                        if (it.body.contentLength() > MAX_BYTES) throw ResumeException(ResumeError.INVALID_FILE)
                        val pending = File.createTempFile("resume-", ".tmp", directory)
                        temporary = pending
                        it.body.byteStream().use { input ->
                            pending.outputStream().use { output ->
                                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                                var size = 0L
                                while (true) {
                                    if (call.isCanceled()) throw IOException("Download cancelled")
                                    val read = try { input.read(buffer) } catch (error: IOException) {
                                        throw ResumeException(ResumeError.NETWORK, error)
                                    }
                                    if (read < 0) break
                                    size += read
                                    if (size > MAX_BYTES) throw ResumeException(ResumeError.INVALID_FILE)
                                    output.write(buffer, 0, read)
                                }
                            }
                        }
                        if (!isPdf(pending)) throw ResumeException(ResumeError.INVALID_FILE)
                        if (call.isCanceled()) throw IOException("Download cancelled")
                        if (!pending.renameTo(target)) throw IOException("Cannot save resume")
                    }
                } catch (error: Exception) {
                    failure = if (error is IOException) ResumeException(ResumeError.STORAGE, error) else error
                } finally {
                    temporary?.delete()
                }
                val error = failure
                if (error == null) continuation.resume(Unit) else continuation.resumeWithException(error)
            }
        })
    }

    private fun isPdf(file: File): Boolean {
        if (!file.isFile || file.length() == 0L) return false
        return file.inputStream().use { input ->
            val header = ByteArray(1024)
            val count = input.read(header)
            count > 0 && String(header, 0, count, Charsets.ISO_8859_1)
                .trimStart('\uFEFF', ' ', '\r', '\n', '\t').startsWith("%PDF-")
        }
    }

    private companion object {
        const val MAX_BYTES = 20L * 1024 * 1024
    }
}
