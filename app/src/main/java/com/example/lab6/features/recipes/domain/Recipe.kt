package com.example.lab6.features.recipes.domain

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle)
 * Modela los datos de una receta y sus ingredientes requeridos. Es una clase de dominio pura
 * que solo se encarga de representar la estructura estática del negocio de recetas.
 */
data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>,
    val prepTimeMinutes: Int,
    val difficulty: String, // ej: "Fácil", "Medio", "Difícil"
    val category: String // ej: "Almuerzo", "Postre", "Cena"
)

data class RecipeIngredient(
    val name: String,
    val quantity: Double,
    val unit: String
)
