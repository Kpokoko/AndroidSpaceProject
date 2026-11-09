package com.example.androidspaceproject.ui.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidspaceproject.data.api.RetrofitInstance
import com.example.androidspaceproject.data.model.Launch
import com.example.androidspaceproject.data.repository.LaunchesRepository
import com.example.androidspaceproject.data.repository.LaunchesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LaunchDetailUiState(
    val isLoading: Boolean = false,
    val launch: Launch? = null,
    val error: String? = null
)

class LaunchDetailViewModel(
    private val repository: LaunchesRepository = LaunchesRepositoryImpl(RetrofitInstance.api)
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaunchDetailUiState(isLoading = true))
    val uiState: StateFlow<LaunchDetailUiState> = _uiState.asStateFlow()

    fun loadLaunch(id: String) {

        viewModelScope.launch {
            try {

                repository.getLaunchById(id).collect { launch ->
                    Log.d(
                        "LaunchAPI",
                        "DETAIL: ответ получен, launch=${launch?.name}"
                    )

                    _uiState.value = LaunchDetailUiState(
                        isLoading = false,
                        launch = launch,
                        error = if (launch == null) {
                            "Запуск с Id $id не найден"
                        } else {
                            null
                        }
                    )

                    Log.d("LaunchAPI", "DETAIL: state обновлён")
                }
            } catch (e: Exception) {
                Log.e("LaunchAPI", "DETAIL: ошибка", e)

                _uiState.value = LaunchDetailUiState(
                    isLoading = false,
                    launch = null,
                    error = e.message ?: "Ошибка загрузки запуска"
                )
            }
        }
    }
}
