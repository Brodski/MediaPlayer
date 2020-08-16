package com.bskimusicplayer.mediaplayer

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
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.abs
import kotlin.math.atan2

class PlayerFragment : Fragment() {

    private var playerView: PlayerView? = null
    private var player: SimpleExoPlayer? = null
    private var listener: PlayerFragListener? = null
    private var skipIncrement: Int = 10000
    private var playbackStateListener: PlaybackStateListener? = null

    private lateinit var audioManager: AudioManager
    private lateinit var mDetector: GestureDetector
    var isKeepScreenOn = false
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
        super.onStart()
        setPlayer()
        getTitleStuff()
        playerView?.showController()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as PlayerFragListener
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: ")
    }
    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: ")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playbackStateListener?.let { player?.removeListener(it) }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        isKeepScreenOn = sharedPreferences.getBoolean(resources.getString(R.string.save_state_screen), false)

        if (isKeepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        forward_zone_degrees = sharedPreferences.getInt(resources.getString(R.string.save_state_skip_zone), 35)
        rewind_zone_degrees = 180 - forward_zone_degrees
        playerView = v.findViewById(R.id.main_view2)
        playerView?.setRewindIncrementMs(skipIncrement)
        playerView?.setFastForwardIncrementMs(skipIncrement)

        player = listener?.getPlayer()
        getTitleStuff()
        playbackStateListener = PlaybackStateListener()
        mDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onShowPress(e: MotionEvent?) {
            }
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                listener?.togglePlayPause()
                return true
            }
            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }
            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                processSwipe(e1?.x, e1?.y, e2?.x, e2?.y)
                return true
            }
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
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
            playbackStateListener?.let { player?.addListener(it) }
        }
    }


    private fun processSwipe(x1: Float?, y1: Float?, x2: Float?, y2: Float?) {
        if (arrayOf(x1, y1, x2, y2).contains(null)) {
            return
        }
        var distanceX = (x2!! - x1!!).toDouble()
        var distanceY = (y2!! - y1!!).toDouble()
        var angle = atan2(distanceY, distanceX)
        angle = Math.toDegrees(angle)

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
            listener?.togglePlayPause()
        }
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
        if (listener?.isService() == true) {
            playerView?.player = listener?.getPlayer()
        }
        if (player == null) {
            player = listener?.getPlayer()
            playbackStateListener?.let { player?.addListener(it) }
        }
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
        inflater.inflate(R.menu.menu_player, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settingsId -> {
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
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
                tvTitle.text = listener?.getSongTitle()
                tvArtist.text = listener?.getSongArtist()
        }
    }
}