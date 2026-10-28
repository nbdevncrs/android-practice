package com.example.android_practice.favorites.data.local

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FileFavoriteArtworkStore(
    private val directory: File,
    private val client: OkHttpClient,
) : FavoriteArtworkStore {
    override fun find(id: Int): String? = imageFile(id).takeIf { it.isFile && it.length() > 0 }?.absolutePath

    override suspend fun save(id: Int, imageUrl: String?) {
        if (imageUrl.isNullOrBlank() || find(id) != null) return
        require(id > 0)
        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Cannot create artwork directory")
        val request = Request.Builder().url(imageUrl).build()
        suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    var temporary: File? = null
                    try {
                        response.use {
                            if (!it.isSuccessful) throw IOException("Artwork request failed: ${it.code}")
                            if (it.body.contentType()?.type != "image") throw IOException("Unexpected artwork content")
                            val pending = File.createTempFile("pokemon-$id-", ".tmp", directory)
                            temporary = pending
                            it.body.byteStream().use { input -> pending.outputStream().use { output -> input.copyTo(output) } }
                            if (call.isCanceled()) throw IOException("Artwork request cancelled")
                            if (pending.length() == 0L || !pending.renameTo(imageFile(id))) {
                                throw IOException("Cannot save artwork")
                            }
                        }
                        continuation.resume(Unit)
                    } catch (error: Exception) {
                        continuation.resumeWithException(error)
                    } finally {
                        temporary?.delete()
                    }
                }
            })
        }
    }

    override fun remove(id: Int) {
        val file = imageFile(id)
        if (file.exists() && !file.delete()) throw IOException("Cannot remove artwork")
    }

    private fun imageFile(id: Int): File = File(directory, "$id.img")
}
