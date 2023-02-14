package com.example.todofirebase.data

import java.time.LocalDate
import java.time.LocalTime

data class ConvertedTask(
    var id:String?,
    var task:String,
    var details:String?,
    var dueDate: String?,
    var dueTime: String?,
    var taskDone:Boolean
)