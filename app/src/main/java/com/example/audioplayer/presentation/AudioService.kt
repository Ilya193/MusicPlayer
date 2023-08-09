package com.example.audioplayer.presentation

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.audioplayer.R
import com.example.audioplayer.core.log
import com.example.audioplayer.core.s149
import java.util.Timer
import java.util.TimerTask

class AudioService : Service() {

    override fun onBind(p0: Intent?): IBinder? = null

    private val mediaPlayer = MediaPlayer()
    private val musics = mutableListOf<Uri>()

    private var currentMusic = 0
    private var isRun = false
    private var isPause = false
    private var currentMusicName = ""

    private lateinit var notificationManager: NotificationManager
    private lateinit var notification: NotificationCompat.Builder
    private lateinit var mediaSession: MediaSessionCompat

    private val receiverRedirect = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            sendBroadcast(Intent("ACTION").apply {
                putExtra("pause", if (isPause) R.drawable.ic_start else R.drawable.ic_pause)
                putExtra("skip", currentMusicName)
            })
        }
    }

    private var timer = Timer()

    override fun onCreate() {
        super.onCreate()
        getAllAudio()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        registerReceiver(receiverRedirect, IntentFilter("DATA"))
        registerReceiver(receiverUpdate, IntentFilter("UPDATE"))
    }

    private val receiverUpdate = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val update = intent?.getBooleanExtra("update", false) ?: false
            if (!update) timer.cancel()
            else settingTimer()
        }
    }

    private fun settingTimer() {
        timer = Timer()
        timer.scheduleAtFixedRate(object  : TimerTask() {
            override fun run() {
                log("RUN")
                sendBroadcast(Intent("DATA").apply {
                    putExtra("currentPosition", mediaPlayer.currentPosition)
                })
            }
        }, 0, 1000)
    }

    private fun getAllAudio() {
        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.AudioColumns.DATA,
            ),
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

    private fun settingMediaPlayer(data: String = "", isPause: Boolean = false) {
        mediaPlayer.stop()
        mediaPlayer.reset()
        if (data.isNotEmpty()) currentMusic = musics.indexOf(Uri.parse(data))
        mediaPlayer.setDataSource(this, musics[currentMusic])
        mediaPlayer.prepare()
        if (!isPause)
            mediaPlayer.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("ACTION") ?: ""

        when (action) {
            "data" -> {
                sendBroadcast(Intent("DATA").apply {
                    putExtra("duration", mediaPlayer.duration)
                    putExtra("name", currentMusicName)
                    putExtra("currentPosition", mediaPlayer.currentPosition)
                })
            }
            "pause" -> {
                val pause = Intent(this, AudioService::class.java).apply {
                    putExtra("ACTION", "pause")
                }
                val pausePendingIntent =
                    PendingIntent.getService(
                        this,
                        2,
                        pause,
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_IMMUTABLE
                    )

                val notificationBuild = notification.build()

                var state = PlaybackStateCompat.STATE_PLAYING

                if (isRun) {
                    timer.cancel()
                    mediaPlayer.pause()
                    notificationBuild.actions[1] =
                        Notification.Action(R.drawable.ic_start, "pause", pausePendingIntent)
                    state = PlaybackStateCompat.STATE_PAUSED
                    sendBroadcast(Intent("ACTION").apply {
                        putExtra("pause", R.drawable.ic_start)
                    })
                } else {
                    sendBroadcast(Intent("ACTION").apply {
                        putExtra("pause", R.drawable.ic_pause)
                    })
                    settingTimer()
                    mediaPlayer.start()

                    notificationBuild.actions[1] =
                        Notification.Action(R.drawable.ic_pause, "pause", pausePendingIntent)
                }

                setPlaybackState(
                    mediaPlayer.currentPosition.toLong(),
                    state
                )

                isPause = !isPause
                isRun = !isRun
                notificationManager.notify(1, notificationBuild)
            }

            "skipPrevious" -> {
                setPlaybackState()
                if (currentMusic == 0) currentMusic = musics.size - 1
                else currentMusic--
                currentMusicName = musics[currentMusic].toString()
                updateNotification(currentMusicName, isPause)
                settingMediaPlayer(isPause = isPause)
                setMetadata()
                sendBroadcast(Intent("DATA").apply {
                    putExtra("duration", mediaPlayer.duration)
                    putExtra("name", currentMusicName)
                    putExtra("currentPosition", mediaPlayer.currentPosition)
                })
            }

            "skipNext" -> {
                setPlaybackState()
                if (currentMusic == musics.size - 1) currentMusic = 0
                else currentMusic++
                currentMusicName = musics[currentMusic].toString()
                updateNotification(currentMusicName, isPause)
                settingMediaPlayer(isPause = isPause)
                setMetadata()
                sendBroadcast(Intent("DATA").apply {
                    putExtra("duration", mediaPlayer.duration)
                    putExtra("name", currentMusicName)
                    putExtra("currentPosition", mediaPlayer.currentPosition)
                })
            }

            "stop" -> {
                sendBroadcast(Intent("ACTION").apply {
                    putExtra("stop", "stop")
                })
                stopSelf()
            }

            else -> {
                currentMusicName = intent?.getStringExtra("TITLE") ?: ""
                if (isRun) {
                    setPlaybackState()
                    settingMediaPlayer(currentMusicName)
                    updateNotification(currentMusicName, isPause)
                    setMetadata()
                } else {
                    if (isPause) {
                        setPlaybackState(state = PlaybackStateCompat.STATE_PAUSED)
                        settingMediaPlayer(currentMusicName, true)
                        updateNotification(currentMusicName, true)
                    }
                    else {
                        mediaPlayer.setOnCompletionListener {
                            val skipNext = Intent(this, AudioService::class.java).apply {
                                putExtra("ACTION", "skipNext")
                            }
                            ContextCompat.startForegroundService(this, skipNext)
                        }
                        settingMediaPlayer(currentMusicName)
                        notification = createNotification()
                        setContentTitleOnNotification(currentMusicName)
                        startForeground(1, notification.build())
                        isRun = true
                    }
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun setPlaybackState(
        seekTo: Long? = null,
        state: Int = PlaybackStateCompat.STATE_PLAYING,
    ) {
        var position = 0L
        if (seekTo != null) position = seekTo
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                state,
                position,
                1f
            )
                .setActions(
                    PlaybackStateCompat.ACTION_SEEK_TO
                            or PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PAUSE
                            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                            or PlaybackStateCompat.ACTION_STOP
                )
                .build()
        )
    }

    private fun setMetadata() {
        val metadata = MediaMetadataCompat.Builder()
            .putLong(MediaMetadata.METADATA_KEY_DURATION, mediaPlayer.duration.toLong())
            .build()
        mediaSession.setMetadata(metadata)
    }

    private fun updateNotification(data: String, saveState: Boolean = false) {
        setContentTitleOnNotification(data)
        if (saveState) {
            val pause = Intent(this, AudioService::class.java).apply {
                putExtra("ACTION", "pause")
            }
            val pausePendingIntent =
                PendingIntent.getService(
                    this,
                    2,
                    pause,
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_IMMUTABLE
                )

            val notificationBuild = notification.build()
            notificationBuild.actions[1] =
                Notification.Action(R.drawable.ic_start, "pause", pausePendingIntent)
            notificationManager.notify(1, notificationBuild)
        }
        else {
            notificationManager.notify(1, notification.build())
        }
    }

    private fun setContentTitleOnNotification(data: String) {
        notification.setContentTitle(
            data.s149()
        )
    }

    private fun createNotification(): NotificationCompat.Builder {
        val skipPrevious = Intent(this, AudioService::class.java).apply {
            putExtra("ACTION", "skipPrevious")
        }

        val pause = Intent(this, AudioService::class.java).apply {
            putExtra("ACTION", "pause")
        }

        val skipNext = Intent(this, AudioService::class.java).apply {
            putExtra("ACTION", "skipNext")
        }

        val stop = Intent(this, AudioService::class.java).apply {
            putExtra("ACTION", "stop")
        }

        val skipPreviousPendingIntent =
            PendingIntent.getService(
                this,
                1,
                skipPrevious,
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_IMMUTABLE
            )

        val pausePendingIntent =
            PendingIntent.getService(
                this,
                2,
                pause,
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_IMMUTABLE
            )

        val skipNextPendingIntent =
            PendingIntent.getService(
                this,
                3,
                skipNext,
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_IMMUTABLE
            )

        val stopPendingIntent =
            PendingIntent.getService(
                this,
                4,
                stop,
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_IMMUTABLE
            )

        mediaSession = MediaSessionCompat(this, "TAG")
        setMetadata()
        setPlaybackState()

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onSeekTo(pos: Long) {
                mediaPlayer.seekTo(pos.toInt())
                setPlaybackState(pos)
            }

            override fun onSkipToNext() {
                ContextCompat.startForegroundService(this@AudioService, skipNext)
            }

            override fun onSkipToPrevious() {
                ContextCompat.startForegroundService(this@AudioService, skipPrevious)
            }

            override fun onPause() {
                isPause = !isPause
                isRun = !isRun
                mediaPlayer.pause()
                setPlaybackState(
                    mediaPlayer.currentPosition.toLong(),
                    PlaybackStateCompat.STATE_PAUSED
                )
                sendBroadcast(Intent("ACTION").apply {
                    putExtra("pause", R.drawable.ic_start)
                })
            }

            override fun onPlay() {
                isPause = !isPause
                isRun = !isRun
                mediaPlayer.start()
                setPlaybackState(mediaPlayer.currentPosition.toLong())
                sendBroadcast(Intent("ACTION").apply {
                    putExtra("pause", R.drawable.ic_pause)
                })
            }

            override fun onStop() {
                log("ON STOP")
                ContextCompat.startForegroundService(this@AudioService, stop)
            }
        })

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2).setMediaSession(mediaSession.sessionToken)
            )
            .addAction(
                R.drawable.ic_skip_previous,
                getString(R.string.skip_previous),
                skipPreviousPendingIntent
            )
            .addAction(R.drawable.ic_pause, getString(R.string.pause), pausePendingIntent)
            .addAction(
                R.drawable.ic_skip_next,
                getString(R.string.skip_next),
                skipNextPendingIntent
            )
            .addAction(
                R.drawable.ic_close,
                getString(R.string.close),
                stopPendingIntent
            )
    }

    override fun onDestroy() {
        isRun = false
        mediaPlayer.stop()
        mediaPlayer.release()
        unregisterReceiver(receiverRedirect)
        unregisterReceiver(receiverUpdate)
        timer.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "1"
        const val CHANNEL_NAME = "CHANNEL_NAME"
    }
}