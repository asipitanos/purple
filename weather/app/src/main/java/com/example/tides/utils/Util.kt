package com.example.tides.utils

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
import com.example.tides.common.THUNDERSTORM
import com.example.tides.common.THUNDERSTORM_WITH_HEAVY_HAIL
import com.example.tides.common.THUNDERSTORM_WITH_LIGHT_HAIL

fun Int.getWeatherCodeString(): String {
    when (this) {
        CLEAR_SKY -> return "Clear Sky"
        MAINLY_CLEAR -> return "Mainly Clear"
        PARTLY_CLOUDY -> return "Partly Cloudy"
        OVERCAST -> return "Overcast"
        FOG -> return "Fog"
        RIME_FOG -> return "Rime Fog"
        LIGHT_DRIZZLE -> return "Light Drizzle"
        MODERATE_DRIZZLE -> return "Moderate Drizzle"
        DENSE_DRIZZLE -> return "Dense Drizzle"
        LIGHT_FREEZING_DRIZZLE -> return "Light Freezing Drizzle"
        DENSE_FREEZING_DRIZZLE -> return "Dense Freezing Drizzle"
        SLIGHT_RAIN -> return "Slight Rain"
        MODERATE_RAIN -> return "Moderate Rain"
        HEAVY_RAIN -> return "Heavy Rain"
        FREEZING_LIGHT_RAIN -> return "Light Freezing Rain"
        FREEZING_HEAVY_RAIN -> return "Heavy Freezing Rain"
        LIGHT_SNOWFALL -> return "Slight Snow"
        MODERATE_SNOWFALL -> return "Moderate Snow"
        HEAVY_SNOWFALL -> return "Heavy Snow"
        SNOW_GRAINS -> return "Snow Grains"
        SLIGHT_SHOWERS -> return "Slight Rain Showers"
        MODERATE_SHOWERS -> return "Moderate Rain Showers"
        HEAVY_SHOWERS -> return "Violent Rain Showers"
        LIGHT_SNOW_SHOWERS -> return "Slight Snow Showers"
        HEAVY_SNOW_SHOWERS -> return "Heavy Snow Showers"
        THUNDERSTORM -> return "Thunderstorm"
        THUNDERSTORM_WITH_LIGHT_HAIL -> return "Slight Hail Thunderstorm"
        THUNDERSTORM_WITH_HEAVY_HAIL -> return "Heavy Hail Thunderstorm"
    }

    return EMPTY
}
