package com.example.todofirebase.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity (tableName = "task_database")
data class Task(
    @PrimaryKey(autoGenerate = true)        var id:Int?,
    @ColumnInfo(name = "task")              var task:String,
    @ColumnInfo(name = "task_details")      var details:String?,
    @ColumnInfo(name = "due_date")          var dueDate:LocalDate?,
    @ColumnInfo(name = "due_time")          var dueTime:LocalTime?,
    @ColumnInfo(name = "task_status")       var taskDone:Boolean
)