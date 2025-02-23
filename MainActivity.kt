package com.example.investigacion01
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

data class Task(var description: String, var category: String?)

class MainActivity : AppCompatActivity() {
    private lateinit var editTextTask: EditText
    private lateinit var buttonAddTask: Button
    private lateinit var listViewTasks: ListView
    private lateinit var taskAdapter: ArrayAdapter<String>
    private var tasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextTask = findViewById(R.id.editTextTask)
        buttonAddTask = findViewById(R.id.buttonAddTask)
        listViewTasks = findViewById(R.id.listViewTasks)

        loadTasks()

        taskAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasks.map { it.description })
        listViewTasks.adapter = taskAdapter

        buttonAddTask.setOnClickListener {
            val taskDescription = editTextTask.text.toString()
            if (taskDescription.isNotEmpty()) {
                val task = Task(taskDescription, null)
                tasks.add(task)
                taskAdapter.add(task.description)
                editTextTask.text.clear()
                saveTasks()
            }
        }

        listViewTasks.setOnItemClickListener { _, _, position, _ ->
            val task = tasks[position]
            val editText = EditText(this)
            editText.setText(task.description)

            AlertDialog.Builder(this)
                .setTitle("Edit Task")
                .setView(editText)
                .setPositiveButton("Save") { dialog, _ ->
                    task.description = editText.text.toString()
                    taskAdapter.clear()
                    taskAdapter.addAll(tasks.map { it.description })
                    saveTasks()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                .create()
                .show()
        }

        listViewTasks.setOnItemLongClickListener { _, _, position, _ ->
            tasks.removeAt(position)
            taskAdapter.clear()
            taskAdapter.addAll(tasks.map { it.description })
            saveTasks()
            true
        }

        listViewTasks.setOnItemLongClickListener { _, _, position, _ ->
            val task = tasks[position]
            val categories = arrayOf("Work", "Personal", "Other")
            AlertDialog.Builder(this)
                .setTitle("Select Category")
                .setItems(categories) { _, which ->
                    task.category = categories[which]
                    saveTasks()
                }
                .create()
                .show()
            true
        }
    }

    private fun saveTasks() {
        val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(tasks)
        editor.putString("task_list", json)
        editor.apply()
    }

    private fun loadTasks() {
        val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("task_list", null)
        val type = object : TypeToken<MutableList<Task>>() {}.type
        if (json != null) {
            tasks = gson.fromJson(json, type)
        }
    }
}