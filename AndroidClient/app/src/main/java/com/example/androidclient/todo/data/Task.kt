package com.example.androidclient.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: Int = -1,
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val priority: Int = 0,
    val completed: Boolean = false,
    val photoUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val pending: Boolean = false
)