package com.bskimusicplayer.mediaplayer

//import androidx.fragment.app.FragmentManager
//import android.widget.Toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.abs
import kotlin.math.atan2


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PlayerFragment : Fragment() {

    private var playerView: PlayerView? = null
    private var player: SimpleExoPlayer? = null
    private var listener: PlayerFragListener? = null
    private var skipIncrement: Int = 10000
    private var playbackStateListener: PlaybackStateListener? = null

    private lateinit var audioManager: AudioManager
    private lateinit var mDetector: GestureDetector
    var slop_prevention = 100
    var forward_zone_degrees = 35
    var rewind_zone_degrees = 180 - forward_zone_degrees
    private lateinit var tvArtist: TextView
    private lateinit var tvTitle: TextView
    private var vibrator: Vibrator? = null
    private val TAG = "PlayerFragment"
    interface PlayerFragListener : IMainActivity {
        fun getPlayer(): SimpleExoPlayer?
        fun skipForward(rewind: Boolean = false)
        fun skipRewind()
        fun isPlaying(): Boolean?
        fun togglePlayPause()
        fun getSongTitle(): String
        fun getSongArtist(): String
    }


    override fun onStart() {
//        Log.e(TAG, "onStart: PlayerFragment")
        super.onStart()
        setPlayer()
        getTitleStuff()
        playerView?.showController()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
//        Log.e(TAG, "onAttach: PlayerFragment")
        listener = context as PlayerFragListener
    }

    override fun onResume() {
        super.onResume()
//        Log.e(TAG, "onResume: PlayerFragment")
    }

    override fun onDetach() {
        super.onDetach()
//        Log.e(TAG, "onDetach: detaching, listenr set to null")
        listener = null
    }

    override fun onStop() {
//        Log.e(TAG, "onStop: stopping player")
        super.onStop()
        
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        Log.e(TAG, "onDestroyView: ------------------ RELEASED -------------------")
//        Log.e(TAG, "onDestroyView: ------------------ RELEASED -------------------")
        playbackStateListener?.let { player?.removeListener(it) }
    }

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.e(TAG, "onCreate: ")
}
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.e(TAG, "onCreateView Player Frag")
        var v: View = inflater.inflate(R.layout.fragment_player, container, false)
        audioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        tvTitle = v.findViewById(R.id.titleTextScrolling)
        tvArtist = v.findViewById(R.id.artistTextScrolling)

        var nav: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigationId)
        var homeIcon: MenuItem = nav.menu.findItem(R.id.nav_home)
        homeIcon.setChecked(true)


        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        skipIncrement = sharedPreferences.getString(resources.getString(R.string.save_state_increment), resources.getString(R.string.default_increment))?.toInt() ?: 15000
        slop_prevention = sharedPreferences.getInt(resources.getString(R.string.save_state_slop), resources.getString(R.string.slop_max).toInt())

        forward_zone_degrees = sharedPreferences.getInt(resources.getString(R.string.save_state_skip_zone), 35)
        rewind_zone_degrees = 180 - forward_zone_degrees
        playerView = v.findViewById(R.id.main_view2)
        playerView?.setRewindIncrementMs(skipIncrement)
        playerView?.setFastForwardIncrementMs(skipIncrement)

        player = listener?.getPlayer()
        getTitleStuff()
        playbackStateListener = PlaybackStateListener()


