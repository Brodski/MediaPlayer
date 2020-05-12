package com.example.mediaplayer

import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Pereference.newInstance] factory method to
 * create an instance of this fragment.
 */
//https://github.com/vickychijwani/kotlin-koans-android/blob/master/app/src/main/code/me/vickychijwani/kotlinkoans/features/settings/SettingsActivity.kt
class PreferenceFragment : PreferenceFragmentCompat() {

    private var param1: String? = null
    private var param2: String? = null
    private val TAG = "PreferenceFragment"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) : PreferenceFragment {
            val preferenceFragment = PreferenceFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            preferenceFragment.arguments = args
            return preferenceFragment
            }
    }
}
