package com.example.audioplayer.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.audioplayer.core.log
import com.example.audioplayer.core.s149
import com.example.audioplayer.databinding.FragmentListAudioBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


class ListAudioFragment : Fragment(), Listeners {
    private var _binding: FragmentListAudioBinding? = null
    private val binding: FragmentListAudioBinding
        get() = _binding!!

    private val viewModel: MainViewModel by viewModel()
    private val musicsAdapter = MusicsAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentListAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        settingViewModel()
        settingClickListener()
    }

    private val receiverUi = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val pause = intent?.getIntExtra("pause", -1) ?: -1
            val skip = intent?.getStringExtra("skip") ?: ""
            val stop = intent?.getStringExtra("stop") ?: ""

            if (pause != -1)
                binding.icPause.setImageResource(pause)

            if (skip.isNotEmpty())
                binding.titleAudio.text = skip.s149()

            if (stop.isNotEmpty()) {
                binding.container.animate().translationY(100f)
                binding.container.visibility = View.INVISIBLE
            }

        }
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(receiverUi, IntentFilter("ACTION"))
        requireContext().sendBroadcast(Intent("DATA"))
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(receiverUi)
    }

    private fun settingClickListener() {
        binding.container.setOnClickListener {
            TODO("AUDIO FRAGMENT")
        }

        binding.icSkipPrevious.setOnClickListener {
            val skipPrevious = Intent(requireContext(), AudioService::class.java).apply {
                putExtra(
                    "ACTION",
                    "skipPrevious"
                )
            }
            ContextCompat.startForegroundService(requireContext(), skipPrevious)
        }

        binding.icPause.setOnClickListener {
            val pause = Intent(requireContext(), AudioService::class.java).apply {
                putExtra(
                    "ACTION",
                    "pause"
                )
            }
            ContextCompat.startForegroundService(requireContext(), pause)
        }

        binding.icSkipNext.setOnClickListener {
            val skipNext = Intent(requireContext(), AudioService::class.java).apply {
                putExtra(
                    "ACTION",
                    "skipNext"
                )
            }
            ContextCompat.startForegroundService(requireContext(), skipNext)
        }
    }

    private fun settingViewModel() {
        viewModel.observe(viewLifecycleOwner) {
            musicsAdapter.submitList(it)
        }
        viewModel.getAllAudio()
    }

    private fun setupRecyclerView() {
        binding.musics.adapter = musicsAdapter
        binding.musics.setHasFixedSize(true)
        binding.musics.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                RecyclerView.VERTICAL
            )
        )
    }

    override fun onClickListeners(position: Int, title: String) {
        if (binding.container.visibility != View.VISIBLE) {
            binding.container.visibility = View.VISIBLE
            binding.container.animate().translationY(0f)
        }

        binding.titleAudio.text = title.s149()

        val intent = Intent(requireContext(), AudioService::class.java).apply {
            putExtra("TITLE", title)
        }

        ContextCompat.startForegroundService(requireContext(), intent)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() =
            ListAudioFragment()
    }
}

