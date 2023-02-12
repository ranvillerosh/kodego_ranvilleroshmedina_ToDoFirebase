package com.example.todofirebase.data

import android.content.Context
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.todofirebase.TaskAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SwipeHelperCallback(adapter:TaskAdapter, context: Context):ItemTouchHelper.Callback() {
    var adapterAccess = adapter
    private val dbAccess by lazy { TaskDatabase.getDatabase(context).taskDao() }
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
            val editedTask = this.adapterAccess.taskList[viewHolder.adapterPosition]
            this.adapterAccess.taskList[viewHolder.adapterPosition].taskDone = false
            editTask(editedTask)
            this.adapterAccess.taskList = getAllTasks()
            this.adapterAccess.notifyItemChanged(this.adapterAccess.taskList.indexOf(editedTask))
            this.adapterAccess.notifyDataSetChanged()
        } else if (direction == ItemTouchHelper.RIGHT) {
            val editedTask = this.adapterAccess.taskList[viewHolder.adapterPosition]
            this.adapterAccess.taskList[viewHolder.adapterPosition].taskDone = true
            editTask(editedTask)
            this.adapterAccess.taskList = getAllTasks()
            this.adapterAccess.notifyItemChanged(this.adapterAccess.taskList.indexOf(editedTask))
            this.adapterAccess.notifyDataSetChanged()
        }
    }

    private fun editTask(task: Task) {
        runBlocking {
            launch (Dispatchers.IO) {
                dbAccess.editTask(task)
            }
        }
    }

    private fun getAllTasks():MutableList<Task> {
        lateinit var allTasks:MutableList<Task>
        runBlocking {
            launch (Dispatchers.IO) {
                allTasks = dbAccess.getAll()
            }
        }
        return allTasks
    }
}