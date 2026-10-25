package com.searchhh.app.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Injectable dispatchers. Without a DI framework we still want to swap
 * `Dispatchers.Main` in unit tests, so every ViewModel depends on this
 * interface instead of touching `Dispatchers` directly.
 */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
    val mainImmediate: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val io: CoroutineDispatcher get() = Dispatchers.IO
    override val default: CoroutineDispatcher get() = Dispatchers.Default
    override val main: CoroutineDispatcher get() = Dispatchers.Main
    override val mainImmediate: CoroutineDispatcher get() = Dispatchers.Main.immediate
}

/** Never send a negative offset to the API. */
fun Int.coerceOffset(): Int = if (this < 0) 0 else this

/** Page size used by every list on the client. */
const val PAGE_SIZE: Int = 20
