package com.example.lab6.features.recipes.domain

import com.example.lab6.features.pantry.domain.PantryItem
import com.example.lab6.features.pantry.domain.PantryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.roundToInt

data class RecipeMatchStatus(
    val recipe: Recipe,
    val matchPercentage: Int, // Porcentaje de ingredientes disponibles en cantidad suficiente (0-100)
    val missingIngredientsSummary: List<String>, // Resumen descriptivo de lo que falta (ej: "Leche (falta 0.5L)")
    val canCook: Boolean // Verdadero si cumple al 100% con todos los ingredientes
)

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle)
 * Este caso de uso coordina la lógica de cruzar los ingredientes disponibles de la despensa con
 * las recetas del catálogo para determinar la viabilidad culinaria. Es el corazón inteligente de la app.
 */
class GetAvailableRecipesUseCase(
    private val recipeRepository: RecipeRepository,
    private val pantryRepository: PantryRepository
) {
    operator fun invoke(): Flow<List<RecipeMatchStatus>> {
        // Combinamos reactivamente el flujo de recetas y el flujo de la despensa
        return combine(
            recipeRepository.getRecipes(),
            pantryRepository.getPantryItems()
        ) { recipes, pantryItems ->
            recipes.map { recipe ->
                calculateMatchStatus(recipe, pantryItems)
            }.sortedByDescending { it.matchPercentage } // Las recetas con más ingredientes disponibles primero
        }
    }

    private fun calculateMatchStatus(recipe: Recipe, pantryItems: List<PantryItem>): RecipeMatchStatus {
        if (recipe.ingredients.isEmpty()) {
            return RecipeMatchStatus(recipe, 100, emptyList(), true)
        }

        var fullyAvailableCount = 0
        val missingList = mutableListOf<String>()

        recipe.ingredients.forEach { reqIng ->
            // Buscamos si tenemos este ingrediente en la despensa (búsqueda insensible a mayúsculas/minúsculas)
            val available = pantryItems.filter { 
                it.name.lowercase().trim() == reqIng.name.lowercase().trim() && 
                it.unit.lowercase().trim() == reqIng.unit.lowercase().trim()
            }
            
            val totalQty = available.sumOf { it.quantity }

            if (totalQty >= reqIng.quantity) {
                fullyAvailableCount++
            } else {
                val diff = reqIng.quantity - totalQty
                if (totalQty > 0) {
                    missingList.add("${reqIng.name} (tienes $totalQty ${reqIng.unit}, te faltan $diff ${reqIng.unit})")
                } else {
                    missingList.add("${reqIng.name} (falta ${reqIng.quantity} ${reqIng.unit})")
                }
            }
        }

        val percentage = ((fullyAvailableCount.toDouble() / recipe.ingredients.size) * 100).roundToInt()
        val canCook = fullyAvailableCount == recipe.ingredients.size

        return RecipeMatchStatus(
            recipe = recipe,
            matchPercentage = percentage,
            missingIngredientsSummary = missingList,
            canCook = canCook
        )
    }
}

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle)
 * Caso de uso específico para validar y agregar una receta en el sistema.
 */
class AddRecipeUseCase(private val recipeRepository: RecipeRepository) {
    suspend operator fun invoke(
        title: String,
        description: String,
        ingredients: List<RecipeIngredient>,
        steps: List<String>,
        prepTimeMinutes: Int,
        difficulty: String,
        category: String
    ) {
        require(title.isNotBlank()) { "El título de la receta no puede estar vacío" }
        require(ingredients.isNotEmpty()) { "La receta debe tener al menos un ingrediente" }
        require(steps.isNotEmpty()) { "La receta debe tener al menos un paso de preparación" }
        require(prepTimeMinutes > 0) { "El tiempo de preparación debe ser mayor a cero" }

        val newRecipe = Recipe(
            id = "rec_" + java.util.UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            ingredients = ingredients,
            steps = steps,
            prepTimeMinutes = prepTimeMinutes,
            difficulty = difficulty,
            category = category
        )
        recipeRepository.addRecipe(newRecipe)
    }
}
