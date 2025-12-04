package com.example.tides.data

import android.util.Log
import com.example.tides.R
import com.example.tides.common.CLEAR_SKY
import com.example.tides.common.DENSE_DRIZZLE
import com.example.tides.common.DENSE_FREEZING_DRIZZLE
import com.example.tides.common.EMPTY
import com.example.tides.common.FOG
import com.example.tides.common.FREEZING_HEAVY_RAIN
import com.example.tides.common.FREEZING_LIGHT_RAIN
import com.example.tides.common.HEAVY_RAIN
import com.example.tides.common.HEAVY_SHOWERS
import com.example.tides.common.HEAVY_SNOWFALL
import com.example.tides.common.HEAVY_SNOW_SHOWERS
import com.example.tides.common.LIGHT_DRIZZLE
import com.example.tides.common.LIGHT_FREEZING_DRIZZLE
import com.example.tides.common.LIGHT_SNOWFALL
import com.example.tides.common.LIGHT_SNOW_SHOWERS
import com.example.tides.common.MAINLY_CLEAR
import com.example.tides.common.MODERATE_DRIZZLE
import com.example.tides.common.MODERATE_RAIN
import com.example.tides.common.MODERATE_SHOWERS
import com.example.tides.common.MODERATE_SNOWFALL
import com.example.tides.common.OVERCAST
import com.example.tides.common.PARTLY_CLOUDY
import com.example.tides.common.RIME_FOG
import com.example.tides.common.SLIGHT_RAIN
import com.example.tides.common.SLIGHT_SHOWERS
import com.example.tides.common.SNOW_GRAINS
import com.example.tides.common.SUNRISE
import com.example.tides.common.SUNSET
import com.example.tides.common.THUNDERSTORM
import com.example.tides.common.THUNDERSTORM_WITH_HEAVY_HAIL
import com.example.tides.common.THUNDERSTORM_WITH_LIGHT_HAIL
import com.example.tides.screens.landingScreen.ForecastDay
import com.example.tides.screens.landingScreen.WeatherDataItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Serializable
data class WeatherResponse(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("hourly")
    val hourlyData: HourlyData,
    @SerialName("daily")
    val dailyData: DailyData,
    @SerialName("current")
    val currentData: CurrentData,
    @SerialName("timezone")
    val timezone: String,
)

@Serializable
data class HourlyData(
    @SerialName("time")
    val time: List<String>,
    @SerialName("temperature_2m")
    val temperatures: List<Double>,
    @SerialName("precipitation_probability")
    val precipitationProbabilities: List<Int>,
    @SerialName("weather_code")
    val weatherCodes: List<Int>,
    @SerialName("is_day")
    val isDaytime: List<Int>,
)

@Serializable
data class DailyData(
    @SerialName("sunset")
    val sunset: List<String>,
    @SerialName("sunrise")
    val sunrise: List<String>,
    @SerialName("temperature_2m_max")
    val maxTemperature: List<Double>,
    @SerialName("temperature_2m_min")
    val minTemperature: List<Double>,
    @SerialName("time")
    val time: List<String>,
    @SerialName("weather_code")
    val weatherCodes: List<Int>,
    @SerialName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Int>,
)

@Serializable
data class CurrentData(
    @SerialName("temperature_2m")
    val temperature: Double,
    @SerialName("apparent_temperature")
    val apparentTemperature: Double,
    @SerialName("is_day")
    val isDay: Int,
    @SerialName("weather_code")
    val weatherCode: Int,
    @SerialName("relative_humidity_2m")
    val relativeHumidity: Double,
    @SerialName("wind_speed_10m")
    val windSpeed: Double,
    @SerialName("wind_direction_10m")
    val windDirection: Double,
)

fun WeatherResponse.getForecastDay(): List<ForecastDay> {
    val daily = this.dailyData
    val forecastList = mutableListOf<ForecastDay>()

    // Formatter to read the date string from the API (e.g., "2025-10-19")
    val dateParser = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    // Formatter to create the abbreviated day of the week (e.g., "SUN")
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE")

    // Iterate over the indices of the time list. We assume all daily lists are the same size.
    // We'll take up to 7 days.
    daily.time.take(7).forEachIndexed { index, dateString ->
        try {
            // Get all data points for the current index
            val maxTemp = daily.maxTemperature[index].roundToInt()
            val minTemp = daily.minTemperature[index].roundToInt()
            val weatherCode = daily.weatherCodes[index]

            // Parse the date string to get the day of the week
            val date = LocalDate.parse(dateString, dateParser)
            val dayOfWeek = date.format(dayFormatter)

            // Use the helper function to get the correct icon for the weather code.
            // For a daily forecast, we can assume it's daytime.
            val weatherIconDay = getIconFromWeatherCode(weatherCode, isDaytime = true)
            val weatherIconNight = getIconFromWeatherCode(weatherCode, isDaytime = false)

            // Create the ForecastDay object and add it to our list
            forecastList.add(
                ForecastDay(
                    dayName = dayOfWeek,
                    high = maxTemp,
                    low = minTemp,
                    weatherCodeDay = weatherCode,
                    weatherCodeNight = weatherCode,
                    weatherIconDay = weatherIconDay,
                    weatherIconNight = weatherIconNight,
                    rainPercentage = daily.precipitationProbabilityMax[index],
                ),
            )
        } catch (e: Exception) {
            // Log the error or handle it, in case of an IndexOutOfBoundsException
            // or parsing error. This makes the function more robust.
            Log.e("getForecastDay", "Error processing daily forecast at index $index", e)
        }
    }
    return forecastList
}

