package com.mattg.simplevoicerecorder.ui.util

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import java.util.*

class SimpleVoiceApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        preferences!!.getString(
            "key_night_mode",
            "auto"
        )!!.apply {
            val mode = NightMode.valueOf(this.toUpperCase(Locale.US))
            Log.i("TESTING", "mode = $mode")
            AppCompatDelegate.setDefaultNightMode(mode.value)
        }

    }

}