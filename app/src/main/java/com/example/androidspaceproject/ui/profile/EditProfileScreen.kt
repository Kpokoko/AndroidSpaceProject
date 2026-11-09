package com.example.androidspaceproject.ui.profile

import android.Manifest
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidspaceproject.notifications.ClassReminder
import java.io.File
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit, viewModel: ProfileViewModel = viewModel()) {
    val state by viewModel.editState.collectAsState()
    val context = LocalContext.current
    val reminder = remember(context) { ClassReminder(context) }
    var showPhotoSources by rememberSaveable { mutableStateOf(false) }
    var cameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var notificationPermissionRequested by rememberSaveable { mutableStateOf(false) }
    val photoPermissions = remember {
        when {
            Build.VERSION.SDK_INT >= 34 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
            Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    val reminderSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.save() }

    fun scheduleOrRequestExactAlarm() {
        if (!reminder.notificationsEnabled()) {
            viewModel.showError("Разрешите уведомления приложения и канал «Начало любимой пары», затем нажмите «Готово»")
            try {
                reminderSettingsLauncher.launch(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                })
            } catch (_: ActivityNotFoundException) {
                viewModel.showError("Разрешите уведомления в системных настройках приложения")
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !reminder.canScheduleExact()) {
            viewModel.showError("Разрешите точные будильники для напоминания в указанную минуту")
            try {
                reminderSettingsLauncher.launch(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
                )
            } catch (_: ActivityNotFoundException) {
                viewModel.showError("Разрешите точные будильники в системных настройках приложения")
            }
        } else {
            viewModel.save()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { allowed ->
        if (allowed) scheduleOrRequestExactAlarm()
        else viewModel.showError("Для напоминания нужно разрешить уведомления. Нажмите «Готово», чтобы повторить")
    }

    val photoPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.none { it }) {
            Toast.makeText(context, "Доступ к фотографиям не разрешён", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.importAvatar(it.toString()) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = cameraUri
        if (success && uri != null) viewModel.importAvatar(uri)
        cameraUri = null
    }
    fun launchCamera() {
        try {
            val directory = File(context.cacheDir, "camera").apply { mkdirs() }
            val file = File.createTempFile("photo_", ".jpg", directory)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
            cameraUri = uri.toString()
            cameraLauncher.launch(uri)
        } catch (_: ActivityNotFoundException) {
            viewModel.showError("На устройстве нет приложения камеры")
        } catch (_: Exception) {
            viewModel.showError("Не удалось открыть камеру")
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        if (allowed) launchCamera() else viewModel.showError("Для съёмки разрешите доступ к камере")
    }

    LaunchedEffect(Unit) {
        if (!permissionRequested) {
            permissionRequested = true
            if (photoPermissions.none { granted(it) }) {
                photoPermissionLauncher.launch(photoPermissions)
            }
        }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    if (showPhotoSources) {
        AlertDialog(
            onDismissRequest = { showPhotoSources = false },
            title = { Text("Выберите фотографию") },
            text = { Text("Откуда взять новую аватарку?") },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoSources = false
                    try {
                        galleryLauncher.launch("image/*")
                    } catch (_: ActivityNotFoundException) {
                        viewModel.showError("На устройстве нет приложения для выбора фото")
                    }
                }) { Text("Галерея") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoSources = false
                    if (granted(Manifest.permission.CAMERA)) launchCamera()
                    else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) { Text("Камера") }
            }
        )
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Редактирование профиля") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            }
        )
    }) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).imePadding()
                    .verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileAvatar(
                    state.profile.avatarUri,
                    Modifier.clickable(enabled = !state.busy) { showPhotoSources = true }
                )
                Text("Нажмите на фото, чтобы изменить его", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = state.profile.fullName,
                    onValueChange = viewModel::updateName,
                    label = { Text("ФИО") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.busy,
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.profile.resumeUrl,
                    onValueChange = viewModel::updateResume,
                    label = { Text("Ссылка на резюме / портфолио") },
                    placeholder = { Text("https://example.com/resume.pdf") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.busy,
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.profile.favoriteClassTime,
                    onValueChange = viewModel::updateClassTime,
                    label = { Text("Время любимой пары") },
                    placeholder = { Text("HH:mm") },
                    supportingText = { Text(state.timeError ?: "Напоминание на сегодня") },
                    isError = state.timeError != null,
                    trailingIcon = {
                        IconButton(
                            enabled = !state.busy,
                            onClick = {
                                val time = state.profile.favoriteClassTime
                                val calendar = Calendar.getInstance()
                                val valid = ClassReminder.isValidTime(time)
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        viewModel.updateClassTime(String.format(Locale.ROOT, "%02d:%02d", hour, minute))
                                    },
                                    if (valid) time.substring(0, 2).toInt() else calendar.get(Calendar.HOUR_OF_DAY),
                                    if (valid) time.substring(3, 5).toInt() else calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                        ) { Icon(Icons.Default.AccessTime, contentDescription = "Выбрать время") }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.busy,
                    singleLine = true
                )
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (state.busy) CircularProgressIndicator()
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= 33 && !granted(Manifest.permission.POST_NOTIFICATIONS) &&
                            !notificationPermissionRequested
                        ) {
                            notificationPermissionRequested = true
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            scheduleOrRequestExactAlarm()
                        }
                    },
                    enabled = !state.busy && state.timeError == null,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Готово") }
            }
        }
    }
}
