package com.example.todofirebase.data


import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.todofirebase.FireBaseTaskAdapter
import com.example.todofirebase.TaskAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime

class SwipeHelperCallback(var adapterAccess: FireBaseTaskAdapter, var dbAccess : TaskDAO):ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = 0
        val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (direction == ItemTouchHelper.LEFT) {
            val editedTask = adapterAccess.getItem(viewHolder.bindingAdapterPosition)
            editedTask.taskDone = false
            editTask(editedTask)
        } else if (direction == ItemTouchHelper.RIGHT) {
            val editedTask = adapterAccess.getItem(viewHolder.bindingAdapterPosition)
            editedTask.taskDone = true
            editTask(editedTask)
        }
    }

    private fun editTask(task: Task) {
        runBlocking {
            launch (Dispatchers.IO) {
                dbAccess.editTask(task)
            }
        }
    }
}