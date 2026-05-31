package com.example.lab6.features.pantry.data

import com.example.lab6.features.pantry.domain.PantryItem
import com.example.lab6.features.pantry.domain.PantryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * PRINCIPIO SOLID APLICADO: LSP (Liskov Substitution Principle)
 * Esta clase es una implementación concreta de PantryRepository en la capa de datos. 
 * Cumple rigurosamente el contrato de su supertipo PantryRepository. Si reemplazamos 
 * esta implementación en memoria por una con Room Database o Firebase en el futuro, 
 * los consumidores (ViewModels o Casos de Uso) no notarán diferencia ni se romperá la lógica,
 * ya que se respetan los tipos de retorno Flow y la naturaleza reactiva de los datos.
 */
class PantryRepositoryImpl : PantryRepository {

    // Almacenamiento reactivo en memoria para simular persistencia
    private val _pantryItems = MutableStateFlow<List<PantryItem>>(emptyList())
    private val pantryItemsState = _pantryItems.asStateFlow()

    init {
        // Precarga de alimentos de prueba para verificar los estados visuales en el laboratorio
        val today = LocalDate.now()
        _pantryItems.value = listOf(
            PantryItem(
                id = "1",
                name = "Pechuga de Pollo",
                quantity = 2.0,
                unit = "kg",
                category = "Carnes",
                expirationDate = today.plusDays(2) // Por vencer (naranja)
            ),
            PantryItem(
                id = "2",
                name = "Leche Entera",
                quantity = 1.5,
                unit = "litros",
                category = "Lácteos",
                expirationDate = today.plusDays(7) // Fresco (verde)
            ),
            PantryItem(
                id = "3",
                name = "Espinaca Fresca",
                quantity = 500.0,
                unit = "g",
                category = "Verduras",
                expirationDate = today.minusDays(2) // Vencido (rojo)
            ),
            PantryItem(
                id = "4",
                name = "Sal Marina",
                quantity = 1.0,
                unit = "kg",
                category = "Abarrotes",
                expirationDate = today,
                isNonPerishable = true // Seguro (azul / verde no expirable)
            ),
            PantryItem(
                id = "5",
                name = "Tomates Italianos",
                quantity = 6.0,
                unit = "unidades",
                category = "Verduras",
                expirationDate = today.plusDays(1) // Por vencer (naranja)
            )
        )
    }

    override fun getPantryItems(): Flow<List<PantryItem>> {
        return pantryItemsState
    }

    override suspend fun addPantryItem(item: PantryItem) {
        val currentList = _pantryItems.value.toMutableList()
        // Si ya existe el ingrediente por nombre, sumamos la cantidad (Clean Business Logic)
        val existingIndex = currentList.indexOfFirst { it.name.lowercase() == item.name.lowercase() && it.unit == item.unit }
        if (existingIndex != -1) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(
                quantity = existing.quantity + item.quantity,
                expirationDate = if (item.expirationDate.isBefore(existing.expirationDate)) item.expirationDate else existing.expirationDate
            )
        } else {
            currentList.add(item)
        }
        _pantryItems.value = currentList
    }

    override suspend fun updatePantryItem(item: PantryItem) {
        val currentList = _pantryItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == item.id }
        if (index != -1) {
            currentList[index] = item
            _pantryItems.value = currentList
        }
    }

    override suspend fun deletePantryItem(itemId: String) {
        val currentList = _pantryItems.value.toMutableList()
        currentList.removeAll { it.id == itemId }
        _pantryItems.value = currentList
    }
}
