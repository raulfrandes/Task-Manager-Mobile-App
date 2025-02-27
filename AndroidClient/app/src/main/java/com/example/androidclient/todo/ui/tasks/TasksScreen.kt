package com.example.androidclient.todo.ui.tasks

import android.app.Application
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidclient.MyApplication
import com.example.androidclient.R
import com.example.androidclient.sensor.ProximitySensorViewModel
import com.example.androidclient.todo.ui.NetworkStatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onTaskClick: (id: Int?) -> Unit,
    onAddTask: () -> Unit,
    onLogout: () -> Unit
) {
    Log.d("TasksScreen", "recompose")

    val tasksViewModel = viewModel<TasksViewModel>(factory = TasksViewModel.Factory)
    val tasksUiState by tasksViewModel.uiState.collectAsStateWithLifecycle(
        initialValue = listOf()
    )

    val networkStatusViewModel = viewModel<NetworkStatusViewModel>(
        factory = NetworkStatusViewModel.Factory(
            application = LocalContext.current.applicationContext as MyApplication
        )
    )

    val context = LocalContext.current
    val proximitySensorViewModel = viewModel<ProximitySensorViewModel>(
        factory = ProximitySensorViewModel.Factory(context.applicationContext as Application)
    )

    val window = (context as ComponentActivity).window

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.tasks)) },
                actions = {
                    Button(onClick = onLogout) { Text("Logout") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("TasksScreen", "add")
                    onAddTask()
                },
            ) { Icon(Icons.Rounded.Add, "Add") }
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            Text("Proximity state: ${if (proximitySensorViewModel.uiState) "Near" else "Far"}")

            NetworkStatus(isOnline = networkStatusViewModel.uiState)

            Spacer(modifier = Modifier.height(16.dp))

            TaskList(
                taskList = tasksUiState,
                onTaskClick = onTaskClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    LaunchedEffect(proximitySensorViewModel.uiState) {
        if (proximitySensorViewModel.uiState) {
            Log.d("TasksScreen", "Near")
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.attributes = window.attributes.apply {
                screenBrightness = 0f
            }
        } else {
            Log.d("TasksScreen", "Far")
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.attributes = window.attributes.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        }
    }

    LaunchedEffect(networkStatusViewModel.uiState) {
        if (networkStatusViewModel.uiState) {
            (context.applicationContext as MyApplication).container.scheduleSyncTasks(context)
        }
    }
}

@Composable
fun NetworkStatus(isOnline: Boolean) {
    val statusText = if (isOnline) "Online" else "Offline"
    val statusColor = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Text (
        text = "Network status: $statusText",
        color = statusColor,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(8.dp)
    )
}

@Preview
@Composable
fun TaskScreenPreview() {
    TasksScreen(onTaskClick = {}, onAddTask = {}, onLogout = {})
}