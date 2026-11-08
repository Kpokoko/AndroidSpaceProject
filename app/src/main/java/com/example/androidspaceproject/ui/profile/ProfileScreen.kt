package com.example.androidspaceproject.ui.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onEdit: () -> Unit, viewModel: ProfileViewModel = viewModel()) {
    val profile by viewModel.profile.collectAsState()
    val downloading by viewModel.downloading.collectAsState()
    val context = LocalContext.current

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Профиль") },
            actions = {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать профиль")
                }
            }
        )
    }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfileAvatar(profile.avatarUri)
            Text(profile.fullName.ifBlank { "Имя не указано" }, style = MaterialTheme.typography.headlineSmall)
            Button(
                enabled = profile.resumeUrl.isNotBlank() && !downloading,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.downloadResume(
                        onDownloaded = { document ->
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(document.uri), document.mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(context, "Резюме скачано, но нет приложения для просмотра этого файла", Toast.LENGTH_LONG).show()
                            }
                        },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                }
            ) { Text(if (downloading) "Скачивание…" else "Резюме") }
        }
    }
}

@Composable
internal fun ProfileAvatar(uri: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = null
        if (uri.isNotBlank()) {
            value = withContext(Dispatchers.IO) {
                try {
                    val imageUri = Uri.parse(uri)
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(imageUri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                    var sample = 1
                    while (options.outWidth / sample > 1024 || options.outHeight / sample > 1024) sample *= 2
                    options.inJustDecodeBounds = false
                    options.inSampleSize = sample
                    val decoded = context.contentResolver.openInputStream(imageUri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                    val orientation = runCatching {
                        context.contentResolver.openInputStream(imageUri)?.use {
                            ExifInterface(it).getAttributeInt(
                                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                            )
                        }
                    }.getOrNull()
                    val matrix = Matrix().apply {
                        when (orientation) {
                            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                            ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                            ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
                            ExifInterface.ORIENTATION_TRANSPOSE -> { setRotate(90f); postScale(-1f, 1f) }
                            ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                            ExifInterface.ORIENTATION_TRANSVERSE -> { setRotate(270f); postScale(-1f, 1f) }
                            ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(270f)
                        }
                    }
                    decoded?.let {
                        Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true).asImageBitmap()
                    }
                } catch (_: Exception) {
                    null
                }
            }
        }
    }
    Box(
        modifier = modifier.size(160.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val image = bitmap
        if (image != null) {
            Image(image, contentDescription = "Аватар", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Icon(Icons.Default.Person, contentDescription = "Аватар", modifier = Modifier.size(80.dp))
        }
    }
}
