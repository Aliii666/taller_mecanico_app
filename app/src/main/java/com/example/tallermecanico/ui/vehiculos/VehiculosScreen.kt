package com.example.tallermecanico.ui.vehiculos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallermecanico.model.Vehiculo
import com.example.tallermecanico.ui.auth.tallerTextFieldColors
import com.example.tallermecanico.ui.components.*
import com.example.tallermecanico.ui.theme.*
import com.example.tallermecanico.viewmodel.VehiculoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculosScreen(
    userRole: String,
    vm: VehiculoViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val puedeCrear   = userRole == "admin" || userRole == "mechanic"
    val puedeEliminar = userRole == "admin"

    var mostrarDialog    by remember { mutableStateOf(false) }
    var vehiculoEditando by remember { mutableStateOf<Vehiculo?>(null) }
    var vehiculoAEliminar by remember { mutableStateOf<Vehiculo?>(null) }

    // Scroll State para paginación infinita
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
            vm.cargarVehiculos(reset = false)
        }
    }

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            TopAppBar(
                title  = { Text("Vehículos", fontWeight = FontWeight.Bold, color = TextoPrimario) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GrisGrafito),
                actions = {
                    if (puedeCrear) {
                        IconButton(onClick = { vehiculoEditando = null; mostrarDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Nuevo vehículo", tint = AzulElectrico)
                        }
                    }
                }
            )
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // ── BARRA DE BÚSQUEDA ──
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { vm.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar marca, modelo, placa, cliente...", color = TextoSecundario) },
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
                    uiState.isLoading && uiState.vehiculos.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AzulElectrico)
                        }
                    }
                    uiState.vehiculos.isEmpty() -> {
                        EmptyState("No se encontraron vehículos")
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(uiState.vehiculos, key = { it.id ?: 0 }) { v ->
                                VehiculoCard(
                                    vehiculo      = v,
                                    puedeEditar   = puedeCrear,
                                    puedeEliminar = puedeEliminar,
                                    onEditar      = { vehiculoEditando = v; mostrarDialog = true },
                                    onEliminar    = { vehiculoAEliminar = v }
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

            if (uiState.isLoading && uiState.vehiculos.isNotEmpty()) {
                LoadingOverlay()
            }

            Column(Modifier.align(Alignment.BottomCenter)) {
                TallerSnackbar(message = uiState.successMessage, isError = false, onDismiss = vm::clearMessages)
                TallerSnackbar(message = uiState.error,          isError = true,  onDismiss = vm::clearMessages)
            }
        }
    }

    if (mostrarDialog) {
        VehiculoFormDialog(
            vehiculoInicial = vehiculoEditando,
            onConfirmar = { cId, marca, modelo, placa, anio ->
                if (vehiculoEditando != null) {
                    vm.actualizar(vehiculoEditando!!.id!!, cId, marca, modelo, placa, anio)
                } else {
                    vm.crear(cId, marca, modelo, placa, anio)
                }
                mostrarDialog = false
            },
            onDismiss = { mostrarDialog = false }
        )
    }

    if (vehiculoAEliminar != null) {
        ConfirmarEliminarDialog(
            titulo = "Eliminar Vehículo",
            mensaje = "¿Está seguro de que desea eliminar el vehículo ${vehiculoAEliminar!!.marca} ${vehiculoAEliminar!!.modelo} (${vehiculoAEliminar!!.placa})?",
            onConfirmar = {
                vm.eliminar(vehiculoAEliminar!!.id!!)
                vehiculoAEliminar = null
            },
            onDismiss = { vehiculoAEliminar = null }
        )
    }
}

@Composable
private fun VehiculoCard(
    vehiculo: Vehiculo,
    puedeEditar: Boolean,
    puedeEliminar: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = FondoCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape    = RoundedCornerShape(50),
                color    = NaranjaIndustrial.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = NaranjaIndustrial)
                }
            }

            Column(Modifier.weight(1f)) {
                Text(
                    "ID: ${vehiculo.id} · ${vehiculo.marca} ${vehiculo.modelo}",
                    fontWeight = FontWeight.Bold,
                    color      = TextoPrimario
                )
                Text("Placa: ${vehiculo.placa} · ${vehiculo.anio}", color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall)
                vehiculo.clienteDetalle?.let {
                    Text("Cliente: ${it.nombre}", color = TextoSecundario,
                        style = MaterialTheme.typography.bodySmall)
                } ?: run {
                    Text("Cliente ID: ${vehiculo.cliente}", color = TextoSecundario,
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            Row {
                if (puedeEditar) {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = AzulElectrico)
                    }
                }
                if (puedeEliminar) {
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = RojoError)
                    }
                }
            }
        }
    }
}

@Composable
private fun VehiculoFormDialog(
    vehiculoInicial: Vehiculo?,
    onConfirmar: (Int, String, String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var clienteId by remember { mutableStateOf(vehiculoInicial?.cliente?.toString() ?: "") }
    var marca     by remember { mutableStateOf(vehiculoInicial?.marca ?: "") }
    var modelo    by remember { mutableStateOf(vehiculoInicial?.modelo ?: "") }
    var placa     by remember { mutableStateOf(vehiculoInicial?.placa ?: "") }
    var anio      by remember { mutableStateOf(vehiculoInicial?.anio?.toString() ?: "") }
    var error     by remember { mutableStateOf<String?>(null) }

    val titulo = if (vehiculoInicial != null) "Editar Vehículo" else "Nuevo Vehículo"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = FondoCard,
        title = { Text(titulo, fontWeight = FontWeight.Bold, color = TextoPrimario) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (error != null) Text(error!!, color = RojoError, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = clienteId, onValueChange = { clienteId = it },
                    label = { Text("ID Cliente *") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors()
                )
                OutlinedTextField(
                    value = marca, onValueChange = { marca = it },
                    label = { Text("Marca *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors()
                )
                OutlinedTextField(
                    value = modelo, onValueChange = { modelo = it },
                    label = { Text("Modelo *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors()
                )
                OutlinedTextField(
                    value = placa, onValueChange = { placa = it },
                    label = { Text("Placa *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors()
                )
                OutlinedTextField(
                    value = anio, onValueChange = { anio = it },
                    label = { Text("Año *") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cid  = clienteId.trim().toIntOrNull()
                    val year = anio.trim().toIntOrNull()
                    error = when {
                        cid == null     -> "ID de cliente inválido"
                        marca.isBlank() -> "La marca es obligatoria"
                        placa.isBlank() -> "La placa es obligatoria"
                        year == null    -> "Año inválido"
                        else -> null
                    }
                    if (error == null)
                        onConfirmar(cid!!, marca.trim(), modelo.trim(), placa.trim().uppercase(), year!!)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzulElectrico)
            ) { Text("Guardar", color = TextoPrimario) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextoSecundario) }
        }
    )
}
