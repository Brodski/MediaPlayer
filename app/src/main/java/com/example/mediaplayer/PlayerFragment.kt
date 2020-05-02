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
class PlayerFragment : Fragment(), IMainActivity {

    private lateinit var playerControls: PlayerControlView
    private var playerView: PlayerView? = null
    private var player: SimpleExoPlayer? = null
    private lateinit var textView: TextView
    private lateinit var mService: AudioPlayerService
    private var mBound: Boolean = false


    //https://www.freeiconspng.com/downloadimg/34238
    private val connection = object : ServiceConnection {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e(TAG, "CONNECTED SERVICE")

            val binder = service as AudioPlayerService.LocalBinder
            mService = binder.getService()
            mBound = true

            playerView?.player = mService.exoPlayer
            playerView?.showController()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e(TAG, "DISCONNECTED SERVICE")
            mBound = false
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(TAG,"Attach Player Frag")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG,"Creat Player Frag")
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG,"onStart Player Frag")
        initPlayer2()
    }

    override fun onDetach() {
        super.onDetach()
        Log.e(TAG,"Detach Player Frag")
    }

    override fun onDestroyView(){
        super.onDestroyView()
        Log.e(TAG,"Destroyed Player Frag")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG,"onStop Player Frag")
        releasePlayer2()
    }

    private fun releasePlayer2() {
        Log.e(TAG,"Release called")
        var intent: Intent = Intent(activity, AudioPlayerService::class.java)
        activity?.unbindService(connection)
        mBound = false
    }

    private fun initPlayer2(){
        // Google' Building feature-rich media apps with ExoPlayer - https://www.youtube.com/watch?v=svdq1BWl4r8
        // https://stackoverflow.com/questions/23017767/communicate-with-foreground-service-android
        var intent: Intent = Intent(activity, AudioPlayerService::class.java)
        activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Util.startForegroundService(activity!!, intent)

    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        Log.e(TAG, "onCreateView Player Frag")
        // Inflate the layout for this fragment
        var v: View = inflater.inflate(R.layout.fragment_player, container, false)
        textView = v.findViewById(R.id.fragTextIdB)

        playerView =  v.findViewById(R.id.main_view2)
        //playerView?.controllerHideOnTouch = false
        //playerView?.controllerShowTimeoutMs = 0


        var bundle: Bundle = this.arguments!!
        var x: String = textView.text.toString()
        if (bundle.containsKey("keyOther")) {
            x += bundle?.getString("keyOther").toString()
        }
        textView.text = x

        val btn:Button = v.findViewById(R.id.btnB) as Button
        btn.setOnClickListener { v -> doSomething(v)  }
        return v

//        var v2: View = inflater.inflate(R.layout.controls_playback, container, false)
//        var cnt = v2.findViewById(R.id.controllerId)

    }

    companion object {
        const val TAG = "PlayerFragment"
        @JvmStatic
        fun newInstance(param1: String, param2: String) : PlayerFragment {
            val playerFragment = PlayerFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            playerFragment.arguments = args
            return playerFragment
        }
    }

    fun doSomething(v: View) {
        Log.e(TAG, "Clicked in Player Fragment")
        Log.e(TAG, mService.exoPlayer!!.currentWindowIndex.toString() )
        Log.e(TAG, mService.exoPlayer!!.currentPeriodIndex.toString())
        Log.e(TAG, mService.exoPlayer!!.toString())

    }

    override fun inflateFragment(fragmentTag: String, message: String) {
        if (fragmentTag == "fragment_songs") {
            var fragment = SongsFragment()
            doFragmentTransaction(fragment, fragmentTag, message);
        }
        else if (fragmentTag == "fragment_player") {
            //var fragment = PlayerFragment()
            var fragment = PlayerFragment.newInstance("pp1,", "pp2")
            doFragmentTransaction(fragment, fragmentTag, message);
        }
    }

    fun doFragmentTransaction(fragment: Fragment, tag: String,  message: String){
        var tag= "keyOther"
        var bundle  = Bundle()
        bundle.putString( tag, message)
        fragment.arguments = bundle

        fragmentManager
            ?.beginTransaction()
            ?.replace(R.id.newmain_view, fragment)
//          .addToBackStack()
            ?.commit()
    }


}

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