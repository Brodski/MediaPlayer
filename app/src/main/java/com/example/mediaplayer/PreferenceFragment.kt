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
class PreferenceFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var param1: String? = null
    private var param2: String? = null
    private val TAG = "PreferenceFragment"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

//    val listener: SharedPreferences.OnSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { }

    
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
//        if (key == getString(R.string.sort_keys)) {
//            Log.i(TAG, "Preference value was updated to: " + sharedPreferences.getString(key, ""))
//        }

        Log.e(TAG, "onSharedPreferenceChanged: key $key")
        Log.e(TAG, "onSharedPreferenceChanged: ${sharedPreferences.getString(key, "")} " )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            // Inflate the layout for this fragment
//        return inflater.inflate(R.xml.preferences, container, false)

//        var nav: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigationId)
//        var settingsIcon: MenuItem =  nav.menu.findItem(R.id.nav_search)
//        settingsIcon.isChecked = true
//        val poopy = ListPreference.SimpleSummaryProvider.getInstance()


        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        Log.e(TAG, "onResume: ?")
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        Log.e(TAG, "onPause: ???")
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
