package com.example.audioplayer.presentation

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.audioplayer.R
import com.example.audioplayer.core.log
import com.example.audioplayer.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val permissionReadExternalStorage =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            permissionCheck(result, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    private val permissionReadMediaAudio =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionCheck(result, android.Manifest.permission.READ_MEDIA_AUDIO)
            }
        }

    private val permissionCheck: (Boolean, String) -> Unit = { result, permission ->
        if (!result) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.need_permission),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            launchFragment(ListAudioFragment.newInstance())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        checkPermissions()

        onBackPressedDispatcher.addCallback(this) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

            if (fragment is AudioFragment) {
                supportFragmentManager.popBackStack()
                sendBroadcast(Intent("ACTION").putExtra("visible", "visible"))
                sendBroadcast(Intent("UPDATE").apply {
                    putExtra("update", false)
                })
            }
        }
    }


    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionReadMediaAudio.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                launchFragment(ListAudioFragment.newInstance())

            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionReadExternalStorage.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                launchFragment(ListAudioFragment.newInstance())
            }
        }
    }

    private fun launchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}