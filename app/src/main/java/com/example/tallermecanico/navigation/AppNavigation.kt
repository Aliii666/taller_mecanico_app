package com.example.tallermecanico.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tallermecanico.ui.auth.LoginScreen
import com.example.tallermecanico.ui.auth.RegistroScreen
import com.example.tallermecanico.ui.clientes.ClientesScreen
import com.example.tallermecanico.ui.facturas.FacturasScreen
import com.example.tallermecanico.ui.ordenes.OrdenesScreen
import com.example.tallermecanico.ui.ordenes.OrdenDetalleScreen
import com.example.tallermecanico.ui.servicios.ServiciosScreen
import com.example.tallermecanico.ui.vehiculos.VehiculosScreen
import com.example.tallermecanico.ui.theme.*
import com.example.tallermecanico.viewmodel.AuthViewModel

// ── Rutas ─────────────────────────────────────────────────────────────────────

private object Routes {
    const val LOGIN     = "login"
    const val REGISTRO  = "registro"
    const val ORDENES   = "ordenes"
    const val FACTURAS  = "facturas"
    const val CLIENTES  = "clientes"
    const val VEHICULOS = "vehiculos"
    const val SERVICIOS = "servicios"
    const val ORDEN_DETALLE = "orden_detalle/{ordenId}"
}

sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    object Ordenes   : Tab(Routes.ORDENES,   "Órdenes",   Icons.Default.Build)
    object Facturas  : Tab(Routes.FACTURAS,  "Facturas",  Icons.Default.Receipt)
    object Clientes  : Tab(Routes.CLIENTES,  "Clientes",  Icons.Default.Group)
    object Vehiculos : Tab(Routes.VEHICULOS, "Vehículos", Icons.Default.DirectionsCar)
    object Servicios : Tab(Routes.SERVICIOS, "Servicios", Icons.Default.Handyman)
}

// ── Helpers de rol ────────────────────────────────────────────────────────────
// Roles reales del backend: "admin" | "mechanic" | "client"

fun String.esAdmin()    = this == "admin"
fun String.esMecanico() = this == "mechanic"
fun String.esCliente()  = this == "client"

fun String.labelRol() = when (this) {
    "admin"    -> "Administrador"
    "mechanic" -> "Mecánico"
    "client"   -> "Cliente"
    else       -> replaceFirstChar { it.uppercase() }
}

// ─────────────────────────────────────────────────────────────────────────────
// Punto de entrada
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val uiState by authViewModel.uiState.collectAsState()
    val role    by authViewModel.roleFlow.collectAsState(initial = null)

    if (!uiState.isLoggedIn) {
        AuthNavHost(authViewModel = authViewModel)
    } else {
        MainScaffold(
            userRole = role ?: "client",
            onLogout = authViewModel::logout
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Flujo de autenticación (sin bottom bar)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AuthNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel            = authViewModel,
                onNavigateToRegistro = { navController.navigate(Routes.REGISTRO) }
            )
        }
        composable(Routes.REGISTRO) {
            RegistroScreen(
                viewModel         = authViewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dashboard principal
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(userRole: String, onLogout: () -> Unit) {
    val navController = rememberNavController()

    // Las tabs visibles dependen del rol
    val tabs = buildList {
        add(Tab.Ordenes)
        if (userRole.esAdmin() || userRole.esCliente()) {
            add(Tab.Facturas)
        }
        add(Tab.Clientes)
        add(Tab.Vehiculos)
        if (userRole.esAdmin() || userRole.esMecanico()) {
            add(Tab.Servicios)
        }
    }

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            TopAppBar(
                title = {
                    Text("Taller Mecánico", fontWeight = FontWeight.Bold, color = TextoPrimario)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GrisGrafito),
                actions = {
                    val rolColor = when {
                        userRole.esAdmin()    -> AzulElectrico
                        userRole.esMecanico() -> NaranjaIndustrial
                        else                  -> VerdeExito
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = rolColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text       = userRole.labelRol(),
                            color      = rolColor,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión", tint = RojoError)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = GrisGrafito) {
                val navBackStackEntry  by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon     = { Icon(tab.icon, contentDescription = tab.label) },
                        label    = { Text(tab.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick  = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = AzulElectrico,
                            selectedTextColor   = AzulElectrico,
                            indicatorColor      = AzulElectrico.copy(alpha = 0.15f),
                            unselectedIconColor = TextoSecundario,
                            unselectedTextColor = TextoSecundario
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController    = navController,
            startDestination = Routes.ORDENES,
            modifier         = Modifier.padding(paddingValues)
        ) {
            composable(Routes.ORDENES) {
                OrdenesScreen(
                    userRole = userRole,
                    onOrdenClick = { ordenId ->
                        navController.navigate("orden_detalle/$ordenId")
                    }
                )
            }
            composable(Routes.FACTURAS)  { FacturasScreen(userRole = userRole) }
            composable(Routes.CLIENTES)  { ClientesScreen(userRole = userRole) }
            composable(Routes.VEHICULOS) { VehiculosScreen(userRole = userRole) }
            composable(Routes.SERVICIOS) { ServiciosScreen(userRole = userRole) }
            composable("orden_detalle/{ordenId}") { backStackEntry ->
                val ordenId = backStackEntry.arguments?.getString("ordenId")?.toIntOrNull() ?: 0
                val ordenViewModel: com.example.tallermecanico.viewmodel.OrdenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                OrdenDetalleScreen(
                    ordenId = ordenId,
                    vm = ordenViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
