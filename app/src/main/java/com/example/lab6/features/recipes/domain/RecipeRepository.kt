package com.example.lab6.features.recipes.domain

import kotlinx.coroutines.flow.Flow

/**
 * PRINCIPIO SOLID APLICADO: DIP (Dependency Inversion Principle)
 * Definición abstracta del repositorio de recetas. Cualquier origen de datos (ej. una API 
 * de comida internacional o una base de datos local pre-cargada) deberá implementar 
 * esta interfaz. Los consumidores (ViewModels, Casos de Uso) no se verán afectados.
 */
interface RecipeRepository {
    fun getRecipes(): Flow<List<Recipe>>
    suspend fun getRecipeById(id: String): Recipe?
    suspend fun addRecipe(recipe: Recipe)
}
