package com.example.assignment

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.*
import com.example.assignment.ui.FoodTrackApp
import com.example.assignment.ui.theme.FoodTrackTheme
import com.example.assignment.util.NotificationWorker
import com.example.assignment.viewmodel.FoodViewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        scheduleExpiryCheck()

        setContent {
            val viewModel: FoodViewModel = viewModel()
            val darkModeSetting by viewModel.darkModeEnabled.collectAsState()
            val darkTheme = when (darkModeSetting) {
                true -> true
                false -> false
                null -> isSystemInDarkTheme()
            }

            FoodTrackTheme(darkTheme = darkTheme) {
                FoodTrackApp()
            }
        }
    }

    private fun scheduleExpiryCheck() {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(6, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "expiry_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
