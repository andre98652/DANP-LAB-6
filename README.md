# 🍳 PantryPal - Asistente de Cocina Inteligente & Despensa

¡Bienvenido a **PantryPal**! Esta es una aplicación móvil avanzada de Android desarrollada íntegramente en **Jetpack Compose**. El proyecto ha sido diseñado desde cero bajo los estándares de ingeniería de software más exigentes de la industria, garantizando una arquitectura **altamente modular, desacoplada, escalable y mantenible**.

El objetivo central de este laboratorio es demostrar con hechos y código limpio la aplicación rigurosa de los **Principios SOLID**, implementando un algoritmo inteligente de coincidencia de recetas que interactúa de manera reactiva con la despensa y una lista de compras automatizada.

---

## 🏗️ 1. Arquitectura del Sistema: Limpia y Modular

Para lograr el máximo desacoplamiento, la aplicación utiliza una arquitectura **Clean Architecture** estructurada por **Paquetes de Características (Feature-by-Package)** que emula a la perfección un sistema multi-módulos físicos:

```
com.example.lab6/
│
├── core/                         # Capa transversal y sistema de diseño
│   ├── di/                       # Inyección de Dependencias Manual (SOLID: DIP)
│   ├── theme/                    # Colores premium (HSL), Tipografías, Tema Oscuro/Claro
│   └── designsystem/             # Componentes visuales genéricos y estilizados (SOLID: SRP)
│
└── features/                     # Capa de Características del Negocio (SOLID: ISP)
    │
    ├── pantry/                   # Módulo: Gestión de Despensa e Ingredientes
    │   ├── domain/               # Entidades, Interfaces de Repositorio y Casos de Uso
    │   ├── data/                 # Implementaciones de Repositorios (Persistencia reactiva en memoria)
    │   └── presentation/         # UI en Jetpack Compose, ViewModels y Gestión de Estados
    │
    ├── recipes/                  # Módulo: Buscador Inteligente de Recetas
    │   ├── domain/
    │   ├── data/
    │   └── presentation/
    │
    └── shopping/                 # Módulo: Lista de Compras y Abastecimiento Inteligente
        ├── domain/
        ├── data/
        └── presentation/
```

> [!NOTE]
> **Reactividad Unidireccional (MVI/UDF):** Cada módulo de presentación expone un estado inmutable único (`StateFlow<State>`) y procesa acciones a través de eventos inmutables (`onEvent(Event)`), previniendo efectos secundarios colaterales en la UI.

---

## 🧠 2. Aplicación Detallada de los Principios SOLID

A continuación se detalla matemáticamente y con ejemplos de código de la app cómo se cumple cada principio SOLID:

