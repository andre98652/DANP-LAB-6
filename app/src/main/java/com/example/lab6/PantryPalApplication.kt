package com.example.lab6

import android.app.Application
import com.example.lab6.core.di.AppContainer
import com.example.lab6.core.di.AppContainerImpl

/**
 * PRINCIPIO SOLID APLICADO: DIP (Dependency Inversion Principle) en el ciclo de vida
 * Inicializa el contenedor central de dependencias (AppContainer) al arrancar la aplicación.
 * Proporciona el punto de acceso global para que las actividades obtengan las dependencias
 * desacopladas necesarias.
 */
class PantryPalApplication : Application() {
    
    // Contenedor expuesto como propiedad de solo lectura externa
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl()
    }
}