//        val toolbar: Toolbar = v.findViewById(R.id.toolbar)
//        toolbar.title = resources.getString(R.string.toolbar_name)
//        setHasOptionsMenu(true)
//        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        mDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onShowPress(e: MotionEvent?) {
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
//                doVibrate()f
                listener?.togglePlayPause()
                return true
            }

            override fun onDown(e: MotionEvent?): Boolean {
                //           Log.e(TAG, "onDown: ")
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

            }
        })
        playerView?.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                mDetector.onTouchEvent(event)
                return true;
            }
        })



        return v
    }

    fun getTitleStuff() {
        tvTitle?.isSelected = true
        tvTitle?.text = listener?.getSongTitle()

        tvArtist?.isSelected = true
        tvArtist?.text = listener?.getSongArtist()
        if (player != null) {
//            Log.e(TAG, "onCreateView: +++++++++++++++++++++ ADDED +++++++++++++++++++++")
//            Log.e(TAG, "onCreateView: +++++++++++++++++++++ ADDED +++++++++++++++++++++")
            playbackStateListener?.let { player?.addListener(it) }
        }
    }


    private fun processSwipe(x1: Float?, y1: Float?, x2: Float?, y2: Float?) {
        if (arrayOf(x1, y1, x2, y2).contains(null)) {
            return
        }
//        Log.e(TAG, "processSwipe: ==========================================")
        var distanceX = (x2!! - x1!!).toDouble()
        var distanceY = (y2!! - y1!!).toDouble()
        var angle = atan2(distanceY, distanceX)
        angle = Math.toDegrees(angle)
//        Log.e(TAG, "processSwipe: distanceX $distanceX")
//        Log.e(TAG, "processSwipe: distanceY $distanceY ")
//        Log.e(TAG, "processSwipe: angle: $angle")
//        Log.e(TAG, "processSwipe: abs(angle): ${abs(angle)}")

        if (abs(angle) < forward_zone_degrees && distanceX > slop_prevention) {
            skipForward()
        } else if (abs(angle) > rewind_zone_degrees && distanceX < (slop_prevention * -1)) {
            skipRewind()
        } else if (distanceY < (slop_prevention * -1)) {
            if (listener?.isPlaying() == true) {
                volumeUp()
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            }
        } else if (distanceY > slop_prevention) {
            if (listener?.isPlaying() == true) {
                volumeDown()
            }
        } else {
//            doVibrate()
            listener?.togglePlayPause()
        }
//        Log.e(TAG, "processSwipe: ==========================================")
    }

    private fun volumeUp() {
        doVibrate()
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    private fun volumeDown() {
        doVibrate()
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)

    }

    private fun doVibrate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.EFFECT_TICK));
        } else {
            //deprecated in API 26
            vibrator?.vibrate(80);
        }
    }

    fun setPlayer() {
//        Log.e(TAG, "setPlayer: ")
        if (listener?.isService() == true) {
//            Log.e(TAG, "setPlayer: SETTING PLAYER ")
            playerView?.player = listener?.getPlayer()
        }
        if (player == null) {
//            Log.e(TAG, "setPlayer: SETTING PLAYER AND IMPLEMETNIGN LISTERN ")
            player = listener?.getPlayer()
            playbackStateListener?.let { player?.addListener(it) }
        }
//        Log.e(TAG, "setPlayer: done")
    }

    fun skipForward() {
        doVibrate()
        listener?.skipForward()
    }

    fun skipRewind() {
        doVibrate()
        listener?.skipRewind()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //return super.onCreateOptionsMenu(menu)
        val frags = requireFragmentManager().fragments

        inflater.inflate(R.menu.menu_player, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settingsId -> {
//                Log.e(TAG, "baby: settings click")
                listener?.handleSettingsClick()
                return false
            }
            R.id.contactId -> {
                listener?.sendEmail()
            }
//            R.id.donateId -> {
//                listener?.plzDoante()
//            }
        }
        return false
    }

    inner class PlaybackStateListener:  Player.EventListener {
        override fun onPlayerStateChanged( playWhenReady: Boolean, playbackState: Int ) {
            val stateString: String
            stateString = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
//            Log.d(TAG, "changed state to " + stateString + " playWhenReady: " + playWhenReady)
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
//                super.onTracksChanged(trackGroups, trackSelections)
//            Log.e(TAG, "onTracksChanged: CHANGED")
                tvTitle.text = listener?.getSongTitle()
                tvArtist.text = listener?.getSongArtist()
        }
    }

}









