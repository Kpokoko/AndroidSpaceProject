package com.example.androidspaceproject.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.content.FileProvider
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.androidspaceproject.domain.profile.Profile
import com.example.androidspaceproject.domain.profile.ProfileRepository
import com.example.androidspaceproject.domain.profile.ResumeDocument
import com.example.androidspaceproject.domain.profile.ResumeDownloadException
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.profileDataStore by preferencesDataStore(name = "profile")

class ProfileRepositoryImpl(private val context: Context) : ProfileRepository {
    private val store = context.profileDataStore
    private val nameKey = stringPreferencesKey("full_name")
    private val avatarKey = stringPreferencesKey("avatar_uri")
    private val resumeKey = stringPreferencesKey("resume_url")
    private val classTimeKey = stringPreferencesKey("favorite_class_time")

    override val profile = store.data.map {
        Profile(it[nameKey].orEmpty(), it[avatarKey].orEmpty(), it[resumeKey].orEmpty(), it[classTimeKey].orEmpty())
    }.catch { error ->
        if (error is IOException) emit(Profile()) else throw error
    }

    override suspend fun save(profile: Profile) {
        store.edit {
            it[nameKey] = profile.fullName
            it[avatarKey] = profile.avatarUri
            it[resumeKey] = profile.resumeUrl
            it[classTimeKey] = profile.favoriteClassTime
        }
    }

    override suspend fun importAvatar(uri: String): String = withContext(Dispatchers.IO) {
        val directory = File(context.filesDir, "avatars").apply { mkdirs() }
        val file = File.createTempFile("avatar_", ".jpg", directory)
        try {
            val input = context.contentResolver.openInputStream(Uri.parse(uri))
                ?: throw IOException("Не удалось открыть фотографию")
            input.use { source -> file.outputStream().use { source.copyTo(it) } }
            if (file.length() == 0L) throw IOException("Фотография пуста")
            Uri.fromFile(file).toString()
        } catch (error: Exception) {
            file.delete()
            throw error
        }
    }

    override suspend fun downloadResume(url: String): ResumeDocument = withContext(Dispatchers.IO) {
        val address = URL(downloadUrl(url))
        require(address.protocol == "https" || address.protocol == "http")
        val connection = address.openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        var file: File? = null
        try {
            if (connection.responseCode !in 200..299) {
                throw ResumeDownloadException("Сервер не разрешил скачать файл (HTTP ${connection.responseCode}). Проверьте доступ к скачиванию по ссылке.")
            }
            val mime = connection.contentType?.substringBefore(';')?.trim()?.lowercase()
            val filename = URLUtil.guessFileName(
                connection.url.toString(), connection.getHeaderField("Content-Disposition"), mime
            )
            val extension = filename.substringAfterLast('.', "")
                .lowercase().takeIf { it.matches(Regex("[a-z0-9]{1,8}")) }
            val responseMimeType = mime?.takeUnless { it == "application/octet-stream" }
                ?: extension?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }
                ?: "application/octet-stream"
            val directory = File(context.filesDir, "resumes").apply { mkdirs() }
            val temporaryFile = File.createTempFile("resume_", ".download", directory)
            file = temporaryFile
            connection.inputStream.use { input ->
                temporaryFile.outputStream().use { input.copyTo(it) }
            }
            if (temporaryFile.length() == 0L) throw IOException("Файл пуст")

            val header = ByteArray(5)
            val isPdf = temporaryFile.inputStream().use {
                it.read(header) == header.size && header.contentEquals("%PDF-".toByteArray(Charsets.US_ASCII))
            }
            val mimeType = if (isPdf) "application/pdf" else responseMimeType
            if (mimeType == "text/html" || mimeType == "application/xhtml+xml") {
                throw ResumeDownloadException(
                    "Вместо файла сервер вернул веб-страницу. Для Google Drive проверьте, что разрешены доступ по ссылке и скачивание файла."
                )
            }
            val suffix = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                ?: extension ?: "bin"
            val destination = File(directory, "${temporaryFile.nameWithoutExtension}.$suffix")
            if (!temporaryFile.renameTo(destination)) throw IOException("Не удалось сохранить файл")
            file = destination
            ResumeDocument(
                FileProvider.getUriForFile(context, "${context.packageName}.files", destination).toString(),
                mimeType
            )
        } catch (error: Exception) {
            file?.delete()
            throw error
        } finally {
            connection.disconnect()
        }
    }

    private fun downloadUrl(url: String): String {
        val uri = Uri.parse(url)
        if (uri.host?.lowercase() !in listOf("drive.google.com", "www.drive.google.com")) return url

        val segments = uri.pathSegments
        val fileId = if (segments.size >= 3 && segments[0] == "file" && segments[1] == "d") {
            segments[2]
        } else {
            uri.getQueryParameter("id")
        }
        if (fileId.isNullOrBlank()) {
            throw ResumeDownloadException("Укажите ссылку на файл Google Drive, а не на папку.")
        }
        return Uri.Builder()
            .scheme("https")
            .authority("drive.usercontent.google.com")
            .path("download")
            .appendQueryParameter("id", fileId)
            .appendQueryParameter("export", "download")
            .appendQueryParameter("confirm", "t")
            .apply {
                uri.getQueryParameter("resourcekey")?.let { appendQueryParameter("resourcekey", it) }
            }
            .build().toString()
    }
}
