# 🍳 PantryPal - Mi Asistente de Cocina Inteligente & Despensa

¡Bienvenido a **PantryPal**! Esta es mi aplicación móvil avanzada de Android desarrollada íntegramente en **Jetpack Compose**. Diseñé este proyecto desde cero bajo los estándares de ingeniería de software más exigentes de la industria, garantizando una arquitectura **altamente modular, desacoplada, escalable y mantenible**.

El objetivo central de mi laboratorio es demostrar con hechos y código limpio cómo apliqué rigurosamente los **Principios SOLID**, implementando un algoritmo inteligente de coincidencia de recetas que interactúa de manera reactiva con la despensa y una lista de compras automatizada.

---

## 🏗️ 1. Mi Arquitectura del Sistema: Limpia y Modular

Para lograr el máximo desacoplamiento, decidí utilizar una arquitectura **Clean Architecture** estructurada por **Paquetes de Características (Feature-by-Package)** que emula a la perfección un sistema multi-módulos físicos:

```
com.example.lab6/
│
├── core/                         # Capa transversal y mi sistema de diseño
│   ├── di/                       # Inyección de Dependencias Manual (SOLID: DIP)
│   ├── theme/                    # Mis colores premium (HSL), Tipografías y Tema
│   └── designsystem/             # Componentes visuales genéricos y estilizados (SOLID: SRP)
│
└── features/                     # Capa de Características del Negocio (SOLID: ISP)
    │
    ├── pantry/                   # Mi Módulo: Gestión de Despensa e Ingredientes
    │   ├── domain/               # Mis Entidades, Interfaces de Repositorio y Casos de Uso
    │   ├── data/                 # Implementaciones de Repositorio (Persistencia reactiva en memoria)
    │   └── presentation/         # UI en Jetpack Compose, ViewModels y Gestión de Estados
    │
    ├── recipes/                  # Mi Módulo: Buscador Inteligente de Recetas
    │   ├── domain/
    │   ├── data/
    │   └── presentation/
    │
    └── shopping/                 # Mi Módulo: Lista de Compras y Abastecimiento
        ├── domain/
        ├── data/
        └── presentation/
```

> [!NOTE]
> **Mi Enfoque de Reactividad Unidireccional (MVI/UDF):** Cada módulo de presentación que diseñé expone un estado inmutable único (`StateFlow<State>`) y procesa acciones a través de eventos inmutables (`onEvent(Event)`), previniendo efectos secundarios colaterales en mi UI.

---

## 🧠 2. Cómo apliqué los Principios SOLID en mi Proyecto

A continuación, detallo y justifico cómo apliqué cada una de las letras de los principios SOLID en mi desarrollo:

