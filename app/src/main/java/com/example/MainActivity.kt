package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.database.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DebtViewModel
import com.example.ui.viewmodel.DebtViewModelFactory
import com.example.workers.DueDateCheckWorker
import java.util.concurrent.TimeUnit

import androidx.compose.runtime.LaunchedEffect
import androidx.work.OneTimeWorkRequestBuilder
import com.example.workers.WhatsAppWorker
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ask for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Setup WorkManager for periodic due date checks (every 12 hours)
        val workRequest = PeriodicWorkRequestBuilder<DueDateCheckWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DueDateCheck",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        // Also setup periodic WhatsApp sync just in case
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()
            
        val whatsappSyncRequest = PeriodicWorkRequestBuilder<WhatsAppWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WhatsAppSync",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            whatsappSyncRequest
        )

        // Initialize SQLite Database and Repository
        val database = AppDatabase.getDatabase(this)
        val repository = AppRepository(database)
        val networkObserver = com.example.data.network.NetworkConnectivityObserver(this)

        // Create ViewModel
        val factory = DebtViewModelFactory(repository, networkObserver)
        val viewModel = ViewModelProvider(this, factory)[DebtViewModel::class.java]

        setContent {
            LaunchedEffect(Unit) {
                viewModel.triggerWhatsAppSync.collectLatest {
                    val oneTimeRequest = OneTimeWorkRequestBuilder<WhatsAppWorker>()
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance(this@MainActivity).enqueue(oneTimeRequest)
                }
            }
            
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}
