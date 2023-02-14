package com.example.todofirebase.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

//@Entity (tableName = "task_database")
data class Task(
    var id:String?,
    var task:String,
    var details:String?,
    var dueDate:LocalDate?,
    var dueTime:LocalTime?,
    var taskDone:Boolean
)