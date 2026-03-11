package com.example.lab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab.Campings
import com.example.lab.data.AppDatabase
import com.example.lab.data.CampingFavorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).favoritesDao()

    val favorites: Flow<List<CampingFavorite>> = dao.getAllFavorites()
    val favoriteIds: Flow<List<String>> = dao.getAllFavoriteIds()

    fun isFavorite(signatura: String): Flow<Boolean> = dao.isFavorite(signatura)

    fun toggleFavorite(camping: Campings, isFav: Boolean) {
        viewModelScope.launch {
            if (isFav) {
                dao.removeFavorite(camping.toFavorite())
            } else {
                dao.addFavorite(camping.toFavorite())
            }
        }
    }
}

// Extension function para convertir Camping → CampingFavorite
fun Campings.toFavorite() = CampingFavorite(
    signatura = signatura,
    nombre = nombre,
    categoria = categoria,
    provincia = provincia,
    municipio = municipio,
    direccion = direccion,
    cp = cp,
    plazas = plazas,
    numParcelas = numParcelas,
    web = web,
    email = email,
    periodo = periodo
)

// Extension function para convertir CampingFavorite → Camping
fun CampingFavorite.toCamping() = Campings(
    signatura = signatura,
    nombre = nombre,
    categoria = categoria,
    provincia = provincia,
    municipio = municipio,
    direccion = direccion,
    cp = cp,
    plazas = plazas,
    numParcelas = numParcelas,
    web = web,
    email = email,
    periodo = periodo
)