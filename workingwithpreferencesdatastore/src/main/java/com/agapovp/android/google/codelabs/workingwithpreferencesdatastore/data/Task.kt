package com.agapovp.android.google.codelabs.workingwithpreferencesdatastore.data

import java.util.*

enum class TaskPriority {
    HIGH, MEDIUM, LOW
}

data class Task(
    val name: String,
    val deadline: Date,
    val priority: TaskPriority,
    val completed: Boolean = false
)
