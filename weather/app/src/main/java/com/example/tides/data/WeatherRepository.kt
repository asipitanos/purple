package com.example.tides.data

import android.util.Log

class WeatherRepository(private val apiService: ApiService) {
    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
    ): WeatherResponse? {
        return try {
            apiService.getWeatherData(latitude, longitude)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Failed to fetch weather data", e)
            null
        }
    }
}
