package com.mattg.simplevoicerecorder.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.mattg.simplevoicerecorder.R
import com.mattg.simplevoicerecorder.ui.util.NightMode

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        //THIS IS KEY, NEED TO GET THE PREFERENCES, AND REGISTER THE LISTENER ALONG WITH OVERRIDING
        //THE METHOD
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        //need to reference context as nullable, the recreation of the activity throws errors if this is unchecked
        val darkModeString = context?.getString(R.string.pref_key_night)
        key?.let{
            if(it == darkModeString && context != null) sharedPreferences.let { pref ->
                val darkModeValues = context?.resources?.getStringArray(R.array.night_mode_value)
                when(pref?.getString(darkModeString, darkModeValues?.get(0))) {
                    //checking against the values in the array, then using the provided function to set the
                    //corresponding value
                    darkModeValues?.get(0) -> { updateTheme(NightMode.AUTO.value) }
                    darkModeValues?.get(1) -> {updateTheme(NightMode.ON.value)}
                    darkModeValues?.get(2) -> {updateTheme(NightMode.OFF.value)}
                    else -> updateTheme(NightMode.AUTO.value)
                }
            }
        }

    }

    private fun updateTheme(nightMode: Int): Boolean {
        AppCompatDelegate.setDefaultNightMode(nightMode)
        requireActivity().recreate()
        return true
    }
}