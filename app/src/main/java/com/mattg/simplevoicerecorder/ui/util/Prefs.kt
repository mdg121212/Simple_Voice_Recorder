package com.mattg.simplevoicerecorder.ui.util

import android.content.Context
import android.content.SharedPreferences

class Prefs(private val context: Context) {

    private fun getPrefs(): SharedPreferences {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    fun getPrefOpens(): Int {
        val prefs = getPrefs()
        return prefs.getInt("num_opens", 0)
    }

    fun nukePrefs() {
        getPrefs().edit().putBoolean("have_asked_review", false).putInt("num_opens", 0).apply()
    }

    fun addOpen() {
        val opens = getPrefOpens()
        val newOpens = opens + 1
        val prefs = getPrefs()
        prefs.edit().putInt("num_opens", newOpens).apply()
    }

    fun setHaveAsked() {
        val prefs = getPrefs()
        prefs.edit().putBoolean("have_asked_review", true).apply()
    }

    fun shouldAskForReview(): Boolean {
        val prefs = getPrefs()
        if (getPrefOpens() > 5) {
            val haveAsked = prefs.getBoolean("have_asked_review", false)
            return haveAsked
        } else return false
    }
}