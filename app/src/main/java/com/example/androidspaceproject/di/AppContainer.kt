package com.example.androidspaceproject.di

import android.content.Context
import androidx.room.Room
import com.example.androidspaceproject.data.local.AppDatabase
import com.example.androidspaceproject.data.preferences.FilterCache
import com.example.androidspaceproject.data.preferences.FilterPreferences

class AppContainer(context: Context) {

    val filterPreferences = FilterPreferences(context)

    val filterCache = FilterCache()

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "space_app_database"
        ).build()
    }

    val favoriteLaunchDao by lazy {
        database.favoriteLaunchDao()
    }
}