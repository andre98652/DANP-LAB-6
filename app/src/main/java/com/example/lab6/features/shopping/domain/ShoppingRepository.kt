package com.example.lab6.features.shopping.domain

import kotlinx.coroutines.flow.Flow

/**
 * PRINCIPIO SOLID APLICADO: DIP (Dependency Inversion Principle) y ISP (Interface Segregation Principle)
 * 
 * Abstracción de operaciones para la lista de compras. Los consumidores dependen de esta interfaz
 * pura sin saber la persistencia que hay detrás. Está segregada para evitar que clases que gestionen
 * compras dependan de métodos de recetas o de despensa directamente.
 */
interface ShoppingRepository {
    fun getShoppingItems(): Flow<List<ShoppingItem>>
    suspend fun addShoppingItem(item: ShoppingItem)
    suspend fun updateShoppingItem(item: ShoppingItem)
    suspend fun deleteShoppingItem(itemId: String)
    suspend fun clearCompleted()
}
