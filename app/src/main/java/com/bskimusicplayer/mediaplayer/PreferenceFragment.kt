package com.bskimusicplayer.mediaplayer

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

//https://github.com/vickychijwani/kotlin-koans-android/blob/master/app/src/main/code/me/vickychijwani/kotlinkoans/features/settings/SettingsActivity.kt
class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

}
