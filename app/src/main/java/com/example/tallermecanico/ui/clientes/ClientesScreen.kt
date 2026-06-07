package com.example.tallermecanico.ui.clientes

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallermecanico.model.Cliente
import com.example.tallermecanico.ui.auth.tallerTextFieldColors
import com.example.tallermecanico.ui.components.*
import com.example.tallermecanico.ui.theme.*
import com.example.tallermecanico.viewmodel.ClienteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    userRole: String,
    vm: ClienteViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val esAdmin = userRole == "admin"

    var mostrarDialog     by remember { mutableStateOf(false) }
    var clienteEditando   by remember { mutableStateOf<Cliente?>(null) }

    // Scroll State para paginación infinita
    val listState = rememberLazyListState()
    
    // Detectar cuando el usuario está cerca del final de la lista (ej. 2 items antes del final)
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
            vm.obtenerClientes(reset = false)
        }
    }

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            TopAppBar(
                title = { Text("Clientes", fontWeight = FontWeight.Bold, color = TextoPrimario) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GrisGrafito),
                actions = {
                    if (esAdmin) {
                        IconButton(onClick = { clienteEditando = null; mostrarDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Nuevo cliente", tint = AzulElectrico)
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
                
                // ── BARRA DE BÚSQUEDA ──
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { vm.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar por nombre, teléfono o correo...", color = TextoSecundario) },
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
                    uiState.isLoading && uiState.clientes.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AzulElectrico)
                        }
                    }
                    uiState.clientes.isEmpty() -> {
                        EmptyState("No se encontraron clientes")
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(uiState.clientes, key = { it.id ?: 0 }) { cliente ->
                                ClienteCard(
                                    cliente    = cliente,
                                    esAdmin    = esAdmin,
                                    onEditar   = { clienteEditando = cliente; mostrarDialog = true },
                                    onEliminar = { vm.eliminarCliente(cliente.id!!) }
                                )
                            }
                            
                            // Indicador de carga inferior durante paginación
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

            // Overlay de carga general (solo operaciones de escritura o carga inicial pesada)
            if (uiState.isLoading && uiState.clientes.isNotEmpty()) {
                LoadingOverlay()
            }

            // Snackbars de alertas de éxito y error
            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                TallerSnackbar(
                    message = uiState.successMessage, isError = false, onDismiss = vm::clearMessages
                )
                TallerSnackbar(
                    message = uiState.error, isError = true, onDismiss = vm::clearMessages
                )
            }
        }
    }

    if (mostrarDialog) {
        ClienteFormDialog(
            clienteInicial = clienteEditando,
            onConfirmar    = { nombre, tel, dir, correo ->
                if (clienteEditando != null) {
                    vm.actualizarCliente(clienteEditando!!.id!!, nombre, tel, dir, correo)
                } else {
                    vm.crearCliente(nombre, tel, dir, correo)
                }
                mostrarDialog = false
            },
            onDismiss = { mostrarDialog = false }
        )
    }
}

@Composable
private fun ClienteCard(
    cliente: Cliente,
    esAdmin: Boolean,
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
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = AzulElectrico.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = AzulElectrico)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(cliente.nombre, fontWeight = FontWeight.Bold, color = TextoPrimario)
                cliente.telefono?.let {
                    if (it.isNotBlank()) {
                        Text(it, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                    }
                }
                cliente.correo?.let {
                    if (it.isNotBlank()) {
                        Text(it, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                    }
                }
                cliente.direccion?.let {
                    if (it.isNotBlank()) {
                        Text(it, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Row {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = AzulElectrico)
                }
                if (esAdmin) {
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = RojoError)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClienteFormDialog(
    clienteInicial: Cliente?,
    onConfirmar: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre   by remember { mutableStateOf(clienteInicial?.nombre   ?: "") }
    var telefono by remember { mutableStateOf(clienteInicial?.telefono ?: "") }
    var direccion by remember { mutableStateOf(clienteInicial?.direccion ?: "") }
    var correo   by remember { mutableStateOf(clienteInicial?.correo   ?: "") }
    var error    by remember { mutableStateOf<String?>(null) }

    val titulo = if (clienteInicial != null) "Editar Cliente" else "Nuevo Cliente"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = FondoCard,
        title = { Text(titulo, fontWeight = FontWeight.Bold, color = TextoPrimario) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (error != null) Text(error!!, color = RojoError, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors()
                )
                OutlinedTextField(
                    value = telefono, onValueChange = { telefono = it },
                    label = { Text("Teléfono") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors()
                )
                OutlinedTextField(
                    value = direccion, onValueChange = { direccion = it },
                    label = { Text("Dirección") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors()
                )
                OutlinedTextField(
                    value = correo, onValueChange = { correo = it },
                    label = { Text("Correo") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        nombre.isBlank() -> error = "El nombre es obligatorio"
                        else -> onConfirmar(nombre.trim(), telefono.trim(), direccion.trim(), correo.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzulElectrico)
            ) { Text("Guardar", color = TextoPrimario) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextoSecundario) }
        }
    )
}
