package com.example.tallermecanico.ui.servicios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallermecanico.model.Servicio
import com.example.tallermecanico.ui.auth.tallerTextFieldColors
import com.example.tallermecanico.ui.components.*
import com.example.tallermecanico.ui.theme.*
import com.example.tallermecanico.viewmodel.ServicioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiciosScreen(
    userRole: String,
    vm: ServicioViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val esAdmin = userRole == "admin"
    val puedeEditar = userRole == "admin" || userRole == "mechanic"

    var mostrarDialog    by remember { mutableStateOf(false) }
    var servicioEditando by remember { mutableStateOf<Servicio?>(null) }
    var servicioAEliminar by remember { mutableStateOf<Servicio?>(null) }

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
            vm.cargarServicios(reset = false)
        }
    }

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            TopAppBar(
                title  = { Text("Catálogo de Servicios", fontWeight = FontWeight.Bold, color = TextoPrimario) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GrisGrafito),
                actions = {
                    if (puedeEditar) {
                        IconButton(onClick = { servicioEditando = null; mostrarDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Nuevo servicio", tint = AzulElectrico)
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
                    placeholder = { Text("Buscar servicio por nombre o descripción...", color = TextoSecundario) },
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
                    uiState.isLoading && uiState.servicios.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AzulElectrico)
                        }
                    }
                    uiState.servicios.isEmpty() -> {
                        EmptyState("No se encontraron servicios")
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(uiState.servicios, key = { it.id ?: 0 }) { s ->
                                ServicioCard(
                                    servicio  = s,
                                    puedeEditar = puedeEditar,
                                    puedeEliminar = esAdmin,
                                    onEditar  = { servicioEditando = s; mostrarDialog = true },
                                    onEliminar = { servicioAEliminar = s }
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

            if (uiState.isLoading && uiState.servicios.isNotEmpty()) {
                LoadingOverlay()
            }

            Column(Modifier.align(Alignment.BottomCenter)) {
                TallerSnackbar(message = uiState.successMessage, isError = false, onDismiss = vm::clearMessages)
                TallerSnackbar(message = uiState.error,          isError = true,  onDismiss = vm::clearMessages)
            }
        }
    }

    if (mostrarDialog) {
        ServicioDialog(
            inicial     = servicioEditando,
            onConfirmar = { nombre, desc, precio ->
                if (servicioEditando != null)
                    vm.actualizar(servicioEditando!!.id!!, nombre, desc, precio)
                else
                    vm.crear(nombre, desc, precio)
                mostrarDialog = false
            },
            onDismiss = { mostrarDialog = false }
        )
    }

    if (servicioAEliminar != null) {
        ConfirmarEliminarDialog(
            titulo = "Eliminar Servicio",
            mensaje = "¿Está seguro de que desea eliminar el servicio ${servicioAEliminar!!.nombre}?",
            onConfirmar = {
                vm.eliminar(servicioAEliminar!!.id!!)
                servicioAEliminar = null
            },
            onDismiss = { servicioAEliminar = null }
        )
    }
}

@Composable
private fun ServicioCard(
    servicio: Servicio,
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
                color    = VerdeExito.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Handyman, contentDescription = null, tint = VerdeExito)
                }
            }

            Column(Modifier.weight(1f)) {
                Text(servicio.nombre, fontWeight = FontWeight.Bold, color = TextoPrimario)
                if (servicio.descripcion.isNotBlank())
                    Text(servicio.descripcion, color = TextoSecundario, style = MaterialTheme.typography.bodySmall,
                        maxLines = 2)
                Text("S/ %.2f".format(servicio.precio), color = NaranjaIndustrial, fontWeight = FontWeight.Bold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
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
private fun ServicioDialog(
    inicial: Servicio?,
    onConfirmar: (String, String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre    by remember { mutableStateOf(inicial?.nombre      ?: "") }
    var descripcion by remember { mutableStateOf(inicial?.descripcion ?: "") }
    var precio    by remember { mutableStateOf(inicial?.precio?.toString() ?: "") }
    var error     by remember { mutableStateOf<String?>(null) }
    val titulo    = if (inicial != null) "Editar Servicio" else "Nuevo Servicio"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = FondoCard,
        title = { Text(titulo, fontWeight = FontWeight.Bold, color = TextoPrimario) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (error != null) Text(error!!, color = RojoError, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors())
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it },
                    label = { Text("Descripción") }, minLines = 2,
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors())
                OutlinedTextField(value = precio, onValueChange = { precio = it },
                    label = { Text("Precio (S/) *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = precio.trim().toDoubleOrNull()
                    error = when {
                        nombre.isBlank() -> "El nombre es obligatorio"
                        p == null || p < 0 -> "Precio inválido"
                        else -> null
                    }
                    if (error == null) onConfirmar(nombre.trim(), descripcion.trim(), p!!)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzulElectrico)
            ) { Text("Guardar", color = TextoPrimario) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextoSecundario) }
        }
    )
}
