package com.example.tides

import android.app.Application
import com.example.tides.koin.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class Weather : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Weather)
            modules(appModule)
        }
    }
}
