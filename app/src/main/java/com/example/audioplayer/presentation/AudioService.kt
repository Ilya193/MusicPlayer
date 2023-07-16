package com.example.audioplayer.presentation

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.example.audioplayer.R

class AudioService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private val mediaPlayer = MediaPlayer()
    private val musics = mutableListOf<Uri>()

    private var currentMusic = 0
    private var isRun = false

    override fun onCreate() {
        super.onCreate()
        getAllAudio()
    }

    private fun getAllAudio() {
        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.AudioColumns.DATA),
            MediaStore.Audio.Media.IS_MUSIC,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val data = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val title = cursor.getString(data)
                if (title.takeLast(3) == "mp3" || title.takeLast(3) == "mp4")
                    musics.add(Uri.parse(cursor.getString(data)))
            }
        }
    }

    private fun settingMediaPlayer(data: String = "") {
        mediaPlayer.stop()
        mediaPlayer.reset()
        if (data.isNotEmpty()) currentMusic = musics.indexOf(Uri.parse(data))
        mediaPlayer.setDataSource(this, musics[currentMusic])
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("ACTION") ?: ""

        when (action) {
            "stop" -> {
                stopSelf()
            }

            "skipPrevious" -> {
                if (currentMusic == 0) currentMusic = musics.size - 1
                else currentMusic--
                settingMediaPlayer()
            }

            "skipNext" -> {
                if (currentMusic == musics.size - 1) currentMusic = 0
                else currentMusic++
                settingMediaPlayer()
            }

            else -> {
                if (isRun) {
                    settingMediaPlayer(intent?.getStringExtra("TITLE") ?: "")
                }
                else {
                    mediaPlayer.setOnCompletionListener {
                        it.stop()
                        it.reset()
                        it.setDataSource(this, musics[++currentMusic])
                        it.prepare()
                        it.start()
                    }
                    settingMediaPlayer(intent?.getStringExtra("TITLE") ?: "")

                    val notification = createNotification().build()
                    startForeground(1, notification)
                }
                isRun = true
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotification(): NotificationCompat.Builder {
        val skipPrevious = Intent(this, AudioService::class.java).apply {
            putExtra("ACTION", "skipPrevious")
        }

        val stop = Intent(this, AudioService::class.java).apply {
            putExtra("ACTION", "stop")
        }

        val skipNext = Intent(this, AudioService::class.java).apply {
            putExtra("ACTION", "skipNext")
        }

        val skipPreviousPendingIntent =
            PendingIntent.getService(this, 1, skipPrevious, PendingIntent.FLAG_UPDATE_CURRENT)
        val stopPendingIntent =
            PendingIntent.getService(this, 2, stop, PendingIntent.FLAG_UPDATE_CURRENT)
        val skipNextPendingIntent =
            PendingIntent.getService(this, 3, skipNext, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Title")
            .setContentText("Text")
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                R.drawable.ic_skip_previous,
                getString(R.string.skip_previous),
                skipPreviousPendingIntent
            )
            .addAction(R.drawable.ic_stop, getString(R.string.stop), stopPendingIntent)
            .addAction(
                R.drawable.ic_skip_next,
                getString(R.string.skip_next),
                skipNextPendingIntent
            )
    }

    override fun onDestroy() {
        super.onDestroy()
        isRun = false
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    companion object {
        const val CHANNEL_ID = "1"
        const val CHANNEL_NAME = "CHANNEL_NAME"
    }
}