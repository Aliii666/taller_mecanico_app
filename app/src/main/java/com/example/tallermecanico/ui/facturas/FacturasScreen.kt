package com.example.tallermecanico.ui.facturas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallermecanico.model.Factura
import com.example.tallermecanico.ui.auth.tallerTextFieldColors
import com.example.tallermecanico.ui.components.*
import com.example.tallermecanico.ui.theme.*
import com.example.tallermecanico.viewmodel.FacturaPagoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturasScreen(
    userRole: String,
    vm: FacturaPagoViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val esAdmin = userRole == "admin"

    var facturaSeleccionada    by remember { mutableStateOf<Factura?>(null) }
    var mostrarDialogPago      by remember { mutableStateOf(false) }
    var mostrarDialogFactura   by remember { mutableStateOf(false) }
    var filtroEstadoPago       by remember { mutableStateOf<String?>(null) }

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
            vm.cargarFacturas(filtroEstadoPago, reset = false)
        }
    }

    LaunchedEffect(filtroEstadoPago) {
        vm.cargarFacturas(filtroEstadoPago, reset = true)
    }

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            TopAppBar(
                title = {
                    Text("Facturas y Pagos", fontWeight = FontWeight.Bold, color = TextoPrimario)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GrisGrafito),
                actions = {
                    if (esAdmin) {
                        IconButton(onClick = { mostrarDialogFactura = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Emitir Factura", tint = AzulElectrico)
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

                // ── FILTROS DE ESTADO DE PAGO ──
                FiltrosEstadoPago(
                    filtroActual   = filtroEstadoPago,
                    onFiltroChange = { filtroEstadoPago = it }
                )

                // ── BARRA DE BÚSQUEDA ──
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { vm.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar por placa o cliente...", color = TextoSecundario) },
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

                // ── RESUMEN KPI ──
                KpiResumen(facturas = uiState.facturas)

                when {
                    uiState.isLoading && uiState.facturas.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AzulElectrico)
                        }
                    }
                    uiState.facturas.isEmpty() -> {
                        EmptyState("No se encontraron facturas")
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(uiState.facturas, key = { it.id ?: 0 }) { factura ->
                                FacturaCard(
                                    factura           = factura,
                                    puedeCobrar       = esAdmin,
                                    onRegistrarPago   = {
                                        facturaSeleccionada = factura
                                        mostrarDialogPago   = true
                                    },
                                    onMarcarPagada    = { vm.marcarPagada(factura.id!!) }
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

            if (uiState.isLoading && uiState.facturas.isNotEmpty()) {
                LoadingOverlay()
            }

            // ── SNACKBARS ──
            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                TallerSnackbar(
                    message  = uiState.successMessage,
                    isError  = false,
                    onDismiss = vm::clearMessages
                )
                TallerSnackbar(
                    message  = uiState.error,
                    isError  = true,
                    onDismiss = vm::clearMessages
                )
            }
        }
    }

    // ── Dialog: Registrar Pago ──
    if (mostrarDialogPago && facturaSeleccionada != null) {
        RegistrarPagoDialog(
            factura     = facturaSeleccionada!!,
            onConfirmar = { monto, metodo ->
                vm.registrarPago(facturaSeleccionada!!.id!!, monto, metodo)
                mostrarDialogPago   = false
                facturaSeleccionada = null
            },
            onDismiss   = {
                mostrarDialogPago   = false
                facturaSeleccionada = null
            }
        )
    }

    // ── Dialog: Emitir Factura ──
    if (mostrarDialogFactura) {
        EmitirFacturaDialog(
            onConfirmar = { ordenId, total ->
                vm.crearFactura(ordenId, total)
                mostrarDialogFactura = false
            },
            onDismiss = { mostrarDialogFactura = false }
        )
    }
}

@Composable
private fun KpiResumen(facturas: List<Factura>) {
    val pendientes = facturas.count { it.estadoPago == "pendiente" }
    val pagadas    = facturas.count { it.estadoPago == "pagado" }
    val totalPend  = facturas.filter { it.estadoPago == "pendiente" }.sumOf { it.total }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KpiCard("Pendientes", pendientes.toString(), AmarilloAlerta, Modifier.weight(1f))
        KpiCard("Pagadas", pagadas.toString(), VerdeExito, Modifier.weight(1f))
        KpiCard("Total Pend.", "S/ %.2f".format(totalPend), RojoError, Modifier.weight(1f))
    }
}

@Composable
private fun KpiCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = FondoCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier              = Modifier.padding(12.dp),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge)
            Text(title, color = TextoSecundario, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun FiltrosEstadoPago(filtroActual: String?, onFiltroChange: (String?) -> Unit) {
    val opciones = listOf(null to "Todas", "pendiente" to "Pendiente", "pagado" to "Pagadas")
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
                    selectedContainerColor = NaranjaIndustrial,
                    selectedLabelColor     = Color.White
                )
            )
        }
    }
}

