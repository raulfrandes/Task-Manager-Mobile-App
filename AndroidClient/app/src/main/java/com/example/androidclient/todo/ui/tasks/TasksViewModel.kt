package com.example.androidclient.todo.ui.tasks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.androidclient.MyApplication
import com.example.androidclient.core.TAG
import com.example.androidclient.todo.data.Task
import com.example.androidclient.todo.data.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TasksViewModel(private val taskRepository: TaskRepository) : ViewModel() {
    val uiState: Flow<List<Task>> = taskRepository.taskStream

    init {
        Log.d(TAG, "init")
        loadTasks()
    }

    private fun loadTasks() {
        Log.d(TAG, "loadTasks...")
        viewModelScope.launch {
            taskRepository.refresh()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                TasksViewModel(app.container.taskRepository)
            }
        }
    }
}