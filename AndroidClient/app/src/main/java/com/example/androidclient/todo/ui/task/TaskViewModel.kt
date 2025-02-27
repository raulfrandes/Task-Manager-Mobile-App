package com.example.androidclient.todo.ui.task

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.androidclient.MyApplication
import com.example.androidclient.todo.data.Task
import com.example.androidclient.core.Result
import com.example.androidclient.core.TAG
import com.example.androidclient.todo.data.TaskRepository
import com.example.androidclient.util.NotificationUtils
import kotlinx.coroutines.launch

data class TaskUiState(
    val taskId: Int? = null,
    val task: Task = Task(),
    var loadResult: Result<Task>? = null,
    var submitResult: Result<Task>? = null
)

class TaskViewModel(private val taskId: Int?, private val taskRepository: TaskRepository) :
    ViewModel() {
    var uiState: TaskUiState by mutableStateOf(TaskUiState(loadResult = Result.Loading))
        private set

    init {
        Log.d(TAG, "init")
        if (taskId != null) {
            loadTask()
        } else {
            uiState = uiState.copy(loadResult = Result.Success(Task()))
        }
    }

    private fun loadTask() {
        viewModelScope.launch {
            taskRepository.taskStream.collect { tasks ->
                if (uiState.loadResult !is Result.Loading) {
                    Log.d(TAG, "loadTask failed")
                    return@collect
                }
                val task = tasks.find { it.id == taskId } ?: Task()
                Log.d(TAG, "loadTask $task")
                uiState = uiState.copy(task = task, loadResult = Result.Success(task))
            }
        }
    }

    fun saveOrUpdateTask(
        title: String,
        description:String,
        dueDate:String,
        priority: Int,
        completed: Boolean,
        photoUrl: String?,
        isOnline: Boolean,
        context: Context
    ) {
        val task = uiState.task.copy(
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority,
            completed = completed,
            photoUrl = photoUrl
        )

        viewModelScope.launch {
            if (isOnline) {
                Log.d(TAG, "(online) saveOrUpdateTask...")
                try {
                    uiState = uiState.copy(submitResult = Result.Loading)
                    val savedTask = if (taskId == null) {
                        taskRepository.save(task)
                    } else {
                        taskRepository.update(task)
                    }
                    Log.d(TAG, "(online) saveOrUpdateTask succeeded")
                    uiState = uiState.copy(submitResult = Result.Success(savedTask))
                } catch (e: Exception) {
                    Log.d(TAG, "(online) saveOrUpdateTask failed")
                    uiState = uiState.copy(submitResult = Result.Error(e))
                }
            } else {
                Log.d(TAG, "(offline) saveOrUpdateTask...")
                try {
                    uiState = uiState.copy(submitResult = Result.Loading)
                    if (taskId == null) {
                        taskRepository.saveOffline(task)
                    } else {
                        taskRepository.updateOffline(task)
                    }
                    Log.d(TAG, "(offline) saveOrUpdateTask succeeded")
                    NotificationUtils.showNotification(
                        context = context,
                        channelId = "SyncChannel",
                        notificationId = task.id,
                        textTitle = "Task Saved Offline",
                        textContent = "Task '${task.title}' will sync when online."
                    )
                    uiState = uiState.copy(submitResult = Result.Success(task))
                } catch (e: Exception) {
                    Log.d(TAG, "(offline) saveOrUpdateTask failed")
                    uiState = uiState.copy(submitResult = Result.Error(e))
                }
            }
        }
    }

    companion object {
        fun Factory(taskId: Int?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                TaskViewModel(taskId, app.container.taskRepository)
            }
        }
    }
}