package com.example.audioplayer.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.audioplayer.R
import com.example.audioplayer.core.log
import com.example.audioplayer.core.s149
import com.example.audioplayer.databinding.FragmentListAudioBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


class ListAudioFragment : BaseFragment(), Listeners {
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

    private val receiverUi = object : ActionBroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            super.onReceive(context, intent)
            val visible = intent?.getStringExtra("visible") ?: ""

            if (pause != -1)
                binding.icPause.setImageResource(pause)

            if (skip.isNotEmpty())
                binding.titleAudio.text = skip.s149()

            if (stop.isNotEmpty()) {
                binding.container.animate().translationY(100f)
                binding.container.visibility = View.INVISIBLE
            }

            if (visible.isNotEmpty())
                binding.root.visibility = View.VISIBLE
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
            binding.root.visibility = View.GONE
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
                )
                .add(R.id.fragmentContainer, AudioFragment.newInstance())
                .addToBackStack(null)
                .commit()
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
        /*val uri = Uri.parse(title)
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(requireContext(), uri)
        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        log((durationStr!!.toLong()) / 1000)*/

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

