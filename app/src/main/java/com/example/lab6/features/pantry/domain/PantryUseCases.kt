package com.example.lab6.features.pantry.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle)
 * Cada clase de caso de uso representa un único caso de uso de negocio con una única razón para cambiar.
 * Encapsulan la lógica de negocio pura y son fácilmente testeables sin dependencias de plataforma.
 */

class GetPantryItemsUseCase(private val repository: PantryRepository) {
    /**
     * Obtiene los alimentos ordenados de la siguiente manera:
     * 1. Alimentos Vencidos primero (Prioridad crítica para descartar/consumir).
     * 2. Alimentos Por Vencer (Alerta).
     * 3. Alimentos Frescos.
     * 4. No perecederos al final.
     */
    operator fun invoke(): Flow<List<PantryItem>> {
        return repository.getPantryItems().map { items ->
            items.sortedWith(
                compareBy<PantryItem> { it.freshnessStatus == FreshnessStatus.EXPIRED }
                    .thenBy { it.freshnessStatus == FreshnessStatus.EXPIRING_SOON }
                    .thenBy { it.daysRemaining() }
                    .reversed()
            )
        }
    }
}

class AddPantryItemUseCase(private val repository: PantryRepository) {
    suspend operator fun invoke(name: String, quantity: Double, unit: String, category: String, expirationDate: LocalDate, isNonPerishable: Boolean) {
        require(name.isNotBlank()) { "El nombre del ingrediente no puede estar vacío" }
        require(quantity > 0) { "La cantidad debe ser mayor a cero" }
        
        val newItem = PantryItem(
            id = java.util.UUID.randomUUID().toString(),
            name = name.trim(),
            quantity = quantity,
            unit = unit,
            category = category,
            expirationDate = expirationDate,
            isNonPerishable = isNonPerishable
        )
        repository.addPantryItem(newItem)
    }
}

class DeletePantryItemUseCase(private val repository: PantryRepository) {
    suspend operator fun invoke(itemId: String) {
        repository.deletePantryItem(itemId)
    }
}
