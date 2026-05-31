package com.example.lab6.features.recipes.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab6.core.designsystem.AppSectionHeader
import com.example.lab6.core.designsystem.GlassmorphicCard
import com.example.lab6.core.designsystem.MatchPercentageBadge
import com.example.lab6.features.recipes.domain.Recipe
import com.example.lab6.features.recipes.domain.RecipeIngredient
import com.example.lab6.features.recipes.domain.RecipeMatchStatus
import com.example.lab6.ui.theme.*

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle)
 * La pantalla de recetas renderiza la lista de platos culinarios cruzando datos en tiempo real.
 * Si el usuario pulsa en una receta, se abre un diálogo con los pasos detallados de cocción.
 * Permite añadir nuevas recetas de forma reactiva gracias a AddRecipeDialog y RecipesViewModel.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    viewModel: RecipesViewModel,
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
                title = "Buscador de Recetas",
                subtitle = "Algoritmo inteligente de coincidencia según tu despensa"
            )

            // Switch de Filtro "Puedo cocinar ya"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🍳 ¿Qué puedo cocinar hoy?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Mostrar solo recetas con 100% de ingredientes disponibles",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = state.filterOnlyCanCook,
                        onCheckedChange = { viewModel.onEvent(RecipesEvent.ToggleFilterCanCook) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.filteredRecipesMatch.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🥣",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ninguna receta disponible",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Agrega más ingredientes a tu despensa para desbloquear recetas.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 12.sp,
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
                        items = state.filteredRecipesMatch,
                        key = { it.recipe.id }
                    ) { item ->
                        RecipeMatchCard(
                            matchStatus = item,
                            onClick = { viewModel.onEvent(RecipesEvent.SelectRecipe(item)) }
                        )
                    }
                }
            }
        }

        // Botón Flotante para Añadir Receta
        FloatingActionButton(
            onClick = { viewModel.onEvent(RecipesEvent.ToggleAddDialog(true)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Receta")
        }

        // Modal de Creación de Receta
        if (state.isAddDialogOpen) {
            AddRecipeDialog(
                errorMessage = state.errorMessage,
                onDismiss = { viewModel.onEvent(RecipesEvent.ToggleAddDialog(false)) },
                onConfirm = { title, description, ingredients, steps, prepTime, difficulty, category ->
                    viewModel.onEvent(
                        RecipesEvent.AddRecipe(
                            title = title,
                            description = description,
                            ingredients = ingredients,
                            steps = steps,
                            prepTimeMinutes = prepTime,
                            difficulty = difficulty,
                            category = category
                        )
                    )
                }
            )
        }

        // Modal/Diálogo de Detalle de Receta
        state.selectedRecipe?.let { selected ->
            RecipeDetailDialog(
                matchStatus = selected,
                onDismiss = { viewModel.onEvent(RecipesEvent.ClearSelectedRecipe) }
            )
        }
    }
}

@Composable
fun RecipeMatchCard(
    matchStatus: RecipeMatchStatus,
    onClick: () -> Unit
) {
    val recipe = matchStatus.recipe

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                MatchPercentageBadge(percentage = matchStatus.matchPercentage)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = recipe.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "⏱️ ${recipe.prepTimeMinutes} min",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "🔥 ${recipe.difficulty}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "🍽️ ${recipe.category}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                if (matchStatus.canCook) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = FoodFresh,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Listo para cocinar",
                            color = FoodFresh,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Si faltan ingredientes, listarlos de forma elegante
            if (!matchStatus.canCook && matchStatus.missingIngredientsSummary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Faltante: " + matchStatus.missingIngredientsSummary.joinToString { it.split(" ").first() },
                    color = FoodExpiringSoon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailDialog(
    matchStatus: RecipeMatchStatus,
    onDismiss: () -> Unit
) {
    val recipe = matchStatus.recipe

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = recipe.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Dificultad: ${recipe.difficulty}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(text = "Tiempo: ${recipe.prepTimeMinutes} min", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ingredientes
                Column {
                    Text(text = "Ingredientes Requeridos", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    recipe.ingredients.forEach { req ->
                        val isAvailable = matchStatus.missingIngredientsSummary.none { it.startsWith(req.name) }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${req.name} (${req.quantity} ${req.unit})",
                                fontSize = 13.sp,
                                color = if (isAvailable) MaterialTheme.colorScheme.onBackground else FoodExpiringSoon
                            )
                            if (isAvailable) {
                                Text("✓ Disponible", color = FoodFresh, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("✗ Faltante", color = FoodExpired, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))

                // Pasos de preparación
                Column {
                    Text(text = "Instrucciones de Cocina", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    recipe.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeDialog(
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        ingredients: List<RecipeIngredient>,
        steps: List<String>,
        prepTime: Int,
        difficulty: String,
        category: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var prepTimeText by remember { mutableStateOf("20") }
    var difficulty by remember { mutableStateOf("Fácil") }
    var category by remember { mutableStateOf("Almuerzo") }
    var ingredientsText by remember { mutableStateOf("") }
    var stepsText by remember { mutableStateOf("") }

    val difficulties = listOf("Fácil", "Medio", "Difícil")
    val categories = listOf("Almuerzo", "Cena", "Desayuno", "Postre", "Entrada", "Otros")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Nueva Receta", fontWeight = FontWeight.Bold) },
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
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = errorMessage, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la Receta") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción corta") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = prepTimeText,
                        onValueChange = { prepTimeText = it },
                        label = { Text("Minutos") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Selector de Dificultad
                    var showDiffMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = difficulty,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Dificultad") },
                            trailingIcon = {
                                TextButton(onClick = { showDiffMenu = true }) { Text("Sel") }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = showDiffMenu,
                            onDismissRequest = { showDiffMenu = false }
                        ) {
                            difficulties.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = {
                                        difficulty = d
                                        showDiffMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Selector de Categoría
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

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Ingredientes Requeridos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Formato (uno por línea): Nombre, Cantidad, Unidad\nEj:\nPechuga de Pollo, 0.5, kg\nTomates Italianos, 2, unidades",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    lineHeight = 14.sp
                )
                OutlinedTextField(
                    value = ingredientsText,
                    onValueChange = { ingredientsText = it },
                    placeholder = { Text("Pollo, 0.5, kg\nSal, 10, g") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Instrucciones de Cocina",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Escribe un paso de preparación por línea:\nEj:\nCorta las papas en bastones.\nFríe en aceite caliente.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    lineHeight = 14.sp
                )
                OutlinedTextField(
                    value = stepsText,
                    onValueChange = { stepsText = it },
                    placeholder = { Text("Paso 1: Cortar el pollo\nPaso 2: Dorar en sartén") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prepTime = prepTimeText.toIntOrNull() ?: 20
                    
                    // Parseo inteligente de los ingredientes
                    val parsedIngredients = ingredientsText.lines()
                        .filter { it.isNotBlank() }
                        .mapNotNull { line ->
                            val parts = line.split(",")
                            if (parts.size >= 3) {
                                RecipeIngredient(
                                    name = parts[0].trim(),
                                    quantity = parts[1].trim().toDoubleOrNull() ?: 1.0,
                                    unit = parts[2].trim()
                                )
                            } else null
                        }

                    // Parseo inteligente de los pasos
                    val parsedSteps = stepsText.lines()
                        .filter { it.isNotBlank() }
                        .map { it.trim() }

                    onConfirm(title, description, parsedIngredients, parsedSteps, prepTime, difficulty, category)
                }
            ) {
                Text("Guardar Receta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
