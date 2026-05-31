package com.example.lab6.features.shopping.domain

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle)
 * Modela un elemento de la lista de compras del supermercado. Esta clase es un modelo 
 * puro de dominio y solo maneja los atributos requeridos por la lista de compras.
 */
data class ShoppingItem(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val isCompleted: Boolean = false,
    val category: String = "Otros"
)
