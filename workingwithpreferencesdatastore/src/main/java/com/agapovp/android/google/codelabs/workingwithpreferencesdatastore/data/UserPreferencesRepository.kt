package com.agapovp.android.google.codelabs.workingwithpreferencesdatastore.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class SortOrder {
    NONE,
    BY_DEADLINE,
    BY_PRIORITY,
    BY_DEADLINE_AND_PRIORITY
}

data class UserPreferences(
    val showCompleted: Boolean,
    val sortOrder: SortOrder
)

/**
 * Class that handles saving and retrieving user preferences
 */
class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val SHOW_COMPLETED = booleanPreferencesKey("show_completed")
        val SORT_ORDER = stringPreferencesKey("sort_order")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                exception.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            mapUserPreferences(preferences)
        }

    suspend fun updateShowCompleted(showCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_COMPLETED] = showCompleted
        }
    }

    suspend fun enableSortByDeadline(enable: Boolean) {
        dataStore.edit { preferences ->
            val currentOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.NONE.name
            )

            val newSortOrder =
                if (enable) {
                    if (currentOrder == SortOrder.BY_PRIORITY) {
                        SortOrder.BY_DEADLINE_AND_PRIORITY
                    } else {
                        SortOrder.BY_DEADLINE
                    }
                } else {
                    if (currentOrder == SortOrder.BY_DEADLINE_AND_PRIORITY) {
                        SortOrder.BY_PRIORITY
                    } else {
                        SortOrder.NONE
                    }
                }

            preferences[PreferencesKeys.SORT_ORDER] = newSortOrder.name
        }
    }

    suspend fun enableSortByPriority(enable: Boolean) {
        dataStore.edit { preferences ->
            val currentOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.NONE.name
            )

            val newSortOrder =
                if (enable) {
                    if (currentOrder == SortOrder.BY_DEADLINE) {
                        SortOrder.BY_DEADLINE_AND_PRIORITY
                    } else {
                        SortOrder.BY_PRIORITY
                    }
                } else {
                    if (currentOrder == SortOrder.BY_DEADLINE_AND_PRIORITY) {
                        SortOrder.BY_DEADLINE
                    } else {
                        SortOrder.NONE
                    }
                }

            preferences[PreferencesKeys.SORT_ORDER] = newSortOrder.name
        }
    }

    suspend fun fetchInitialPreferences() =
        mapUserPreferences(dataStore.data.first().toPreferences())

    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        val sortOrder =
            SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.NONE.name
            )

        val showCompleted = preferences[PreferencesKeys.SHOW_COMPLETED] ?: false
        return UserPreferences(showCompleted, sortOrder)
    }
}
