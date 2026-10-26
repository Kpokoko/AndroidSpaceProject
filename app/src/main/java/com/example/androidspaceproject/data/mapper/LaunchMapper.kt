package com.example.androidspaceproject.data.mapper

import com.example.androidspaceproject.data.dto.LaunchDto
import com.example.androidspaceproject.data.model.Launch
import com.example.androidspaceproject.data.model.LaunchServiceProvider
import com.example.androidspaceproject.data.model.LaunchStatus
import com.example.androidspaceproject.data.model.Location
import com.example.androidspaceproject.data.model.Mission
import com.example.androidspaceproject.data.model.Orbit
import com.example.androidspaceproject.data.model.Pad
import com.example.androidspaceproject.data.model.Rocket
import com.example.androidspaceproject.data.model.RocketConfiguration

fun LaunchDto.toDomain(): Launch {
    return Launch(
        id = id,
        name = name,

        status = LaunchStatus(
            id = status.id,
            name = status.name,
            abbrev = status.abbrev,
            description = status.description
        ),

        net = net,

        launchServiceProvider = LaunchServiceProvider(
            id = launch_service_provider?.id ?: 0,
            name = launch_service_provider?.name ?: "Неизвестно",
            type = launch_service_provider?.type?.name,
            countryCode = null
        ),

        rocket = Rocket(
            id = rocket?.id ?: 0,
            configuration = RocketConfiguration(
                id = rocket?.configuration?.id ?: 0,
                name = rocket?.configuration?.name ?: "Неизвестная ракета",
                family = rocket?.configuration?.family ?: "",
                fullName = rocket?.configuration?.full_name ?: "",
                variant = rocket?.configuration?.variant
            )
        ),

        mission = mission?.let {
            Mission(
                id = it.id,
                name = it.name,
                description = it.description ?: "",
                type = it.type ?: "",
                orbit = it.orbit?.let { orbit ->
                    Orbit(
                        id = orbit.id,
                        name = orbit.name,
                        abbrev = orbit.abbrev
                    )
                }
            )
        },

        pad = Pad(
            id = pad?.id ?: 0,
            name = pad?.name ?: "Неизвестная площадка",
            location = pad?.location?.let {
                Location(
                    id = it.id,
                    name = it.name
                )
            },
            latitude = pad?.latitude,
            longitude = pad?.longitude
        ),

        image = image?.image_url,
        probability = probability,
        failreason = failreason,
        webcastLive = webcast_live ?: false
    )
}