fun WeatherResponse.getDayOrNight(): Boolean {
    val indexOfCurrentTime =
        this.hourlyData.time.indexOf(dateFormatFromDateTimeToString(LocalDateTime.now()))
    return if (indexOfCurrentTime != -1) {
        this.hourlyData.isDaytime[indexOfCurrentTime] == 1
    } else {
        false
    }
}

/**
 * Finds the maximum temperature for today's date from the API response's daily forecast.
 * @return The max temperature as an Int, or null if today's date isn't found in the data.
 */
fun WeatherResponse.getMaxTemperature(): Int? {
    val indexOfToday = this.dailyData.time.indexOf(getMinMaxTemperatureFormattedString())

    return if (indexOfToday != -1) {
        this.dailyData.maxTemperature[indexOfToday].roundToInt()
    } else {
        null
    }
}

/**
 * Finds the minimum temperature for today's date from the API response's daily forecast.
 * @return The min temperature as an Int, or null if today's date isn't found in the data.
 */
fun WeatherResponse.getMinTemperature(): Int? {
    val indexOfToday = this.dailyData.time.indexOf(getMinMaxTemperatureFormattedString())

    return if (indexOfToday != -1) {
        this.dailyData.minTemperature[indexOfToday].roundToInt()
    } else {
        null
    }
}

fun WeatherResponse.getHourlyData(): List<HourlyForecast> {
    val currentTimeString = dateFormatFromDateTimeToString(LocalDateTime.now())

    val indexOfCurrentTime = this.hourlyData.time.indexOf(currentTimeString)

    if (indexOfCurrentTime == -1) {
        return emptyList()
    }

    // Drop everything before indexOfCurrentTime
    val times =
        this.hourlyData.time
            .drop(indexOfCurrentTime + 1)
            .take(24)
            .map { date ->
                timeFormat(dateFormatFromStringToLocalDateTime(date))
            }

    val fullTime =
        this.hourlyData.time
            .drop(indexOfCurrentTime + 1)
            .take(24)

    val temps =
        this.hourlyData.temperatures
            .drop(indexOfCurrentTime)
            .take(24)

    val precipitation =
        this.hourlyData.precipitationProbabilities
            .drop(indexOfCurrentTime + 1)
            .take(24)

    val codes =
        this.hourlyData.weatherCodes
            .drop(indexOfCurrentTime + 1)
            .take(24)

    val isDayTime =
        this.hourlyData.isDaytime
            .drop(indexOfCurrentTime + 1)
            .take(24)

    val hourlyForecastList = mutableListOf<HourlyForecast>()

    times.forEachIndexed { index, time ->
        hourlyForecastList.add(
            HourlyForecast(
                temperature = temps[index].roundToInt(),
                precipitationProbability = precipitation[index],
                weatherCode = codes[index],
                date = fullTime[index],
                isDaytime = isDayTime[index] == 1,
            ),
        )
    }

    return hourlyForecastList
}

