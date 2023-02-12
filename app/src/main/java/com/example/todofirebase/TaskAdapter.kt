package com.example.todofirebase

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todofirebase.data.Task
import com.example.todofirebase.databinding.TaskViewHolderBinding
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TaskAdapter(var taskList: MutableList<Task>):RecyclerView.Adapter<TaskListViewHolder>() {
    lateinit var onBtnDeleteTask: (Int) -> Unit
    lateinit var onTaskClick: (Task, Int) -> Unit
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListViewHolder {
        val layoutInflater =LayoutInflater.from(parent.context)
        val binding = TaskViewHolderBinding.inflate(layoutInflater, parent, false)
        return TaskListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: TaskListViewHolder, position: Int) {
        holder.binding.apply {
            tvTask.text = if(taskList[position].task == null || taskList[position].task.isEmpty()) {
                ""
            } else {
                taskList[position].task
            }
            tvTaskDetails.text = if(taskList[position].details == null) {
                ""
            } else {
                taskList[position].details
            }
            if(taskList[position].details == null) {
                tvTaskDetails.visibility = View.GONE
            }

            tvDueDate.text = try {
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(taskList[position].dueDate)
            } catch (e:Exception) {
                "No due date set."
            }
            tvDueTime.text = try {
                DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(taskList[position].dueTime)
            } catch (e:Exception) {
                "Time due not set."
            }
            imgbtnDeleteTask.setOnClickListener {
                onBtnDeleteTask.invoke(position)
            }
            holder.itemView.setOnClickListener {
                onTaskClick.invoke(taskList[position], position)
            }
            if(taskList[position].taskDone) {
                cvTaskItem.setCardBackgroundColor(Color.parseColor("#BCBCBC"))
            } else {
                cvTaskItem.setCardBackgroundColor(Color.parseColor("#FFBB86FC"))
            }
        }
    }
}

class TaskListViewHolder(val binding: TaskViewHolderBinding):RecyclerView.ViewHolder(binding.root)