package com.example.tides.screens.landingScreen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.tides.R
import com.example.tides.common.CELSIUS
import com.example.tides.common.DEGREE
import com.example.tides.common.EMPTY
import com.example.tides.common.FAHRENHEIT
import com.example.tides.common.PERCENTAGE
import com.example.tides.data.HourlyForecast
import com.example.tides.screens.landingScreen.components.ForecastItem
import com.example.tides.screens.landingScreen.components.HalfWidthComponent
import com.example.tides.ui.theme.TidesTheme
import com.example.tides.ui.theme.Typography
import com.example.tides.utils.getWeatherCodeString
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LandingScreen(
    darkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
) {
    val viewModel: LandingScreenViewModel = koinViewModel()

    val location = viewModel.locationState.collectAsState()
    val weather = viewModel.uiState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val temperature = viewModel.temperatureUnit.collectAsState().value

    val lastUpdatedTimestamp = viewModel.lastUpdatedTimestamp.collectAsState().value

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    // When the app is resumed, trigger a refresh
                    viewModel.refreshWeatherData()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                SettingsDrawerContent(
                    isCelsius = temperature == CELSIUS,
                    onUnitChange = { viewModel.saveTemperatureUnit(if (it) CELSIUS else FAHRENHEIT) },
                    isDarkMode = darkMode,
                    onThemeChange = {
                        onThemeChange(it)
                    },
                )
            },
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                MainScreen(
                    drawerState = drawerState,
                    location = location,
                    weather = weather,
                    isCelsius = temperature == CELSIUS,
                    isRefreshing = weather.value.isLoading,
                    onRefresh = viewModel::refreshWeatherData,
                    lastUpdatedTimestamp = lastUpdatedTimestamp,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    drawerState: DrawerState,
    location: State<String?>,
    weather: State<TideScreenState>,
    isCelsius: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    lastUpdatedTimestamp: Long?,
) {
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier,
                        text = location.value ?: "Location",
                        style = Typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = "settings",
                        modifier = Modifier.clickable { scope.launch { drawerState.open() } },
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = !weather.value.isNetworkAvailable,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No internet connection",
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        lastUpdatedTimestamp?.let {
                            Text(
                                text = "Last updated at: ${formatTimestamp(it)}",
                                modifier =
                                    Modifier
                                        .fillMaxWidth(),
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            item {
                CurrentTemperatureView(
                    weather.value.currentData?.currentWeatherIcon,
                    weather.value.currentData?.currentWeatherCode,
                    weather.value.currentData?.currentTemperature,
                    weather.value.currentData?.currentApparentTemperature,
                    weather.value.high,
                    weather.value.low,
                    isCelsius,
                )
            }

            item {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                ) {
                    HalfWidthComponent(
                        text = "Wind",
                        value = weather.value.currentData?.windSpeedText ?: "",
                        modifier = Modifier.weight(1f),
                    )

                    HalfWidthComponent(
                        text = "Humidity",
                        value = weather.value.currentData?.humidityText ?: "",
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                    )
                }
            }

            item {
                HourlyWeatherRow(weather.value.forecastItems, isCelsius)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            weather.value.forecastDays?.let {
                item {
                    Column(
                        modifier =
                            Modifier
                                .padding(vertical = 16.dp)
                                .fillMaxHeight(),
                    ) {
                        Text("7-Day Forecast", style = Typography.titleLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                itemsIndexed(it) { index, item ->
                    if (index == 0) {
                        ForecastItem(item, isToday = true, isCelsius = isCelsius)
                    } else {
                        ForecastItem(item, isCelsius = isCelsius)
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentTemperatureView(
    currentWeatherIcon: Int?,
    currentWeatherCode: Int?,
    currentTemperature: Int?,
    currentApparentTemperature: Int?,
    highValue: Int?,
    lowValue: Int?,
    isCelsius: Boolean,
) {
    Row(
        modifier = Modifier.padding(top = 16.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        currentWeatherIcon?.let {
            Image(
                modifier = Modifier.size(54.dp),
                painter = painterResource(it),
                contentDescription = "currentWeatherIcon",
            )
        }

        currentWeatherCode?.let {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = it.getWeatherCodeString(),
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        currentTemperature?.let { it ->
            if (isCelsius) {
                Text(text = it.toString() + CELSIUS, fontSize = 46.sp)
            } else {
                val fahrenheit = (it * 9 / 5) + 32
                Text(text = fahrenheit.toString() + FAHRENHEIT, fontSize = 46.sp)
            }
        }
    }
    Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)) {
        highValue?.let {
            if (isCelsius) {
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = "High: $it$DEGREE | Low: ${lowValue}$DEGREE",
                )
            } else {
                val fahrenheitHigh = (it * 9 / 5) + 32
                val fahrenheitLow =
                    lowValue?.let { low ->
                        (low * 9 / 5) + 32
                    } ?: EMPTY
                Text(
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                    text = "High: $fahrenheitHigh$DEGREE | Low: ${fahrenheitLow}$DEGREE",
                )
            }
        }
        Spacer(Modifier.weight(1f))
        currentApparentTemperature?.let {
            if (isCelsius) {
                Text("Feels like: $it$DEGREE")
            } else {
                Text("Feels like: ${(it * 9 / 5) + 32}$DEGREE")
            }
        }
    }
}

@Composable
fun HourlyWeatherRow(
    forecastItems: List<WeatherDataItem>?,
    isCelsius: Boolean,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        forecastItems?.let {
            items(it) { item ->
                when (item) {
                    is WeatherDataItem.TemperatureEvent ->
                        HourlyWeatherItem(
                            item = item,
                            isCelsius = isCelsius,
                        )

                    is WeatherDataItem.SunRiseSunSetEvent ->
                        EventWeatherItem(
                            item = item,
                        )
                }
            }
        }
    }
}

@Composable
fun HourlyWeatherItem(
    item: WeatherDataItem.TemperatureEvent,
    isCelsius: Boolean,
) {
    val temp =
        if (isCelsius) {
            item.hourlyForecast.temperature
        } else {
            (item.hourlyForecast.temperature * 9 / 5) + 32
        }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(item.time)
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(item.icon),
            contentDescription = "Weather condition",
        )
        Text("$temp$DEGREE")

        if (item.hourlyForecast.precipitationProbability > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    modifier = Modifier.size(8.dp),
                    painter = painterResource(R.drawable.ic_raindrop),
                    contentDescription = "raindrop",
                )
                Spacer(Modifier.width(2.dp))
                Text("${item.hourlyForecast.precipitationProbability}$PERCENTAGE", fontSize = 8.sp)
            }
        } else {
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun EventWeatherItem(item: WeatherDataItem.SunRiseSunSetEvent) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(item.time)
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(item.icon),
            contentDescription = item.name,
        )
        Text(item.name)
    }
}

@Composable
fun SettingsDrawerContent(
    isCelsius: Boolean,
    onUnitChange: (Boolean) -> Unit,
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.7f),
        drawerContainerColor = MaterialTheme.colorScheme.background,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Settings", style = Typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))

                // Setting for Temperature Unit
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Use Celsius (°C)")
                    Switch(
                        checked = isCelsius,
                        onCheckedChange = onUnitChange,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Setting for Dark Mode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Dark Mode")
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onThemeChange,
                    )
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun MainScreenPreview() {
    val previewForecastItems =
        listOf(
            WeatherDataItem.SunRiseSunSetEvent(
                name = "Sunrise",
                time = "06:22",
                icon = R.drawable.ic_sunrise,
            ),
            WeatherDataItem.TemperatureEvent(
                time = "07:00",
                hourlyForecast =
                    HourlyForecast(
                        temperature = 15,
                        weatherCode = 1,
                        precipitationProbability = 0,
                        date = "2024-05-25T07:00",
                        isDaytime = true,
                    ),
                icon = R.drawable.ic_partly_cloudy_day,
            ),
            WeatherDataItem.TemperatureEvent(
                time = "10:00",
                hourlyForecast =
                    HourlyForecast(
                        temperature = 19,
                        weatherCode = 0,
                        precipitationProbability = 0,
                        date = "2024-05-25T10:00",
                        isDaytime = true,
                    ),
                icon = R.drawable.ic_sunny,
            ),
            WeatherDataItem.TemperatureEvent(
                time = "13:00",
                hourlyForecast =
                    HourlyForecast(
                        temperature = 23,
                        weatherCode = 61,
                        precipitationProbability = 40,
                        date = "2024-05-25T13:00",
                        isDaytime = true,
                    ),
                icon = R.drawable.ic_rain,
            ),
            WeatherDataItem.TemperatureEvent(
                time = "16:00",
                hourlyForecast =
                    HourlyForecast(
                        temperature = 21,
                        weatherCode = 3,
                        precipitationProbability = 10,
                        date = "2024-05-25T16:00",
                        isDaytime = true,
                    ),
                icon = R.drawable.ic_overcast_day,
            ),
            WeatherDataItem.SunRiseSunSetEvent(
                name = "Sunset",
                time = "20:45",
                icon = R.drawable.ic_sunset,
            ),
        )

    val previewFiveDayForecast =
        listOf(
            ForecastDay(
                high = -225,
                low = -115,
                weatherCodeDay = 0,
                weatherIconDay = R.drawable.ic_sunny,
            ),
            ForecastDay(
                high = 22,
                low = 14,
                weatherCodeDay = 3,
                weatherIconDay = R.drawable.ic_overcast_day,
            ),
            ForecastDay(
                high = 0,
                low = 0,
                weatherCodeDay = 61,
                weatherIconDay = R.drawable.ic_rain,
            ),
            ForecastDay(
                high = 20,
                low = 12,
                weatherCodeDay = 95,
                weatherIconDay = R.drawable.ic_thunder_storm_day,
            ),
            ForecastDay(
                high = 23,
                low = 16,
                weatherCodeDay = 2,
                weatherIconDay = R.drawable.ic_partly_cloudy_day,
            ),
            ForecastDay(
                high = 23,
                low = 16,
                weatherCodeDay = 2,
                weatherIconDay = R.drawable.ic_partly_cloudy_day,
            ),
            ForecastDay(
                high = 23,
                low = 16,
                weatherCodeDay = 2,
                weatherIconDay = R.drawable.ic_partly_cloudy_day,
            ),
        )
    TidesTheme {
        MainScreen(
            drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
            location = mutableStateOf("California"),
            weather =
                mutableStateOf(
                    TideScreenState(
                        isLoading = false,
                        currentData =
                            CurrentData(
                                currentTemperature = 23,
                                currentWeatherIcon = R.drawable.ic_sunny,
                                currentWeatherCode = 1,
                            ),
                        high = 27,
                        low = -5,
                        forecastItems = previewForecastItems,
                        forecastDays = previewFiveDayForecast,
                    ),
                ),
            isCelsius = true,
            onRefresh = {},
            isRefreshing = false,
            lastUpdatedTimestamp = System.currentTimeMillis(),
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val date = Date(timestamp)
    return sdf.format(date)
}
