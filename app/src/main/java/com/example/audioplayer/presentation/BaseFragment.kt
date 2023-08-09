package com.example.audioplayer.presentation

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    protected fun skipPrevious() {
        val skipPrevious = Intent(requireContext(), AudioService::class.java).apply {
            putExtra(
                "ACTION",
                "skipPrevious"
            )
        }
        ContextCompat.startForegroundService(requireContext(), skipPrevious)
    }

    protected fun pause() {
        val pause = Intent(requireContext(), AudioService::class.java).apply {
            putExtra(
                "ACTION",
                "pause"
            )
        }
        ContextCompat.startForegroundService(requireContext(), pause)
    }

    protected fun skipNext() {
        val skipNext = Intent(requireContext(), AudioService::class.java).apply {
            putExtra(
                "ACTION",
                "skipNext"
            )
        }
        ContextCompat.startForegroundService(requireContext(), skipNext)
    }
}