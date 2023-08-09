package com.example.audioplayer.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

abstract class ActionBroadcastReceiver : BroadcastReceiver() {
    protected var pause = -1
    protected var skip = ""
    protected var stop = ""

    override fun onReceive(context: Context?, intent: Intent?) {
        pause = intent?.getIntExtra("pause", -1) ?: -1
        skip = intent?.getStringExtra("skip") ?: ""
        stop = intent?.getStringExtra("stop") ?: ""
    }
}