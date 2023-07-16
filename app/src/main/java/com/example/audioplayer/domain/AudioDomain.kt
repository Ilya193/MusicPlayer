package com.example.audioplayer.domain

import com.example.audioplayer.core.ToMapper
import com.example.audioplayer.presentation.AudioUi

sealed class AudioDomain : ToMapper<AudioUi> {

    data class Base(
        private val id: Int,
        private val title: String,
        var isRun: Boolean = false
    ) : AudioDomain() {
        override fun map(): AudioUi = AudioUi.Base(id, title, isRun)
    }

    object Empty : AudioDomain() {
        override fun map(): AudioUi = AudioUi.Empty
    }
}

