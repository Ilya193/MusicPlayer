package com.example.audioplayer.data

import com.example.audioplayer.core.ToMapper
import com.example.audioplayer.domain.AudioDomain

data class AudioData(
    private val id: Int,
    val title: String,
) : ToMapper<AudioDomain> {
    override fun map(): AudioDomain = AudioDomain.Base(id, title)
}
