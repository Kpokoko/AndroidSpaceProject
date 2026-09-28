package com.example.androidspaceproject.data.model

data class Launch(
    val id: String,
    val name: String,
    val status: LaunchStatus,
    val net: String,
    val launchServiceProvider: LaunchServiceProvider,
    val rocket: Rocket,
    val mission: Mission?,
    val pad: Pad,
    val image: String? = null,
    val probability: Int? = null,
    val failreason: String? = null,
    val webcastLive: Boolean = false
)

data class LaunchStatus(
    val id: Int,
    val name: String,
    val abbrev: String,
    val description: String
)

data class LaunchServiceProvider(
    val id: Int,
    val name: String,
    val type: String? = null,
    val countryCode: String? = null
)

data class RocketConfiguration(
    val id: Int,
    val name: String,
    val family: String,
    val fullName: String,
    val variant: String? = null
)

data class Rocket(
    val id: Int,
    val configuration: RocketConfiguration
)

data class Orbit(
    val id: Int,
    val name: String,
    val abbrev: String? = null
)

data class Mission(
    val id: Int,
    val name: String,
    val description: String,
    val type: String,
    val orbit: Orbit? = null
)

data class Location(
    val id: Int,
    val name: String
)

data class Pad(
    val id: Int,
    val name: String,
    val location: Location? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    val locationName: String
        get() = location?.name ?: ""
}
