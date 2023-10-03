package com.example.audioplayer.presentation

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.elveum.elementadapter.SimpleBindingAdapter
import com.elveum.elementadapter.adapter
import com.elveum.elementadapter.addBinding
import com.example.audioplayer.R
import com.example.audioplayer.core.s149
import com.example.audioplayer.databinding.AudioItemBinding
import com.example.audioplayer.databinding.AudioItemEmptyBinding
import com.example.audioplayer.databinding.BannedLayoutBinding
import com.example.audioplayer.databinding.FragmentListAudioBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class ListAudioFragment : BaseFragment(), Listeners {
    private var _binding: FragmentListAudioBinding? = null
    private val binding: FragmentListAudioBinding
        get() = _binding!!

    private val viewModel: MainViewModel by sharedViewModel()
    //private val musicsAdapter = MusicsAdapter(this)

    private val adapter: SimpleBindingAdapter<AudioUi> by lazy {
        adapter<AudioUi> {
            addBinding<AudioUi.Base, AudioItemBinding> {
                areItemsSame = { old, new -> old.same(new) }
                areContentsSame = { old, new -> old.sameContent(new) }

                bind { item ->
                    tvTitleAudio.text = item.title
                }

                listeners {
                    root.onClick { item ->
                        onClickListeners(index(), item.fullTitle)
                    }
                }
            }

            addBinding<AudioUi.Message, AudioItemEmptyBinding> {
                bind { item ->
                    tvMessage.text = context?.getString(item.msg)
                }
            }

            addBinding<AudioUi.Banned, BannedLayoutBinding> {
                listeners {
                    bind {
                        tvMessage.text = context?.getString(R.string.banned)
                    }

                    btnRetry.onClick {
                        setFragmentResult("permission", bundleOf())
                    }
                }
            }

            addBinding<AudioUi.FullBanned, BannedLayoutBinding> {
                bind {
                    tvMessage.text = context?.getString(R.string.full_banned)
                }

                listeners {
                    btnRetry.onClick {
                        setFragmentResult("permission", bundleOf())
                    }
                }
            }
        }
    }

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
                binding.tvTitleAudio.text = skip.s149()

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
        context?.registerReceiver(receiverUi, IntentFilter("ACTION"))
        context?.sendBroadcast(Intent("DATA"))
    }

    override fun onPause() {
        super.onPause()
        context?.unregisterReceiver(receiverUi)
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
                .replace(R.id.fragmentContainer, AudioFragment.newInstance())
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
        viewModel.uiState.observe(viewLifecycleOwner) {
            when (it) {
                is AudioUiState.Success -> adapter.submitList(it.data)
                is AudioUiState.Empty -> adapter.submitList(listOf(AudioUi.Message()))
                is AudioUiState.Waiting -> adapter.submitList(listOf(AudioUi.Message(R.string.wait)))
                is AudioUiState.Banned -> adapter.submitList(listOf(AudioUi.Banned))
                is AudioUiState.FullBanned -> adapter.submitList(listOf(AudioUi.FullBanned))
                else -> {}
            }
        }
    }

    private fun setupRecyclerView() {
        binding.musics.adapter = adapter
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

        binding.tvTitleAudio.text = title.s149()

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

