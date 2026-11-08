package com.example.androidspaceproject.domain.profile

import kotlinx.coroutines.flow.Flow

data class Profile(
    val fullName: String = "",
    val avatarUri: String = "",
    val resumeUrl: String = ""
)

data class ResumeDocument(val uri: String, val mimeType: String)

class ResumeDownloadException(message: String) : Exception(message)

interface ProfileRepository {
    val profile: Flow<Profile>
    suspend fun save(profile: Profile)
    suspend fun importAvatar(uri: String): String
    suspend fun downloadResume(url: String): ResumeDocument
}
