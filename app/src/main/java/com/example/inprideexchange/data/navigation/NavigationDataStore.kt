package com.example.inprideexchange.data.navigation


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.navDataStore by preferencesDataStore("nav_prefs")

@Singleton
class NavigationDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val LAST_SOURCE_KEY = stringPreferencesKey("last_source")
    }

    suspend fun saveLastSource(route: String) {
        context.navDataStore.edit { prefs ->
            prefs[LAST_SOURCE_KEY] = route
        }
    }

    val lastSource: Flow<String> =
        context.navDataStore.data.map { prefs ->
            prefs[LAST_SOURCE_KEY] ?: ""
        }

    suspend fun clear() {
        context.navDataStore.edit { it.clear() }
    }
}