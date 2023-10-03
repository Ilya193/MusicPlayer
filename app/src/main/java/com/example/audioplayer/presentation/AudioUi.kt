package com.example.audioplayer.presentation

import com.example.audioplayer.R
import com.example.audioplayer.core.Comparing

sealed class AudioUi(
    open val title: String = "",
    open val fullTitle: String = ""
) : Comparing<AudioUi> {
    override fun same(item: AudioUi): Boolean = false
    override fun sameContent(item: AudioUi): Boolean = false

    data class Base(
        private val id: Int,
        override val title: String,
        override val fullTitle: String,
        private val isRun: Boolean,
    ) : AudioUi(title, fullTitle) {
        override fun same(item: AudioUi): Boolean {
            return item is Base && id == item.id
        }

        override fun sameContent(item: AudioUi): Boolean = this == item

        /* override fun changePayload(item: AudioUi): Any {
             if (item is Base && isRun != item.isRun)
                 return true
             return false
         }*/
    }

    data class Message(val msg: Int = R.string.music_not_found) : AudioUi()
    object Banned : AudioUi()
    object FullBanned : AudioUi()

}

sealed class AudioUiState {

    data class Success(val data: List<AudioUi>) : AudioUiState()

    object Waiting : AudioUiState()

    object Banned : AudioUiState()

    object FullBanned : AudioUiState()

    object Allowed : AudioUiState()

    object Empty : AudioUiState()
}