@Composable
private fun FacturaCard(
    factura: Factura,
    puedeCobrar: Boolean,
    onRegistrarPago: () -> Unit,
    onMarcarPagada: () -> Unit
) {
    val esPendiente = factura.estadoPago == "pendiente"

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = FondoCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text       = "Factura #${factura.id}",
                        fontWeight = FontWeight.Bold,
                        color      = TextoPrimario,
                        style      = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text  = "Emitida: ${factura.fechaEmision}",
                        color = TextoSecundario,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                EstadoPagoChip(estadoPago = factura.estadoPago)
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = GrisMedio, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            factura.ordenDetalle?.let { orden ->
                orden.vehiculoDetalle?.let { v ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("Vehículo: ", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${v.marca} ${v.modelo} · ${v.placa}",
                            color = TextoPrimario,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("Orden: ", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                    Text("#${orden.id} — ${orden.estado.replace("_", " ").replaceFirstChar { it.uppercase() }}",
                        color = TextoPrimario, style = MaterialTheme.typography.bodySmall)
                }
            } ?: run {
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("Orden ID: ", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                    Text("#${factura.orden}", color = TextoPrimario, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Total:", color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text       = "S/ %.2f".format(factura.total),
                    color      = if (esPendiente) NaranjaIndustrial else VerdeExito,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (esPendiente && puedeCobrar) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick  = onRegistrarPago,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(8.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = NaranjaIndustrial)
                    ) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Registrar Pago", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onMarcarPagada,
                        shape   = RoundedCornerShape(8.dp),
                        colors  = ButtonDefaults.outlinedButtonColors(contentColor = VerdeExito)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Marcar Pagada")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrarPagoDialog(
    factura: Factura,
    onConfirmar: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    val metodosPago  = listOf("efectivo", "tarjeta", "transferencia")
    var monto        by remember { mutableStateOf("") }
    var metodo       by remember { mutableStateOf("efectivo") }
    var expanded     by remember { mutableStateOf(false) }
    var error        by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = FondoCard,
        title = {
            Column {
                Text("Registrar Pago", fontWeight = FontWeight.Bold, color = TextoPrimario)
                Text(
                    "Factura #${factura.id} · Total: S/ %.2f".format(factura.total),
                    color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (error != null) {
                    Text(
                        error!!,
                        color = RojoError,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RojoError.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }

                OutlinedTextField(
                    value         = monto,
                    onValueChange = {
                        monto = it
                        error = null
                    },
                    label         = { Text("Monto (S/)") },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = tallerTextFieldColors(),
                    leadingIcon   = {
                        Text("S/", color = TextoSecundario,
                             modifier = Modifier.padding(start = 8.dp))
                    }
                )

                ExposedDropdownMenuBox(
                    expanded         = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value            = metodo.replaceFirstChar { it.uppercase() },
                        onValueChange    = {},
                        readOnly         = true,
                        label            = { Text("Método de Pago") },
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
                        metodosPago.forEach { m ->
                            DropdownMenuItem(
                                text    = {
                                    Text(
                                        m.replaceFirstChar { it.uppercase() },
                                        color = TextoPrimario
                                    )
                                },
                                onClick = {
                                    metodo   = m
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val montoDouble = monto.trim().toDoubleOrNull()
                    when {
                        montoDouble == null || montoDouble <= 0 ->
                            error = "Ingresa un monto válido mayor a 0"
                        montoDouble > factura.total ->
                            error = "El monto no puede superar S/ %.2f".format(factura.total)
                        else -> onConfirmar(montoDouble, metodo)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NaranjaIndustrial)
            ) {
                Text("Confirmar Pago", color = Color.White, fontWeight = FontWeight.Bold)
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
private fun EmitirFacturaDialog(
    onConfirmar: (Int, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var ordenId by remember { mutableStateOf("") }
    var total   by remember { mutableStateOf("") }
    var error   by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = FondoCard,
        title = { Text("Emitir Factura", fontWeight = FontWeight.Bold, color = TextoPrimario) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (error != null) Text(error!!, color = RojoError, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = ordenId, onValueChange = { ordenId = it },
                    label = { Text("ID Orden de Trabajo *") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors()
                )
                OutlinedTextField(
                    value = total, onValueChange = { total = it },
                    label = { Text("Monto Total (S/) *") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), colors = tallerTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val oid = ordenId.trim().toIntOrNull()
                    val tot = total.trim().toDoubleOrNull()
                    error = when {
                        oid == null     -> "ID de orden inválido"
                        tot == null || tot <= 0 -> "El total debe ser un monto mayor a 0"
                        else -> null
                    }
                    if (error == null) onConfirmar(oid!!, tot!!)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzulElectrico)
            ) { Text("Emitir", color = TextoPrimario) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextoSecundario) }
        }
    )
}
