package com.example.audioplayer.core

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

interface Communication<T> {
    fun map(data: T)
    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<T>)

    class Base<T : Any> : Communication<T> {
        private val liveData = MutableLiveData<T>()

        override fun map(data: T) {
            liveData.value = data
        }

        override fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
            liveData.observe(lifecycleOwner, observer)
        }

    }
}