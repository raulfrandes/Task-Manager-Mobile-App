package com.example.androidclient.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.androidclient.MyApplication
import com.example.androidclient.util.NotificationUtils.showNotification

class CustomSyncWorker(
    context: Context,
    val workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val taskRepository = (context.applicationContext as MyApplication).container.taskRepository

    override suspend fun doWork(): Result {
        return try {
            val pendingTasks = taskRepository.getPendingTasks()
            if (pendingTasks.isNotEmpty()) {
                pendingTasks.forEach { task ->
                    taskRepository.syncTaskWithServer(task)
                }
                taskRepository.refresh()

                showNotification(
                    context = applicationContext,
                    channelId = "SyncChannel",
                    notificationId = 1,
                    textTitle = "Task Sync Complete",
                    textContent = "${pendingTasks.size} tasks synced with the server."
                )
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("CustomSyncWorker", "Error syncing tasks: ${e.message}")
            Result.retry()
        }
    }
}