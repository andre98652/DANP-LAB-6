package com.example.lab6.core.di

import com.example.lab6.features.pantry.data.PantryRepositoryImpl
import com.example.lab6.features.pantry.domain.PantryRepository
import com.example.lab6.features.recipes.data.RecipeRepositoryImpl
import com.example.lab6.features.recipes.domain.RecipeRepository
import com.example.lab6.features.shopping.data.ShoppingRepositoryImpl
import com.example.lab6.features.shopping.domain.ShoppingRepository

/**
 * PRINCIPIO SOLID APLICADO: DIP (Dependency Inversion Principle)
 * 
 * En lugar de utilizar librerías de generación de código pesadas (como Hilt), definimos un
 * contenedor central de dependencias manual (AppContainer). 
 * 
 * Los componentes de la UI y los ViewModels dependen únicamente de las abstracciones (interfaces)
 * expuestas por este contenedor. La clase concreta AppContainerImpl se encarga de crear las 
 * instancias únicas (Singletons en memoria) y cablear las dependencias correspondientes.
 * 
 * Esto simplifica enormemente las pruebas unitarias (se puede inyectar un MockContainer muy fácil)
 * y garantiza total transparencia sin magia oculta en tiempo de compilación.
 */
interface AppContainer {
    val pantryRepository: PantryRepository
    val recipeRepository: RecipeRepository
    val shoppingRepository: ShoppingRepository
}

class AppContainerImpl : AppContainer {
    // Inicialización perezosa de repositorios como instancias compartidas (Singletons)
    override val pantryRepository: PantryRepository by lazy {
        PantryRepositoryImpl()
    }

    override val recipeRepository: RecipeRepository by lazy {
        RecipeRepositoryImpl()
    }

    override val shoppingRepository: ShoppingRepository by lazy {
        ShoppingRepositoryImpl()
    }
}
