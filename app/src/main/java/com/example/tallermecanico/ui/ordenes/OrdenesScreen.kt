package com.example.tallermecanico.ui.ordenes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallermecanico.model.OrdenTrabajo
import com.example.tallermecanico.ui.auth.tallerTextFieldColors
import com.example.tallermecanico.ui.components.*
import com.example.tallermecanico.ui.theme.*
import com.example.tallermecanico.viewmodel.OrdenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdenesScreen(
    userRole: String,
    onOrdenClick: (Int) -> Unit,
    vm: OrdenViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val esAdmin = userRole == "admin"

    var ordenSeleccionada   by remember { mutableStateOf<OrdenTrabajo?>(null) }
    var mostrarDialogEstado by remember { mutableStateOf(false) }
    var mostrarDialogNueva  by remember { mutableStateOf(false) }
    var filtroEstado        by remember { mutableStateOf<String?>(null) }
    var ordenAEliminar      by remember { mutableStateOf<OrdenTrabajo?>(null) }

    val listState = rememberLazyListState()

    val debeCargarMas = remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val totalItems = info.totalItemsCount
            val ultimoVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            ultimoVisible >= totalItems - 2 && totalItems > 0
        }
    }

    LaunchedEffect(debeCargarMas.value) {
        if (debeCargarMas.value && uiState.hasMore && !uiState.isLoading && !uiState.isPaginating) {
            vm.cargarOrdenes(estado = filtroEstado, reset = false)
        }
    }

    LaunchedEffect(filtroEstado) {
        vm.cargarOrdenes(estado = filtroEstado, reset = true)
    }

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            TopAppBar(
                title = {
                    Text("Órdenes de Trabajo", fontWeight = FontWeight.Bold, color = TextoPrimario)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GrisGrafito),
                actions = {
                    if (esAdmin) {
                        IconButton(onClick = { mostrarDialogNueva = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Nueva orden", tint = AzulElectrico)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── FILTROS DE ESTADO ──
                FiltrosEstado(
                    filtroActual = filtroEstado,
                    onFiltroChange = { filtroEstado = it }
                )

                // ── BARRA DE BÚSQUEDA ──
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { vm.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar por placa, modelo, cliente...", color = TextoSecundario) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextoSecundario) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { vm.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextoSecundario)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    colors = tallerTextFieldColors()
                )

                when {
                    uiState.isLoading && uiState.ordenes.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AzulElectrico)
                        }
                    }
                    uiState.ordenes.isEmpty() -> {
                        EmptyState("No se encontraron órdenes de trabajo")
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.ordenes, key = { it.id ?: 0 }) { orden ->
                                OrdenCard(
                                    orden     = orden,
                                    esAdmin   = esAdmin,
                                    puedeCambiarEstado = userRole == "admin" || userRole == "mechanic",
                                    onCambiarEstado = {
                                        ordenSeleccionada   = orden
                                        mostrarDialogEstado = true
                                    },
                                    onEliminar = { ordenAEliminar = orden },
                                    onOrdenClick = { onOrdenClick(orden.id!!) }
                                )
                            }
                            
                            if (uiState.isPaginating) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = AzulElectrico, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading && uiState.ordenes.isNotEmpty()) {
                LoadingOverlay()
            }

            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                TallerSnackbar(
                    message = uiState.successMessage,
                    isError = false,
                    onDismiss = vm::clearMessages
                )
                TallerSnackbar(
                    message = uiState.error,
                    isError = true,
                    onDismiss = vm::clearMessages
                )
            }
        }
    }

    if (mostrarDialogEstado && ordenSeleccionada != null) {
        CambiarEstadoDialog(
            orden       = ordenSeleccionada!!,
            onConfirmar = { nuevoEstado, obs ->
                vm.cambiarEstado(ordenSeleccionada!!.id!!, nuevoEstado, obs)
                mostrarDialogEstado = false
                ordenSeleccionada   = null
            },
            onDismiss   = {
                mostrarDialogEstado = false
                ordenSeleccionada   = null
            }
        )
    }

    if (mostrarDialogNueva) {
        NuevaOrdenDialog(
            onConfirmar = { vehiculoId, mecanicoId, obs ->
                vm.crearOrden(vehiculoId, mecanicoId, obs)
                mostrarDialogNueva = false
            },
            onDismiss = { mostrarDialogNueva = false }
        )
    }

    if (ordenAEliminar != null) {
        ConfirmarEliminarDialog(
            titulo = "Eliminar Orden",
            mensaje = "¿Está seguro de que desea eliminar la orden de trabajo #${ordenAEliminar!!.id}?",
            onConfirmar = {
                vm.eliminarOrden(ordenAEliminar!!.id!!)
                ordenAEliminar = null
            },
            onDismiss = { ordenAEliminar = null }
        )
    }
}

