package com.example.tides.screens.landingScreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tides.R
import com.example.tides.common.DEGREE
import com.example.tides.common.PERCENTAGE
import com.example.tides.screens.landingScreen.ForecastDay
import com.example.tides.ui.theme.TidesTheme
import com.example.tides.ui.theme.Typography

@Composable
fun SevenDayForecast(
    forecastDay: List<ForecastDay>?,
    isCelsius: Boolean = true,
) {
    forecastDay?.let {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxHeight(),
        ) {
            Text("7-Day Forecast", style = Typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
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
fun ForecastItem(
    item: ForecastDay,
    isToday: Boolean = false,
    isCelsius: Boolean = true,
) {
    Card(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    MaterialTheme.colorScheme.secondary,
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(if (isToday) "Today" else item.dayName)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Image(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(item.weatherIconDay),
                    contentDescription = "Weather condition",
                )
                Image(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(item.weatherIconNight),
                    contentDescription = "Weather condition",
                )
                Spacer(modifier = Modifier)
                Row(
                    modifier = Modifier.width(50.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier.size(12.dp),
                        painter = painterResource(R.drawable.ic_raindrop),
                        contentDescription = "raindrop",
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "${item.rainPercentage}$PERCENTAGE",
                        fontSize = 12.sp,
                    )
                }
                Spacer(modifier = Modifier)
                val maxTemp =
                    if (isCelsius) {
                        item.high
                    } else {
                        (item.high * 9 / 5) + 32
                    }
                val minTemp =
                    if (isCelsius) {
                        item.low
                    } else {
                        (item.low * 9 / 5) + 32
                    }
                Text(
                    modifier = Modifier.width(42.dp),
                    text = "${maxTemp}$DEGREE",
                    textAlign = TextAlign.Center,
                )

                Text(
                    modifier = Modifier.width(42.dp),
                    text = "${minTemp}$DEGREE",
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
@Preview
fun FiveDaysForecastPreview() {
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
        SevenDayForecast(previewFiveDayForecast)
    }
}
