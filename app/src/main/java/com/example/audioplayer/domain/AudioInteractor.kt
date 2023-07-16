package com.example.audioplayer.domain

import com.example.audioplayer.data.AudioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AudioInteractor {
    suspend fun getAllAudio(): Flow<List<AudioDomain>>

    class Base(
        private val audioRepository: AudioRepository,
    ) : AudioInteractor {
        override suspend fun getAllAudio(): Flow<List<AudioDomain>> = flow {
            audioRepository.getAllAudio().collect {
                val musicsDomain = mutableListOf<AudioDomain>()

                it.forEach { audio ->
                    var index = 0
                    val title = audio.title
                    var filterTitle = ""
                    if (title.takeLast(3) == "mp3" || title.takeLast(3) == "mp4") {
                        filterTitle =
                            title.substring(title.lastIndexOf("/") + 1, title.lastIndexOf("."))
                                .replace("_", " ")
                    }
                    if (filterTitle.isNotEmpty())
                        musicsDomain.add(AudioDomain.Base(index++, filterTitle, title))
                }

                if (musicsDomain.isEmpty()) musicsDomain.add(AudioDomain.Empty)
                emit(musicsDomain)
            }
        }
    }
}