@Composable
private fun FiltrosEstado(filtroActual: String?, onFiltroChange: (String?) -> Unit) {
    val opciones = listOf(null to "Todos", "pendiente" to "Pendiente",
                          "en_proceso" to "En Proceso", "terminado" to "Terminado")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GrisGrafito)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEach { (valor, etiqueta) ->
            FilterChip(
                selected = filtroActual == valor,
                onClick  = { onFiltroChange(valor) },
                label    = { Text(etiqueta) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AzulElectrico,
                    selectedLabelColor     = Color.White
                )
            )
        }
    }
}

@Composable
private fun OrdenCard(
    orden: OrdenTrabajo,
    esAdmin: Boolean,
    puedeCambiarEstado: Boolean,
    onCambiarEstado: () -> Unit,
    onEliminar: () -> Unit,
    onOrdenClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOrdenClick() },
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = FondoCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Orden #${orden.id}",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = TextoPrimario
                )
                EstadoChip(estado = orden.estado)
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = GrisMedio, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            orden.vehiculoDetalle?.let { v ->
                LabelValue("Vehículo", "${v.marca} ${v.modelo} (${v.placa})")
                LabelValue("Cliente", v.clienteDetalle?.nombre ?: "—")
            } ?: run {
                LabelValue("Vehículo ID", orden.vehiculo.toString())
            }

            orden.mecanicoDetalle?.let { m ->
                LabelValue("Mecánico", m.username)
            }

            LabelValue("Ingreso", orden.fechaIngreso)
            if (!orden.observaciones.isNullOrBlank()) {
                LabelValue("Observaciones", orden.observaciones)
            }

            if (puedeCambiarEstado || esAdmin) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (puedeCambiarEstado) {
                        OutlinedButton(
                            onClick = onCambiarEstado,
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = AzulElectrico)
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Cambiar Estado")
                        }
                    }

                    if (esAdmin) {
                        IconButton(
                            onClick = onEliminar,
                            modifier = Modifier
                                .background(RojoError.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = RojoError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        Text(value, color = TextoPrimario, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CambiarEstadoDialog(
    orden: OrdenTrabajo,
    onConfirmar: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val estados = listOf("pendiente", "en_proceso", "terminado")
    var estadoSeleccionado by remember { mutableStateOf(orden.estado) }
    var observaciones      by remember { mutableStateOf(orden.observaciones ?: "") }
    var expanded           by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = FondoCard,
        title = {
            Text(
                "Actualizar Orden #${orden.id}",
                fontWeight = FontWeight.Bold,
                color = TextoPrimario
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value            = estadoSeleccionado.replaceFirstChar { it.uppercase() }
                            .replace("_", " "),
                        onValueChange    = {},
                        readOnly         = true,
                        label            = { Text("Nuevo Estado") },
                        trailingIcon     = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier         = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors           = tallerTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded         = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor   = GrisMedio
                    ) {
                        estados.forEach { estado ->
                            DropdownMenuItem(
                                text    = { Text(estado.replace("_", " ").replaceFirstChar { it.uppercase() }, color = TextoPrimario) },
                                onClick = {
                                    estadoSeleccionado = estado
                                    expanded           = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value         = observaciones,
                    onValueChange = { observaciones = it },
                    label         = { Text("Observaciones") },
                    minLines      = 3,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = tallerTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(estadoSeleccionado, observaciones) },
                colors  = ButtonDefaults.buttonColors(containerColor = AzulElectrico)
            ) {
                Text("Confirmar", color = TextoPrimario)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextoSecundario)
            }
        }
    )
}

@Composable
private fun NuevaOrdenDialog(
    onConfirmar: (Int, Int?, String) -> Unit,
    onDismiss: () -> Unit
) {
    var vehiculoId   by remember { mutableStateOf("") }
    var mecanicoId   by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var error         by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = FondoCard,
        title = { Text("Nueva Orden", fontWeight = FontWeight.Bold, color = TextoPrimario) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (error != null) {
                    Text(error!!, color = RojoError, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(
                    value         = vehiculoId,
                    onValueChange = { vehiculoId = it },
                    label         = { Text("ID Vehículo *") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = tallerTextFieldColors()
                )
                OutlinedTextField(
                    value         = mecanicoId,
                    onValueChange = { mecanicoId = it },
                    label         = { Text("ID Mecánico (opcional)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = tallerTextFieldColors()
                )
                OutlinedTextField(
                    value         = observaciones,
                    onValueChange = { observaciones = it },
                    label         = { Text("Observaciones") },
                    minLines      = 3,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = tallerTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val vidInt = vehiculoId.trim().toIntOrNull()
                    if (vidInt == null) { error = "ID de vehículo inválido"; return@Button }
                    onConfirmar(vidInt, mecanicoId.trim().toIntOrNull(), observaciones.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzulElectrico)
            ) { Text("Crear", color = TextoPrimario) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextoSecundario) }
        }
    )
}
