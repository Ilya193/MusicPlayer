package com.example.audioplayer.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AudioRepository {
    suspend fun getAllAudio(): Flow<List<AudioData>>

    class Base(
        private val localDataSource: LocalDataSource,
    ) : AudioRepository {
        override suspend fun getAllAudio(): Flow<List<AudioData>> = flow {
            localDataSource.getAllAudio().collect {
                emit(it.map { audio ->
                    audio.map()
                })
            }
        }
    }
}
