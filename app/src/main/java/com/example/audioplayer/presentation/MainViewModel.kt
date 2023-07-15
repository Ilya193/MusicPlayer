package com.example.audioplayer.presentation

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.Communication
import com.example.audioplayer.domain.AudioInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val audioInteractor: AudioInteractor,
    private val communication: Communication<List<AudioUi>>,
) : ViewModel() {

    //private var list = mutableListOf<AudioUi>()

    fun getAllAudio() = viewModelScope.launch(Dispatchers.IO) {
        audioInteractor.getAllAudio().collect {
            val uiList = it.map { audio ->
                audio.map()
            }
            /*list = uiList.map {
                if (it is AudioUi.Base) it.copy() else AudioUi.Empty
            }.toMutableList()
            list = uiList.toMutableList()*/
            withContext(Dispatchers.Main) {
                communication.map(uiList)
            }
        }
    }

    fun set(position: Int) {
        /*(list[position] as AudioUi.Base).isRun = !(list[position] as AudioUi.Base).isRun
        communication.map(list.map {
            if (it is AudioUi.Base) it.copy() else AudioUi.Empty
        })*/
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<List<AudioUi>>) {
        communication.observe(lifecycleOwner, observer)
    }
}