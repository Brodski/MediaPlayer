package com.bskimusicplayer.mediaplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

//https://github.com/vickychijwani/kotlin-koans-android/blob/master/app/src/main/code/me/vickychijwani/kotlinkoans/features/settings/SettingsActivity.kt
class PreferenceFragment : PreferenceFragmentCompat() {

    private var param1: String? = null
    private var param2: String? = null
    private val TAG = "PreferenceFragment"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

}
