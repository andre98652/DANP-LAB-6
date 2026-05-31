package com.example.lab6.features.pantry.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab6.core.designsystem.AppSectionHeader
import com.example.lab6.core.designsystem.FreshnessBadge
import com.example.lab6.core.designsystem.GlassmorphicCard
import com.example.lab6.features.pantry.domain.FreshnessStatus
import com.example.lab6.features.pantry.domain.PantryItem
import com.example.lab6.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle)
 * La pantalla PantryScreen solo se encarga del renderizado de la UI de la despensa.
 * Depende únicamente de un estado inmutable y envía eventos de usuario de vuelta al ViewModel.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    viewModel: PantryViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            AppSectionHeader(
                title = "Mi Despensa Inteligente",
                subtitle = "Controla la frescura e ingredientes disponibles en tu cocina"
            )

            if (state.items.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🍳",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tu despensa está vacía",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Agrega ingredientes o completa compras para empezar.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = state.items,
                        key = { it.id }
                    ) { item ->
                        PantryItemCard(
                            item = item,
                            onDelete = { viewModel.onEvent(PantryEvent.DeleteItem(item.id)) }
                        )
                    }
                }
            }
        }

        // Botón Flotante para Añadir Alimento
        FloatingActionButton(
            onClick = { viewModel.onEvent(PantryEvent.ToggleDialog(true)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Ingrediente")
        }

        // Diálogo para Añadir Ingrediente
        if (state.isAddDialogOpen) {
            AddPantryItemDialog(
                errorMessage = state.errorMessage,
                onDismiss = { viewModel.onEvent(PantryEvent.ToggleDialog(false)) },
                onConfirm = { name, qty, unit, cat, days, isNonPerishable ->
                    viewModel.onEvent(
                        PantryEvent.AddItem(
                            name = name,
                            quantity = qty,
                            unit = unit,
                            category = cat,
                            expirationDate = LocalDate.now().plusDays(days),
                            isNonPerishable = isNonPerishable
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun PantryItemCard(
    item: PantryItem,
    onDelete: () -> Unit
) {
    // Definimos el color de la tarjeta según el estado
    val borderBrush = when (item.freshnessStatus) {
        FreshnessStatus.EXPIRED -> FoodExpired
        FreshnessStatus.EXPIRING_SOON -> FoodExpiringSoon
        FreshnessStatus.FRESH -> FoodFresh
        FreshnessStatus.SAFE -> FoodSafeBlue
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador visual de frescura a la izquierda
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(borderBrush)
            )
            
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Fila 1: Nombre del ingrediente (Con elipsis si es extremadamente largo)
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Fila 2: Categoría con estilo premium (Mayúsculas, tono sutil, espaciado de letras)
                Text(
                    text = item.category.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Fila 3: Cantidad y Fecha de Vencimiento
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (!item.isNonPerishable) {
                        val days = item.daysRemaining()
                        val textDate = when {
                            days < 0 -> "Venció hace ${-days} días"
                            days == 0L -> "Vence hoy"
                            days == 1L -> "Vence mañana"
                            else -> "Vence en $days días"
                        }
                        Text(
                            text = "• $textDate",
                            fontSize = 12.sp,
                            color = if (days <= 3) FoodExpired else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            FreshnessBadge(status = item.freshnessStatus)

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar ingrediente",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPantryItemDialog(
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, qty: Double, unit: String, category: String, daysToExpire: Long, isNonPerishable: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("unidades") }
    var category by remember { mutableStateOf("Verduras") }
    var daysText by remember { mutableStateOf("7") }
    var isNonPerishable by remember { mutableStateOf(false) }

    val categories = listOf("Verduras", "Carnes", "Lácteos", "Abarrotes", "Panadería", "Bebidas", "Otros")
    val units = listOf("unidades", "kg", "g", "litros", "ml")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Alimento", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = errorMessage, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Alimento") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    // Spinner básico de Unidades
                    var showUnitMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad") },
                            trailingIcon = {
                                TextButton(onClick = { showUnitMenu = true }) { Text("Sel") }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = showUnitMenu,
                            onDismissRequest = { showUnitMenu = false }
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        showUnitMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Spinner básico de Categorías
                var showCatMenu by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = {
                            TextButton(onClick = { showCatMenu = true }) { Text("Sel") }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = showCatMenu,
                        onDismissRequest = { showCatMenu = false }
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    category = c
                                    showCatMenu = false
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isNonPerishable,
                        onCheckedChange = { isNonPerishable = it }
                    )
                    Text("Es no perecedero (no vence)", fontSize = 14.sp)
                }

                if (!isNonPerishable) {
                    OutlinedTextField(
                        value = daysText,
                        onValueChange = { daysText = it },
                        label = { Text("Días hasta su vencimiento") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toDoubleOrNull() ?: 0.0
                    val days = daysText.toLongOrNull() ?: 7
                    onConfirm(name, qty, unit, category, days, isNonPerishable)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
