package com.example.android_practice.favorites.data.local

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.util.Base64

class FileFavoriteArtworkStoreTest {
    @get:Rule
    val folder = TemporaryFolder()
    private val server = MockWebServer()
    private val client = OkHttpClient()
    private val png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jzfoAAAAASUVORK5CYII=")

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun downloadedImageSurvivesStoreRecreationWithoutNetwork() = runBlocking {
        server.enqueue(MockResponse().setHeader("Content-Type", "image/png").setBody(Buffer().write(png)))
        val directory = folder.newFolder("artwork")
        val url = server.url("/image.png").toString()
        FileFavoriteArtworkStore(directory, client).save(1, url)
        val recreated = FileFavoriteArtworkStore(directory, client)
        recreated.save(1, url)

        assertNotNull(recreated.find(1))
        assertArrayEquals(png, File(requireNotNull(recreated.find(1))).readBytes())
        assertEquals(1, server.requestCount)
        recreated.remove(1)
        assertNull(recreated.find(1))
    }

    @Test
    fun failedDownloadLeavesNoIncompleteImage() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503))
        val directory = folder.newFolder("artwork")
        val store = FileFavoriteArtworkStore(directory, client)
        try {
            store.save(25, server.url("/image.png").toString())
            fail("Expected download failure")
        } catch (_: IOException) {
        }

        assertNull(store.find(25))
        assertEquals(0, directory.listFiles()?.size)
    }

    @Test
    fun missingImageDoesNotSendRequest() = runBlocking {
        val store = FileFavoriteArtworkStore(folder.newFolder("artwork"), client)
        store.save(25, null)
        assertNull(store.find(25))
        assertEquals(0, server.requestCount)
    }
}
