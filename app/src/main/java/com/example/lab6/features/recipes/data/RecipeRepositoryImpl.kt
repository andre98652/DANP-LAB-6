package com.example.lab6.features.recipes.data

import com.example.lab6.features.recipes.domain.Recipe
import com.example.lab6.features.recipes.domain.RecipeIngredient
import com.example.lab6.features.recipes.domain.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PRINCIPIO SOLID APLICADO: LSP (Liskov Substitution Principle)
 * Implementa la interfaz de dominio de recetas de forma impecable y desacoplada, 
 * suministrando recetas locales cargadas en memoria. Puede intercambiarse por una API 
 * de red en cualquier momento sin romper el resto de la aplicación.
 */
class RecipeRepositoryImpl : RecipeRepository {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val recipesState = _recipes.asStateFlow()

    init {
        _recipes.value = listOf(
            Recipe(
                id = "rec_1",
                title = "Tacos de Pollo con Guacamole",
                description = "Un plato mexicano vibrante, sabroso y fácil de preparar. Ideal para cenas rápidas.",
                ingredients = listOf(
                    RecipeIngredient("Pechuga de Pollo", 0.5, "kg"),
                    RecipeIngredient("Tomates Italianos", 2.0, "unidades"),
                    RecipeIngredient("Palta / Aguacate", 1.0, "unidades"),
                    RecipeIngredient("Tortillas de Maíz", 4.0, "unidades"),
                    RecipeIngredient("Sal Marina", 10.0, "g")
                ),
                steps = listOf(
                    "Corta la pechuga de pollo en tiras finas y sazónala con sal marina.",
                    "Cocina el pollo en una sartén caliente con un chorrito de aceite hasta que esté dorado.",
                    "Prepara el guacamole machacando la palta y mezclándola con tomate picado y un toque de sal.",
                    "Calienta las tortillas de maíz en una sartén limpia.",
                    "Arma los tacos colocando el pollo sobre las tortillas y decorando con abundante guacamole."
                ),
                prepTimeMinutes = 20,
                difficulty = "Fácil",
                category = "Cena"
            ),
            Recipe(
                id = "rec_2",
                title = "Saltado Criollo de Pollo",
                description = "Una exquisita adaptación peruana rápida, jugosa y llena de sabor criollo.",
                ingredients = listOf(
                    RecipeIngredient("Pechuga de Pollo", 0.5, "kg"),
                    RecipeIngredient("Tomates Italianos", 3.0, "unidades"),
                    RecipeIngredient("Cebolla Roja", 1.0, "unidades"),
                    RecipeIngredient("Papas Amarillas", 3.0, "unidades"),
                    RecipeIngredient("Sal Marina", 15.0, "g")
                ),
                steps = listOf(
                    "Corta las papas en bastones y fríelas en abundante aceite caliente hasta que estén crujientes.",
                    "Corta el pollo en cubos, sazónalo y saltéalo en un wok a fuego muy alto para lograr el toque ahumado.",
                    "Agrega la cebolla cortada en gajos gruesos al wok y saltea por 1 minuto.",
                    "Añade el tomate picado y saltea manteniendo la frescura de los vegetales.",
                    "Mezcla todo con las papas fritas y sirve de inmediato acompañado de arroz caliente."
                ),
                prepTimeMinutes = 25,
                difficulty = "Medio",
                category = "Almuerzo"
            ),
            Recipe(
                id = "rec_3",
                title = "Tortilla Fit de Espinaca",
                description = "Una opción saludable, rica en hierro y sumamente proteica para empezar el día.",
                ingredients = listOf(
                    RecipeIngredient("Espinaca Fresca", 200.0, "g"),
                    RecipeIngredient("Huevos", 3.0, "unidades"),
                    RecipeIngredient("Queso Fresco", 100.0, "g"),
                    RecipeIngredient("Sal Marina", 5.0, "g")
                ),
                steps = listOf(
                    "Lava bien la espinaca fresca y saltéala en una sartén durante 2 minutos hasta que reduzca.",
                    "En un recipiente aparte, bate los huevos enérgicamente y condimenta con sal marina.",
                    "Incorpora la espinaca y el queso fresco cortado en cubos pequeños a la mezcla de huevos.",
                    "Vierte la mezcla en la sartén a fuego medio-bajo y cocina tapado por 5 minutos.",
                    "Da la vuelta con cuidado con un plato y cocina 2 minutos más por el otro lado."
                ),
                prepTimeMinutes = 15,
                difficulty = "Fácil",
                category = "Desayuno"
            )
        )
    }

    override fun getRecipes(): Flow<List<Recipe>> {
        return recipesState
    }

    override suspend fun getRecipeById(id: String): Recipe? {
        return _recipes.value.find { it.id == id }
    }

    override suspend fun addRecipe(recipe: Recipe) {
        val currentList = _recipes.value.toMutableList()
        currentList.add(recipe)
        _recipes.value = currentList
    }
}
