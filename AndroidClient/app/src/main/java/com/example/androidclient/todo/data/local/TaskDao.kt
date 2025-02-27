package com.example.androidclient.todo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.androidclient.todo.data.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE pending = 1")
    suspend fun getAllPendingTasks(): List<Task>

    @Query("UPDATE tasks SET pending = 0 WHERE id = :id")
    suspend fun markTaskAsSynced(id: Int)

    @Query("SELECT * FROM tasks")
    fun getAll(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<Task>)

    @Update
    suspend fun update(task: Task): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}