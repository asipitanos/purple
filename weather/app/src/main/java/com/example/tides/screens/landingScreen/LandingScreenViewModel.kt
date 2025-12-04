package com.example.tides.screens.landingScreen

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tides.R
import com.example.tides.common.CELSIUS
import com.example.tides.common.PERCENTAGE
import com.example.tides.data.HourlyForecast
import com.example.tides.data.WeatherRepository
import com.example.tides.data.WeatherResponse
import com.example.tides.data.getCombinedForecastItems
import com.example.tides.data.getForecastDay
import com.example.tides.data.getIconFromWeatherCode
import com.example.tides.data.getMaxTemperature
import com.example.tides.data.getMinTemperature
import com.example.tides.datastore.AppPreferences
import com.example.tides.location.LocationRepository
import com.example.tides.utils.ConnectivityObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Calendar
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours

class LandingScreenViewModel(
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val locationPreferences: AppPreferences,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {
    private val _locationState = MutableStateFlow<String?>(null)
    val locationState = _locationState.asStateFlow()

    private val _uiState = MutableStateFlow(TideScreenState())
    val uiState = _uiState.asStateFlow()
    val location =
        locationPreferences.location.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Triple(0.0, 0.0, "0"), // default state
        )

    val lastUpdatedTimestamp: StateFlow<Long?> =
        locationPreferences.lastUpdatedTimestamp.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    init {
        observeNetworkStatus()
        viewModelScope.launch {
            val cachedJson = locationPreferences.cachedWeatherResponse.firstOrNull()
            if (!cachedJson.isNullOrBlank()) {
                try {
                    val cachedResponse = Json.decodeFromString<WeatherResponse>(cachedJson)
                    processAndSetState(cachedResponse)
                } catch (_: Exception) {
                }
            }
            location.collect { (lat, lon, name) ->
                fetchTideDataForCurrentLocation()
                if (lat != 0.0 && lat != null && lon != 0.0 && lon != null && name != "0" && name != null) {
                    _locationState.value = name
                }
            }
        }
        startAutomaticRefresh()
    }

    private fun startAutomaticRefresh() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val millisUntilNextHour =
                (60 - calendar.get(Calendar.MINUTE)) * 60_000L -
                        calendar.get(Calendar.SECOND) * 1000L -
                        calendar.get(Calendar.MILLISECOND)
            delay(millisUntilNextHour)

            while (true) {
                if (_uiState.value.isNetworkAvailable) {
                    refreshWeatherData()
                }
                delay(1.hours.inWholeMilliseconds)
            }
        }
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                val isAvailable = status == ConnectivityObserver.Status.Available
                _uiState.update { it.copy(isNetworkAvailable = isAvailable) }
            }
        }
    }

    fun refreshWeatherData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val (lat, lon, _) = location.value
                if (lat != 0.0 && lon != 0.0 && lat != null && lon != null) {
                    fetchData(lat, lon)
                } else {
                    fetchTideDataForCurrentLocation()
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to refresh data.")
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    suspend fun saveLocation(
        lat: Double,
        lon: Double,
        name: String,
    ) {
        locationPreferences.saveLocation(lat, lon, name)
    }

    fun getLocationName(
        lat: Double,
        long: Double,
    ) {
        viewModelScope.launch {
            _locationState.value = locationRepository.getPlaceName(lat, long)
            _locationState.value?.let {
                saveLocation(lat, long, it)
            }
        }
    }

    fun fetchTideDataForCurrentLocation() {
        viewModelScope.launch {
            val location = locationRepository.getCurrentLocation()
            if (location != null) {
                fetchData(location.latitude, location.longitude)
                getLocationName(location.latitude, location.longitude)
            } else {
                _locationState.value = "Failed to get location."
            }
        }
    }

    fun fetchData(
        lat: Double,
        long: Double,
    ) {
        viewModelScope.launch {
            try {
                val weatherResponse = weatherRepository.fetchWeather(lat, long)

                if (weatherResponse != null) {
                    val responseJson = Json.encodeToString(weatherResponse)
                    locationPreferences.saveWeatherResponse(responseJson)
                    locationPreferences.saveLastUpdatedTimestamp(System.currentTimeMillis())

                    processAndSetState(weatherResponse)
                }
            } catch (_: Exception) {
            }
        }
    }

    val temperatureUnit: StateFlow<String> =
        locationPreferences.temperatureUnit.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CELSIUS,
        )

    fun saveTemperatureUnit(unit: String) {
        viewModelScope.launch {
            locationPreferences.saveTemperatureUnit(unit)
        }
    }

    private fun processAndSetState(
        weatherResponse: WeatherResponse,
        isLoading: Boolean = false,
    ) {
        val forecastItems = weatherResponse.getCombinedForecastItems()
        val forecastDays = weatherResponse.getForecastDay()

        val currentData =
            CurrentData(
                currentTemperature = weatherResponse.currentData.temperature.roundToInt(),
                currentApparentTemperature = weatherResponse.currentData.apparentTemperature.roundToInt(),
                currentWeatherCode = weatherResponse.currentData.weatherCode,
                currentWeatherIcon =
                    getIconFromWeatherCode(
                        weatherResponse.currentData.weatherCode,
                        weatherResponse.currentData.isDay == 1,
                    ),
                windSpeedText = "${weatherResponse.currentData.windSpeed} km/h ${
                    degreesToCardinalDirection(
                        weatherResponse.currentData.windDirection,
                    )
                }",
                humidityText = "${weatherResponse.currentData.relativeHumidity.toInt()}$PERCENTAGE",
            )

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = isLoading,
                currentData = currentData,
                forecastItems = forecastItems,
                high = weatherResponse.getMaxTemperature(),
                low = weatherResponse.getMinTemperature(),
                forecastDays = forecastDays,
                errorMessage = null,
            )
        }
    }
}

