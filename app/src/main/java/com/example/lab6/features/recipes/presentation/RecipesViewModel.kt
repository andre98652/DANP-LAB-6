package com.example.lab6.features.recipes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab6.features.recipes.domain.AddRecipeUseCase
import com.example.lab6.features.recipes.domain.GetAvailableRecipesUseCase
import com.example.lab6.features.recipes.domain.RecipeIngredient
import com.example.lab6.features.recipes.domain.RecipeMatchStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RecipesState(
    val allRecipesMatch: List<RecipeMatchStatus> = emptyList(),
    val filteredRecipesMatch: List<RecipeMatchStatus> = emptyList(),
    val filterOnlyCanCook: Boolean = false,
    val isLoading: Boolean = false,
    val selectedRecipe: RecipeMatchStatus? = null,
    val isAddDialogOpen: Boolean = false,
    val errorMessage: String? = null
)

sealed interface RecipesEvent {
    data object ToggleFilterCanCook : RecipesEvent
    data class SelectRecipe(val matchStatus: RecipeMatchStatus) : RecipesEvent
    data object ClearSelectedRecipe : RecipesEvent
    data class ToggleAddDialog(val isOpen: Boolean) : RecipesEvent
    data class AddRecipe(
        val title: String,
        val description: String,
        val ingredients: List<RecipeIngredient>,
        val steps: List<String>,
        val prepTimeMinutes: Int,
        val difficulty: String,
        val category: String
    ) : RecipesEvent
}

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle) y DIP (Dependency Inversion Principle)
 * 
 * 1. SRP: El ViewModel gestiona únicamente el estado de la presentación de recetas (si se filtran,
 *    se seleccionan o si se abre el diálogo de creación de nuevas recetas).
 * 2. DIP: Depende de GetAvailableRecipesUseCase y AddRecipeUseCase (abstracciones de casos de uso).
 */
class RecipesViewModel(
    private val getAvailableRecipesUseCase: GetAvailableRecipesUseCase,
    private val addRecipeUseCase: AddRecipeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecipesState())
    val state = _state.asStateFlow()

    init {
        loadAvailableRecipes()
    }

    private fun loadAvailableRecipes() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getAvailableRecipesUseCase()
                .catch { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
                .collect { matches ->
                    _state.update { oldState ->
                        oldState.copy(
                            isLoading = false,
                            allRecipesMatch = matches,
                            filteredRecipesMatch = applyFilter(matches, oldState.filterOnlyCanCook),
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun onEvent(event: RecipesEvent) {
        when (event) {
            is RecipesEvent.ToggleFilterCanCook -> {
                _state.update { oldState ->
                    val newFilter = !oldState.filterOnlyCanCook
                    oldState.copy(
                        filterOnlyCanCook = newFilter,
                        filteredRecipesMatch = applyFilter(oldState.allRecipesMatch, newFilter)
                    )
                }
            }
            is RecipesEvent.SelectRecipe -> {
                _state.update { it.copy(selectedRecipe = event.matchStatus) }
            }
            is RecipesEvent.ClearSelectedRecipe -> {
                _state.update { it.copy(selectedRecipe = null) }
            }
            is RecipesEvent.ToggleAddDialog -> {
                _state.update { it.copy(isAddDialogOpen = event.isOpen, errorMessage = null) }
            }
            is RecipesEvent.AddRecipe -> {
                viewModelScope.launch {
                    try {
                        addRecipeUseCase(
                            title = event.title,
                            description = event.description,
                            ingredients = event.ingredients,
                            steps = event.steps,
                            prepTimeMinutes = event.prepTimeMinutes,
                            difficulty = event.difficulty,
                            category = event.category
                        )
                        _state.update { it.copy(isAddDialogOpen = false, errorMessage = null) }
                    } catch (e: Exception) {
                        _state.update { it.copy(errorMessage = e.message) }
                    }
                }
            }
        }
    }

    private fun applyFilter(list: List<RecipeMatchStatus>, onlyCanCook: Boolean): List<RecipeMatchStatus> {
        return if (onlyCanCook) {
            list.filter { it.canCook }
        } else {
            list
        }
    }
}
