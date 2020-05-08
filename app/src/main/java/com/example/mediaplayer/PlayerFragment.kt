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
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.TextView
//import android.widget.Toolbar
import androidx.appcompat.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.IOException
import java.lang.RuntimeException

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ItemsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayerFragment : Fragment() {

    private lateinit var playerControls: PlayerControlView
    private var playerView: PlayerView? = null
    private var player: SimpleExoPlayer? = null
    private lateinit var textView: TextView
    private lateinit var mService: AudioPlayerService
    private var mBound: Boolean = false
    private var listener: PlayerFragListener? = null
    private lateinit var correctMenu: Menu

    interface PlayerFragListener {
        fun onPlayerSent(num: Int)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlayerService.LocalBinder
            mService = binder.getService()
            mBound = true

            playerView?.player = mService.exoPlayer
            playerView?.showController()
            //Log.e(TAG, mService.exoPlayer?.currentWindowIndex.toString())
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e(TAG, "Disconnected Service :o")
            mBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        initPlayer2()
        Log.e(tag, "onstart Player frag")
//        playerView?.player = (activity as MainActivity).getPlayer()
//        playerView?.showController()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // other class must implement this
        listener = context as PlayerFragListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStop() {
        super.onStop()
        releasePlayer2()
    }

    private fun releasePlayer2() {
        Log.e(TAG, "Release called")
//        var intent: Intent = Intent(activity, AudioPlayerService::class.java)
        activity?.unbindService(connection)
        mBound = false
    }

    private fun initPlayer2() {
        // Google' Building feature-rich media apps with ExoPlayer - https://www.youtube.com/watch?v=svdq1BWl4r8
        // https://stackoverflow.com/questions/23017767/communicate-with-foreground-service-android
        var intent: Intent = Intent(activity, AudioPlayerService::class.java)
        //startService(intent)
        activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Util.startForegroundService(requireActivity(), intent)
        //Util.startForegroundService(activity!!, intent)

    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.e(TAG, "onCreate: Created!!")
//        setHasOptionsMenu(true)
//    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        Log.e(TAG, "onCreateView Player Frag")
        var v: View = inflater.inflate(R.layout.fragment_player, container, false)

        var nav: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigationId)
        var homeIcon: MenuItem =  nav.menu.findItem(R.id.nav_home)
        homeIcon.setChecked(true)

        playerView = v.findViewById(R.id.main_view2)
        listener!!.onPlayerSent(420);

        val toolbar: Toolbar = v.findViewById(R.id.toolbar)
        setHasOptionsMenu(true)
        toolbar.title = "Cool Player"
        (activity as AppCompatActivity).setSupportActionBar(toolbar)


//        playerView?.showController()

        var bundle: Bundle? = this!!.arguments
        if (bundle!!.containsKey(getString(R.string.song_bundle))){
            Log.e(TAG, "HAS SONG BUNDLE!!!")
            val s =bundle.getParcelable<Song>(getString(R.string.song_bundle))
            Log.e(TAG, s.toString())
        }

        val btn: Button = v.findViewById(R.id.btnB) as Button
        btn.setOnClickListener { v -> talkService(v) }
        return v

//        var v2: View = inflater.inflate(R.layout.controls_playback, container, false)
//        var cnt = v2.findViewById(R.id.controllerId)

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

    fun configPlayer(mPlayer: SimpleExoPlayer?){
        Log.e(tag,"configPlayer")

        Log.e(tag, mPlayer.toString())
        Log.e(tag, playerView?.player.toString())
        Log.e(tag, mPlayer.toString())
        Log.e(tag, mPlayer.toString())
        playerView?.player = mPlayer
        playerView?.showController()
    }

    fun playAtIndex(index: Int, uri: String) {
        Log.e(TAG, "here in player fragment, playing at index $index")
        Log.e(TAG, "here in player fragment, playing at index $uri")
        Log.e(TAG, mService.exoPlayer?.currentWindowIndex.toString())
        //val wtf = mService.exoPlayer.contentPosition
        //val wtf2 = mService.exoPlayer
        mService.exoPlayer?.seekTo(index,0)
        mService.exoPlayer?.playWhenReady = true

    }

    fun talkService2() {
        Log.e(TAG, "were takling and i'm in player")
    }

    fun talkService(v: View) {
        Log.e(TAG, "Clicked in Player Fragment")
        (activity as MainActivity).talkToMain()
//        Log.e(TAG, mService.exoPlayer!!.currentWindowIndex.toString())
//        Log.e(TAG, mService.exoPlayer!!.currentPeriodIndex.toString())
//        Log.e(TAG, mService.exoPlayer!!.toString())

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //return super.onCreateOptionsMenu(menu)
        Log.e(TAG, "+++ onCreateOptionsMenu +++")
        val frags = fragmentManager!!.fragments
        frags?.forEach {
            Log.e(TAG, "onCreateOptionsMenu: ${it.tag}")
            Log.e(TAG, "onCreateOptionsMenu: ${it.toString()}")
        }
        //val menu: Menu =
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


}