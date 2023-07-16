package com.example.audioplayer.domain

import com.example.audioplayer.core.ToMapper
import com.example.audioplayer.presentation.AudioUi

sealed class AudioDomain : ToMapper<AudioUi> {

    data class Base(
        private val id: Int,
        private val title: String,
        private val fullTitle: String = ""
    ) : AudioDomain() {
        override fun map(): AudioUi = AudioUi.Base(id, title, fullTitle, false)
    }

    object Empty : AudioDomain() {
        override fun map(): AudioUi = AudioUi.Empty
    }
}

