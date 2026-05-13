package com.example.lab.ui.state

enum class SortOption {
    NAME_ASC, NAME_DESC,
    PLACES_ASC, PLACES_DESC,
    CATEGORY_ASC, CATEGORY_DESC,
    DISTANCE_ASC, DISTANCE_DESC
}

fun sortOptionLabel(option: SortOption): String = when (option) {
    SortOption.NAME_ASC -> "Nombre (A→Z)"
    SortOption.NAME_DESC -> "Nombre (Z→A)"
    SortOption.PLACES_ASC -> "Plazas (↑)"
    SortOption.PLACES_DESC -> "Plazas (↓)"
    SortOption.CATEGORY_ASC -> "Categoría (A→Z)"
    SortOption.CATEGORY_DESC -> "Categoría (Z→A)"
    SortOption.DISTANCE_ASC -> "Distancia (cerca→lejos)"
    SortOption.DISTANCE_DESC -> "Distancia (lejos→cerca)"
}