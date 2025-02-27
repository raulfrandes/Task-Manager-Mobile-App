package com.example.androidclient.todo.ui.task

import android.graphics.drawable.Icon
import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.androidclient.MyApplication
import com.example.androidclient.R
import com.example.androidclient.core.Result
import com.example.androidclient.gallery.EMPTY_IMAGE_URI
import com.example.androidclient.todo.ui.NetworkStatusViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    taskId: Int?,
    onClose: () -> Unit
) {
    val taskViewModel = viewModel<TaskViewModel>(factory = TaskViewModel.Factory(taskId))
    val taskUiState = taskViewModel.uiState
    var title by rememberSaveable { mutableStateOf(taskUiState.task.title) }
    var description by rememberSaveable { mutableStateOf(taskUiState.task.description) }
    var dueDate by rememberSaveable { mutableStateOf(taskUiState.task.dueDate) }
    var priority by rememberSaveable { mutableStateOf(taskUiState.task.priority) }
    var completed by rememberSaveable { mutableStateOf(taskUiState.task.completed) }
    var photoUrl by rememberSaveable { mutableStateOf(taskUiState.task.photoUrl) }
    Log.d("TaskScreen", "recompose, id: $taskId, title: $title, description: $description, dueDate: $dueDate, priority: $priority, completed: $completed, photoUrl: $photoUrl")

    val networkStatusViewModel = viewModel<NetworkStatusViewModel>(
        factory = NetworkStatusViewModel.Factory(
            application = LocalContext.current.applicationContext as MyApplication
        )
    )

    val context = LocalContext.current

    LaunchedEffect(taskUiState.submitResult) {
        Log.d("TaskScreen", "Submit = ${taskUiState.submitResult}")
        if (taskUiState.submitResult is Result.Success) {
            Log.d("TaskScreen", "Closing screen")
            onClose()
        }
    }

    var taskInitialized by remember { mutableStateOf(taskId == null) }
    LaunchedEffect(taskId, taskUiState.loadResult) {
        Log.d("TaskScreen", "Task Initialized = ${taskUiState.loadResult}")
        when (val result = taskUiState.loadResult) {
            is Result.Success -> {
                title = taskUiState.task.title
                description = taskUiState.task.description
                dueDate = taskUiState.task.dueDate
                priority = taskUiState.task.priority
                completed = taskUiState.task.completed
                photoUrl = taskUiState.task.photoUrl
                taskInitialized = true
            }
            is Result.Error -> {
                Log.d("TaskScreen", "Failed to load task - ${result.exception?.message}")
            }

            Result.Loading -> {}
            null -> {}
        }
    }

    var imageUri by remember { mutableStateOf(EMPTY_IMAGE_URI) }
    var onTakePhoto by remember { mutableStateOf(false) }
    LaunchedEffect(imageUri) {
        if (imageUri != EMPTY_IMAGE_URI) {
            photoUrl = imageUri.toString()
        }
    }

    if (!onTakePhoto) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.task)) },
                    actions = {
                        Button(onClick = {
                            Log.d(
                                "TaskScreen",
                                "save task title: $title, description: $description, dueDate: $dueDate, priority: $priority, completed: $completed, photoUrl: $photoUrl"
                            )
                            taskViewModel.saveOrUpdateTask(
                                title,
                                description,
                                dueDate,
                                priority,
                                completed,
                                photoUrl,
                                networkStatusViewModel.uiState,
                                context
                            )
                        }) { Text("Save") }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                if (taskUiState.loadResult is Result.Loading) {
                    CircularProgressIndicator()
                }
                if (taskUiState.submitResult is Result.Loading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) { LinearProgressIndicator() }
                }
                if (taskUiState.loadResult is Result.Error) {
                    Text(text = "Failed to load tasks - ${(taskUiState.loadResult as Result.Error).exception?.message}")
                }
                Row {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Row {
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Row {
                    var expanded by remember { mutableStateOf(false) }
                    TextField(
                        value = when (priority) {
                            1 -> "Low"
                            2 -> "Medium"
                            3 -> "High"
                            else -> ""
                        },
                        onValueChange = {},
                        label = { Text("Priority") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf(1, 2, 3).forEach {
                            DropdownMenuItem(
                                text = {
                                    when (it) {
                                        1 -> Text("Low")
                                        2 -> Text("Medium")
                                        3 -> Text("High")
                                    }
                                },
                                onClick = {
                                    priority = it
                                    expanded = false
                                })
                        }
                    }
                }
                Row {
                    val calendar = Calendar.getInstance()
                    val datePickerDialog = android.app.DatePickerDialog(
                        context,
                        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                            val formattedDate = "$year-${month + 1}-$dayOfMonth"
                            dueDate = formattedDate
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    TextField(
                        value = dueDate.split("T")[0],
                        onValueChange = {},
                        label = { Text("Due Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Completed")
                    Switch(
                        checked = completed,
                        onCheckedChange = { completed = it },
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (photoUrl != null) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .aspectRatio(1f),
                                painter = rememberAsyncImagePainter(Uri.parse(photoUrl)),
                                contentDescription = "Task photo"
                            )
                            Text(
                                text = "Photo Preview",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "No photo added.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    Button(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = {
                            Log.d("TaskScreen", "take photo")
                            onTakePhoto = true
                        }
                    ) {
                        Text("Take Photo")
                    }
                }
                if (taskUiState.submitResult is Result.Error) {
                    Text(
                        text = "Failed to submit task - ${(taskUiState.submitResult as Result.Error).exception?.message}",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    } else {
        PhotoScreen(
            onImageUri = {
                imageUri = it
                onTakePhoto = false
            }
        )
    }
}

@Preview
@Composable
fun PreviewTaskScreen() {
    TaskScreen(taskId = 0, onClose = {})
}