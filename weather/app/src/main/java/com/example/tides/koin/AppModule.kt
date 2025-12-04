package com.example.tides.koin

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.tides.MainActivityViewModel
import com.example.tides.data.ApiService
import com.example.tides.data.WeatherRepository
import com.example.tides.datastore.AppPreferences
import com.example.tides.datastore.dataStore
import com.example.tides.location.LocationRepository
import com.example.tides.screens.landingScreen.LandingScreenViewModel
import com.example.tides.utils.ConnectivityObserver
import com.example.tides.utils.NetworkConnectivityObserver
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

private val json = Json { ignoreUnknownKeys = true }
val appModule =
    module {
        single<DataStore<Preferences>> {
            androidContext().dataStore
        }
        single<ConnectivityObserver> { NetworkConnectivityObserver(androidContext()) }
        single { AppPreferences(androidContext(), get()) }
        single { LocationRepository(androidContext()) }
        single { WeatherRepository(get()) }

        viewModel {
            LandingScreenViewModel(
                locationRepository = get(),
                weatherRepository = get(),
                locationPreferences = get(),
                connectivityObserver = get(),
            )
        }

        viewModel {
            MainActivityViewModel(appPreferences = get())
        }

        single<Retrofit> {
            Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/v1/")
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
        }

        single<ApiService> {
            get<Retrofit>().create(ApiService::class.java)
        }
    }
