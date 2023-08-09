package com.example.audioplayer.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.audioplayer.R
import com.example.audioplayer.core.log
import com.example.audioplayer.core.s149
import com.example.audioplayer.databinding.FragmentAudioBinding

class AudioFragment : BaseFragment() {
    private var _binding: FragmentAudioBinding? = null
    private val binding: FragmentAudioBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textView.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.icSkipPrevious.setOnClickListener {
            skipPrevious()
        }

        binding.icPause.setOnClickListener {
            pause()
        }

        binding.icSkipNext.setOnClickListener {
            skipNext()
        }

        binding.seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
                log("TEST")
            }
        })
    }

    private val receiverData = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val duration = intent?.getIntExtra("duration", -1) ?: -1
            val name = intent?.getStringExtra("name") ?: ""
            val currentPosition = intent?.getIntExtra("currentPosition", -1) ?: -1

            if (duration != -1) {
                binding.seekbar.max = duration / 1000
                val min = duration / 60000
                val sec = duration / 1000
                val secPrint = sec - min * 60
                if (secPrint < 10) binding.duration.text = "${duration / 60000}:0$secPrint"
                else binding.duration.text = "${duration / 60000}:$secPrint"
            }
            if (currentPosition != -1) {
                binding.seekbar.progress = currentPosition / 1000
                if ((currentPosition / 1000) < 10) binding.currentPosition.text = "${currentPosition / 60000}:0${currentPosition / 1000}"
                else binding.currentPosition.text = "${currentPosition / 60000}:${currentPosition / 1000}"
            }
            if (name.isNotEmpty()) binding.textView.text = name.s149()

            /*log("duration: ${duration / 60000}:${duration / 1000}")
            log("name $name")
            log("currentPosition ${currentPosition / 1000}")*/
        }
    }

    private val receiverUi = object : ActionBroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            super.onReceive(context, intent)

            if (pause != -1)
                binding.icPause.setImageResource(pause)

            if (skip.isNotEmpty())
                binding.textView.text = skip.s149()

            if (stop.isNotEmpty()) {
                parentFragmentManager.popBackStack()
                requireContext().sendBroadcast(Intent("ACTION").putExtra("visible", "visible"))
                requireContext().sendBroadcast(Intent("UPDATE").apply {
                    putExtra("update", false)
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()

        requireContext().registerReceiver(receiverData, IntentFilter("DATA"))
        requireContext().registerReceiver(receiverUi, IntentFilter("ACTION"))

        requireContext().sendBroadcast(Intent("UPDATE").apply {
            putExtra("update", true)
        })

        val intent = Intent(requireContext(), AudioService::class.java).apply {
            putExtra("ACTION", "data")
        }

        ContextCompat.startForegroundService(requireContext(), intent)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(receiverData)
        requireContext().unregisterReceiver(receiverUi)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() =
            AudioFragment()
    }
}