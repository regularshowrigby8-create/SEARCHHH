package info.plateaukao.einkbro.searchhh.local

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import java.io.IOException
import kotlin.coroutines.resumeWithException

/** OkHttp's asynchronous API tied to coroutine cancellation, including body reads. */
suspend fun Call.awaitPage(limit: Long = 2L * 1024 * 1024): PublicSearch.Page =
    suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    if (continuation.isActive) continuation.resumeWithException(e)
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    try {
                        val page =
                            response.use {
                                val body = it.body ?: error("Empty response")
                                val source = body.source()
                                source.request(limit + 1)
                                require(source.buffer.size <= limit) { "Response too large" }
                                PublicSearch.Page(
                                    it.code,
                                    source.readUtf8(),
                                    it.header("Content-Type").orEmpty(),
                                    it.header("Retry-After"),
                                    it.header("Location"),
                                )
                            }
                        if (continuation.isActive) continuation.resumeWith(Result.success(page))
                    } catch (e: Exception) {
                        if (continuation.isActive) continuation.resumeWithException(e)
                    }
                }
            },
        )
    }
