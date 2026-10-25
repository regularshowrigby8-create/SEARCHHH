package com.searchhh.app.data.remote

import com.searchhh.app.BuildConfig
import io.sentry.okhttp.SentryOkHttpInterceptor
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Networking stack: OkHttp + Retrofit + kotlinx.serialization.
 *
 * The base URL is resolved per-request through [BaseUrlInterceptor] so the
 * Settings screen can repoint the app at a local backend without restarting.
 */
object NetworkModule {

    /** Shared JSON config. Unknown keys are ignored so the client survives
     *  additive backend changes; nulls fall back to Kotlin defaults. */
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
        encodeDefaults = true
    }

    fun baseUrlInterceptor(baseUrlProvider: () -> String): Interceptor =
        object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                val configured = baseUrlProvider().trim()
                val httpUrl = runCatching { if (configured.endsWith("/")) configured.toHttpUrl() else "$configured/".toHttpUrl() }
                    .getOrElse {
                        Timber.w(it, "Invalid API base URL '%s', using default", configured)
                        return chain.proceed(request)
                    }
                val patched = request.url.newBuilder()
                    .scheme(httpUrl.scheme)
                    .host(httpUrl.host)
                    .port(httpUrl.port)
                    .build()
                return chain.proceed(request.newBuilder().url(patched).build())
            }
        }

    fun okHttpClient(baseUrlProvider: () -> String): OkHttpClient {
        val logging = HttpLoggingInterceptor { message ->
            if (BuildConfig.DEBUG) Timber.tag("HTTP").d(message)
        }.apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(baseUrlInterceptor(baseUrlProvider))
            .addInterceptor(logging)
            .addInterceptor(SentryOkHttpInterceptor(treat404AsError = false))
            .build()
    }

    fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        // Placeholder host; BaseUrlInterceptor rewrites scheme/host/port per call.
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    fun searchApi(retrofit: Retrofit): SearchApi = retrofit.create(SearchApi::class.java)
}
