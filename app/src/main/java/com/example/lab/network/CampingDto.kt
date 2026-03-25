package com.example.lab.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.example.lab.Campings

// Wrapper raíz: { "result": { "records": [...] } }
@JsonClass(generateAdapter = true)
data class CampingApiResponse(
    @Json(name = "result") val result: CampingResult
)

@JsonClass(generateAdapter = true)
data class CampingResult(
    @Json(name = "records") val records: List<CampingDto>
)

@JsonClass(generateAdapter = true)
data class CampingDto(
    @Json(name = "Signatura")     val signatura: String?,
    @Json(name = "Nombre")        val nombre: String?,
    @Json(name = "Categoria")     val categoria: String?,
    @Json(name = "Provincia")     val provincia: String?,
    @Json(name = "Municipio")     val municipio: String?,
    @Json(name = "Direccion")     val direccion: String?,
    @Json(name = "CP")            val cp: String?,
    @Json(name = "Plazas")        val plazas: String?,
    @Json(name = "Num. Parcelas") val numParcelas: String?,
    @Json(name = "Web")           val web: String?,
    @Json(name = "Email")         val email: String?,
    @Json(name = "Periodo")       val periodo: String?
)


fun CampingDto.toCampings() = Campings(
    signatura   = signatura   ?: "",
    nombre      = nombre      ?: "Sin nombre",
    categoria   = categoria   ?: "",
    provincia   = provincia   ?: "",
    municipio   = municipio   ?: "",
    direccion   = direccion   ?: "",
    cp          = cp?.toIntOrNull()          ?: 0,
    plazas      = plazas?.toIntOrNull()      ?: 0,
    numParcelas = numParcelas?.toIntOrNull() ?: 0,
    web         = web         ?: "",
    email       = email       ?: "",
    periodo     = periodo     ?: ""
)
