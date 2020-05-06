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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
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
       // initPlayer2()
        Log.e(tag, "onstart Player frag")
//        playerView?.player = (activity as MainActivity).getPlayer()
//        playerView?.showController()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(tag, "onAttach Player frag")
        // mainactivity class must implement this
        listener = context as PlayerFragListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStop() {
        super.onStop()
    //    releasePlayer2()
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
        Util.startForegroundService(activity!!, intent)

    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        Log.e(TAG, "onCreateView Player Frag")
        var v: View = inflater.inflate(R.layout.fragment_player, container, false)

        playerView = v.findViewById(R.id.main_view2)
        listener!!.onPlayerSent(420);
        //playerView?.player = mService.exoPlayer
//        playerView?.player = (activity as MainActivity).getPlayer()
//        playerView?.showController()

        var bundle: Bundle = this.arguments!!
        if (bundle.containsKey(getString(R.string.song_bundle))){
            Log.e(TAG, "HAS SONG BUNDLE!!!")
            val s =bundle.getParcelable<Song>(getString(R.string.song_bundle))
            Log.e(TAG, s.toString())
        }
//        val wtf= bundle?.getString("keyOther2")
//        if (wtf != null) {
//           Log.e(TAG, "found keyOther2 in bundle")
//           Log.e(TAG, bundle?.getString("keyOther2").toString())
//        } else {
//            Log.e(TAG, "found jack shit")
//        }

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
        playerView?.player = mPlayer
        playerView?.showController()
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
}
//
//    override fun inflateFragment(fragmentTag: String, message: String) {
//        if (fragmentTag == "fragment_songs") {
//            var fragment = SongsFragment()
//            doFragmentTransaction(fragment, fragmentTag, message);
//        }
//        else if (fragmentTag == "fragment_player") {
//            //var fragment = PlayerFragment()
//            var fragment = PlayerFragment.newInstance("pp1,", "pp2")
//            doFragmentTransaction(fragment, fragmentTag, message);
//        }
//    }
//
//    fun doFragmentTransaction(fragment: Fragment, tag: String,  message: String){
//        var tag= "keyOther"
//        var bundle  = Bundle()
//        bundle.putString( tag, message)
//        fragment.arguments = bundle
//
//        fragmentManager
//            ?.beginTransaction()
//            ?.replace(R.id.newmain_view, fragment)
////          .addToBackStack()
//            ?.commit()
//    }
//
//
//}
/////////////////////////////////////////////////////////

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
////                return resources.getDrawable(id, context.getTheme());
////            } else {
////                return resources.getDrawable(id);
////            }

//btn.setOnClickListener {
//    Log.e(TAG, "FRAG OTHER CLICK")
//            var bundle: Bundle = Bundle()
//            bundle.putString("keyOther", " HI ")
//            var frag: ItemsFragment = ItemsFragment()
//            frag.arguments = bundle
//            fragmentManager
//                ?.beginTransaction()
//                ?.replace(R.id.container_a, frag)
//                ?.commit()