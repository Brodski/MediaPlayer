package com.example.mediaplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
//import androidx.fragment.app.FragmentManager
//import android.widget.Toolbar
import androidx.appcompat.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.IOException
import java.lang.RuntimeException
import kotlin.math.log

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ItemsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayerFragment : Fragment() {

    private var playerView: PlayerView? = null
    private var player: SimpleExoPlayer? = null
    private lateinit var mService: AudioPlayerService
    private var listener: PlayerFragListener? = null
    private var listener2: PlayerFragListener? = null

    interface PlayerFragListener: IMainActivity {
        fun getPlayer(): SimpleExoPlayer?
    }


    override fun onStart() {
        Log.e(TAG, "onStart: PlayerFragment")
        super.onStart()
        //initPlayer2()

        setPlayer()
        playerView?.showController()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(TAG, "onAttach: PlayerFragment")
        // other class must implement this
        Log.e(TAG, "onAttach: $context")
        Log.e(TAG, "onAttach: $activity")
        listener = context as PlayerFragListener
//        listener = activity as PlayerFragListener
        Log.e(TAG, "onAttach: $listener")
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume: PlayerFragment")
    }
    
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: stopping player")
//        releasePlayer2()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e(TAG, "onDestroyView: destroying view Player")
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        Log.e(TAG, "onCreateView Player Frag")
        var v: View = inflater.inflate(R.layout.fragment_player, container, false)

        var nav: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigationId)
        var homeIcon: MenuItem =  nav.menu.findItem(R.id.nav_home)
        homeIcon.setChecked(true)

        playerView = v.findViewById(R.id.main_view2)
//        playerView?.player = listener?.getPlayer()

        val toolbar: Toolbar = v.findViewById(R.id.toolbar)
        setHasOptionsMenu(true)
        toolbar.title = "Cool Player"
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

//        playerView?.showController()
        
        var bundle: Bundle? = this!!.arguments
        if (bundle!!.containsKey(getString(R.string.song_bundle))) {
            Log.e(TAG, "HAS SONG BUNDLE!!!")
            val s =bundle.getParcelable<Song>(getString(R.string.song_bundle))
            Log.e(TAG, s.toString())
        }

        val btn: Button = v.findViewById(R.id.btnB) as Button
        btn.setOnClickListener { v -> Log.e(TAG, "onCreateView: Clicked me!") }
        return v
    }

    fun setPlayer(){
        Log.e(TAG, "setPlayer: now setting")
//        if ( listener?.getPlayer() != null ){
        if ( listener?.isService() == true ){
            playerView?.player = listener?.getPlayer()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //return super.onCreateOptionsMenu(menu)
        val frags = requireFragmentManager().fragments

        inflater.inflate(R.menu.menu_player, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.item1 -> {
                Log.e(TAG, "item 1")
                return false
            }
            R.id.item2 -> {
                Log.e(TAG, "item 2")
                return false
            }
            R.id.item3 -> {
                Log.e(TAG, "item 3")
                return false
            }
            R.id.sub1 -> {
                Log.e(TAG, "sub 1")
                return false
            }
        }
        return false
        //return super.onOptionsItemSelected(item)
    }
    companion object {
        const val TAG = "PlayerFragment"
        @JvmStatic
        fun newInstance(param1: String, param2: String): PlayerFragment {
            val playerFragment = PlayerFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            playerFragment.arguments = args
            return playerFragment
        }
    }


//    private fun releasePlayer2() {
//        Log.e(TAG, "Release called")
////        var intent: Intent = Intent(activity, AudioPlayerService::class.java)
//        activity?.unbindService(connection)
//    }
//
//    private fun initPlayer2() {
//        // Google' Building feature-rich media apps with ExoPlayer - https://www.youtube.com/watch?v=svdq1BWl4r8
//        // https://stackoverflow.com/questions/23017767/communicate-with-foreground-service-android
//        var intent: Intent = Intent(activity, AudioPlayerService::class.java)
//        //startService(intent)
//        activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
//        Util.startForegroundService(requireActivity(), intent)
//        //Util.startForegroundService(activity!!, intent)
//    }
//    private val connection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            val binder = service as AudioPlayerService.LocalBinder
//            mService = binder.getService()
//            playerView?.player = mService.exoPlayer
//            playerView?.showController()
//        }
//        override fun onServiceDisconnected(name: ComponentName?) {
//            Log.e(TAG, "Disconnected Service :o")
//        }
//    }

}