package com.example.tallermecanico.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var username    by remember { mutableStateOf("") }
    var email       by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var password2   by remember { mutableStateOf("") }
    var showPass    by remember { mutableStateOf(false) }
    var showPass2   by remember { mutableStateOf(false) }
    var localError  by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(FondoOscuro, GrisGrafito))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Ícono + títulos ───────────────────────────────────────────
            Surface(
                shape    = RoundedCornerShape(20.dp),
                color    = NaranjaIndustrial,
                modifier = Modifier.size(72.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Default.Build,
                        contentDescription = null,
                        tint               = TextoPrimario,
                        modifier           = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text       = "Crear cuenta",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = TextoPrimario
            )
            Text(
                text  = "Taller Mecánico · Sistema de Gestión",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )

            Spacer(Modifier.height(4.dp))

            // ── Campos ────────────────────────────────────────────────────
            OutlinedTextField(
                value         = username,
                onValueChange = { username = it; localError = null },
                label         = { Text("Nombre de usuario *") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                colors        = tallerTextFieldColors()
            )

            OutlinedTextField(
                value           = email,
                onValueChange   = { email = it; localError = null },
                label           = { Text("Correo electrónico *") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier        = Modifier.fillMaxWidth(),
                colors          = tallerTextFieldColors()
            )

            OutlinedTextField(
                value                = password,
                onValueChange        = { password = it; localError = null },
                label                = { Text("Contraseña *") },
                singleLine           = true,
                visualTransformation = if (showPass) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            imageVector        = if (showPass) Icons.Default.VisibilityOff
                                                 else Icons.Default.Visibility,
                            contentDescription = null,
                            tint               = TextoSecundario
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors   = tallerTextFieldColors()
            )

            OutlinedTextField(
                value                = password2,
                onValueChange        = { password2 = it; localError = null },
                label                = { Text("Confirmar contraseña *") },
                singleLine           = true,
                visualTransformation = if (showPass2) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showPass2 = !showPass2 }) {
                        Icon(
                            imageVector        = if (showPass2) Icons.Default.VisibilityOff
                                                 else Icons.Default.Visibility,
                            contentDescription = null,
                            tint               = TextoSecundario
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors   = tallerTextFieldColors()
            )

            // ── Aviso de rol ──────────────────────────────────────────────
            Surface(
                color    = AzulElectrico.copy(alpha = 0.1f),
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text     = "ℹ️  Las cuentas nuevas se crean con rol de cliente. " +
                               "Un administrador puede cambiar el rol desde el panel.",
                    color    = TextoSecundario,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            // ── Error de validación ───────────────────────────────────────
            if (localError != null) {
                Surface(
                    color    = RojoError.copy(alpha = 0.15f),
                    shape    = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = localError!!,
                        color    = RojoError,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // ── Botón ─────────────────────────────────────────────────────
            Button(
                onClick = {
                    localError = when {
                        username.isBlank()      -> "El nombre de usuario es obligatorio"
                        email.isBlank()         -> "El correo es obligatorio"
                        password.isBlank()      -> "La contraseña es obligatoria"
                        password.length < 6     -> "La contraseña debe tener al menos 6 caracteres"
                        password != password2   -> "Las contraseñas no coinciden"
                        else                    -> null
                    }
                    if (localError == null) {
                        viewModel.registro(
                            username  = username.trim(),
                            email     = email.trim(),
                            password  = password,
                            password2 = password2
                        )
                    }
                },
                enabled  = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NaranjaIndustrial)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color       = TextoPrimario,
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Registrarse", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            TextButton(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text       = "¿Ya tienes cuenta? Inicia sesión",
                    color      = AzulElectrico,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            TallerSnackbar(
                message   = uiState.error,
                isError   = true,
                onDismiss = viewModel::clearError
            )
        }
    }
}
