package com.example.androidclient

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.androidclient.auth.data.AuthRepository
import com.example.androidclient.auth.data.remote.AuthDataSource
import com.example.androidclient.core.TAG
import com.example.androidclient.core.data.UserPreferencesRepository
import com.example.androidclient.core.data.remote.Api
import com.example.androidclient.todo.data.TaskRepository
import com.example.androidclient.todo.data.remote.TaskService
import com.example.androidclient.todo.data.remote.TaskWsClient
import com.example.androidclient.util.CustomSyncWorker
import com.example.androidclient.util.NotificationUtils.createNotificationChannel

val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences"
)

class AppContainer(val context: Context) {
    init {
        Log.d(TAG, "init")
    }

    private val taskService: TaskService = Api.retrofit.create(TaskService::class.java)
    private val taskWsClient: TaskWsClient = TaskWsClient(Api.getUnsafeOkHttpClient())
    private val authDataSource: AuthDataSource = AuthDataSource()

    private val database: MyAppDatabase by lazy { MyAppDatabase.getDatabase(context) }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(taskService, taskWsClient, database.taskDao())
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authDataSource)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.userPreferencesDataStore)
    }

    fun scheduleSyncTasks(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<CustomSyncWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}