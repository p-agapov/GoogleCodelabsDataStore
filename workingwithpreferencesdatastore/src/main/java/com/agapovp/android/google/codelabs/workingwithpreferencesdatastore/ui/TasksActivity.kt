package com.agapovp.android.google.codelabs.workingwithpreferencesdatastore.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.agapovp.android.google.codelabs.workingwithpreferencesdatastore.data.SortOrder
import com.agapovp.android.google.codelabs.workingwithpreferencesdatastore.data.TasksRepository
import com.agapovp.android.google.codelabs.workingwithpreferencesdatastore.data.UserPreferencesRepository
import com.agapovp.android.google.codelabs.workingwithpreferencesdatastore.databinding.ActivityTasksBinding

private const val USER_PREFERENCES_NAME = "user_preferences"

private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, USER_PREFERENCES_NAME))
    }
)

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private val adapter = TasksAdapter()

    private lateinit var viewModel: TasksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(
            this,
            TasksViewModelFactory(
                TasksRepository,
                UserPreferencesRepository(dataStore)
            )
        )[(TasksViewModel::class.java)]

        setupRecyclerView()

        viewModel.initialSetupEvent.observe(this) { initialSetupEvent ->
            updateTaskFilters(initialSetupEvent.sortOrder, initialSetupEvent.showCompleted)
            setupOnCheckedChangeListeners()
            observePreferenceChanges()
        }
    }

    private fun observePreferenceChanges() {
        viewModel.tasksUiModel.observe(this) { tasksUiModel ->
            adapter.submitList(tasksUiModel.tasks)
            updateTaskFilters(tasksUiModel.sortOrder, tasksUiModel.showCompleted)
        }
    }

    private fun setupRecyclerView() {
        // add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.list.addItemDecoration(decoration)

        binding.list.adapter = adapter
    }

    private fun setupOnCheckedChangeListeners() {
        binding.sortDeadline.setOnCheckedChangeListener { _, checked ->
            viewModel.enableSortByDeadline(checked)
        }
        binding.sortPriority.setOnCheckedChangeListener { _, checked ->
            viewModel.enableSortByPriority(checked)
        }
        binding.showCompletedSwitch.setOnCheckedChangeListener { _, checked ->
            viewModel.showCompletedTasks(checked)
        }
    }

    private fun updateTaskFilters(sortOrder: SortOrder, showCompleted: Boolean) {
        with(binding) {
            showCompletedSwitch.isChecked = showCompleted
            sortDeadline.isChecked =
                sortOrder == SortOrder.BY_DEADLINE || sortOrder == SortOrder.BY_DEADLINE_AND_PRIORITY
            sortPriority.isChecked =
                sortOrder == SortOrder.BY_PRIORITY || sortOrder == SortOrder.BY_DEADLINE_AND_PRIORITY
        }
    }
}
