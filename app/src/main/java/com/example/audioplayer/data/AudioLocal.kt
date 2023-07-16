package com.example.audioplayer.data

import com.example.audioplayer.core.ToMapper

data class AudioLocal(
    private val id: Int,
    private val title: String,
    var isRun: Boolean = false
) : ToMapper<AudioData> {
    override fun map(): AudioData = AudioData(id, title, isRun)
}
