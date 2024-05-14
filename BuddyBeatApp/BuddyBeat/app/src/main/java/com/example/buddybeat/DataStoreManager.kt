package com.example.buddybeat

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreManager(
    val context: Context
) {
    private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val dataStore = context.dataStore

    companion object{
        val IS_UPLOADED_KEY = booleanPreferencesKey("IS_UPLOADED_KEY")
        val BPM_UPDATED_KEY = booleanPreferencesKey("BPM_UPDATED_KEY")
    }

    suspend fun setPreference(key: Preferences.Key<Boolean>, value:Boolean){
        dataStore.edit { pref ->
            pref[key] = value
        }
    }

    fun getPreference(key: Preferences.Key<Boolean>) : Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { pref ->
                val value = pref[key] ?: false
                value
            }
    }


}