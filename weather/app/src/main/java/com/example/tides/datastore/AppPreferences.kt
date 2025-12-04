package com.example.tides.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tides.common.CELSIUS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("user_location")

class AppPreferences(private val context: Context, private val dataStore: DataStore<Preferences>) {
    companion object {
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val NAME = stringPreferencesKey("location_name")

        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")

        val CACHED_WEATHER_RESPONSE = stringPreferencesKey("cached_weather_response")
        val LAST_UPDATED_TIMESTAMP = longPreferencesKey("last_updated_timestamp")
    }

    suspend fun saveLocation(
        lat: Double,
        lon: Double,
        name: String,
    ) {
        context.dataStore.edit { prefs ->
            prefs[LATITUDE] = lat
            prefs[LONGITUDE] = lon
            prefs[NAME] = name
        }
    }

    val location: Flow<Triple<Double?, Double?, String?>> =
        context.dataStore.data.map { prefs ->
            Triple(
                prefs[LATITUDE],
                prefs[LONGITUDE],
                prefs[NAME],
            )
        }

    suspend fun saveDarkMode(darkMode: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = darkMode
        }
    }

    val darkMode: Flow<Boolean?> =
        dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY]
        }

    suspend fun saveTemperatureUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[TEMPERATURE_UNIT] = unit
        }
    }

    val temperatureUnit: Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[TEMPERATURE_UNIT] ?: CELSIUS
        }

    val cachedWeatherResponse: Flow<String?> =
        dataStore.data.map { preferences ->
            preferences[CACHED_WEATHER_RESPONSE]
        }

    suspend fun saveWeatherResponse(weatherResponseJson: String) {
        dataStore.edit { preferences ->
            preferences[CACHED_WEATHER_RESPONSE] = weatherResponseJson
        }
    }

    val lastUpdatedTimestamp: Flow<Long?> =
        dataStore.data.map { preferences ->
            preferences[LAST_UPDATED_TIMESTAMP]
        }

    suspend fun saveLastUpdatedTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_UPDATED_TIMESTAMP] = timestamp
        }
    }
}
