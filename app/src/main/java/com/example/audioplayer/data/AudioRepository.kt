package com.example.audioplayer.data

import com.example.audioplayer.core.log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AudioRepository {
    suspend fun getAllAudio(): Flow<List<AudioData>>
    suspend fun set(position: Int): Flow<List<AudioData>>

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

        override suspend fun set(position: Int): Flow<List<AudioData>> = flow {
            localDataSource.set(position).collect {
                emit(it.map { audio ->
                    audio.map()
                })
            }
        }
    }
}
