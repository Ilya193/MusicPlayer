package com.example.audioplayer.domain

import com.example.audioplayer.data.AudioData
import com.example.audioplayer.data.AudioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AudioInteractor {
    suspend fun getAllAudio(): Flow<List<AudioDomain>>
    suspend fun set(position: Int): Flow<List<AudioDomain>>

    class Base(
        private val audioRepository: AudioRepository,
    ) : AudioInteractor {
        override suspend fun getAllAudio(): Flow<List<AudioDomain>> = flow {
            audioRepository.getAllAudio().collect {
                emit(filterAndMapData(it))
            }
        }

        override suspend fun set(position: Int): Flow<List<AudioDomain>> = flow {
            audioRepository.set(position).collect {
                emit(filterAndMapData(it))
            }
        }

        private fun filterAndMapData(list: List<AudioData>): List<AudioDomain> {
            val musicsDomain = mutableListOf<AudioDomain>()

            list.forEach { audio ->
                var index = 0
                val title = audio.title
                var name = ""
                if (title.takeLast(3) == "mp3" || title.takeLast(3) == "mp4") {
                    name =
                        title.substring(title.lastIndexOf("/") + 1, title.lastIndexOf("."))
                            .replace("_", " ")
                }
                if (name.isNotEmpty())
                    musicsDomain.add(AudioDomain.Base(index++, name, audio.isRun))
            }

            if (musicsDomain.isEmpty()) musicsDomain.add(AudioDomain.Empty)
            return musicsDomain
        }
    }
}