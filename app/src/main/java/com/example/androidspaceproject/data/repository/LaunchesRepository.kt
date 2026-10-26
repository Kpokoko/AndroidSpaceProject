package com.example.androidspaceproject.data.repository

import com.example.androidspaceproject.data.model.Launch
import com.example.androidspaceproject.data.model.LaunchServiceProvider
import com.example.androidspaceproject.data.model.LaunchStatus
import com.example.androidspaceproject.data.model.Location
import com.example.androidspaceproject.data.model.Mission
import com.example.androidspaceproject.data.model.Orbit
import com.example.androidspaceproject.data.model.Pad
import com.example.androidspaceproject.data.model.Rocket
import com.example.androidspaceproject.data.model.RocketConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

interface LaunchesRepository {
    fun getLaunches(): Flow<List<Launch>>
    fun getLaunchById(id: String): Flow<Launch?>
    fun searchLaunches(query: String): Flow<List<Launch>>
}

class MockLaunchesRepository : LaunchesRepository {

    private val launchesState = MutableStateFlow(mockLaunchesList)

    override fun getLaunches(): Flow<List<Launch>> = launchesState.asStateFlow()

    override fun getLaunchById(id: String): Flow<Launch?> {
        return launchesState.map { list -> list.find { it.id == id } }
    }

    override fun searchLaunches(query: String): Flow<List<Launch>> {
        return launchesState.map { list ->
            if (query.isBlank()) {
                list
            } else {
                list.filter { launch ->
                    launch.name.contains(query, ignoreCase = true) ||
                            launch.launchServiceProvider.name.contains(query, ignoreCase = true) ||
                            launch.rocket.configuration.name.contains(query, ignoreCase = true) ||
                            launch.status.name.contains(query, ignoreCase = true)
                }
            }
        }
    }