fun WeatherResponse.getCombinedForecastItems(): List<WeatherDataItem> {
    val hourlyForecasts = this.getHourlyData()

    val sunRiseTimeToday = dateFormatFromStringToLocalDateTime(this.dailyData.sunrise.first())
    val sunRiseTimeTomorrow = dateFormatFromStringToLocalDateTime(this.dailyData.sunrise[1])

    val sunsetTimeToday = dateFormatFromStringToLocalDateTime(this.dailyData.sunset.first())
    val sunsetTimeTomorrow = dateFormatFromStringToLocalDateTime(this.dailyData.sunset[1])

    //  build the combined list
    val combinedList = mutableListOf<WeatherDataItem>()

    hourlyForecasts.forEachIndexed { index, item ->

        val currentPositionDateTime = dateFormatFromStringToLocalDateTime(item.date)
        val nextHour = currentPositionDateTime?.plusHours(1)

        currentPositionDateTime?.let {
            combinedList.add(
                WeatherDataItem.TemperatureEvent(
                    time = timeFormat(it),
                    hourlyForecast = item,
                    icon = getIconFromWeatherCode(item.weatherCode, item.isDaytime),
                ),
            )
        }

        sunRiseTimeToday?.let {
            if (!it.isBefore(currentPositionDateTime) &&
                it.isBefore(nextHour)
            ) {
                combinedList.add(
                    WeatherDataItem.SunRiseSunSetEvent(
                        name = SUNRISE,
                        time = timeFormat(it),
                        icon = R.drawable.ic_sunrise,
                    ),
                )
            }
        }

        sunRiseTimeTomorrow?.let {
            if (!it.isBefore(currentPositionDateTime) && it.isBefore(nextHour)) {
                combinedList.add(
                    WeatherDataItem.SunRiseSunSetEvent(
                        name = SUNRISE,
                        time = timeFormat(it),
                        icon = R.drawable.ic_sunrise,
                    ),
                )
            }
        }

        sunsetTimeToday?.let {
            if (!it.isBefore(currentPositionDateTime) && it.isBefore(nextHour)) {
                combinedList.add(
                    WeatherDataItem.SunRiseSunSetEvent(
                        name = SUNSET,
                        time = timeFormat(it),
                        icon = R.drawable.ic_sunset,
                    ),
                )
            }
        }

        sunsetTimeTomorrow?.let {
            if (!it.isBefore(currentPositionDateTime) && it.isBefore(nextHour)) {
                combinedList.add(
                    WeatherDataItem.SunRiseSunSetEvent(
                        name = SUNSET,
                        time = timeFormat(it),
                        icon = R.drawable.ic_sunset,
                    ),
                )
            }
        }
    }

    return combinedList
}

fun dateFormatFromStringToLocalDateTime(date: String): LocalDateTime? {
    val fullDateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    return try {
        LocalDateTime.parse(date, fullDateTimeFormatter)
    } catch (_: Exception) {
        Log.e("dateFormat", "Error parsing date: $date")
        null
    }
}

fun dateFormatFromDateTimeToString(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00")

    return dateTime.format(formatter)
}

fun timeFormat(dateTime: LocalDateTime?): String {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    return dateTime?.format(timeFormatter) ?: EMPTY
}

data class HourlyForecast(
    val temperature: Int,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val date: String,
    val isDaytime: Boolean,
)

fun getMinMaxTemperatureFormattedString(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.now().format(formatter)
}

fun getIconFromWeatherCode(
    weatherCode: Int,
    isDaytime: Boolean = true,
): Int {
    return when (weatherCode) {
        CLEAR_SKY -> {
            if (isDaytime) R.drawable.ic_sunny else R.drawable.ic_night_clear
        }

        MAINLY_CLEAR -> {
            if (isDaytime) R.drawable.ic_mainly_clear_day else R.drawable.ic_mainly_clear_night
        }

        PARTLY_CLOUDY -> {
            if (isDaytime) R.drawable.ic_partly_cloudy_day else R.drawable.ic_partly_cloudy_night
        }

        OVERCAST -> {
            if (isDaytime) R.drawable.ic_overcast_day else R.drawable.ic_overcast_night
        }

        FOG, RIME_FOG -> {
            if (isDaytime) R.drawable.ic_overcast_day else R.drawable.ic_overcast_night // todo
        }

        LIGHT_DRIZZLE, MODERATE_DRIZZLE, DENSE_DRIZZLE, LIGHT_FREEZING_DRIZZLE,
        DENSE_FREEZING_DRIZZLE, SLIGHT_RAIN, MODERATE_RAIN, HEAVY_RAIN, FREEZING_LIGHT_RAIN,
        FREEZING_HEAVY_RAIN, SLIGHT_SHOWERS, MODERATE_SHOWERS, HEAVY_SHOWERS,
            -> {
            if (isDaytime) R.drawable.ic_rain else R.drawable.ic_night_rain
        }

        LIGHT_SNOWFALL, MODERATE_SNOWFALL, HEAVY_SNOWFALL, SNOW_GRAINS, LIGHT_SNOW_SHOWERS, HEAVY_SNOW_SHOWERS -> {
            if (isDaytime) R.drawable.ic_snow else R.drawable.ic_snow
        }

        THUNDERSTORM, THUNDERSTORM_WITH_LIGHT_HAIL, THUNDERSTORM_WITH_HEAVY_HAIL -> {
            if (isDaytime) R.drawable.ic_thunder_storm_day else R.drawable.ic_thunder_storm_night
        }

        else -> {
            R.drawable.ic_sunny
        }
    }
}
