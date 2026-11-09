package com.example.androidspaceproject.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_launches")
data class FavoriteLaunchEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val providerName: String,
    val statusName: String,
    val rocketName: String,
    val padName: String,
    val date: String,
    val image: String? = null
)