    companion object {
        private val statusSuccess = LaunchStatus(
            id = 3,
            name = "Запуск успешен",
            abbrev = "Success",
            description = "Запуск выполнен успешно, полезная нагрузка выведена на целевую орбиту."
        )

        private val statusGo = LaunchStatus(
            id = 1,
            name = "Разрешение на запуск",
            abbrev = "Go",
            description = "Запуск подтвержден и пройдет по расписанию."
        )

        private val statusTbd = LaunchStatus(
            id = 2,
            name = "Дата уточняется",
            abbrev = "TBD",
            description = "Дата и время запуска находятся на рассмотрении оператора."
        )

        private val spacex = LaunchServiceProvider(
            id = 121,
            name = "SpaceX",
            type = "Коммерческий",
            countryCode = "США"
        )

        private val nasa = LaunchServiceProvider(
            id = 44,
            name = "NASA",
            type = "Государственный",
            countryCode = "США"
        )

        private val rocketLab = LaunchServiceProvider(
            id = 147,
            name = "Rocket Lab",
            type = "Коммерческий",
            countryCode = "США / НЗЛ"
        )

        private val arianespace = LaunchServiceProvider(
            id = 115,
            name = "Arianespace",
            type = "Коммерческий",
            countryCode = "Франция"
        )

        val mockLaunchesList = listOf(
            Launch(
                id = "e373a213-333e-425b-8777-6e9389f5a707",
                name = "Falcon 9 Block 5 | Starlink Group 6-44",
                status = statusGo,
                net = "2025-04-12T18:45:00Z",
                launchServiceProvider = spacex,
                rocket = Rocket(
                    id = 1,
                    configuration = RocketConfiguration(
                        id = 101,
                        name = "Falcon 9 Block 5",
                        family = "Falcon",
                        fullName = "Falcon 9 Full Assessment Block 5",
                        variant = "v1.2"
                    )
                ),
                mission = Mission(
                    id = 501,
                    name = "Starlink Группа 6-44",
                    description = "Группа из 23 высокопроизводительных спутников Starlink для расширения глобального интернет-покрытия.",
                    type = "Связь",
                    orbit = Orbit(id = 8, name = "Низкая околоземная орбита", abbrev = "НОО")
                ),
                pad = Pad(
                    id = 801,
                    name = "Space Launch Complex 40 (SLC-40)",
                    location = Location(id = 11, name = "Космодром на мысе Канаверал, Флорида, США"),
                    latitude = 28.5619,
                    longitude = -80.5772
                ),
                probability = 95,
                webcastLive = true
            ),
            Launch(
                id = "a291f210-4411-4b12-911a-1f8129a00821",
                name = "Falcon Heavy | Europa Clipper",
                status = statusSuccess,
                net = "2024-10-14T16:06:00Z",
                launchServiceProvider = nasa,
                rocket = Rocket(
                    id = 2,
                    configuration = RocketConfiguration(
                        id = 102,
                        name = "Falcon Heavy",
                        family = "Falcon",
                        fullName = "Falcon Heavy Tri-Core Expendable",
                        variant = "Heavy"
                    )
                ),
                mission = Mission(
                    id = 502,
                    name = "Europa Clipper",
                    description = "Флагманская исследовательская миссия НАСА по изучению юпитерианского спутника Европа и поиска условий для жизни.",
                    type = "Исследование планет",
                    orbit = Orbit(id = 15, name = "Траектория к Юпитеру", abbrev = "JTO")
                ),
                pad = Pad(
                    id = 802,
                    name = "Launch Complex 39A (LC-39A)",
                    location = Location(id = 12, name = "Космический центр им. Кеннеди, Флорида, США"),
                    latitude = 28.6083,
                    longitude = -80.6042
                ),
                probability = 90,
                webcastLive = false
            ),
            Launch(
                id = "b103192e-9182-4122-8211-128f1190a881",
                name = "Space Launch System Block 1 | Artemis II",
                status = statusGo,
                net = "2025-09-15T12:00:00Z",
                launchServiceProvider = nasa,
                rocket = Rocket(
                    id = 3,
                    configuration = RocketConfiguration(
                        id = 103,
                        name = "SLS Block 1",
                        family = "Space Launch System",
                        fullName = "Space Launch System Crew Configuration",
                        variant = "Block 1 Crew"
                    )
                ),
                mission = Mission(
                    id = 503,
                    name = "Артемида II (Artemis II)",
                    description = "Первая пилотируемая миссия программы НАСА «Артемида» с облетом Луны экипажем из четырех астронавтов на корабле Orion.",
                    type = "Пилотируемый полет",
                    orbit = Orbit(id = 18, name = "Траектория свободного возврата от Луны", abbrev = "TLI")
                ),
                pad = Pad(
                    id = 803,
                    name = "Launch Complex 39B (LC-39B)",
                    location = Location(id = 12, name = "Космический центр им. Кеннеди, Флорида, США"),
                    latitude = 28.6272,
                    longitude = -80.6208
                ),
                probability = 85,
                webcastLive = true
            ),
            Launch(
                id = "c4819201-1002-4112-8819-2182019a1122",
                name = "Electron | Owl Night Long",
                status = statusSuccess,
                net = "2025-02-18T09:30:00Z",
                launchServiceProvider = rocketLab,
                rocket = Rocket(
                    id = 4,
                    configuration = RocketConfiguration(
                        id = 104,
                        name = "Electron",
                        family = "Electron",
                        fullName = "Rocket Lab Electron Light Rocket"
                    )
                ),
                mission = Mission(
                    id = 504,
                    name = "Owl Night Long",
                    description = "Миссия по выводу специализированного спутника радиолокационного зондирования Земли (SAR) на солнечно-синхронную орбиту.",
                    type = "Дистанционное зондирование",
                    orbit = Orbit(id = 17, name = "Солнечно-синхронная орбита", abbrev = "ССО")
                ),
                pad = Pad(
                    id = 804,
                    name = "Launch Complex 1A (LC-1A)",
                    location = Location(id = 14, name = "Полуостров Махия, Новая Зеландия"),
                    latitude = -39.2608,
                    longitude = 177.8658
                ),
                probability = 100,
                webcastLive = false
            ),
            Launch(
                id = "d5910212-2003-4921-9920-3183020b2233",
                name = "Ariane 64 | Flight VA262",
                status = statusTbd,
                net = "2025-11-20T21:15:00Z",
                launchServiceProvider = arianespace,
                rocket = Rocket(
                    id = 5,
                    configuration = RocketConfiguration(
                        id = 105,
                        name = "Ariane 64",
                        family = "Ariane",
                        fullName = "Ariane 6 Heavy Four-Booster Configuration",
                        variant = "A64"
                    )
                ),
                mission = Mission(
                    id = 505,
                    name = "Развертывание спутников Kuiper",
                    description = "Коммерческий запуск европейской спутниковой группировки связи на среднюю околоземную орбиту.",
                    type = "Связь",
                    orbit = Orbit(id = 12, name = "Средняя околоземная орбита", abbrev = "СОО")
                ),
                pad = Pad(
                    id = 805,
                    name = "ELA-4 (Ensemble de Lancement Ariane 4)",
                    location = Location(id = 19, name = "Космодром Куру, Французская Гвиана"),
                    latitude = 5.239,
                    longitude = -52.768
                ),
                probability = null,
                webcastLive = false
            )
        )
    }
}
