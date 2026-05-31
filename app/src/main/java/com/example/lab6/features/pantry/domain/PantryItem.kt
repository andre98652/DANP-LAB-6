package com.example.lab6.features.pantry.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * PRINCIPIO SOLID APLICADO: SRP (Single Responsibility Principle)
 * Esta clase es una entidad pura del dominio. Su única responsabilidad es modelar un
 * ingrediente de la despensa y calcular su estado de frescura en base a reglas de negocio puras.
 * No sabe nada de base de datos, persistencia, UI o redes.
 */
data class PantryItem(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String, // ej: "kg", "unidades", "litros"
    val category: String, // ej: "Carnes", "Verduras", "Lácteos"
    val expirationDate: LocalDate, // Fecha de vencimiento
    val isNonPerishable: Boolean = false // Verdadero si no vence (ej: Sal, Azúcar)
) {
    /**
     * Calcula los días restantes para que expire.
     */
    fun daysRemaining(): Long {
        if (isNonPerishable) return 9999
        return ChronoUnit.DAYS.between(LocalDate.now(), expirationDate)
    }

    /**
     * Determina el estado de frescura según reglas de negocio corporativas.
     */
    val freshnessStatus: FreshnessStatus
        get() {
            if (isNonPerishable) return FreshnessStatus.SAFE
            val days = daysRemaining()
            return when {
                days < 0 -> FreshnessStatus.EXPIRED
                days <= 3 -> FreshnessStatus.EXPIRING_SOON
                else -> FreshnessStatus.FRESH
            }
        }
}

enum class FreshnessStatus {
    FRESH,         // Fresco / Seguro (> 3 días)
    EXPIRING_SOON, // Por vencer (<= 3 días)
    EXPIRED,       // Vencido (< 0 días)
    SAFE           // No perecedero (seguro siempre)
}
