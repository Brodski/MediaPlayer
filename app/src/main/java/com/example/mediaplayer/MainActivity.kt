package com.example.mediaplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ServiceCompat.stopForeground
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import java.io.File

// Media streaming with ExoPlayer
// https://codelabs.developers.google.com/codelabs/exoplayer-intro/#2
// Notification
// https://dev.to/mgazar_/playing-local-and-remote-media-files-on-android-using-exoplayer-g3a
class MainActivity : AppCompatActivity(), IMainActivity {

    data class AudioFile(val uri: Uri,
                         val title: String,
                         val artist: String?
    )
    private val MY_PERM_REQUEST = 1

    private var playerView: PlayerView? = null
    private var player: SimpleExoPlayer? = null

    private lateinit var mService: AudioPlayerService

    private var playWhenReady = true
    private var currentWindow = 0
    private var playBackPosition: Long = 0
    //private static appContext: Context

    companion object { const val TAG = "MainActivity" }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        var mp4VideoUri: Uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        //var videoSource: MediaSource = buildMediaSource(mp4VideoUri)

        playerView?.player = mService.exoPlayer
        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playBackPosition)
  //      player?.prepare(videoSource, false, false)
    }

    private fun releasePlayer() {
        if (player != null) {
            playWhenReady = player!!.playWhenReady
            Log.e("release,when ready", playWhenReady.toString())
            playBackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.release()
            player = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
   //     appContext = applicationContext

        Log.e(TAG,"CREATED MainActivity")

        mService = AudioPlayerService()
        Log.e(TAG,mService.randomNumber.toString())

        var bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener {onNavClick(it) }
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG,"START MainActivity")
        askPermissions()
     //   mService.queryWithPermissions(this)
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG,"RESUME MainActivity")
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG,"PAUSE MainActivity")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG,"STOP MainActivity")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG,"DESTROY MainActivity")
    }

    fun onButtonClick(v: View){
        val num = mService.randomNumber
        Toast.makeText(this,"num: $num", Toast.LENGTH_SHORT).show()
        Log.e(TAG,mService.randomNumber.toString())
    }


    fun continueBuildApp() {
        inflateFragment("fragment_player", "123!")
        //val audioList = queryActually()
        Log.e(TAG, "Is granted")
    }


    // Android's Request App Permissions - https://developer.android.com/training/permissions/requesting
    // How to Request a Run Time Permission - Android Studio Tutorial https://www.youtube.com/watch?v=SMrB97JuIoM
    fun askPermissions() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED) {
            // Has permissions
            continueBuildApp()
        } else {

            Log.e(TAG, "READ EXTERNAL NOT GRANTED")
            // shouldShowRequestPermissionRationale: false if disabled or "do not ask again"
            // if true, show a dialog that explains why we need permission. shows when user already denied it but trying again
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERM_REQUEST)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERM_REQUEST)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERM_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.e(TAG,":) Permission granted!")
                    continueBuildApp()
                } else {
                    var startMain = Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                    Log.e(TAG,":( Permission not granted.")
                }
                return
            }
        }
    }

    fun onNavClick(menuItem: MenuItem) : Boolean {
        var bool = false
        when (menuItem.itemId) {
            R.id.nav_home -> {
                val message = "hello from nav_home"
                //   mIMainActivity?.inflateFragment("fragment_a", message)
                inflateFragment("fragment_player", message)
                bool = true
            }
            R.id.nav_favorites -> {
                val message = "hello from nav_favorites"
                inflateFragment("fragment_songs", message)
                bool = true
            }
            R.id.nav_search -> {
                val message = "hello from nav_search"
                inflateFragment("fragment_songs", message)
                bool = true
            }
            else -> bool = false
        }
        return bool
    }

    override fun inflateFragment(fragmentTag: String, message: String) {
        if (fragmentTag == "fragment_songs") {
            //var fragment = SongsFragment()
            var fragment = SongsFragment.newInstance("pp1", "pp2")
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

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.newmain_view, fragment)
//          .addToBackStack()
            .commit()
    }

    fun myGoToFragment(v: View){
        //multi fragments
        var songsFragment: SongsFragment = SongsFragment.newInstance("pp1,", "pp2")
        var playerFragment: PlayerFragment = PlayerFragment.newInstance("pp1,", "pp2")
        // this object lets us put the fragment into the layout

//        supportFragmentManager
//            .beginTransaction()
//            .replace(R.id.container_b, songsFragment)
//            .replace(R.id.container_a, playerFragment)
//            //.replace(R.id.container_itmes, playerFragment)
//            //.addToBackStack(songsFragment.toString())
//            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//            .commit()
    }
}
