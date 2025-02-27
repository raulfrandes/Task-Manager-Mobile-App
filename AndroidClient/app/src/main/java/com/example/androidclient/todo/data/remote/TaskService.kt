package com.example.androidclient.todo.data.remote

import com.example.androidclient.todo.data.Task
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskService {
    @GET("/api/tasks")
    suspend fun find(
        @Query("skip") skip: Int,
        @Query("take") take: Int
    ): TasksResponse

    @GET("/api/tasks/{id}")
    suspend fun read(
        @Path("id") id: Int
    ): Task

    @Headers("Content-Type: application/json")
    @POST("/api/tasks")
    suspend fun create(
        @Body task: Task
    ): Task

    @Headers("Content-Type: application/json")
    @PUT("/api/tasks/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Body task: Task
    ): Task
}