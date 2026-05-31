package com.example.lab6.features.shopping.domain

import com.example.lab6.features.pantry.domain.PantryItem
import com.example.lab6.features.pantry.domain.PantryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle)
 * Define casos de uso enfocados para la gestión de compras. 
 * Destaca PurchaseCompletedItemsUseCase, que implementa la orquestación entre la lista de compras
 * y la despensa, manteniendo los repositorios totalmente independientes entre sí (Desacoplamiento).
 */

class GetShoppingItemsUseCase(private val repository: ShoppingRepository) {
    operator fun invoke(): Flow<List<ShoppingItem>> {
        return repository.getShoppingItems().map { items ->
            // Ordena colocando los completados al final, y ordenando por categoría
            items.sortedWith(
                compareBy<ShoppingItem> { it.isCompleted }
                    .thenBy { it.category }
                    .thenBy { it.name }
            )
        }
    }
}

class AddShoppingItemUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(name: String, quantity: Double, unit: String, category: String) {
        require(name.isNotBlank()) { "El nombre del artículo no puede estar vacío" }
        require(quantity > 0) { "La cantidad debe ser mayor a cero" }

        val newItem = ShoppingItem(
            id = java.util.UUID.randomUUID().toString(),
            name = name.trim(),
            quantity = quantity,
            unit = unit,
            category = category,
            isCompleted = false
        )
        repository.addShoppingItem(newItem)
    }
}

class ToggleShoppingItemUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(item: ShoppingItem) {
        repository.updateShoppingItem(item.copy(isCompleted = !item.isCompleted))
    }
}

class DeleteShoppingItemUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(itemId: String) {
        repository.deleteShoppingItem(itemId)
    }
}

class ClearCompletedShoppingItemsUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke() {
        repository.clearCompleted()
    }
}

class PurchaseCompletedItemsUseCase(
    private val shoppingRepository: ShoppingRepository,
    private val pantryRepository: PantryRepository
) {
    /**
     * Orquesta el flujo de negocio:
     * 1. Obtiene los artículos marcados como listos en la lista de compras.
     * 2. Los transforma en PantryItems con fecha de vencimiento estimada según su categoría.
     * 3. Los inserta/suma en la Despensa.
     * 4. Limpia la lista de compras removiendo los elementos adquiridos.
     */
    suspend operator fun invoke() {
        val allItems = shoppingRepository.getShoppingItems().first()
        val purchasedItems = allItems.filter { it.isCompleted }
        
        if (purchasedItems.isEmpty()) return

        val today = LocalDate.now()

        purchasedItems.forEach { item ->
            // Estimación inteligente de fecha de vencimiento según la categoría
            val estimatedExpiration = when (item.category.lowercase()) {
                "carnes" -> today.plusDays(4)
                "lácteos" -> today.plusDays(7)
                "verduras", "frutas" -> today.plusDays(5)
                "panadería" -> today.plusDays(3)
                else -> today.plusDays(30) // Abarrotes, etc.
            }

            val isNonPerishable = item.category.lowercase() in listOf("abarrotes", "bebidas", "otros") && 
                    item.name.lowercase() in listOf("sal", "azúcar", "sal marina", "arroz", "fideos")

            val pantryItem = PantryItem(
                id = java.util.UUID.randomUUID().toString(),
                name = item.name,
                quantity = item.quantity,
                unit = item.unit,
                category = item.category,
                expirationDate = estimatedExpiration,
                isNonPerishable = isNonPerishable
            )
            pantryRepository.addPantryItem(pantryItem)
        }

        // Limpiamos los elementos comprados de la lista de compras
        shoppingRepository.clearCompleted()
    }
}
