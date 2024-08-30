package com.example.buddybeat

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
        val ALL_SONGS_KEY = longPreferencesKey("ALL_SONGS_KEY")
        val FAVORITES_KEY = longPreferencesKey("FAVORITES_KEY")
        val MODE = longPreferencesKey("MODE")
        //val I_AM_RUNNING = booleanPreferencesKey("RUNNING")
        //val MODALITY = longPreferencesKey("MODALITY")
        //val MANUAL_BPM = longPreferencesKey("MANUAL_BPM")
    }

    suspend fun setPreference(key: Preferences.Key<Boolean>, value:Boolean){
        dataStore.edit { pref ->
            pref[key] = value
        }
    }

    suspend fun setPreferenceLong(key: Preferences.Key<Long>, value:Long){
        dataStore.edit { pref ->
            pref[key] = value
        }
    }

    fun getPreferenceLong(key: Preferences.Key<Long>) : Flow<Long> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { pref ->
                val value = pref[key] ?: 0L
                value
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