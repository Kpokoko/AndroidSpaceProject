
package com.example.androidspaceproject.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidspaceproject.SpaceApplication
import com.example.androidspaceproject.data.preferences.FilterPreferences
import com.example.androidspaceproject.data.preferences.LaunchFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val filters: LaunchFilters = LaunchFilters(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
)

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val appContainer =
        (application as SpaceApplication).appContainer

    private val preferences = appContainer.filterPreferences
    private val filterCache = appContainer.filterCache

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadFilters()
    }

    private fun loadFilters() {
        viewModelScope.launch {
            val savedFilters = preferences.filters.first()

            filterCache.update(savedFilters)

            _uiState.value = SettingsUiState(
                filters = savedFilters,
                isLoading = false
            )
        }
    }

    fun updateStatus(value: String) {
        updateFilters {
            copy(status = value)
        }
    }

    fun updateRocket(value: String) {
        updateFilters {
            copy(rocket = value)
        }
    }

    fun updateProvider(value: String) {
        updateFilters {
            copy(provider = value)
        }
    }

    private fun updateFilters(
        update: LaunchFilters.() -> LaunchFilters
    ) {
        _uiState.value = _uiState.value.copy(
            filters = _uiState.value.filters.update(),
            isSaved = false
        )
    }

    fun saveFilters(onSaved: () -> Unit) {
        viewModelScope.launch {
            val filters = _uiState.value.filters

            preferences.saveFilters(filters)
            filterCache.update(filters)

            _uiState.value = _uiState.value.copy(isSaved = true)
            onSaved()
        }
    }
}
