package com.example.audioplayer.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.domain.AudioInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val audioInteractor: AudioInteractor,
) : ViewModel() {

    //private var list = mutableListOf<AudioUi>()

    private val _uiState = MutableLiveData<AudioUiState>()
    val uiState: LiveData<AudioUiState> get() = _uiState

    fun getAllAudio() = viewModelScope.launch(Dispatchers.IO) {
        audioInteractor.getAllAudio().collect {
            val uiList = it.map { audio ->
                audio.map()
            }

            /*list = uiList.map {
                if (it is AudioUi.Base) it.copy() else AudioUi.Empty
            }.toMutableList()
            list = uiList.toMutableList()*/
            _uiState.postValue(AudioUiState.Success(uiList))
        }
    }

    fun set(position: Int) {
        /*(list[position] as AudioUi.Base).isRun = !(list[position] as AudioUi.Base).isRun
        communication.map(list.map {
            if (it is AudioUi.Base) it.copy() else AudioUi.Empty
        })*/
    }

    fun setState(state: AudioUiState) {
        when (state) {
            is AudioUiState.Allowed -> getAllAudio()
            is AudioUiState.Banned -> _uiState.value = state
            is AudioUiState.FullBanned -> _uiState.value = state
            is AudioUiState.Waiting -> _uiState.value = state
            else -> {}
        }
    }
}