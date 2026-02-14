# Project Structure & Implementation Guide for TrilhosPT

This document outlines the recommended file and folder structure for your Android application, focusing on a clean architecture (MVVM) to support the Retrofit implementation you've started.

## 1. Recommended Folder Structure

Your project should be organized by **features** or **layers**. Given the current state, a layer-based approach with feature subdivisions is recommended.

### `app/src/main/java/com/example/trilhospt/`

```text
com.example.trilhospt
├── TrilhosPTApplication.kt           # (Exists) Application class
├── data                              # (Exists) Data Layer
│   ├── remote
│   │   ├── api                       # (Exists) API interfaces & Clients
│   │   │   ├── ApiService.kt
│   │   │   ├── AuthInterceptor.kt
│   │   │   └── RetrofitClient.kt
│   │   └── dto                       # (Exists) Data Transfer Objects
│   │       ├── AuthResponse.kt
│   │       ├── TrailDto.kt
│   │       └── ...
│   ├── local                         # [NEW] Local Persistence (Room)
│   │   ├── dao                       # Data Access Objects
│   │   │   └── TrailDao.kt
│   │   ├── database                  # Room Database Setup
│   │   │   └── AppDatabase.kt
│   │   └── entity                    # Database Entities
│   │       └── TrailEntity.kt
│   └── repository                    # (Exists) Repositories
│       ├── ApiRepository.kt          # Remote Data operations
│       └── LocalRepository.kt         # [NEW] Local Data operations (Cache)
├── ui                                # (Exists) UI Layer
│   ├── adapters                      # (Exists) RecyclerView Adapters
│   │   └── TrailAdapter.kt
│   ├── auth                          # (Exists) Authentication Feature
│   │   ├── LoginFragment.kt
│   │   ├── RegisterFragment.kt
│   │   └── AuthViewModel.kt
│   ├── trails                        # (Exists) Trails Feature
│   │   ├── TrailListFragment.kt
│   │   ├── TrailDetailFragment.kt
│   │   ├── CreateTrailFragment.kt
│   │   ├── EditTrailFragment.kt      # [NEW] Trail Editing (Admin)
│   │   ├── TrailViewModel.kt
│   │   └── TrailViewModelWithCache.kt # [NEW] Cache Example
│   ├── MainActivity.kt               # (Exists) Main Activity hosting navigation
│   └── ViewModelFactory.kt           # (Exists) Factory to create ViewModels
└── utils                             # (Exists) Utility Classes
    ├── Constants.kt                  # Base URLs, static constants
    ├── Resource.kt                   # UI State Wrapper
    └── ValidationUtils.kt            # [NEW] Form Validation Logic
```

## 2. Dependencies to Add

To fully implement the UI and connect it to your Data layer, you should add these dependencies to `app/build.gradle.kts` in the `dependencies` block:

```kotlin
dependencies {
    // ... existing dependencies

    // Lifecycle components (ViewModel, LiveData)
    val lifecycle_version = "2.6.2"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version") // For lifecycleScope

    // Navigation Component (for moving between Fragments)
    val nav_version = "2.7.5"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Image Loading (Coil is modern and Kotlin-first)
    implementation("io.coil-kt:coil:2.5.0")
    
    // Fragment KTX
    implementation("androidx.fragment:fragment-ktx:1.6.2")
}
```

## 3. Files to Create/Update

### A. Utility Wrapper for UI State (`utils/Resource.kt`)
This class helps the UI know if data is loading, successful, or failed.

```kotlin
package com.example.trilhospt.utils

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Exception? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
```

### B. ViewModel Factory (`ui/ViewModelFactory.kt`)
Since you are not using a Dependency Injection framework like Hilt yet, you need a custom factory to pass the `ApiRepository` to your ViewModels.

```kotlin
package com.example.trilhospt.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.trilhospt.data.repository.ApiRepository

class ViewModelFactory(private val repository: ApiRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check for specific ViewModels and return them with the repository
        if (modelClass.isAssignableFrom(com.example.trilhospt.ui.auth.AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return com.example.trilhospt.ui.auth.AuthViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(com.example.trilhospt.ui.trails.TrailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return com.example.trilhospt.ui.trails.TrailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

### C. Main Activity (`ui/MainActivity.kt`)
This will be the container for your fragments. You need to set up the `FragmentContainerView` in `activity_main.xml` and handle navigation here.

### D. Layout Files (`res/layout/`)
You will need XML layouts for each screen:
1.  `activity_main.xml` (Holds the `NavHostFragment`)
2.  `fragment_login.xml` (Email/Password inputs, Login button)
3.  `fragment_register.xml` (Registration fields)
4.  `fragment_trail_list.xml` (RecyclerView)
5.  `item_trail.xml` (Design for a single trail row in the list)
6.  `fragment_trail_detail.xml` (Detailed view of a trail)

## 4. Next Implementation Steps

1.  **Create the `utils` package** and add `Resource.kt`.
2.  **Create the `ui` package** and sub-packages (`auth`, `trails`, `adapters`).
3.  **Implement `AuthViewModel`** in `ui/auth/` to handle Login/Register calls using `ApiRepository`.
4.  **Create Fragments** (`LoginFragment`, `TrailListFragment`) and their XML layouts.
5.  **Set up Navigation Graph** (`res/navigation/nav_graph.xml`) to connect these fragments.
6.  **Initialize ViewModels** in Fragments using the `ViewModelFactory`.

Example of initializing a ViewModel in a Fragment:
```kotlin
private val viewModel: AuthViewModel by viewModels {
    ViewModelFactory(com.example.trilhospt.data.repository.ApiRepository(com.example.trilhospt.data.remote.api.RetrofitClient.apiService))
}
```
