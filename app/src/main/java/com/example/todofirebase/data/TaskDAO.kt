package com.example.todofirebase.data

import androidx.room.*

@Dao
interface TaskDAO {
    @Insert
    suspend fun addNewTask(task: Task):Long

    @Delete
    suspend fun deleteTask(task: Task)

    @Update
    suspend fun editTask(task: Task)

    @Query("SELECT * FROM task_database ORDER BY task_status ASC, due_date ASC, due_time ASC")
    fun getAll():MutableList<Task>
}