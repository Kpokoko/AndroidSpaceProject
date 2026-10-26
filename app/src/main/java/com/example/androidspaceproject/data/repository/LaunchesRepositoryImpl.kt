package com.example.androidspaceproject.data.repository

import android.util.Log
import com.example.androidspaceproject.data.api.LaunchLibraryApi
import com.example.androidspaceproject.data.mapper.toDomain
import com.example.androidspaceproject.data.model.Launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LaunchesRepositoryImpl(
    private val api: LaunchLibraryApi
) : LaunchesRepository {

    override fun getLaunches(): Flow<List<Launch>> = flow {
        Log.d("LaunchAPI", "Repository: начинаем api.getLaunches()")

        val response = api.getLaunches(
            limit = 20
        )

        Log.d(
            "LaunchAPI",
            "Repository: ответ получен, count=${response.count}"
        )

        emit(
            response.results.map { it.toDomain() }
        )
    }.flowOn(Dispatchers.IO)

    override fun getLaunchById(id: String): Flow<Launch?> = flow {
        val launch = api.getLaunchById(id).toDomain()
        emit(launch)
    }.flowOn(Dispatchers.IO)

    override fun searchLaunches(query: String): Flow<List<Launch>> = flow {
        val response = api.getLaunches(
            limit = 20,
            search = query.takeIf { it.isNotBlank() }
        )

        emit(
            response.results.map { it.toDomain() }
        )
    }.flowOn(Dispatchers.IO)
}