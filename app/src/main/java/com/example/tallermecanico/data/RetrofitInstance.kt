package com.example.tallermecanico.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    // URL de producción del backend Django
    internal const val BASE_URL = "https://mazsorra-taller.uaeftt-ute.site/"

    private var _sessionManager: SessionManager? = null

    /** Debe llamarse una sola vez en TallerApp.onCreate() */
    fun init(sessionManager: SessionManager) {
        _sessionManager = sessionManager
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        requireNotNull(_sessionManager) {
            "RetrofitInstance.init(sessionManager) no fue llamado. Hazlo en TallerApp.onCreate()."
        }
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(_sessionManager!!))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
