package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.ComplaintRepository
import com.example.ui.AppNavigation
import com.example.ui.AppViewModel
import com.example.ui.AppViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import androidx.lifecycle.lifecycleScope
import com.example.data.FirestoreSyncService

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(this)
    val repository = ComplaintRepository(database.complaintDao())
    val factory = AppViewModelFactory(application, repository)
    val viewModel = ViewModelProvider(this, factory)[AppViewModel::class.java]

    val syncService = FirestoreSyncService(repository, lifecycleScope)
    syncService.start()

    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Box(modifier = Modifier.padding(innerPadding)) {
            AppNavigation(viewModel = viewModel)
          }
        }
      }
    }
  }
}


