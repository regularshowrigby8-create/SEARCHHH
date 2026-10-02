package info.plateaukao.einkbro.unit

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException

class SupernoteStorageTest {
    private val context = mockk<Context>()
    private val resolver = mockk<ContentResolver>()
    private val treeUri = mockk<Uri>()
    private val destinationUri = mockk<Uri>()
    private val tree = mockk<DocumentFile>()
    private val document = mockk<DocumentFile>()
    private val previous = mockk<DocumentFile>()
    private val stream = TrackingStream()

    @Before
    fun prepareProviderBoundary() {
        mockkStatic(DocumentFile::class)
        every { DocumentFile.fromTreeUri(context, treeUri) } returns tree
        every { tree.findFile(FILE_NAME) } returns previous
        every { previous.delete() } returns true
        every { tree.createFile(any(), FILE_NAME) } returns document
        every { document.uri } returns destinationUri
        every { context.contentResolver } returns resolver
        every { resolver.openOutputStream(destinationUri) } returns stream
    }

    @After
    fun releaseStaticMock() {
        unmockkStatic(DocumentFile::class)
    }

    @Test
    fun successfulWriteClosesBeforeReturningUriAndPreservesReplacementOrder() {
        val bytes = "actual document bytes".toByteArray()
        val result =
            SupernoteStorage.write(context, treeUri, FILE_NAME, MIME_TYPE) {
                assertSame(stream, it)
                assertEquals(0, stream.closes)
                it.write(bytes)
            }
        assertSame(destinationUri, result)
        assertArrayEquals(bytes, stream.toByteArray())
        assertEquals(1, stream.closes)
        verifyOrder {
            tree.findFile(FILE_NAME)
            previous.delete()
            tree.createFile(MIME_TYPE, FILE_NAME)
            resolver.openOutputStream(destinationUri)
        }
    }

    @Test
    fun blankMimeUsesOctetStreamAndNewNameDoesNotRequireDeletion() {
        every { tree.findFile(FILE_NAME) } returns null
        assertSame(destinationUri, SupernoteStorage.write(context, treeUri, FILE_NAME, " ") { it.write(1) })
        assertEquals(1, stream.closes)
        verify(exactly = 1) { tree.createFile("application/octet-stream", FILE_NAME) }
        verify(exactly = 0) { previous.delete() }
    }

    @Test
    fun unavailableTreeDoesNotWriteOrOpenStream() {
        every { DocumentFile.fromTreeUri(context, treeUri) } returns null
        assertUnavailableDestination()
        verify(exactly = 0) { resolver.openOutputStream(any()) }
    }

    @Test
    fun failedDocumentCreationDoesNotWriteOrOpenStream() {
        every { tree.createFile(any(), FILE_NAME) } returns null
        assertUnavailableDestination()
        verify(exactly = 0) { resolver.openOutputStream(any()) }
    }

    @Test
    fun nullOutputStreamDoesNotInvokeWriterOrReturnSuccess() {
        every { resolver.openOutputStream(destinationUri) } returns null
        assertUnavailableDestination()
    }

    @Test
    fun openFailureReachesCallerWithoutInvokingWriter() {
        val failure = IOException("provider open failed")
        every { resolver.openOutputStream(destinationUri) } throws failure
        var invoked = false
        val actual =
            assertThrows(IOException::class.java) {
                SupernoteStorage.write(context, treeUri, FILE_NAME, MIME_TYPE) { invoked = true }
            }
        assertSame(failure, actual)
        assertFalse(invoked)
        assertEquals(0, stream.closes)
    }

    @Test
    fun writeFailureStillClosesAndReachesCaller() {
        val failure = IOException("write failed")
        val actual =
            assertThrows(IOException::class.java) {
                SupernoteStorage.write(context, treeUri, FILE_NAME, MIME_TYPE) { throw failure }
            }
        assertSame(failure, actual)
        assertEquals(1, stream.closes)
    }

    @Test
    fun closeFailureCannotReturnSuccessfulUri() {
        val failure = IOException("close failed")
        stream.closeFailure = failure
        val actual =
            assertThrows(IOException::class.java) {
                SupernoteStorage.write(context, treeUri, FILE_NAME, MIME_TYPE) { it.write(1) }
            }
        assertSame(failure, actual)
        assertEquals(1, stream.closes)
    }

    @Test
    fun simultaneousWriteAndCloseFailuresPreservePrimaryAndSuppressedCause() {
        val writeFailure = IOException("write failed")
        val closeFailure = IOException("close failed")
        stream.closeFailure = closeFailure
        val actual =
            assertThrows(IOException::class.java) {
                SupernoteStorage.write(context, treeUri, FILE_NAME, MIME_TYPE) { throw writeFailure }
            }
        assertSame(writeFailure, actual)
        assertArrayEquals(arrayOf(closeFailure), actual.suppressed)
        assertEquals(1, stream.closes)
    }

    private fun assertUnavailableDestination() {
        var invoked = false
        val result = SupernoteStorage.write(context, treeUri, FILE_NAME, MIME_TYPE) { invoked = true }
        assertNull(result)
        assertFalse(invoked)
        assertEquals(0, stream.closes)
    }

    private class TrackingStream : ByteArrayOutputStream() {
        var closes = 0
        var closeFailure: IOException? = null

        override fun close() {
            closes++
            super.close()
            closeFailure?.let { throw it }
        }
    }

    private companion object {
        const val FILE_NAME = "document.txt"
        const val MIME_TYPE = "text/plain"
    }
}
