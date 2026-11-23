package com.example.android_practice.profile.data.repository

import com.example.android_practice.profile.domain.model.ResumeError
import com.example.android_practice.profile.domain.model.ResumeException
import java.io.File
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class HttpResumeRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()
    private val server = MockWebServer()
    private val client = OkHttpClient()
    private val pdf = "%PDF-1.4\n1 0 obj<</Type/Catalog>>endobj\n%%EOF"

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun pdfIsSavedAndReusedAfterRepositoryRecreation() = runBlocking {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/octet-stream").setBody(pdf))
        val directory = folder.newFolder("resumes")
        val url = server.url("/resume").toString()
        val first = HttpResumeRepository(directory, client).download(url)
        val reopened = HttpResumeRepository(directory, client).download(url)

        assertEquals(pdf, File(first.localPath).readText())
        assertEquals(first, reopened)
        assertEquals(1, server.requestCount)
        assertTrue(first.localPath.endsWith(".pdf"))
    }

    @Test
    fun htmlResponseIsRejectedEvenWhenContentTypeClaimsPdf() = runBlocking {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/pdf").setBody("<html>Sign in</html>"))
        val directory = folder.newFolder("resumes")
        val repository = HttpResumeRepository(directory, client)

        expectError(ResumeError.INVALID_FILE) { repository.download(server.url("/resume.pdf").toString()) }
        assertTrue(directory.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun serverFailureDoesNotPreventRetry() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setBody(pdf))
        val repository = HttpResumeRepository(folder.newFolder("resumes"), client)
        val url = server.url("/resume.pdf").toString()

        expectError(ResumeError.SERVER) { repository.download(url) }
        assertEquals(pdf, File(repository.download(url).localPath).readText())
        assertEquals(2, server.requestCount)
    }

    @Test
    fun invalidUrlAndOversizedResponseAreRejected() = runBlocking {
        val directory = folder.newFolder("resumes")
        val repository = HttpResumeRepository(directory, client)
        expectError(ResumeError.INVALID_URL) { repository.download("file:///resume.pdf") }
        assertEquals(0, server.requestCount)
        server.enqueue(MockResponse().setBody(pdf).setHeader("Content-Length", 21L * 1024 * 1024))
        expectError(ResumeError.INVALID_FILE) { repository.download(server.url("/large.pdf").toString()) }
        assertTrue(directory.listFiles().orEmpty().isEmpty())
    }

    private suspend fun expectError(reason: ResumeError, block: suspend () -> Unit) {
        try {
            block()
            fail("Expected $reason")
        } catch (error: ResumeException) {
            assertEquals(reason, error.error)
        }
    }
}
