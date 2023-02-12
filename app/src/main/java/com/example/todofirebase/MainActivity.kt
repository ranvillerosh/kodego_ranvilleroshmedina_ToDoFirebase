package com.example.todofirebase

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todofirebase.data.SwipeHelperCallback
import com.example.todofirebase.data.Task
import com.example.todofirebase.data.TaskDatabase
import com.example.todofirebase.databinding.ActivityMainBinding
import com.example.todofirebase.databinding.AddNewTaskDialogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TaskAdapter
    val dbAccess by lazy { TaskDatabase.getDatabase(applicationContext).taskDao() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TaskAdapter(getAllTasks())
        populateRecyclerView(adapter)

        binding.fabAddNewTask.setOnClickListener {
            showAddNewTaskDialog()
        }

        adapter.onBtnDeleteTask = {
            deleteTask(adapter.taskList[it])
            adapter.taskList.removeAt(it)
            adapter.notifyItemRemoved(it)
            adapter.notifyItemRangeChanged(it, adapter.itemCount)
        }

        adapter.onTaskClick = {
            task: Task, position: Int ->
            editTaskDialog(task, position)
        }
        val callback:ItemTouchHelper.Callback = SwipeHelperCallback(adapter, applicationContext)
        val mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(binding.rvTaskRecycler)
    }
    private fun populateRecyclerView(adapter: TaskAdapter){
        binding.rvTaskRecycler.adapter = adapter
        binding.rvTaskRecycler.layoutManager = LinearLayoutManager(this)
    }

    private fun addNewTask(task: Task):Long {
        var taskID:Long = 0
        runBlocking {
            launch (Dispatchers.IO) {
                taskID = dbAccess.addNewTask(task)
            }
        }
        return taskID
    }

    private fun editTask(task: Task) {
        runBlocking {
            launch (Dispatchers.IO) {
                dbAccess.editTask(task)
            }
        }
    }

    private fun deleteTask(task: Task) {
        GlobalScope.launch (Dispatchers.IO) {
            dbAccess.deleteTask(task)
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
    private fun showAddNewTaskDialog() {
        val dialog = Dialog(this)
        val binding:AddNewTaskDialogBinding = AddNewTaskDialogBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        var dueDate : LocalDate? = null
        var dueTime : LocalTime? = null

        binding.btnSave.setOnClickListener {

            try {
                if(binding.etTask.text.isEmpty() && binding.etTaskDetails.text.isEmpty()){
                    Toast.makeText(applicationContext, "Task Creation Cancelled.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    val taskDetails = if(binding.etTaskDetails.text.isEmpty()){
                        null
                    } else {
                        binding.etTaskDetails.text.toString()
                    }
                    var newTask = Task(null, binding.etTask.text.toString(), taskDetails, dueDate, dueTime, false)

                    newTask.id = addNewTask(newTask).toInt()
                    adapter.taskList = getAllTasks()
                    adapter.notifyItemInserted(adapter.taskList.indexOf(newTask))
                    adapter.notifyItemRangeChanged(adapter.taskList.indexOf(newTask),adapter.itemCount)
                }

                dialog.dismiss()
            } catch (e:Exception) {
                Toast.makeText(applicationContext, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDueDate.setOnClickListener {
            val d = LocalDate.now()
            val day = d.dayOfMonth
            val month = d.monthValue-1
            val year = d.year
            
            var dateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                dueDate = LocalDate.of(year, month+1, day)
                binding.btnDueDate.setText("Due: ${DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(dueDate)}")
            }
            
            val dialog = DatePickerDialog(this, dateSetListener, year, month, day)
            dialog.show()
        }

        binding.btnDueTime.setOnClickListener {
            val t = LocalTime.now()
            val hour = t.hour
            val minute = t.minute

            var timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                dueTime = LocalTime.of(hour, minute)
                binding.btnDueTime.setText("At: ${DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(dueTime)}")
            }

            val dialog = TimePickerDialog(this, timeSetListener, hour, minute, false)
            dialog.show()
        }
    }

    private fun editTaskDialog(task: Task, position: Int) {
        val dialog = Dialog(this)
        val binding:AddNewTaskDialogBinding = AddNewTaskDialogBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        var dueDate : LocalDate? = task.dueDate
        var dueTime : LocalTime? = task.dueTime

        binding.apply {
            etTask.setText(task.task)
            etTaskDetails.setText(task.details)
            btnDueDate.setText (
                try {
                    "Due on: ${DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(task.dueDate)}"
                } catch (e:Exception) {
                    "No due date set."
                }
            )
            btnDueTime.setText(
                try {
                    "At: ${DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(task.dueTime)}"
                } catch (e:Exception) {
                    "Time due not set."
                }
            )
        }
        binding.btnSave.setOnClickListener {

            try {

                val taskDetails = if(binding.etTaskDetails.text.isEmpty()){
                    null
                } else {
                    binding.etTaskDetails.text.toString()
                }
                var editedTask = Task(task.id, binding.etTask.text.toString(), taskDetails, dueDate, dueTime, false)
                editTask(editedTask)
                adapter.taskList = getAllTasks()
                adapter.notifyItemChanged(adapter.taskList.indexOf(editedTask))
                adapter.notifyDataSetChanged()

                dialog.dismiss()
            } catch (e:Exception) {
                Toast.makeText(applicationContext, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDueDate.setOnClickListener {
            val d = LocalDate.now()
            val day = d.dayOfMonth
            val month = d.monthValue-1
            val year = d.year

            var dateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                dueDate = LocalDate.of(year, month+1, day)
                binding.btnDueDate.setText("Due: ${DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(dueDate)}")
            }

            val dialog = DatePickerDialog(this, dateSetListener, year, month, day)
            dialog.show()
        }

        binding.btnDueTime.setOnClickListener {
            val t = LocalTime.now()
            val hour = t.hour
            val minute = t.minute

            var timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                dueTime = LocalTime.of(hour, minute)
                binding.btnDueTime.setText("At: ${DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(dueTime)}")
            }

            val dialog = TimePickerDialog(this, timeSetListener, hour, minute, false)
            dialog.show()
        }
    }
}
