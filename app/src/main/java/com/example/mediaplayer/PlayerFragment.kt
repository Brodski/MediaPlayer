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

    interface PlayerFragListener: IMainActivity {
        fun getPlayer(): SimpleExoPlayer?
        fun skipForward(rewind: Boolean = false)
        fun skipRewind()
        fun isPlaying(): Boolean?
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
        val skipIncrement = sharedPreferences.getString(resources.getString(R.string.save_state_increment),resources.getString(R.string.default_increment))?.toInt() ?: 15000
        slop_prevention = sharedPreferences.getInt(resources.getString(R.string.save_state_slop), resources.getString(R.string.slop_max).toInt())
//        val slopMax = resources.getString(R.string.slop_max).toInt()
//        slop_prevention = slopMax - slop_prevention
        forward_zone_degrees = sharedPreferences.getInt(resources.getString(R.string.save_state_skip_zone), 35)
        rewind_zone_degrees = 180 - forward_zone_degrees

        Log.e(TAG, "onCreateView: slop_prevention $slop_prevention")
        Log.e(TAG, "onCreateView: slop_prevention $slop_prevention")
        Log.e(TAG, "onCreateView: slop_prevention $slop_prevention")
        Log.e(TAG, "onCreateView: slop_prevention $slop_prevention")
        Log.e(TAG, "onCreateView: slop_prevention $slop_prevention")
        Log.e(TAG, "onCreateView: slop_prevention $slop_prevention")
        Log.e(TAG, "onCreateView: slop_prevention $slop_prevention")
        Log.e(TAG, "onCreateView: slop_prevention $slop_prevention")
        Log.e(TAG, "onCreateView: forward_zone_degrees $forward_zone_degrees")
        Log.e(TAG, "onCreateView: rewind_zone_degrees $rewind_zone_degrees")

        playerView = v.findViewById(R.id.main_view2)
        playerView?.setRewindIncrementMs(skipIncrement)
        playerView?.setFastForwardIncrementMs(skipIncrement)

        val toolbar: Toolbar = v.findViewById(R.id.toolbar)
        setHasOptionsMenu(true)
        toolbar.title = "Cool Player"
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

//        playerView?.showController()
        
//        var bundle: Bundle? = this!!.arguments
//        if (bundle!!.containsKey(getString(R.string.song_bundle))) {
//            Log.e(TAG, "HAS SONG BUNDLE!!!")
//            val s =bundle.getParcelable<Song>(getString(R.string.song_bundle))
//            Log.e(TAG, s.toString())
//        }

        mDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onShowPress(e: MotionEvent?) {
                Log.e(TAG, "onShowPress: ")
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                Log.e(TAG, "00000000000000000000000 onSingleTapUp: 00000000000000000000000 ")
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
//        var distanceX = x2!! - x1!!
//        var distanceY = y2!! - y1!!
        var distanceX = (x2!! - x1!!).toDouble()
        var distanceY = (y2!! - y1!!).toDouble()
        Log.e(TAG, "processSwipe: distanceX $distanceX")
        Log.e(TAG, "processSwipe: distanceY $distanceY ")

//        val slop_prevention  = 100
//        val forward_zone_degrees = 35
//        val rewind_zone_degrees = 180 - forward_zone_degrees

        var angle = atan2(distanceY, distanceX)
        angle = Math.toDegrees(angle)
//        Log.e(TAG, "processSwipe: forward zone: $forward_zone_degrees")
//        Log.e(TAG, "processSwipe: rewind zone: $rewind_zone_degrees")
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
//            volumeDown()
            Log.e(TAG, "processSwipe: ${listener?.isPlaying()}")
            if (listener?.isPlaying() == true) {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            }
        }
        else {
            Log.e(TAG, "00000000000000000000000 onSingleTapUp: 00000000000000000000000  ")
        }

        // absDiff = diagonal detection (if we run diagonally, then abs diff will be small. If x = y, then perfect 45 diagonal
        val absDiff = if (abs(distanceX) > abs(distanceY)) abs(distanceX) - abs(distanceY) else abs(distanceY) - abs(distanceX) // I'm tired, dont judge me
  //      Log.e(TAG, "processSwipe: abs(distanceX - distanceY) xxxxxxxxxxxxxx $absDiff")

//        if (absDiff < 50) {
//            Log.e(TAG, "processSwipe: NOPE-NOPE-NOPE-NOPE-")
//            return
//        }
//        if (distanceX > 100) {
//            Log.e(TAG, " >>>>>>>>>>>>>>> processSwipe: fast forward")
//            skipForward()
//        } else if (distanceX < -100) {
//            Log.e(TAG, " <<<<<<<<<<< processSwipe: rewind")
//            skipRewind()
//        } else if (distanceY > 100) {
//            Log.e(TAG, " ------------- processSwipe: turn up volue")
//
//            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
//
//        } else if (distanceY < -100) {
//            Log.e(TAG, " ++++++++++++ processSwipe: turn up volue")
//            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
//
//        } else {
//            Log.e(TAG, "processSwipe: Not today + Not today + Not today ")
//        }
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
            R.id.item2 -> {
                Log.e(TAG, "item 2")
                return false
            }
            R.id.settingsId -> {
                Log.e(TAG, "baby: settings click")
                listener?.handleSettingsClick()
                return false
            }
            R.id.supportId -> {
                Log.e(TAG, "baby: supportId click")

                return false
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

///////////////////////////////////////////////////////////////////////


//                when (action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        Log.e(TAG, "onTouch: ------------- ACTION_DOWN")
//                        val pointerIndex = event.actionIndex
//
//                        mLastTouchX = event.getX(pointerIndex)
//                        mLastTouchY = event.getY(pointerIndex)
//
//                        mActivePointerId = event.getPointerId(0)
//                        Log.e(TAG, "onTouch: mlast $mLastTouchX")
//                        Log.e(TAG, "onTouch: mlast $mLastTouchY")
//                        Log.e(TAG, "onTouch: mActivePointerId $mActivePointerId")
//                    }
//                    MotionEvent.ACTION_UP -> {
//                        Log.e(TAG, "onTouch: --------------- ACTION_UP")
//                        mActivePointerId = 999
//                    }
//                    MotionEvent.ACTION_CANCEL -> {
//                        Log.e(TAG, "onTouch: --------------- ACTION_CANCEL")
//                        mActivePointerId = 999
//                    }
//                    MotionEvent.ACTION_POINTER_UP -> {
//                        Log.e(TAG, "onTouch: --------------- ACTION_POINTER_UP")
//                        mActivePointerId = 999
//                        val pointerIndex = event.actionIndex
//                        val pointerId = event.getPointerId(pointerIndex)
//                      //  if (pointerId == mActivePointerId) {
//
//                            val newPointerIdex = if (pointerIndex == 0) 1 else 0
//                            mLastTouchX = event.getX(newPointerIdex)
//                            mLastTouchY = event.getY(newPointerIdex)
//                            mActivePointerId = event.getPointerId(newPointerIdex)
//                            Log.e(TAG, "onTouch: mlast $mLastTouchX")
//                            Log.e(TAG, "onTouch: mlast $mLastTouchY")
//                            Log.e(TAG, "onTouch: mActivePointerId $mActivePointerId")
//                        //}
//                    }
//                }

//                if (mDetector.onTouchEvent(event) ) {
//                    Log.e(TAG, "o?????nTouch: mDetector activiated")
//                    return true
//                }