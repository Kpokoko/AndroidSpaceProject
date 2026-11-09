package com.example.androidspaceproject.ui.profile

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidspaceproject.SpaceApplication
import com.example.androidspaceproject.notifications.ClassReminder
import com.example.androidspaceproject.domain.profile.Profile
import com.example.androidspaceproject.domain.profile.ResumeDocument
import com.example.androidspaceproject.domain.profile.ResumeDownloadException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditProfileState(
    val profile: Profile = Profile(),
    val loading: Boolean = true,
    val busy: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
) {
    val timeError: String?
        get() = when {
            profile.favoriteClassTime.isEmpty() -> "Укажите время любимой пары"
            !ClassReminder.isValidTime(profile.favoriteClassTime) -> "Введите время в формате HH:mm (00:00–23:59)"
            else -> null
        }
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as SpaceApplication).appContainer.profileRepository
    private val reminder = ClassReminder(application)
    val profile = repository.profile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Profile())
    private val _editState = MutableStateFlow(EditProfileState())
    val editState = _editState.asStateFlow()
    private val _downloading = MutableStateFlow(false)
    val downloading = _downloading.asStateFlow()

    init {
        viewModelScope.launch {
            _editState.value = EditProfileState(profile = repository.profile.first(), loading = false)
        }
    }

    fun updateName(value: String) {
        _editState.value = _editState.value.copy(
            profile = _editState.value.profile.copy(fullName = value), error = null
        )
    }

    fun updateResume(value: String) {
        _editState.value = _editState.value.copy(
            profile = _editState.value.profile.copy(resumeUrl = value), error = null
        )
    }

    fun updateClassTime(value: String) {
        _editState.value = _editState.value.copy(
            profile = _editState.value.profile.copy(favoriteClassTime = value), error = null
        )
    }

    fun importAvatar(uri: String) {
        viewModelScope.launch {
            _editState.value = _editState.value.copy(busy = true, error = null)
            try {
                val avatar = repository.importAvatar(uri)
                _editState.value = _editState.value.copy(
                    profile = _editState.value.profile.copy(avatarUri = avatar)
                )
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                showError("Не удалось загрузить фотографию")
            } finally {
                _editState.value = _editState.value.copy(busy = false)
            }
        }
    }

    fun showError(message: String) {
        _editState.value = _editState.value.copy(error = message)
    }

    fun save() {
        if (_editState.value.loading || _editState.value.busy) return
        _editState.value.timeError?.let {
            showError(it)
            return
        }
        val profile = _editState.value.profile.let {
            it.copy(fullName = it.fullName.trim(), resumeUrl = it.resumeUrl.trim())
        }
        val triggerAt = ClassReminder.todayAt(profile.favoriteClassTime)
        if (triggerAt <= System.currentTimeMillis()) {
            showError("Это время сегодня уже прошло. Выберите время позже текущего")
            return
        }
        if (!reminder.notificationsEnabled()) {
            showError("Разрешите уведомления в настройках приложения")
            return
        }
        if (!reminder.canScheduleExact()) {
            showError("Разрешите точные будильники и напоминания в настройках приложения")
            return
        }
        if (profile.resumeUrl.isNotEmpty() &&
            (!(profile.resumeUrl.startsWith("https://") || profile.resumeUrl.startsWith("http://")) ||
                !Patterns.WEB_URL.matcher(profile.resumeUrl).matches())
        ) {
            showError("Укажите ссылку на файл, начиная с https:// или http://")
            return
        }
        viewModelScope.launch {
            _editState.value = _editState.value.copy(busy = true, error = null)
            try {
                repository.save(profile)
                reminder.schedule(triggerAt, profile.fullName)
                _editState.value = _editState.value.copy(saved = true)
            } catch (_: SecurityException) {
                showError("Для напоминания разрешите уведомления и точные будильники")
            } catch (_: IllegalArgumentException) {
                showError("Выберите время позже текущего и сохраните ещё раз")
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                showError("Не удалось сохранить профиль")
            } finally {
                _editState.value = _editState.value.copy(busy = false)
            }
        }
    }

    fun downloadResume(onDownloaded: (ResumeDocument) -> Unit, onError: (String) -> Unit) {
        if (_downloading.value) return
        viewModelScope.launch {
            _downloading.value = true
            try {
                onDownloaded(repository.downloadResume(profile.value.resumeUrl))
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                onError(
                    if (error is ResumeDownloadException) error.message.orEmpty()
                    else "Не удалось скачать резюме. Проверьте интернет и повторите попытку."
                )
            } finally {
                _downloading.value = false
            }
        }
    }
}
