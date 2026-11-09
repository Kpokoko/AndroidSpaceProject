package com.example.androidspaceproject.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidspaceproject.SpaceApplication
import com.example.androidspaceproject.data.local.FavoriteLaunchEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val appContainer =
        (application as SpaceApplication).appContainer

    private val favoriteDao = appContainer.favoriteLaunchDao

    val favorites: StateFlow<List<FavoriteLaunchEntity>> =
        favoriteDao.getAllFavorites()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun addFavorite(launch: FavoriteLaunchEntity) {
        viewModelScope.launch {
            favoriteDao.addFavorite(launch)
        }
    }

    fun removeFavorite(launch: FavoriteLaunchEntity) {
        viewModelScope.launch {
            favoriteDao.removeFavorite(launch)
        }
    }

    fun isFavorite(launchId: String) =
        favoriteDao.isFavorite(launchId)
}