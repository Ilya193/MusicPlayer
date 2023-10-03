package com.example.audioplayer.di

import com.example.audioplayer.core.Communication
import com.example.audioplayer.data.AudioRepository
import com.example.audioplayer.data.LocalDataSource
import com.example.audioplayer.domain.AudioInteractor
import com.example.audioplayer.presentation.AudioUi
import com.example.audioplayer.presentation.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    factory<LocalDataSource> {
        LocalDataSource.Base(androidApplication())
    }

    factory<AudioRepository> {
        AudioRepository.Base(get())
    }

    factory<AudioInteractor> {
        AudioInteractor.Base(get())
    }

    viewModel<MainViewModel> {
        MainViewModel(get())
    }

    factory<Communication<List<AudioUi>>> {
        Communication.Base()
    }
}