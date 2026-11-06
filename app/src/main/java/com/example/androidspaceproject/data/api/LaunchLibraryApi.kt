package com.example.androidspaceproject.data.api

import com.example.androidspaceproject.data.dto.LaunchDto
import com.example.androidspaceproject.data.dto.LaunchesResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LaunchLibraryApi {

    @GET("2.3.0/launches/")
    suspend fun getLaunches(
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("ordering") ordering: String = "net"
    ): LaunchesResponseDto

    @GET("2.3.0/launches/{id}/")
    suspend fun getLaunchById(
        @Path("id") id: String
    ): LaunchDto
}