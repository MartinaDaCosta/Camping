package com.example.lab.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class CampingFavorite(
    @PrimaryKey
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
    val periodo: String
)