package com.example.androidclient.todo.data.remote

import com.example.androidclient.todo.data.Task

data class TaskEvent(val eventType: String, val payload: Payload)

data class Payload(val task: Task)
