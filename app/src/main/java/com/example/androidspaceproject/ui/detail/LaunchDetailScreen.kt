package com.example.androidspaceproject.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidspaceproject.data.model.Launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchDetailScreen(
    launchId: String,
    onBackClick: () -> Unit,
    viewModel: LaunchDetailViewModel = viewModel()
) {
    LaunchedEffect(launchId) {
        viewModel.loadLaunch(launchId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали запуска") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "Ошибка загрузки",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                uiState.launch?.let { launch ->
                    LaunchDetailContentConstraintLayout(
                        launch = launch
                    )
                }
            }
        }
    }
}

@Composable
fun LaunchDetailContentConstraintLayout(
    launch: Launch
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                val (
                    providerRef,
                    statusRef,
                    titleRef,
                    divider1Ref,
                    dateLabelRef,
                    dateValueRef,
                    divider2Ref,
                    missionTitleRef,
                    missionDescRef,
                    missionTypeRef,
                    divider3Ref,
                    specsTitleRef,
                    rocketRef,
                    padRef
                ) = createRefs()

                Text(
                    text = launch.launchServiceProvider.name +
                            (launch.launchServiceProvider.countryCode?.let { " ($it)" } ?: ""),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.constrainAs(providerRef) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
                )

                Text(
                    text = launch.status.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.constrainAs(statusRef) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
                )

                Text(
                    text = launch.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.constrainAs(titleRef) {
                        top.linkTo(providerRef.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.constrainAs(divider1Ref) {
                        top.linkTo(titleRef.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )

                Text(
                    text = "Время старта",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.constrainAs(dateLabelRef) {
                        top.linkTo(divider1Ref.bottom, margin = 12.dp)
                        start.linkTo(parent.start)
                    }
                )

                Text(
                    text = launch.net.replace("T", " ").replace("Z", " UTC") +
                            (launch.probability?.let { " (Вероятность погоды: $it%)" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.constrainAs(dateValueRef) {
                        top.linkTo(dateLabelRef.bottom, margin = 2.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.constrainAs(divider2Ref) {
                        top.linkTo(dateValueRef.bottom, margin = 12.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )

                if (launch.mission != null) {
                    Text(
                        text = "О миссии",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.constrainAs(missionTitleRef) {
                            top.linkTo(divider2Ref.bottom, margin = 12.dp)
                            start.linkTo(parent.start)
                        }
                    )

                    Text(
                        text = launch.mission.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.constrainAs(missionDescRef) {
                            top.linkTo(missionTitleRef.bottom, margin = 4.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                    )

                    Text(
                        text = "Тип: ${launch.mission.type}" +
                                (launch.mission.orbit?.let { " • Орбита: ${it.name}" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.constrainAs(missionTypeRef) {
                            top.linkTo(missionDescRef.bottom, margin = 6.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.constrainAs(divider3Ref) {
                            top.linkTo(missionTypeRef.bottom, margin = 12.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                    )
                }

                Text(
                    text = "Характеристики",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.constrainAs(specsTitleRef) {
                        top.linkTo(
                            if (launch.mission != null) divider3Ref.bottom else divider2Ref.bottom,
                            margin = 12.dp
                        )
                        start.linkTo(parent.start)
                    }
                )

                Text(
                    text = "Ракета: ${launch.rocket.configuration.fullName} (${launch.rocket.configuration.family})",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.constrainAs(rocketRef) {
                        top.linkTo(specsTitleRef.bottom, margin = 4.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )

                Text(
                    text = "Площадка: ${launch.pad.name}, ${launch.pad.locationName}" +
                            if (launch.pad.latitude != null && launch.pad.longitude != null) " (${launch.pad.latitude}, ${launch.pad.longitude})" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.constrainAs(padRef) {
                        top.linkTo(rocketRef.bottom, margin = 4.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
