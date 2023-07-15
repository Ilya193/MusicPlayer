package com.example.audioplayer.data

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface LocalDataSource {
    suspend fun getAllAudio(): Flow<List<AudioLocal>>

    class Base(
        private val context: Context,
    ) : LocalDataSource {
        override suspend fun getAllAudio(): Flow<List<AudioLocal>> = flow {
            val musics = mutableListOf<AudioLocal>()
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.AudioColumns.DATA),
                MediaStore.Audio.Media.IS_MUSIC,
                null,
                null
            )?.use { cursor ->
                var i = 0
                while (cursor.moveToNext()) {
                    val data = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    val title = cursor.getString(data)
                    musics.add(AudioLocal(i++, title))
                }
                emit(musics)
            }
        }
    }
}
