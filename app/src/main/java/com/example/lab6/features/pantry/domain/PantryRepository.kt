package com.example.lab6.features.pantry.domain

import kotlinx.coroutines.flow.Flow

/**
 * PRINCIPIO SOLID APLICADO: DIP (Dependency Inversion Principle) y ISP (Interface Segregation Principle)
 * 
 * 1. DIP: Esta interfaz actúa como una abstracción. Los casos de uso e interfaces de la UI dependen 
 *    de esta abstracción, no de la base de datos de Room o de Firebase. Así, la UI no sabe de dónde vienen los datos.
 * 2. ISP: Esta interfaz está segregada y se enfoca exclusivamente en operaciones relacionadas con la despensa.
 *    No se mezcla con la lista de compras ni con recetas, evitando obligar a los clientes a depender de métodos innecesarios.
 */
interface PantryRepository {
    fun getPantryItems(): Flow<List<PantryItem>>
    suspend fun addPantryItem(item: PantryItem)
    suspend fun updatePantryItem(item: PantryItem)
    suspend fun deletePantryItem(itemId: String)
}
