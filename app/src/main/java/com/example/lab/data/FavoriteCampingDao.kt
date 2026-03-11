package com.example.lab.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Query("SELECT * FROM favorites ORDER BY nombre ASC")
    fun getAllFavorites(): Flow<List<CampingFavorite>>

    @Query("SELECT signatura FROM favorites")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: CampingFavorite)

    @Delete
    suspend fun removeFavorite(favorite: CampingFavorite)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE signatura = :signatura)")
    fun isFavorite(signatura: String): Flow<Boolean>
}