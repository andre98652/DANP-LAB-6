package com.example.lab6.features.shopping.data

import com.example.lab6.features.shopping.domain.ShoppingItem
import com.example.lab6.features.shopping.domain.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PRINCIPIO SOLID APLICADO: LSP (Liskov Substitution Principle)
 * Implementa la interfaz de la lista de compras de forma limpia y reactiva en memoria.
 * Es perfectamente sustituible por cualquier otra fuente de datos física sin alterar
 * la lógica del sistema.
 */
class ShoppingRepositoryImpl : ShoppingRepository {

    private val _shoppingItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    private val shoppingItemsState = _shoppingItems.asStateFlow()

    init {
        // Carga inicial de elementos sugeridos para comprar para realizar pruebas inmediatas
        _shoppingItems.value = listOf(
            ShoppingItem(
                id = "shop_1",
                name = "Tortillas de Maíz",
                quantity = 8.0,
                unit = "unidades",
                isCompleted = false,
                category = "Panadería"
            ),
            ShoppingItem(
                id = "shop_2",
                name = "Huevos",
                quantity = 12.0,
                unit = "unidades",
                isCompleted = false,
                category = "Lácteos"
            ),
            ShoppingItem(
                id = "shop_3",
                name = "Cebolla Roja",
                quantity = 2.0,
                unit = "unidades",
                isCompleted = true, // Ya comprado, listo para agregar a la despensa
                category = "Verduras"
            )
        )
    }

    override fun getShoppingItems(): Flow<List<ShoppingItem>> {
        return shoppingItemsState
    }

    override suspend fun addShoppingItem(item: ShoppingItem) {
        val currentList = _shoppingItems.value.toMutableList()
        // Si el elemento ya existe, acumulamos
        val existingIndex = currentList.indexOfFirst { it.name.lowercase() == item.name.lowercase() && it.unit == item.unit }
        if (existingIndex != -1) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(quantity = existing.quantity + item.quantity)
        } else {
            currentList.add(item)
        }
        _shoppingItems.value = currentList
    }

    override suspend fun updateShoppingItem(item: ShoppingItem) {
        val currentList = _shoppingItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == item.id }
        if (index != -1) {
            currentList[index] = item
            _shoppingItems.value = currentList
        }
    }

    override suspend fun deleteShoppingItem(itemId: String) {
        val currentList = _shoppingItems.value.toMutableList()
        currentList.removeAll { it.id == itemId }
        _shoppingItems.value = currentList
    }

    override suspend fun clearCompleted() {
        val currentList = _shoppingItems.value.toMutableList()
        currentList.removeAll { it.isCompleted }
        _shoppingItems.value = currentList
    }
}
