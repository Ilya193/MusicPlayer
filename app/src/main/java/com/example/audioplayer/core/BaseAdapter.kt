package com.example.audioplayer.core

import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.audioplayer.databinding.AudioItemEmptyBinding
import com.example.audioplayer.presentation.AudioUi

abstract class BaseAdapter<T : Comparing<T>, E : BaseViewHolder<T>> : ListAdapter<T, E>(
    DiffUtilCallback()
) {
    override fun onBindViewHolder(holder: E, position: Int) {
        holder.bind(getItem(position))
    }
}

abstract class BaseViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: T)
    open fun bindRun() {}

    class EmptyViewHolder(view: AudioItemEmptyBinding) : BaseViewHolder<AudioUi>(view.root) {
        override fun bind(item: AudioUi) {}
    }
}