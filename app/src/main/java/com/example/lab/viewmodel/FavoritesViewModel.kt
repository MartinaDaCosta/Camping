package com.example.lab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab.Campings
import com.example.lab.data.AppDatabase
import com.example.lab.data.CampingFavorite
import com.example.lab.data.FavoritesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Extensión para convertir CampingFavorite → Campings
fun CampingFavorite.toCamping() = Campings(
    signatura   = signatura,
    nombre      = nombre,
    categoria   = categoria,
    provincia   = provincia,
    municipio   = municipio,
    direccion   = direccion,
    cp          = cp,
    plazas      = plazas,
    numParcelas = numParcelas,
    web         = web,
    email       = email,
    periodo     = periodo
)

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: FavoritesDao =
        AppDatabase.getInstance(application).favoritesDao()

    val favorites: Flow<List<CampingFavorite>> =
        dao.getAllFavorites()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun isFavorite(signatura: String): Flow<Boolean> =
        dao.isFavorite(signatura)

    fun toggleFavorite(camping: Campings, currentlyFavorite: Boolean) {
        viewModelScope.launch {
            val entity = CampingFavorite(
                signatura   = camping.signatura,
                nombre      = camping.nombre,
                categoria   = camping.categoria,
                provincia   = camping.provincia,
                municipio   = camping.municipio,
                direccion   = camping.direccion,
                cp          = camping.cp,
                plazas      = camping.plazas,
                numParcelas = camping.numParcelas,
                web         = camping.web,
                email       = camping.email,
                periodo     = camping.periodo
            )
            if (currentlyFavorite) dao.removeFavorite(entity)
            else dao.addFavorite(entity)
        }
    }
}