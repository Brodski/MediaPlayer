package com.example.mediaplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.net.Uri
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
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.IOException
import java.lang.RuntimeException
import kotlin.math.abs
import kotlin.math.atan2
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
    private var skipIncrement: Int = 10000

    interface PlayerFragListener: IMainActivity {
        fun getPlayer(): SimpleExoPlayer?
        fun skipForward(rewind: Boolean = false)
        fun skipRewind()
        fun isPlaying(): Boolean?
        fun togglePlayPause()
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

    private lateinit var audioManager: AudioManager
    private lateinit var mDetector: GestureDetector
    var slop_prevention  = 100
    var forward_zone_degrees = 35
    var rewind_zone_degrees = 180 - forward_zone_degrees

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        audioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        Log.e(TAG, "onCreateView Player Frag")
        var v: View = inflater.inflate(R.layout.fragment_player, container, false)

        var nav: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigationId)
        var homeIcon: MenuItem =  nav.menu.findItem(R.id.nav_home)
        homeIcon.setChecked(true)


        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        skipIncrement = sharedPreferences.getString(resources.getString(R.string.save_state_increment),resources.getString(R.string.default_increment))?.toInt() ?: 15000
        slop_prevention = sharedPreferences.getInt(resources.getString(R.string.save_state_slop), resources.getString(R.string.slop_max).toInt())

        forward_zone_degrees = sharedPreferences.getInt(resources.getString(R.string.save_state_skip_zone), 35)
        rewind_zone_degrees = 180 - forward_zone_degrees

        playerView = v.findViewById(R.id.main_view2)
        playerView?.setRewindIncrementMs(skipIncrement)
        playerView?.setFastForwardIncrementMs(skipIncrement)

        player = listener?.getPlayer()


        val toolbar: Toolbar = v.findViewById(R.id.toolbar)
        setHasOptionsMenu(true)
        toolbar.title = resources.getString(R.string.toolbar_name)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        mDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onShowPress(e: MotionEvent?) {
                Log.e(TAG, "onShowPress: ")
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                Log.e(TAG, "00000000000000000000000 onSingleTapUp: 00000000000000000000000 ")
                listener?.togglePlayPause()
                return true
            }

            override fun onDown(e: MotionEvent?): Boolean {
                Log.e(TAG, "onDown: ")
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
//                Log.e(TAG, "onFling: ")
//                Log.e(TAG, "onFling: e1: ${e1?.x}, ${e1?.y}")
//                Log.e(TAG, "onFling: e2: ${e2?.x}, ${e2?.y}")

                processSwipe(e1?.x, e1?.y, e2?.x, e2?.y)

                return true
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
//                Log.e(TAG, "onScroll: ")
//                // downtime
//                Log.e(TAG, "onScroll: e1: ${e1?.x}, ${e1?.y}")
//                Log.e(TAG, "onScroll: e2: ${e2?.x}, ${e2?.y}")
                return true
            }

            override fun onLongPress(e: MotionEvent?) {
                Log.e(TAG, "onLongPress: ")
            }
        })

        playerView?.setOnTouchListener(object: View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                Log.e(TAG, "Top of it all: mActivePointerId ------ $mActivePointerId")
                mDetector.onTouchEvent(event)
                return true;
            }
        })



        return v
    }


    private fun processSwipe(x1: Float?, y1: Float?, x2: Float?, y2: Float?) {
        if (arrayOf(x1, y1, x2, y2).contains(null)) {
            return
        }
        Log.e(TAG, "processSwipe: ==========================================")
        var distanceX = (x2!! - x1!!).toDouble()
        var distanceY = (y2!! - y1!!).toDouble()
        Log.e(TAG, "processSwipe: distanceX $distanceX")
        Log.e(TAG, "processSwipe: distanceY $distanceY ")
        var angle = atan2(distanceY, distanceX)
        angle = Math.toDegrees(angle)
        Log.e(TAG, "processSwipe: angle: $angle")
        Log.e(TAG, "processSwipe: abs(angle): ${abs(angle)}")

        if (abs(angle) < forward_zone_degrees && distanceX > slop_prevention) {
            Log.e(TAG, "processSwipe: ++++++++++++++++ ")
            skipForward()
        }
        else if (abs(angle) > rewind_zone_degrees && distanceX < (slop_prevention * -1) ) {
            Log.e(TAG, "processSwipe: --------------- ")
            skipRewind()
        }
        else if (distanceY < (slop_prevention * -1) ) {
            Log.e(TAG, "processSwipe: VOLUME UP ")
            Log.e(TAG, "processSwipe: ${listener?.isPlaying()}")
            if (listener?.isPlaying() == true) {
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            }
        }
        else if (distanceY > slop_prevention ) {
            Log.e(TAG, "processSwipe: VOLUME DOWN ")
            Log.e(TAG, "processSwipe: ${listener?.isPlaying()}")
            if (listener?.isPlaying() == true) {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            }
        }
        else {
            Log.e(TAG, "00000000000000000000000 onSingleTapUp: 00000000000000000000000  ")
            listener?.togglePlayPause()
        }
        Log.e(TAG, "processSwipe: ==========================================")
    }

    private fun volumeUp() {
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }
    private fun volumeDown() {
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)

    }

    fun setPlayer(){
        Log.e(TAG, "setPlayer: now setting")
//        if ( listener?.getPlayer() != null ){
        if ( listener?.isService() == true ){
            playerView?.player = listener?.getPlayer()
        }
    }

    fun skipForward() {
        listener?.skipForward()
    }

    fun skipRewind() {
        listener?.skipRewind()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //return super.onCreateOptionsMenu(menu)
        val frags = requireFragmentManager().fragments

        inflater.inflate(R.menu.menu_player, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.settingsId -> {
                Log.e(TAG, "baby: settings click")
                listener?.handleSettingsClick()
                return false
            }
            R.id.contactId -> {
                listener?.sendEmail()
            }
            R.id.donateId -> {
                listener?.plzDoante()
            }
        }
        return false
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

}