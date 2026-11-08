package com.example.androidspaceproject.data.dto

data class LaunchesResponseDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<LaunchDto>
)