### 🧠 **S** - Single Responsibility Principle (Principio de Responsabilidad Única)
* **Mi Definición:** Diseñé cada clase, función o archivo para tener una, y solo una, razón para cambiar.
* **Cómo lo apliqué en mi código:**
  * **Capa de Dominio Pura:** Mi clase `PantryItem` ([PantryItem.kt](file:///d:/lab6/app/src/main/java/com/example/lab6/features/pantry/domain/PantryItem.kt)) tiene la única responsabilidad de modelar la estructura de datos del ingrediente y calcular su frescura interna en base a reglas de negocio puras. No sabe nada de UI, bases de datos o redes.
  * **Casos de Uso Unitarios:** Creé un archivo de casos de uso para cada tarea lógica. Por ejemplo, `AddPantryItemUseCase` ([PantryUseCases.kt](file:///d:/lab6/app/src/main/java/com/example/lab6/features/pantry/domain/PantryUseCases.kt)) solo se encarga de validar los datos del alimento y llamar al repositorio para persistirlo.
  * **Desacoplamiento de UI:** Mis composables de Compose (`PantryScreen`, `RecipesScreen`) solo dibujan en pantalla. La lógica de control del estado visual la delegué en exclusiva a los ViewModels correspondientes.

### 🔌 **O** - Open/Closed Principle (Principio de Abierto/Cerrado)
* **Mi Definición:** Diseñé mi código para estar abierto para su extensión pero cerrado para su modificación.
* **Cómo lo apliqué en mi código:**
  * **Extensibilidad de Recetas:** Si en el futuro decido reemplazar mi catálogo de recetas locales por una API en la nube, **no tendré que modificar una sola línea de código** de mi UI ni de mi algoritmo de coincidencia. Simplemente crearé una nueva implementación de `RecipeRepository` (ej. `CloudRecipeRepositoryImpl`) y la inyectaré en mi contenedor central de dependencias.
  * **Cálculo de Frescura Flexible:** Mi sistema evalúa el estado de frescura de los alimentos dinámicamente mediante el enum `FreshnessStatus`. Si decido añadir un nuevo estado (ej. *"Congelado"* o *"Maduro"*), solo tengo que extender el enum y las reglas en `daysRemaining()` sin alterar la estructura de mis repositorios.

### 🔄 **L** - Liskov Substitution Principle (Principio de Sustitución de Liskov)
* **Mi Definición:** Diseñé mis subtipos para poder sustituir a sus supertipos sin romper la consistencia de mi programa.
* **Cómo lo apliqué en mi código:**
  * **Sustitución de Repositorios:** Mi interfaz `PantryRepository` ([PantryRepository.kt](file:///d:/lab6/app/src/main/java/com/example/lab6/features/pantry/domain/PantryRepository.kt)) es implementada en memoria por `PantryRepositoryImpl`. Si mañana decido implementar una base de datos física local utilizando **Room** (`RoomPantryRepositoryImpl`), podré intercambiarlas en mi `AppContainerImpl` de forma inmediata. 
  * Todos mis métodos respetan estrictamente los contratos reactivos mediante `Flow<List<PantryItem>>` y funciones suspendibles, sin lanzar excepciones imprevistas.

### 📐 **I** - Interface Segregation Principle (Principio de Segregación de Interfaces)
* **Mi Definición:** Evité crear interfaces monopólicas. Preferí diseñar múltiples interfaces delgadas y altamente cohesivas.
* **Cómo lo apliqué en mi código:**
  * **Interfaces Especializadas:** En lugar de crear una gran interfaz `AppRepository` para toda la app, decidí segregar mis operaciones en tres interfaces enfocadas por dominio:
    1. `PantryRepository` - Dedicado únicamente al inventario y frescura.
    2. `RecipeRepository` - Dedicado únicamente a la consulta de recetas.
    3. `ShoppingRepository` - Dedicado únicamente a la lista de compras del supermercado.
  * Esto previene que mi pantalla de recetas tenga acceso a operaciones de borrado de la lista de compras, asegurando que mis clases dependan solo de lo que necesitan.

### 💉 **D** - Dependency Inversion Principle (Principio de Inversión de Dependencias)
* **Mi Definición:** Mis módulos de alto nivel (UI/ViewModels) no dependen de detalles de bajo nivel (persistencia o redes). Ambos dependen exclusivamente de abstracciones (interfaces).
* **Cómo lo apliqué en mi código:**
  * **Sin Dependencia de Detalles:** Mis ViewModels jamás instancian directamente las implementaciones de mis datos (`PantryRepositoryImpl`) ni clientes de red. Dependen exclusivamente de mis interfaces de dominio.
  * **Mi Contenedor de Inyección Manual (`AppContainer`):** Para evitar fallos de compilación con Hilt, implementé un contenedor central de dependencias manual ([AppContainer.kt](file:///d:/lab6/app/src/main/java/com/example/lab6/core/di/AppContainer.kt)):
    ```kotlin
    interface AppContainer {
        val pantryRepository: PantryRepository
        val recipeRepository: RecipeRepository
        val shoppingRepository: ShoppingRepository
    }
    ```
  * Mis ViewModels se inyectan a nivel de actividad en `MainActivity.kt` mediante el delegado oficial de Kotlin `by viewModels` y `viewModelFactory`:
    ```kotlin
    private val pantryViewModel: PantryViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = application as PantryPalApplication
                val container = app.container
                PantryViewModel(
                    getPantryItemsUseCase = GetPantryItemsUseCase(container.pantryRepository),
                    addPantryItemUseCase = AddPantryItemUseCase(container.pantryRepository),
                    deletePantryItemUseCase = DeletePantryItemUseCase(container.pantryRepository)
                )
            }
        }
    }
    ```

---

## 🌟 3. Características Estrella que Desarrollé en mi App

1. **Algoritmo Reactivo "¿Qué puedo cocinar hoy?":**
   Construí un caso de uso (`GetAvailableRecipesUseCase`) que cruza en tiempo real todos los ingredientes requeridos de cada receta con las existencias en mi despensa. Calcula de forma automática el porcentaje de coincidencia exacta y destaca con precisión qué ingredientes faltan y en qué cantidad específica (ej. *"Te falta 0.5 kg de Pechuga de Pollo"*), empujando las recetas con mayor viabilidad al inicio de la lista.
2. **Ciclo de Abastecimiento Automatizado (Traspaso):**
   Al ir de compras, marco los casilleros de los artículos adquiridos en mi pestaña **Compras**. Con un solo toque en **Completar**, mi sistema calcula una fecha de vencimiento inteligente estimada según la categoría del alimento (ej. 4 días para carnes, 7 para lácteos, no perecederos permanentes) e inyecta de forma automática los alimentos en mi **Despensa**, limpiando mi lista de compras en una única transacción.
3. **Jerarquía Visual y Control Cromático de Frescura:**
   La UI utiliza un esquema de color HSL premium y dinámico. Los alimentos se marcan con colores vibrantes según su fecha límite (Rojo = Vencido, Naranja = Por vencer en menos de 3 días, Verde = Fresco y Seguro, Azul = No perecedero), con una ordenación inteligente que empuja los productos vencidos a la parte superior de la despensa. Además, rediseñé las tarjetas para que la categoría aparezca como un subtítulo estilizado en mayúsculas con espaciado de letras, asegurando que nunca se oculte si el nombre del alimento es largo.
4. **Adición Dinámica de Recetas con Parser Inteligente:**
   Implementé la posibilidad de añadir nuevas recetas a través de un Botón de Acción Flotante (FAB). Diseñé un cuadro de diálogo con un parser de texto que analiza líneas escritas en formato `Nombre, Cantidad, Unidad` para los ingredientes y procesa las instrucciones línea por línea. Al guardarse, se integra al instante con el buscador inteligente de forma reactiva.

---

## 🚀 4. Requisitos y Ejecución de mi Proyecto

* **Android SDK mínimo:** API 24 (Android 7.0 Nougat).
* **Compilación recomendada:** Android Studio Ladybug / Koala o superior con **JDK 17**.
* **Framework principal:** Jetpack Compose (Kotlin `2.2.10` / Compose BOM `2026.02.01`).

Para ejecutar mi proyecto localmente, simplemente abre esta carpeta raíz en tu Android Studio, sincroniza el proyecto con los archivos de Gradle e inicia la aplicación en tu emulador o dispositivo físico.

---

> [!TIP]
> **Mi Estrategia de Defensa:** La inyección de dependencias manual mediante `AppContainer` y mis Casos de Uso cohesivos son ideales para sustentar. Puedo mostrarle a mi docente exactamente cómo se inyectan los repositorios en cada constructor, demostrando un dominio absoluto de la arquitectura de software sin la necesidad de ocultar la lógica en anotaciones complejas autogeneradas.
