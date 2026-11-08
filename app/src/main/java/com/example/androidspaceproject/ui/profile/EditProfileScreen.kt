package com.example.androidspaceproject.ui.profile

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.os.Build
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
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit, viewModel: ProfileViewModel = viewModel()) {
    val state by viewModel.editState.collectAsState()
    val context = LocalContext.current
    var showPhotoSources by rememberSaveable { mutableStateOf(false) }
    var cameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
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
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (state.busy) CircularProgressIndicator()
                Button(
                    onClick = viewModel::save,
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Готово") }
            }
        }
    }
}
