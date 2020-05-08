package com.example.mediaplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Pereference.newInstance] factory method to
 * create an instance of this fragment.
 */
class PreferenceFragment : PreferenceFragmentCompat() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    //    override fun onCreate(savedInstanceState: Bundle?){
//        super.onCreate(savedInstanceState)
//        addPreferencesFromResource(R.xml.preferences);
//    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            // Inflate the layout for this fragment
//        return inflater.inflate(R.xml.preferences, container, false)

        var nav: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigationId)
        var settingsIcon: MenuItem =  nav.menu.findItem(R.id.nav_search)
        settingsIcon.isChecked = true
        return super.onCreateView(inflater, container, savedInstanceState)
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
