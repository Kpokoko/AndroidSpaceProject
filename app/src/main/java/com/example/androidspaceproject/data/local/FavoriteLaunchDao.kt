package com.example.androidspaceproject.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLaunchDao {

    @Query("SELECT * FROM favorite_launches ORDER BY date ASC")
    fun getAllFavorites(): Flow<List<FavoriteLaunchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(launch: FavoriteLaunchEntity)

    @Delete
    suspend fun removeFavorite(launch: FavoriteLaunchEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_launches WHERE id = :launchId)")
    fun isFavorite(launchId: String): Flow<Boolean>
}