package com.example.audioplayer.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.audioplayer.core.BaseAdapter
import com.example.audioplayer.core.BaseViewHolder
import com.example.audioplayer.databinding.AudioItemBinding
import com.example.audioplayer.databinding.AudioItemEmptyBinding

interface Listeners {
    fun onClickListeners(position: Int, title: String)
}

class MusicsAdapter(
    private val listeners: Listeners,
) : BaseAdapter<AudioUi, BaseViewHolder<AudioUi>>() {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AudioUi.Base -> AUDIO_VIEW_TYPE
            else -> EMPTY_VIEW_TYPE
        }
    }

    abstract class BaseMusicViewHolder(view: View) : BaseViewHolder<AudioUi>(view) {
        //private val imageView: ImageView = view.findViewById(R.id.isRun)
        //private var run = false

        /*override fun bindRun() {
            if (run) {
                imageView.setImageResource(R.drawable.ic_start)
                run = false
            }
            else if (!run) {
                imageView.setImageResource(R.drawable.ic_pause)
                run = true
            }
        }*/
    }

    inner class MusicViewHolder(private val view: AudioItemBinding) :
        BaseMusicViewHolder(view.root) {
        override fun bind(item: AudioUi) {
            view.nameAudio.text = if (item is AudioUi.Base) item.title else ""
            view.root.setOnClickListener {
                listeners.onClickListeners(adapterPosition, (getItem(adapterPosition) as AudioUi.Base).fullTitle)
            }
            /*if (item is AudioUi.Base) {
                if (!item.isRun) view.isRun.setImageResource(R.drawable.ic_start)
                else view.isRun.setImageResource(R.drawable.ic_pause)
            }
            view.isRun.setOnClickListener {
                listeners.onClickListeners(adapterPosition)
            }*/
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<AudioUi> {
        return when (viewType) {
            AUDIO_VIEW_TYPE -> MusicViewHolder(
                AudioItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> BaseViewHolder.EmptyViewHolder(
                AudioItemEmptyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    /*override fun onBindViewHolder(
        holder: BaseViewHolder<AudioUi>,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            holder.bindRun()
        }
    }*/

    private companion object {
        const val AUDIO_VIEW_TYPE = 1
        const val EMPTY_VIEW_TYPE = 2
    }
}