package com.example.tallermecanico

import android.app.Application
import com.example.tallermecanico.data.RetrofitInstance
import com.example.tallermecanico.data.SessionManager

/**
 * Application entry point.
 * Inicializa RetrofitInstance con el SessionManager para que el
 * AuthInterceptor pueda leer el token en cada petición.
 */
class TallerApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(applicationContext)
        RetrofitInstance.init(sessionManager)
    }
}
