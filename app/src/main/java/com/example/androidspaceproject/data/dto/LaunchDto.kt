package com.example.androidspaceproject.data.dto

data class LaunchDto(
    val id: String,
    val name: String,
    val status: LaunchStatusDto,
    val net: String,
    val launch_service_provider: LaunchServiceProviderDto?,
    val rocket: RocketDto?,
    val mission: MissionDto?,
    val pad: PadDto?,
    val image: ImageDto?,
    val probability: Int?,
    val failreason: String?,
    val webcast_live: Boolean?
)

data class LaunchStatusDto(
    val id: Int,
    val name: String,
    val abbrev: String,
    val description: String
)

data class LaunchServiceProviderDto(
    val id: Int,
    val name: String,
    val type: LaunchServiceProviderTypeDto?
)

data class LaunchServiceProviderTypeDto(
    val id: Int?,
    val name: String?
)

data class RocketDto(
    val id: Int,
    val configuration: RocketConfigurationDto?
)

data class RocketConfigurationDto(
    val id: Int,
    val name: String,
    val full_name: String?,
    val family: String?,
    val variant: String?
)

data class MissionDto(
    val id: Int,
    val name: String,
    val description: String?,
    val type: String?,
    val orbit: OrbitDto?
)

data class OrbitDto(
    val id: Int,
    val name: String,
    val abbrev: String?
)

data class PadDto(
    val id: Int,
    val name: String,
    val location: LocationDto?,
    val latitude: Double?,
    val longitude: Double?
)

data class LocationDto(
    val id: Int,
    val name: String
)