fun degreesToCardinalDirection(degrees: Double): String {
    // A list of all 16 cardinal directions
    val directions =
        listOf(
            "N",
            "NNE",
            "NE",
            "ENE",
            "E",
            "ESE",
            "SE",
            "SSE",
            "S",
            "SSW",
            "SW",
            "WSW",
            "W",
            "WNW",
            "NW",
            "NNW",
        )

    val index = floor((degrees + 11.25) / 22.5).toInt() % 16
    return directions[index]
}

data class TideScreenState(
    val isLoading: Boolean = false,
    val isNetworkAvailable: Boolean = true,
    val forecastItems: List<WeatherDataItem>? = emptyList(),
    val errorMessage: String? = null,
    val currentData: CurrentData? = null,
    val high: Int? = null,
    val low: Int? = null,
    val forecastDays: List<ForecastDay>? = emptyList(),
)

data class ForecastDay(
    val high: Int = 24,
    val low: Int = -5,
    val weatherCodeDay: Int = 0,
    val weatherCodeNight: Int = 0,
    val weatherIconDay: Int = R.drawable.ic_sunny,
    val weatherIconNight: Int = R.drawable.ic_sunny,
    val rainPercentage: Int = 0,
    val dayName: String = "Monday",
)

sealed class WeatherDataItem {
    data class TemperatureEvent(
        val time: String,
        val hourlyForecast: HourlyForecast,
        @param:DrawableRes val icon: Int,
    ) : WeatherDataItem()

    data class SunRiseSunSetEvent(
        val name: String,
        val time: String,
        @param:DrawableRes val icon: Int,
    ) : WeatherDataItem()
}

data class CurrentData(
    val currentTemperature: Int? = null,
    val currentWeatherIcon: Int? = null,
    val currentWeatherCode: Int? = null,
    val currentApparentTemperature: Int? = null,
    val windSpeedText: String? = null,
    val humidityText: String? = null,
)
