package com.example.lab6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lab6.features.pantry.domain.AddPantryItemUseCase
import com.example.lab6.features.pantry.domain.DeletePantryItemUseCase
import com.example.lab6.features.pantry.domain.GetPantryItemsUseCase
import com.example.lab6.features.pantry.presentation.PantryScreen
import com.example.lab6.features.pantry.presentation.PantryViewModel
import com.example.lab6.features.recipes.domain.AddRecipeUseCase
import com.example.lab6.features.recipes.domain.GetAvailableRecipesUseCase
import com.example.lab6.features.recipes.presentation.RecipesScreen
import com.example.lab6.features.recipes.presentation.RecipesViewModel
import com.example.lab6.features.shopping.domain.*
import com.example.lab6.features.shopping.presentation.ShoppingScreen
import com.example.lab6.features.shopping.presentation.ShoppingViewModel
import com.example.lab6.ui.theme.Lab6Theme

class MainActivity : ComponentActivity() {

    // INYECCIÓN DE DEPENDENCIAS A NIVEL DE ACTIVIDAD (SOLID: DIP)
    private val pantryViewModel: PantryViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = application as PantryPalApplication
                val container = app.container
                PantryViewModel(
                    getPantryItemsUseCase = GetPantryItemsUseCase(container.pantryRepository),
                    addPantryItemUseCase = AddPantryItemUseCase(container.pantryRepository),
                    deletePantryItemUseCase = DeletePantryItemUseCase(container.pantryRepository)
                )
            }
        }
    }

    private val recipesViewModel: RecipesViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = application as PantryPalApplication
                val container = app.container
                RecipesViewModel(
                    getAvailableRecipesUseCase = GetAvailableRecipesUseCase(
                        recipeRepository = container.recipeRepository,
                        pantryRepository = container.pantryRepository
                    ),
                    addRecipeUseCase = AddRecipeUseCase(container.recipeRepository)
                )
            }
        }
    }

    private val shoppingViewModel: ShoppingViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = application as PantryPalApplication
                val container = app.container
                ShoppingViewModel(
                    getShoppingItemsUseCase = GetShoppingItemsUseCase(container.shoppingRepository),
                    addShoppingItemUseCase = AddShoppingItemUseCase(container.shoppingRepository),
                    toggleShoppingItemUseCase = ToggleShoppingItemUseCase(container.shoppingRepository),
                    deleteShoppingItemUseCase = DeleteShoppingItemUseCase(container.shoppingRepository),
                    clearCompletedShoppingItemsUseCase = ClearCompletedShoppingItemsUseCase(container.shoppingRepository),
                    purchaseCompletedItemsUseCase = PurchaseCompletedItemsUseCase(
                        shoppingRepository = container.shoppingRepository,
                        pantryRepository = container.pantryRepository
                    )
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Llamamos a PantryPalApp directamente. Al ser setContent una función precompilada 
            // de la biblioteca oficial, el compilador reconoce este bloque como @Composable al 100%.
            PantryPalApp(
                pantryViewModel = pantryViewModel,
                recipesViewModel = recipesViewModel,
                shoppingViewModel = shoppingViewModel
            )
        }
    }
}

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle) en UI
 * 
 * Envolvemos el Scaffold con Lab6Theme dentro de esta función @Composable de primer orden.
 * Esto asegura que tanto el tema de la aplicación como el andamiaje principal corran dentro
 * de un bloque 100% de composición nativa, eludiendo cualquier problema de caché de metadatos locales
 * en el compilador de Kotlin durante el proceso de compilación intermedia.
 */
@Composable
fun PantryPalApp(
    pantryViewModel: PantryViewModel,
    recipesViewModel: RecipesViewModel,
    shoppingViewModel: ShoppingViewModel
) {
    Lab6Theme {
        var currentTab by remember { mutableStateOf(NavigationTab.PANTRY) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.PANTRY,
                        onClick = { currentTab = NavigationTab.PANTRY },
                        label = { Text("Despensa") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Despensa"
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.RECIPES,
                        onClick = { currentTab = NavigationTab.RECIPES },
                        label = { Text("Recetas") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Recetas"
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.SHOPPING,
                        onClick = { currentTab = NavigationTab.SHOPPING },
                        label = { Text("Compras") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Compras"
                            )
                        }
                    )
                }
            }
        ) { innerPadding ->
            val modifier = Modifier.padding(innerPadding)
            when (currentTab) {
                NavigationTab.PANTRY -> PantryScreen(
                    viewModel = pantryViewModel,
                    modifier = modifier
                )
                NavigationTab.RECIPES -> RecipesScreen(
                    viewModel = recipesViewModel,
                    modifier = modifier
                )
                NavigationTab.SHOPPING -> ShoppingScreen(
                    viewModel = shoppingViewModel,
                    modifier = modifier
                )
            }
        }
    }
}

enum class NavigationTab {
    PANTRY,
    RECIPES,
    SHOPPING
}