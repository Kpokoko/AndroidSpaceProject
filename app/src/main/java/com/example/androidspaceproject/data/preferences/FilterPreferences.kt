
package com.example.androidspaceproject.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "launch_filter_settings"
)

data class LaunchFilters(
    val status: String = "",
    val rocket: String = "",
    val provider: String = ""
)

class FilterPreferences(context: Context) {

    private val dataStore = context.dataStore

    private object Keys {
        val STATUS = stringPreferencesKey("status")
        val ROCKET = stringPreferencesKey("rocket")
        val PROVIDER = stringPreferencesKey("provider")
    }

    val filters: Flow<LaunchFilters> = dataStore.data.map { preferences ->
        LaunchFilters(
            status = preferences[Keys.STATUS] ?: "",
            rocket = preferences[Keys.ROCKET] ?: "",
            provider = preferences[Keys.PROVIDER] ?: ""
        )
    }

    suspend fun saveFilters(filters: LaunchFilters) {
        dataStore.edit { preferences ->
            preferences[Keys.STATUS] = filters.status
            preferences[Keys.ROCKET] = filters.rocket
            preferences[Keys.PROVIDER] = filters.provider
        }
    }
}
