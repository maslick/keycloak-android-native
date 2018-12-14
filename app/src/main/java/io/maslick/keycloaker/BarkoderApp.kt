package io.maslick.keycloaker

import android.app.Application
import io.maslick.keycloaker.di.apiModule
import io.maslick.keycloaker.di.sharedPrefsModule
import org.koin.android.ext.android.startKoin


class BarkoderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(apiModule, sharedPrefsModule))
    }
}