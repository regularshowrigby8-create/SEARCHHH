package com.searchhh.app.core

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Result type used by every repository call.
 *
 * UI code never sees exceptions: [Failure] carries a small [AppError] enum that
 * maps 1:1 to a user-facing string. This keeps ViewModels free of try/catch
 * noise and makes error paths testable.
 */
sealed interface ApiResult<out T> {

    data class Success<T>(val data: T) : ApiResult<T>

    data class Failure(val error: AppError, val cause: Throwable? = null) : ApiResult<Nothing>

    data object Loading : ApiResult<Nothing>
}

enum class AppError {
    OFFLINE,
    TIMEOUT,
    SERVER,
    UNAUTHORIZED,
    PARSE,
    UNKNOWN,
}

/** Convert a raw exception into the small, UI-friendly enum. */
fun Throwable.toAppError(): AppError = when (this) {
    // Order matters: SocketTimeoutException and UnknownHostException both extend
    // IOException, so the specific cases must come first.
    is SocketTimeoutException -> AppError.TIMEOUT
    is UnknownHostException -> AppError.OFFLINE
    is IOException -> AppError.OFFLINE
    is SerializationException -> AppError.PARSE
    is HttpException -> when (code()) {
        401, 403 -> AppError.UNAUTHORIZED
        in 500..599 -> AppError.SERVER
        else -> AppError.UNKNOWN
    }
    else -> AppError.UNKNOWN
}

/**
 * Runs [block] and wraps outcome. Never throws.
 *
 * `CancellationException` is deliberately rethrown so structured concurrency
 * keeps working when a screen is disposed mid-request.
 */
suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (cancellation: kotlinx.coroutines.CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        ApiResult.Failure(throwable.toAppError(), throwable)
    }

/** Convenience for the `when` expression in ViewModels. */
inline fun <T, R> ApiResult<T>.onSuccess(action: (T) -> R): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T, R> ApiResult<T>.onFailure(action: (AppError) -> R): ApiResult<T> {
    if (this is ApiResult.Failure) action(error)
    return this
}

fun <T> ApiResult<T>.dataOrNull(): T? = (this as? ApiResult.Success)?.data
