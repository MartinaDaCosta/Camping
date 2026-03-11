package com.example.lab


data class Campings(
    val signatura: String,
    val nombre: String,
    val categoria: String,
    val provincia: String,
    val municipio: String,
    val direccion: String,
    val cp: Int,
    val plazas: Int,
    val numParcelas: Int,
    val web: String,
    val email: String,
    val periodo: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distanceKm: Float? = null
)

