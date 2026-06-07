package com.example.tallermecanico.ui.ordenes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tallermecanico.model.OrdenTrabajo
import com.example.tallermecanico.ui.components.*
import com.example.tallermecanico.ui.theme.*
import com.example.tallermecanico.viewmodel.OrdenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdenDetalleScreen(
    ordenId: Int,
    vm: OrdenViewModel,
    onBack: () -> Unit
) {
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(ordenId) {
        vm.cargarOrdenDetalle(ordenId)
    }

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Orden #$ordenId", color = TextoPrimario, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = TextoPrimario)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GrisGrafito)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoadingDetalle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AzulElectrico)
                    }
                }
                uiState.ordenDetalle == null -> {
                    EmptyState("No se pudo cargar el detalle de la orden")
                }
                else -> {
                    val orden = uiState.ordenDetalle!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Tarjeta de Encabezado / Estado
                        CardEstadoOrden(orden)

                        // Tarjeta de Vehículo
                        CardSeccion(
                            titulo = "Vehículo",
                            icono = Icons.Default.DirectionsCar,
                            colorIcono = NaranjaIndustrial
                        ) {
                            orden.vehiculoDetalle?.let { v ->
                                DetalleFila("Marca / Modelo", "${v.marca} ${v.modelo}")
                                DetalleFila("Placa", v.placa)
                                DetalleFila("Año", v.anio.toString())
                            } ?: DetalleFila("Vehículo ID", orden.vehiculo.toString())
                        }

                        // Tarjeta de Cliente
                        CardSeccion(
                            titulo = "Propietario / Cliente",
                            icono = Icons.Default.Person,
                            colorIcono = AzulElectrico
                        ) {
                            orden.vehiculoDetalle?.clienteDetalle?.let { c ->
                                DetalleFila("Nombre", c.nombre)
                                DetalleFila("Teléfono", c.telefono ?: "—")
                                DetalleFila("Correo", c.correo ?: "—")
                                DetalleFila("Dirección", c.direccion ?: "—")
                            } ?: DetalleFila("Cliente Vinculado", "ID: ${orden.vehiculoDetalle?.cliente ?: "—"}")
                        }

                        // Tarjeta de Mecánico
                        CardSeccion(
                            titulo = "Mecánico Asignado",
                            icono = Icons.Default.Build,
                            colorIcono = AmarilloAlerta
                        ) {
                            orden.mecanicoDetalle?.let { m ->
                                DetalleFila("Nombre de usuario", m.username)
                                DetalleFila("Correo", m.email)
                            } ?: DetalleFila("Mecánico", "Sin asignar")
                        }

                        // Tarjeta de Fechas e Información Operativa
                        CardSeccion(
                            titulo = "Información Operativa",
                            icono = Icons.Default.CalendarToday,
                            colorIcono = VerdeExito
                        ) {
                            DetalleFila("Fecha de Ingreso", orden.fechaIngreso)
                            DetalleFila("Observaciones", orden.observaciones ?: "Ninguna")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardEstadoOrden(orden: OrdenTrabajo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Estado Actual", color = TextoSecundario, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    orden.estado.replace("_", " ").replaceFirstChar { it.uppercase() },
                    color = TextoPrimario,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            EstadoChip(estado = orden.estado)
        }
    }
}

@Composable
private fun CardSeccion(
    titulo: String,
    icono: ImageVector,
    colorIcono: androidx.compose.ui.graphics.Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(24.dp))
                Text(
                    text = titulo,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = GrisMedio, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
    }
}

@Composable
private fun DetalleFila(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = TextoPrimario, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}
