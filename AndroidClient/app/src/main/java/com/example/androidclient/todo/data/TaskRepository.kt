package com.example.androidclient.todo.data

import android.util.Log
import com.example.androidclient.core.Result
import com.example.androidclient.core.TAG
import com.example.androidclient.core.data.remote.Api
import com.example.androidclient.todo.data.local.TaskDao
import com.example.androidclient.todo.data.remote.TaskEvent
import com.example.androidclient.todo.data.remote.TaskService
import com.example.androidclient.todo.data.remote.TaskWsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class TaskRepository(
    private val taskService: TaskService,
    private val taskWsClient: TaskWsClient,
    private val taskDao: TaskDao
) {
    val taskStream by lazy { taskDao.getAll() }

    init {
        Log.d(TAG, "init")
    }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

    suspend fun refresh() {
        Log.d(TAG, "refresh started")
        try {
            val tasks = taskService.find(0, 100)
            taskDao.deleteAll()
            tasks.tasks.forEach { taskDao.insert(it) }
            Log.d(TAG, "refresh succeeded")
        } catch (e: Exception) {
            Log.w(TAG, "refresh failed", e)
        }
    }

    suspend fun getPendingTasks(): List<Task> {
        return taskDao.getAllPendingTasks()
    }

    suspend fun saveOffline(task: Task) {
        Log.d(TAG, "(offline) update $task")
        handleTaskCreated(task.copy(pending = true))
    }

    suspend fun updateOffline(task: Task) {
        Log.d(TAG, "(offline) update $task")
        handleTaskUpdated(task.copy(pending = true))
    }

    suspend fun syncTaskWithServer(task: Task) {
        try {
            val savedTask = if (task.id == -1) {
                taskService.create(task = task)
            } else {
                taskService.update(id = task.id, task = task)
            }
            taskDao.update(task.copy(id = savedTask.id, pending = false))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync task ${task.id}: ${e.message}")
            throw e
        }
    }

    suspend fun openWsClient() {
        Log.d(TAG, "openWsClient")
        withContext(Dispatchers.IO) {
            getTaskEvents().collect() {
                Log.d(TAG, "Task event collected $it")
                if (it.isSuccess) {
                    val taskEvent = it.getOrNull()
                    when (taskEvent?.eventType) {
                        "TaskAdded" -> handleTaskCreated(taskEvent.payload.task)
                        "TaskUpdated" -> handleTaskUpdated(taskEvent.payload.task)
                        "TaskDeleted" -> handleTaskDeleted(taskEvent.payload.task)
                    }
                }
            }
        }
    }

    suspend fun closeWsClient() {
        Log.d(TAG, "closeWsClient")
        withContext(Dispatchers.IO) {
            taskWsClient.closeSocket()
        }
    }

    private suspend fun getTaskEvents(): Flow<kotlin.Result<TaskEvent>> = callbackFlow {
        Log.d(TAG, "getTaskEvents started")
        taskWsClient.openSocket(
            onEvent = {
                Log.d(TAG, "onEvent $it")
                if (it != null) {
                    trySend(kotlin.Result.success(it))
                }
            },
            onClosed = { close() },
            onFailure = { close() })
        awaitClose { taskWsClient.closeSocket() }
    }

    suspend fun update(task: Task): Task {
        Log.d(TAG, "(online) update $task...")
        val updatedTask =
            taskService.update(id = task.id, task = task.copy(pending = false))
        Log.d(TAG, "(online) update $task succeeded")
        handleTaskUpdated(updatedTask)
        return updatedTask
    }

    suspend fun save(task: Task): Task {
        Log.d(TAG, "(online) save $task...")
        val createdTask =
            taskService.create(task = task.copy(pending = false))
        Log.d(TAG, "(online) save $task succeeded")
        handleTaskCreated(createdTask)
        return createdTask
    }

    private suspend fun handleTaskDeleted(task: Task) {
        Log.d(TAG, "handleTaskDeleted - todo $task")
    }

    private suspend fun handleTaskUpdated(task: Task) {
        Log.d(TAG, "handleTaskUpdated...")
        taskDao.update(task)
    }

    private suspend fun handleTaskCreated(task: Task) {
        Log.d(TAG, "handleTaskCreated...")
        taskDao.insert(task)
    }

    suspend fun deleteAll() {
        taskDao.deleteAll()
    }

    fun setToken(token: String) {
        taskWsClient.authorize(token)
    }
}