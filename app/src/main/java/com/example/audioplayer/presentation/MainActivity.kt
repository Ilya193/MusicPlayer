package com.example.audioplayer.presentation

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.audioplayer.R
import com.example.audioplayer.core.log
import com.example.audioplayer.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), Listeners {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: MainViewModel by viewModel()
    private val musicsAdapter = MusicsAdapter(this)

    private val permissionReadExternalStorage =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (!result) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.need_permission),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                viewModel.observe(this) {
                    musicsAdapter.submitList(it)
                }
                viewModel.getAllAudio()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupRecyclerView()
        checkPermissions()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionReadExternalStorage.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            viewModel.observe(this) {
                musicsAdapter.submitList(it)
            }
            viewModel.getAllAudio()
        }
    }

    private fun setupRecyclerView() {
        binding.musics.adapter = musicsAdapter
        binding.musics.setHasFixedSize(true)
        binding.musics.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
    }

    override fun onClickListeners(position: Int, title: String) {
        //viewModel.set(position)
        val intent = Intent(this, AudioService::class.java).apply {
            putExtra("TITLE", title)
        }
        ContextCompat.startForegroundService(this, intent)
    }
}