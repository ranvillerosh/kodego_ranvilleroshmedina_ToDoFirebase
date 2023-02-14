package com.example.todofirebase

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todofirebase.data.Task
import com.example.todofirebase.databinding.TaskViewHolderBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class FireBaseTaskAdapter(options: FirebaseRecyclerOptions<Task>):
    FirebaseRecyclerAdapter<Task, TaskListViewHolder>(options) {
    lateinit var onBtnDeleteTask: (TaskListViewHolder, Task) -> Unit
    lateinit var onTaskClick: (TaskListViewHolder, Task) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TaskViewHolderBinding.inflate(layoutInflater, parent, false)
        return TaskListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskListViewHolder, position: Int, model: Task) {
//        taskList.add(model)
        
        holder.binding.apply {
            tvTask.text = if(model.task == null || model.task.isEmpty()) {
                ""
            } else {
                model.task
            }
            tvTaskDetails.text = if(model.details == null) {
                ""
            } else {
                model.details
            }
            if(model.details == null) {
                tvTaskDetails.visibility = View.GONE
            }

            tvDueDate.text = try {
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(model.dueDate)
            } catch (e:Exception) {
                "No due date set."
            }
            tvDueTime.text = try {
                DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(model.dueTime)
            } catch (e:Exception) {
                "Time due not set."
            }
            imgbtnDeleteTask.setOnClickListener {
                onBtnDeleteTask.invoke(holder, model)
            }
            holder.itemView.setOnClickListener {
                onTaskClick.invoke(holder, model)
            }
            if(model.taskDone) {
                cvTaskItem.setCardBackgroundColor(Color.parseColor("#BCBCBC"))
            } else {
                cvTaskItem.setCardBackgroundColor(Color.parseColor("#FFBB86FC"))
            }
        }
    }
}

class TaskListViewHolder(val binding: TaskViewHolderBinding): RecyclerView.ViewHolder(binding.root)