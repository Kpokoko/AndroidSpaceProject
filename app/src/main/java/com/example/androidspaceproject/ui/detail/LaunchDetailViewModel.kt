package com.example.androidspaceproject.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidspaceproject.data.model.Launch
import com.example.androidspaceproject.data.repository.LaunchesRepository
import com.example.androidspaceproject.data.repository.MockLaunchesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class LaunchDetailUiState(
    val isLoading: Boolean = false,
    val launch: Launch? = null,
    val error: String? = null
)

class LaunchDetailViewModel(
    private val repository: LaunchesRepository = MockLaunchesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaunchDetailUiState(isLoading = true))
    val uiState: StateFlow<LaunchDetailUiState> = _uiState.asStateFlow()

    fun loadLaunch(launchId: String) {
        viewModelScope.launch {
            _uiState.value = LaunchDetailUiState(isLoading = true)
            repository.getLaunchById(launchId).collectLatest { launch ->
                if (launch != null) {
                    _uiState.value = LaunchDetailUiState(isLoading = false, launch = launch)
                } else {
                    _uiState.value = LaunchDetailUiState(
                        isLoading = false,
                        error = "Запуск с ID '$launchId' не найден."
                    )
                }
            }
        }
    }
}
