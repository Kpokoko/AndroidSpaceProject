
package com.example.androidspaceproject.ui.launches

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidspaceproject.SpaceApplication
import com.example.androidspaceproject.data.api.RetrofitInstance
import com.example.androidspaceproject.data.model.Launch
import com.example.androidspaceproject.data.preferences.FilterPreferences
import com.example.androidspaceproject.data.preferences.LaunchFilters
import com.example.androidspaceproject.data.repository.LaunchesRepository
import com.example.androidspaceproject.data.repository.LaunchesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class LaunchesUiState(
    val isLoading: Boolean = false,
    val launches: List<LaunchUiModel> = emptyList(),
    val searchQuery: String = "",
    val activeFilters: LaunchFilters = LaunchFilters(),
    val error: String? = null
)

class LaunchesListViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val appContainer =
        (application as SpaceApplication).appContainer

    private val repository: LaunchesRepository =
        LaunchesRepositoryImpl(RetrofitInstance.api)

    private val preferences = appContainer.filterPreferences
    private val filterCache = appContainer.filterCache

    private var allLaunches: List<LaunchUiModel> = emptyList()

    private val _uiState = MutableStateFlow(
        LaunchesUiState(isLoading = true)
    )

    val uiState: StateFlow<LaunchesUiState> =
        _uiState.asStateFlow()

    init {
        observeFilters()
        loadLaunches()
    }

    private fun Launch.toUiModel(): LaunchUiModel {
        return LaunchUiModel(
            id = id,
            providerName = launchServiceProvider.name,
            statusName = status.name,
            name = name,
            rocketName = rocket.configuration.name,
            padName = pad.name,
            date = net
                .replace("T", " ")
                .replace("Z", " UTC")
        )
    }

    private fun observeFilters() {
        viewModelScope.launch {
            preferences.filters.collectLatest { filters ->
                filterCache.update(filters)

                _uiState.value = _uiState.value.copy(
                    activeFilters = filters
                )

                applyFilters()
            }
        }
    }

    private fun loadLaunches() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                repository.getLaunches().collectLatest { launches ->

                    Log.d(
                        "LaunchAPI",
                        "Получено запусков: ${launches.size}"
                    )

                    allLaunches = launches.map { it.toUiModel() }

                    applyFilters()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("LaunchAPI", "ОШИБКА", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка загрузки данных"
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )

        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val query = state.searchQuery.trim()
        val filters = state.activeFilters

        val filtered = allLaunches.filter { launch ->

            val matchesSearch =
                query.isBlank() ||
                        launch.name.contains(query, ignoreCase = true) ||
                        launch.providerName.contains(query, ignoreCase = true) ||
                        launch.rocketName.contains(query, ignoreCase = true)

            val matchesStatus =
                filters.status.isBlank() ||
                        launch.statusName.contains(
                            filters.status.trim(),
                            ignoreCase = true
                        )

            val matchesRocket =
                filters.rocket.isBlank() ||
                        launch.rocketName.contains(
                            filters.rocket.trim(),
                            ignoreCase = true
                        )

            val matchesProvider =
                filters.provider.isBlank() ||
                        launch.providerName.contains(
                            filters.provider.trim(),
                            ignoreCase = true
                        )

            matchesSearch &&
                    matchesStatus &&
                    matchesRocket &&
                    matchesProvider
        }

        _uiState.value = _uiState.value.copy(
            launches = filtered
        )
    }
}
