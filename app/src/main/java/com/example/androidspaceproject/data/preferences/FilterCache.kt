package com.example.androidspaceproject.data.preferences

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FilterCache {

    private val _filters = MutableStateFlow(LaunchFilters())

    val filters: StateFlow<LaunchFilters> =
        _filters.asStateFlow()

    fun update(filters: LaunchFilters) {
        _filters.value = filters
    }

    fun activeFiltersCount(): Int {
        val filters = _filters.value

        return listOf(
            filters.status,
            filters.rocket,
            filters.provider
        ).count { it.isNotBlank() }
    }
}