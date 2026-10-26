package com.example.androidspaceproject.ui.launches

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidspaceproject.data.model.Launch
import com.example.androidspaceproject.data.repository.LaunchesRepository
import com.example.androidspaceproject.data.repository.LaunchesRepositoryImpl
import com.example.androidspaceproject.data.api.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class LaunchesUiState(
    val isLoading: Boolean = false,
    val launches: List<LaunchUiModel> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

class LaunchesListViewModel(
    private val repository: LaunchesRepository = LaunchesRepositoryImpl(
        RetrofitInstance.api
    )
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LaunchesUiState(isLoading = true)
    )

    val uiState: StateFlow<LaunchesUiState> =
        _uiState.asStateFlow()

    init {
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

                    val query = _uiState.value.searchQuery
                    val filteredLaunches = filterLaunches(
                        launches,
                        query
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        launches = filteredLaunches.map {
                            it.toUiModel()
                        },
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e(
                    "LaunchAPI",
                    "ОШИБКА",
                    e
                )

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

        viewModelScope.launch {
            try {
                repository.searchLaunches(query).collectLatest { launches ->

                    _uiState.value = _uiState.value.copy(
                        launches = launches.map {
                            it.toUiModel()
                        },
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Ошибка поиска"
                )
            }
        }
    }

    private fun filterLaunches(
        list: List<Launch>,
        query: String
    ): List<Launch> {
        if (query.isBlank()) {
            return list
        }

        return list.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.launchServiceProvider.name.contains(
                        query,
                        ignoreCase = true
                    ) ||
                    it.rocket.configuration.name.contains(
                        query,
                        ignoreCase = true
                    )
        }
    }
}