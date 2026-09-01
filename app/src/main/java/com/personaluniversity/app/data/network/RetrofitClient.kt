package com.personaluniversity.app.data.network

import android.content.Context
import com.personaluniversity.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    @Volatile
    private var customBaseUrl: String? = null

    @Volatile
    private var apiInstance: ApiService? = null

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        customBaseUrl = prefs.getString("server_url", null)
    }

    fun getBaseUrl(): String {
        val url = customBaseUrl?.takeIf { it.isNotBlank() } ?: BuildConfig.BASE_URL
        return if (url.endsWith("/")) url else "$url/"
    }

    fun updateBaseUrl(context: Context, newUrl: String) {
        val formatted = newUrl.trim().let { if (it.endsWith("/")) it else "$it/" }
        customBaseUrl = formatted
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("server_url", formatted).apply()
        synchronized(this) {
            apiInstance = createApiService(formatted)
        }
    }

    private fun createApiService(url: String): ApiService {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val api: ApiService
        get() {
            return apiInstance ?: synchronized(this) {
                apiInstance ?: createApiService(getBaseUrl()).also { apiInstance = it }
            }
        }
}
