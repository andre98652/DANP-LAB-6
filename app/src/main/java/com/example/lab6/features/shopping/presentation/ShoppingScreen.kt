package com.example.lab6.features.shopping.presentation

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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab6.core.designsystem.AppSectionHeader
import com.example.lab6.core.designsystem.GlassmorphicCard
import com.example.lab6.features.shopping.domain.ShoppingItem
import com.example.lab6.ui.theme.*

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle)
 * La pantalla de lista de compras se encarga del renderizado y el procesamiento local de eventos.
 * Ofrece la increíble funcionalidad de traspasar compras a la despensa de forma instantánea.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    viewModel: ShoppingViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    val checkedCount = state.items.count { it.isCompleted }

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
                title = "Lista de Compras",
                subtitle = "Gestiona lo que necesitas abastecer en tu cocina"
            )

            // Panel superior inteligente para compras completadas
            AnimatedVisibility(
                visible = checkedCount > 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FoodFresh.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🛍️ ¡Tienes $checkedCount artículos comprados!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = FoodFresh
                            )
                            Text(
                                text = "Presiona 'Completar' para ingresarlos a tu despensa automáticamente.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                        Button(
                            onClick = { viewModel.onEvent(ShoppingEvent.CompletePurchase) },
                            colors = ButtonDefaults.buttonColors(containerColor = FoodFresh)
                        ) {
                            Text("Completar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (state.items.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tienes compras pendientes",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Tu despensa está bien abastecida o aún no planificas compras.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = state.items,
                        key = { it.id }
                    ) { item ->
                        ShoppingItemCard(
                            item = item,
                            onToggle = { viewModel.onEvent(ShoppingEvent.ToggleItem(item)) },
                            onDelete = { viewModel.onEvent(ShoppingEvent.DeleteItem(item.id)) }
                        )
                    }
                }
            }
        }

        // Botón Flotante para Añadir Compra
        FloatingActionButton(
            onClick = { viewModel.onEvent(ShoppingEvent.ToggleDialog(true)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir a lista")
        }

        // Diálogo para Añadir Artículo de Compras
        if (state.isAddDialogOpen) {
            AddShoppingItemDialog(
                errorMessage = state.errorMessage,
                onDismiss = { viewModel.onEvent(ShoppingEvent.ToggleDialog(false)) },
                onConfirm = { name, qty, unit, category ->
                    viewModel.onEvent(
                        ShoppingEvent.AddItem(
                            name = name,
                            quantity = qty,
                            unit = unit,
                            category = category
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = FoodFresh
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isCompleted) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "• ${item.category}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar de la lista",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShoppingItemDialog(
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, qty: Double, unit: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("unidades") }
    var category by remember { mutableStateOf("Verduras") }

    val categories = listOf("Verduras", "Carnes", "Lácteos", "Abarrotes", "Panadería", "Bebidas", "Otros")
    val units = listOf("unidades", "kg", "g", "litros", "ml")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir a la Lista de Compras", fontWeight = FontWeight.Bold) },
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
                    label = { Text("Nombre del Artículo") },
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

                    // Selector de Unidades
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

                // Selector de Categorías
                var showCatMenu by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría / Pasillo") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toDoubleOrNull() ?: 1.0
                    onConfirm(name, qty, unit, category)
                }
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
