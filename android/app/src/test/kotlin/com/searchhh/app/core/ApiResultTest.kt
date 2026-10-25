package com.searchhh.app.core

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiResultTest {

    @Test
    fun `unknown host maps to offline`() {
        assertEquals(AppError.OFFLINE, UnknownHostException("dns").toAppError())
    }

    @Test
    fun `io exception maps to offline`() {
        assertEquals(AppError.OFFLINE, IOException("socket").toAppError())
    }

    @Test
    fun `socket timeout maps to timeout`() {
        assertEquals(AppError.TIMEOUT, SocketTimeoutException("slow").toAppError())
    }

    @Test
    fun `unknown throwable maps to unknown`() {
        assertEquals(AppError.UNKNOWN, IllegalStateException("boom").toAppError())
    }

    @Test
    fun `apiCall wraps success`() = runTest {
        val result = apiCall { 42 }
        assertTrue(result is ApiResult.Success)
        assertEquals(42, (result as ApiResult.Success).data)
    }

    @Test
    fun `apiCall captures failures instead of throwing`() = runTest {
        val result = apiCall<Int> { throw IOException("offline") }
        assertTrue(result is ApiResult.Failure)
        assertEquals(AppError.OFFLINE, (result as ApiResult.Failure).error)
    }

    @Test
    fun `apiCall rethrows cancellation`() = runTest {
        var cancelled = false
        try {
            apiCall<Int> { throw kotlinx.coroutines.CancellationException("stop") }
        } catch (expected: kotlinx.coroutines.CancellationException) {
            cancelled = true
        }
        assertTrue("cancellation must propagate", cancelled)
    }

    @Test
    fun `dataOrNull returns value only on success`() {
        assertEquals(7, ApiResult.Success(7).dataOrNull())
        assertEquals(null, ApiResult.Failure(AppError.SERVER).dataOrNull())
    }
}
