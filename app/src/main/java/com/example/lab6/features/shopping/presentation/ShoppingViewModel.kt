package com.example.lab6.features.shopping.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab6.features.shopping.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ShoppingState(
    val items: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAddDialogOpen: Boolean = false
)

sealed interface ShoppingEvent {
    data class AddItem(val name: String, val quantity: Double, val unit: String, val category: String) : ShoppingEvent
    data class ToggleItem(val item: ShoppingItem) : ShoppingEvent
    data class DeleteItem(val id: String) : ShoppingEvent
    data object ClearCompleted : ShoppingEvent
    data object CompletePurchase : ShoppingEvent
    data class ToggleDialog(val isOpen: Boolean) : ShoppingEvent
}

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle) y DIP (Dependency Inversion Principle)
 * 
 * 1. SRP: El ViewModel gestiona el estado de presentación para la lista de compras del supermercado.
 *    Delega la orquestación pesada de compras a los casos de uso respectivos.
 * 2. DIP: Depende de casos de uso inyectados por constructor de forma abstracta.
 */
class ShoppingViewModel(
    private val getShoppingItemsUseCase: GetShoppingItemsUseCase,
    private val addShoppingItemUseCase: AddShoppingItemUseCase,
    private val toggleShoppingItemUseCase: ToggleShoppingItemUseCase,
    private val deleteShoppingItemUseCase: DeleteShoppingItemUseCase,
    private val clearCompletedShoppingItemsUseCase: ClearCompletedShoppingItemsUseCase,
    private val purchaseCompletedItemsUseCase: PurchaseCompletedItemsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ShoppingState())
    val state = _state.asStateFlow()

    init {
        loadShoppingItems()
    }

    private fun loadShoppingItems() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getShoppingItemsUseCase()
                .catch { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
                .collect { items ->
                    _state.update { it.copy(isLoading = false, items = items, errorMessage = null) }
                }
        }
    }

    fun onEvent(event: ShoppingEvent) {
        when (event) {
            is ShoppingEvent.AddItem -> {
                viewModelScope.launch {
                    try {
                        addShoppingItemUseCase(
                            name = event.name,
                            quantity = event.quantity,
                            unit = event.unit,
                            category = event.category
                        )
                        _state.update { it.copy(isAddDialogOpen = false, errorMessage = null) }
                    } catch (e: Exception) {
                        _state.update { it.copy(errorMessage = e.message) }
                    }
                }
            }
            is ShoppingEvent.ToggleItem -> {
                viewModelScope.launch {
                    toggleShoppingItemUseCase(event.item)
                }
            }
            is ShoppingEvent.DeleteItem -> {
                viewModelScope.launch {
                    deleteShoppingItemUseCase(event.id)
                }
            }
            is ShoppingEvent.ClearCompleted -> {
                viewModelScope.launch {
                    clearCompletedShoppingItemsUseCase()
                }
            }
            is ShoppingEvent.CompletePurchase -> {
                viewModelScope.launch {
                    purchaseCompletedItemsUseCase()
                }
            }
            is ShoppingEvent.ToggleDialog -> {
                _state.update { it.copy(isAddDialogOpen = event.isOpen, errorMessage = null) }
            }
        }
    }
}