### 🧠 **S** - Single Responsibility Principle (Responsabilidad Única)
* **Teoría:** Un componente (clase, función, módulo) debe tener una, y solo una, razón para cambiar.
* **Aplicación en PantryPal:**
  * **Capa de Dominio Pura:** La clase `PantryItem` ([PantryItem.kt](file:///d:/lab6/app/src/main/java/com/example/lab6/features/pantry/domain/PantryItem.kt)) tiene la única responsabilidad de modelar la estructura de datos del ingrediente y calcular su frescura interna en base a reglas de negocio puras. No sabe nada de UI ni de almacenamiento de datos.
  * **Casos de Uso Unitarios:** Cada caso de uso realiza una única transacción de negocio. Por ejemplo, `AddPantryItemUseCase` ([PantryUseCases.kt](file:///d:/lab6/app/src/main/java/com/example/lab6/features/pantry/domain/PantryUseCases.kt)) solo se encarga de validar los datos del alimento y llamar al repositorio para persistirlo.
  * **Desacoplamiento de UI:** Los composables de Compose (`PantryScreen`, `RecipesScreen`) solo dibujan en pantalla. La lógica de control del estado visual es delegada en exclusiva a los ViewModels.

### 🔌 **O** - Open/Closed Principle (Abierto/Cerrado)
* **Teoría:** El software debe estar abierto para su extensión pero cerrado para su modificación.
* **Aplicación en PantryPal:**
  * **Extensibilidad de Recetas:** Si en el futuro deseas reemplazar el catálogo de recetas locales por una API en la nube, **no tienes que modificar una sola línea de código** de la UI ni de la lógica del algoritmo de coincidencia. Simplemente creas una nueva implementación de `RecipeRepository` (ej. `CloudRecipeRepositoryImpl`) y la inyectas en el contenedor central.
  * **Cálculo de Frescura Flexible:** El sistema evalúa el estado del alimento dinámicamente mediante el enum `FreshnessStatus`. Si la empresa decide añadir un nuevo estado (ej. *"Congelado"* o *"Maduro"*), se extiende el enum y las reglas en `daysRemaining()` sin alterar la estructura básica de almacenamiento de datos.

### 🔄 **L** - Liskov Substitution Principle (Sustitución de Liskov)
* **Teoría:** Si $S$ es un subtipo de $T$, los objetos de tipo $T$ deben poder ser reemplazados por objetos de tipo $S$ sin alterar las propiedades del programa.
* **Aplicación en PantryPal:**
  * **Sustitución de Orígenes de Datos:** La interfaz `PantryRepository` ([PantryRepository.kt](file:///d:/lab6/app/src/main/java/com/example/lab6/features/pantry/domain/PantryRepository.kt)) es implementada en memoria por `PantryRepositoryImpl`. Si mañana se implementa una base de datos local utilizando SQLite/Room (`RoomPantryRepositoryImpl`), ambas clases pueden ser intercambiadas en `AppContainerImpl` con total transparencia. 
  * Los métodos respetan estrictamente los contratos reactivos mediante `Flow<List<PantryItem>>` y funciones suspendibles, sin lanzar excepciones no controladas ni requerir comportamientos anómalos.

### 📐 **I** - Interface Segregation Principle (Segregación de Interfaces)
* **Teoría:** Los clientes no deben ser obligados a depender de interfaces que no utilizan. Es mejor muchas interfaces delgadas y enfocadas que una sola interfaz monolítica.
* **Aplicación en PantryPal:**
  * **Interfaces Especializadas:** En lugar de crear un único y gigantesco repositorio de datos para toda la aplicación (`AppRepository`), se han segregado tres interfaces altamente enfocadas y cohesivas en sus respectivos dominios:
    1. `PantryRepository` ([PantryRepository.kt](file:///d:/lab6/app/src/main/java/com/example/lab6/features/pantry/domain/PantryRepository.kt)) - Exclusivo para el inventario de cocina.
    2. `RecipeRepository` ([RecipeRepository.kt](file:///d:/lab6/app/src/main/java/com/example/lab6/features/recipes/domain/RecipeRepository.kt)) - Exclusivo para consulta del catálogo culinario.
    3. `ShoppingRepository` ([ShoppingRepository.kt](file:///d:/lab6/app/src/main/java/com/example/lab6/features/shopping/domain/ShoppingRepository.kt)) - Exclusivo para la lista de compras.
  * Esto previene que la pantalla de recetas tenga acceso indebido a operaciones de eliminación de la lista de compras, reduciendo exponencialmente el acoplamiento destructivo.

### 💉 **D** - Dependency Inversion Principle (Inversión de Dependencias)
* **Teoría:** Los módulos de alto nivel no deben depender de módulos de bajo nivel. Ambos deben depender de abstracciones (interfaces).
* **Aplicación en PantryPal:**
  * **Sin Dependencia de Detalles:** Los ViewModels (alto nivel) jamás instancian directamente las implementaciones de datos en memoria (`PantryRepositoryImpl`) ni clientes de red. Dependen exclusivamente de las interfaces abstractas de la capa de dominio.
  * **Contenedor de Inyección Manual (`AppContainer`):** Implementamos un contenedor transparente y limpio ([AppContainer.kt](file:///d:/lab6/app/src/main/java/com/example/lab6/core/di/AppContainer.kt)) que gestiona las dependencias del proyecto de manera limpia:
    ```kotlin
    interface AppContainer {
        val pantryRepository: PantryRepository
        val recipeRepository: RecipeRepository
        val shoppingRepository: ShoppingRepository
    }
    ```
  * Los ViewModels se inyectan en `MainActivity.kt` de forma declarativa mediante Compose `initializer`:
    ```kotlin
    val recipesViewModel: RecipesViewModel = viewModel(
        initializer = {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PantryPalApplication
            val container = app.container
            RecipesViewModel(
                getAvailableRecipesUseCase = GetAvailableRecipesUseCase(
                    recipeRepository = container.recipeRepository,
                    pantryRepository = container.pantryRepository
                )
            )
        }
    )
    ```

---

## 🌟 3. Características Estrella Implementadas

1. **Algoritmo Inteligente "¿Qué puedo cocinar hoy?":**
   Cruza en tiempo real todos los ingredientes requeridos de cada receta con las existencias de la despensa. Calcula el porcentaje de coincidencia exacta y destaca exactamente qué ingredientes faltan y en qué cantidad específica (ej. *"Te falta 0.5 kg de Pechuga de Pollo"*), ordenando las recetas con mayor viabilidad al inicio de la lista.
2. **Ciclo de Abastecimiento Automatizado (Traspaso):**
   Al ir de compras, marcas los casilleros de los artículos adquiridos en la pestaña **Compras**. Con un solo toque en **Completar Compra**, el sistema calcula una fecha de vencimiento inteligente estimada en función de la categoría del alimento (ej. 4 días para carnes, 7 para lácteos, no perecederos permanentes), inyecta automáticamente los alimentos nuevos o incrementa la cantidad existente en la **Despensa**, y limpia la lista de compras en una única transacción de dominio coordinada por `PurchaseCompletedItemsUseCase`.
3. **Control Visual de Alertas de Frescura:**
   La UI utiliza un esquema de color HSL premium y dinámico. Los alimentos se marcan con colores vibrantes según su fecha límite (Rojo = Vencido, Naranja = Por vencer en menos de 3 días, Verde = Fresco y Seguro, Azul = No perecedero), con ordenación inteligente que empuja los productos vencidos a la parte superior de la despensa para evitar el desperdicio de alimentos.
4. **Diseño Visual Glassmorphic:**
   Uso de tarjetas semitransparentes personalizadas, elevaciones controladas, tipografía moderna y micro-interacciones interactivas de Jetpack Compose en un esquema de color inspirado en la frescura y la cocina mediterránea.

---

## 🚀 4. Requisitos y Compilación

* **Android SDK mínimo:** API 24 (Android 7.0 Nougat).
* **Compilación recomendada:** Android Studio Ladybug / Koala o superior con **JDK 17**.
* **Framework principal:** Jetpack Compose (Kotlin `2.2.10` / Compose BOM `2026.02.01`).

Para ejecutar el proyecto localmente, simplemente abre la carpeta raíz en Android Studio, deja que sincronice con Gradle e inicia la aplicación en tu emulador o dispositivo físico. 

---

> [!TIP]
> **Fácil de Defender en Exámenes:** La inyección de dependencias manual mediante `AppContainer` y los Casos de Uso cohesivos son el material de defensa ideal para un examen oral o sustentación de laboratorio. Puedes mostrarle a tu docente exactamente cómo se inyectan los repositorios en cada constructor, demostrando un dominio absoluto de la arquitectura de software sin la necesidad de ocultar la lógica en anotaciones autogeneradas.
