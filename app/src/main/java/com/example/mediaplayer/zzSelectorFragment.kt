//package com.example.mediaplayer
//
//import android.R
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import com.google.android.material.bottomnavigation.BottomNavigationView
//
//
//class zzSelectorFragment : Fragment() {
//
//
//    private val mIMainActivity: IMainActivity? = null
//
//    private val TAG = "SelectorFragment"
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        var view: View = inflater.inflate(com.example.mediaplayer.R.layout.fragment_selector, container, false)
//        //val view: View = inflater.inflate(R.layout., container, false)
//
//        var bottomNav: BottomNavigationView = view.findViewById(com.example.mediaplayer.R.id.bottom_navigation)
//        bottomNav.setOnNavigationItemSelectedListener {
//            when (it.itemId) {
//                com.example.mediaplayer.R.id.nav_home -> {
//                    val message = "hello from nav_home"
//                    mIMainActivity?.inflateFragment("fragment_a", message)
//                    true
//                }
//                com.example.mediaplayer.R.id.nav_favorites -> {
//                    val message = "hello from nav_favorites"
//                    mIMainActivity?.inflateFragment("fragment_b", message)
//                    true
//                }
//                com.example.mediaplayer.R.id.nav_search -> {
//                    val message = "hello from nav_search"
//                    mIMainActivity?.inflateFragment("fragment_b", message)
//                    true
//                }
//                else -> false
//            }
//        }
//        return view
//    }
//}