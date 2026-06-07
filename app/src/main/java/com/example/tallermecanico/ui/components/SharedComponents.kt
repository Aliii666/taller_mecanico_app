package com.example.tallermecanico.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tallermecanico.ui.theme.*
import kotlinx.coroutines.delay

// ── Snackbar / Toast visual ───────────────────────────────────────────────────

@Composable
fun TallerSnackbar(
    message: String?,
    isError: Boolean = false,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (message != null) {
            LaunchedEffect(message) {
                delay(3000)
                onDismiss()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isError) RojoError else VerdeExito,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(text = message, color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ── Indicador de carga ────────────────────────────────────────────────────────

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AzulElectrico)
    }
}

// ── Chip de estado de orden ───────────────────────────────────────────────────

@Composable
fun EstadoChip(estado: String) {
    val (bgColor, label) = when (estado) {
        "pendiente"  -> AmarilloAlerta.copy(alpha = 0.2f) to "Pendiente"
        "en_proceso" -> AzulElectrico.copy(alpha = 0.2f)  to "En Proceso"
        "terminado"  -> VerdeExito.copy(alpha = 0.2f)     to "Terminado"
        else          -> GrisClaro.copy(alpha = 0.2f)     to estado
    }
    val textColor = when (estado) {
        "pendiente"  -> AmarilloAlerta
        "en_proceso" -> AzulElectrico
        "terminado"  -> VerdeExito
        else          -> TextoSecundario
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ── Chip de estado de pago ────────────────────────────────────────────────────

@Composable
fun EstadoPagoChip(estadoPago: String) {
    val (bgColor, textColor, label) = when (estadoPago) {
        "pagado"    -> Triple(VerdeExito.copy(alpha = 0.2f), VerdeExito, "Pagado")
        "pendiente" -> Triple(AmarilloAlerta.copy(alpha = 0.2f), AmarilloAlerta, "Pendiente")
        else         -> Triple(GrisClaro.copy(alpha = 0.2f), TextoSecundario, estadoPago)
    }
    Surface(color = bgColor, shape = RoundedCornerShape(50)) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ── Pantalla vacía ────────────────────────────────────────────────────────────

@Composable
fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = TextoSecundario, style = MaterialTheme.typography.bodyLarge)
    }
}

// ── Separador de sección ──────────────────────────────────────────────────────

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextoPrimario,
        modifier = modifier.padding(bottom = 8.dp)
    )
}
