package com.example.tallermecanico.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Paleta industrial oscura ─────────────────────────────────────────────────
val AzulElectrico   = Color(0xFF2979FF)
val AzulOscuro      = Color(0xFF1565C0)
val NaranjaIndustrial = Color(0xFFFF6D00)
val GrisGrafito     = Color(0xFF263238)
val GrisMedio       = Color(0xFF37474F)
val GrisClaro       = Color(0xFF546E7A)
val FondoOscuro     = Color(0xFF102027)
val FondoCard       = Color(0xFF1C313A)
val TextoPrimario   = Color(0xFFECEFF1)
val TextoSecundario = Color(0xFF90A4AE)
val VerdeExito      = Color(0xFF00C853)
val AmarilloAlerta  = Color(0xFFFFD600)
val RojoError       = Color(0xFFD32F2F)

private val TallerColorScheme = darkColorScheme(
    primary          = AzulElectrico,
    onPrimary        = Color.White,
    primaryContainer = AzulOscuro,
    secondary        = NaranjaIndustrial,
    onSecondary      = Color.White,
    background       = FondoOscuro,
    surface          = FondoCard,
    onBackground     = TextoPrimario,
    onSurface        = TextoPrimario,
    error            = RojoError,
    onError          = Color.White,
    outline          = GrisClaro
)

@Composable
fun TallerMecanicoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TallerColorScheme,
        content     = content
    )
}
