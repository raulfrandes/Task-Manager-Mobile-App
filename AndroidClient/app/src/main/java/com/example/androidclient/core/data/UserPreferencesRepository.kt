package com.example.androidclient.core.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.androidclient.core.TAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private object PreferencesKeys {
        val username = stringPreferencesKey("username")
        val token = stringPreferencesKey("token")
    }

    init {
        Log.d(TAG, "init")
    }

    val userPreferencesStream: Flow<UserPreferences> = dataStore.data
        .catch {
            if (it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { mapUserPreferences(it) }

    suspend fun save(userPreferences: UserPreferences) {
        dataStore.edit {
            it[PreferencesKeys.username] = userPreferences.username
            it[PreferencesKeys.token] = userPreferences.token
        }
    }

    private fun mapUserPreferences(preferences: Preferences) =
        UserPreferences(
            username = preferences[PreferencesKeys.username] ?: "",
            token = preferences[PreferencesKeys.token] ?: ""
        )
}