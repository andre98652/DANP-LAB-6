package com.example.lab6.features.pantry.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab6.features.pantry.domain.AddPantryItemUseCase
import com.example.lab6.features.pantry.domain.DeletePantryItemUseCase
import com.example.lab6.features.pantry.domain.GetPantryItemsUseCase
import com.example.lab6.features.pantry.domain.PantryItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PantryState(
    val items: List<PantryItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAddDialogOpen: Boolean = false
)

sealed interface PantryEvent {
    data class AddItem(
        val name: String,
        val quantity: Double,
        val unit: String,
        val category: String,
        val expirationDate: LocalDate,
        val isNonPerishable: Boolean
    ) : PantryEvent
    data class DeleteItem(val id: String) : PantryEvent
    data class ToggleDialog(val isOpen: Boolean) : PantryEvent
}

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle) y DIP (Dependency Inversion Principle)
 * 
 * 1. SRP: El ViewModel maneja únicamente la lógica de presentación del estado de la despensa.
 *    No conoce los detalles de los repositorios de datos ni la lógica de base de datos; simplemente
 *    delega en los Casos de Uso.
 * 2. DIP: El ViewModel depende puramente de los Casos de Uso (abstracciones de lógica de negocio)
 *    inyectados por constructor.
 */
class PantryViewModel(
    private val getPantryItemsUseCase: GetPantryItemsUseCase,
    private val addPantryItemUseCase: AddPantryItemUseCase,
    private val deletePantryItemUseCase: DeletePantryItemUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PantryState())
    val state = _state.asStateFlow()

    init {
        loadPantryItems()
    }

    private fun loadPantryItems() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getPantryItemsUseCase()
                .catch { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
                .collect { items ->
                    _state.update { it.copy(isLoading = false, items = items, errorMessage = null) }
                }
        }
    }

    fun onEvent(event: PantryEvent) {
        when (event) {
            is PantryEvent.AddItem -> {
                viewModelScope.launch {
                    try {
                        addPantryItemUseCase(
                            name = event.name,
                            quantity = event.quantity,
                            unit = event.unit,
                            category = event.category,
                            expirationDate = event.expirationDate,
                            isNonPerishable = event.isNonPerishable
                        )
                        _state.update { it.copy(isAddDialogOpen = false, errorMessage = null) }
                    } catch (e: Exception) {
                        _state.update { it.copy(errorMessage = e.message) }
                    }
                }
            }
            is PantryEvent.DeleteItem -> {
                viewModelScope.launch {
                    deletePantryItemUseCase(event.id)
                }
            }
            is PantryEvent.ToggleDialog -> {
                _state.update { it.copy(isAddDialogOpen = event.isOpen, errorMessage = null) }
            }
        }
    }
}
