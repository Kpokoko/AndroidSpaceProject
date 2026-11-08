
package com.example.androidspaceproject.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onDone: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Настройки фильтрации")
                }
            )
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Укажите параметры фильтрации",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = uiState.filters.status,
                    onValueChange = viewModel::updateStatus,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Статус запуска") },
                    placeholder = { Text("Например: Launch Failure") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.filters.rocket,
                    onValueChange = viewModel::updateRocket,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Название ракеты") },
                    placeholder = { Text("Например: Vanguard") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.filters.provider,
                    onValueChange = viewModel::updateProvider,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Поставщик запуска") },
                    placeholder = { Text("Например: US Navy") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.saveFilters(onDone)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Готово")
                }
            }
        }
    }
}
