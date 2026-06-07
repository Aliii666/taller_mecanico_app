package com.example.tallermecanico

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.tallermecanico.navigation.AppNavigation
import com.example.tallermecanico.repository.AuthRepository
import com.example.tallermecanico.ui.theme.TallerMecanicoTheme
import com.example.tallermecanico.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Construye el AuthViewModel con su factory (inyecta SessionManager)
        val app = application as TallerApp
        val repository = AuthRepository(app.sessionManager)
        authViewModel = ViewModelProvider(
            this,
            AuthViewModel.Factory(repository, app.sessionManager)
        )[AuthViewModel::class.java]

        setContent {
            TallerMecanicoTheme {
                AppNavigation(authViewModel = authViewModel)
            }
        }
    }
}
