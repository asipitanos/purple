package com.example.tides.data

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("forecast") // The endpoint path
    suspend fun getWeatherData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("timezone") timezone: String = "auto",
        @Query("hourly") hourly: String = "temperature_2m,precipitation_probability,weather_code,is_day",
        @Query("daily") daily: String = "sunrise,sunset,temperature_2m_max,temperature_2m_min,weather_code,precipitation_probability_max",
        @Query(
            "current",
        ) current: String =
            "apparent_temperature,temperature_2m,weather_code," +
                    "is_day,relative_humidity_2m,wind_speed_10m,wind_direction_10m",
    ): WeatherResponse
}
