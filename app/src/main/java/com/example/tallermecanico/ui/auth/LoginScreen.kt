package com.example.tallermecanico.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tallermecanico.ui.components.TallerSnackbar
import com.example.tallermecanico.ui.theme.*
import com.example.tallermecanico.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegistro: () -> Unit
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FondoOscuro, GrisGrafito)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Logo / Ícono ────────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AzulElectrico,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Logo",
                        tint = TextoPrimario,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "Taller Mecánico",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextoPrimario
            )
            Text(
                text = "Sistema de Gestión",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario
            )

            Spacer(Modifier.height(8.dp))

            // ── Campos ──────────────────────────────────────────────────────
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = tallerTextFieldColors()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (showPass) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            imageVector = if (showPass) Icons.Default.VisibilityOff
                                           else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextoSecundario
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = tallerTextFieldColors()
            )

            // ── Botón de ingreso ────────────────────────────────────────────
            Button(
                onClick = { viewModel.login(email.trim(), password) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulElectrico)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = TextoPrimario,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Ingresar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            TextButton(
                onClick = onNavigateToRegistro,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿No tienes cuenta? Regístrate aquí",
                    color = AzulElectrico,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ── Snackbar de error ────────────────────────────────────────────────
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            TallerSnackbar(
                message = uiState.error,
                isError = true,
                onDismiss = viewModel::clearError
            )
        }
    }
}

@Composable
fun tallerTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = AzulElectrico,
    unfocusedBorderColor = GrisClaro,
    focusedLabelColor    = AzulElectrico,
    cursorColor          = AzulElectrico,
    focusedTextColor     = TextoPrimario,
    unfocusedTextColor   = TextoPrimario,
    unfocusedLabelColor  = TextoSecundario
)
