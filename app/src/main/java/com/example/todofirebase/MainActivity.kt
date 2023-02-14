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
import com.example.todofirebase.data.TaskDAO
import com.example.todofirebase.databinding.ActivityMainBinding
import com.example.todofirebase.databinding.AddNewTaskDialogBinding
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var adapter: FireBaseTaskAdapter
    private val dbAccess = TaskDAO()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val snapshotParser = { snapshot: DataSnapshot ->
            Task(
                snapshot.child("id").value.toString(),
                snapshot.child("task").value.toString(),
                snapshot.child("details").value?.toString(),
                try {
                    LocalDate.parse(snapshot.child("dueDate").value?.toString())
                } catch (e:NullPointerException) { null },
                try {
                    LocalTime.parse(snapshot.child("dueTime").value?.toString())
                } catch (e:NullPointerException) { null },
                snapshot.child("taskDone").value.toString().toBooleanStrict()
            )
        }

        val options = FirebaseRecyclerOptions.Builder<Task>().setQuery(dbAccess.getAll(), snapshotParser )
        .build()

        adapter = FireBaseTaskAdapter(options)
        binding.rvTaskRecycler.layoutManager = LinearLayoutManager(applicationContext)
        binding.rvTaskRecycler.adapter = adapter
//        adapter.snapshots.sortWith(compareBy<Task> {it.taskDone}.thenBy { it.dueDate }.thenBy { it.dueTime })

        binding.fabAddNewTask.setOnClickListener {
            showAddNewTaskDialog()
        }

        adapter.onBtnDeleteTask = {
            holder: TaskListViewHolder, task: Task ->
            deleteTask(holder, task)
        }

        adapter.onTaskClick = {
            holder: TaskListViewHolder, task: Task ->
            editTaskDialog(task)
        }

        val callback:ItemTouchHelper.Callback = SwipeHelperCallback(adapter, dbAccess)
        val mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(binding.rvTaskRecycler)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
    private fun addNewTask(task: Task):String {
        var taskID:String = ""
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

    private fun deleteTask( holder: TaskListViewHolder, task: Task) {
        val position = holder.absoluteAdapterPosition
        GlobalScope.launch (Dispatchers.IO) {
            dbAccess.deleteTask(task)
        }
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

                    newTask.id = addNewTask(newTask)
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

    private fun editTaskDialog(task: Task) {
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
                var editedTask = Task(task.id, binding.etTask.text.toString(), taskDetails, dueDate, dueTime, task.taskDone)
                editTask(editedTask)
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
