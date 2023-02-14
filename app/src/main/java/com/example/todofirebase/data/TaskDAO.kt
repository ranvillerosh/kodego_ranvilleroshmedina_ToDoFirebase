package com.example.todofirebase.data

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Objects

class TaskDAO {
    var dbReference = Firebase.database("https://todofirebase-a02a1-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

    fun addNewTask(task: Task):String {
        var pushKey = dbReference.push().key!!
        var convertedTask = ConvertedTask(
            pushKey,
            task.task,
            task.details,
            task.dueDate?.toString(),
            task.dueTime?.toString(),
            task.taskDone
        )
        dbReference.child(pushKey).setValue(convertedTask)
        return pushKey
    }


    fun deleteTask(task: Task) {
        dbReference.child(task.id!!).removeValue()
    }


    fun editTask(task: Task) {
        var convertedTask = ConvertedTask(
            task.id,
            task.task,
            task.details,
            task.dueDate?.toString(),
            task.dueTime?.toString(),
            task.taskDone
        )
        dbReference.child(task.id.toString()).setValue(convertedTask)
    }

    fun getAll():Query {
        return dbReference.orderByKey()
    }
}