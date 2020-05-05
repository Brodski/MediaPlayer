package com.example.mediaplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomnavigation.BottomNavigationView

// Media streaming with ExoPlayer
// https://codelabs.developers.google.com/codelabs/exoplayer-intro/#2
// Notification
// https://dev.to/mgazar_/playing-local-and-remote-media-files-on-android-using-exoplayer-g3a
class MainActivity : AppCompatActivity(), IMainActivity, SongsFragment.SongsFragListener {

    private val MY_PERM_REQUEST = 1

    private var playerView: PlayerView? = null
    private var player: SimpleExoPlayer? = null

    private  var someInt: Int = 0

    private lateinit var mService: AudioPlayerService

    private var playWhenReady = true
    private var currentWindow = 0
    private var playBackPosition: Long = 0

    companion object { const val TAG = "MainActivity" }

    private lateinit var songsFragment: SongsFragment
    private lateinit var playerFragment: PlayerFragment

    private var restoredFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e(TAG,"CREATED MainActivity")

        if (savedInstanceState != null && savedInstanceState.containsKey("currentFragment")) {
            restoredFragment = supportFragmentManager.getFragment(savedInstanceState, "currentFragment")
            Log.e(TAG, "++++++++++++++++++++++++onCreate bundle Frag $restoredFragment++++++++++++++++++++++++")
        } else {
            Log.e(TAG, "---------------------------onCreate no bundlde---------------------------")
        }

        songsFragment = SongsFragment.newInstance("pp1", "pp2")
        playerFragment = PlayerFragment.newInstance("pp1", "pp2")
        mService = AudioPlayerService()

        var bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener { onNavClick(it) }
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG,"START MainActivity")
        askPermissions()

        Log.e(TAG,"START 2 MainActivity")
        Log.e(TAG, someInt.toString())
     //   mService.queryWithPermissions(this)
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG,"RESUME MainActivity")
        Log.e(TAG, someInt.toString())
        if (restoredFragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.newmain_view, restoredFragment!!, restoredFragment!!.tag)
                .commit()
        }
    }

    fun saveThingy() {
        var frag: Fragment? = null
        Log.e(TAG, "Found fragment: " + supportFragmentManager.backStackEntryCount.toString())
        for (entry: Int in 0 until supportFragmentManager.backStackEntryCount) {
            val ff = supportFragmentManager.getBackStackEntryAt(entry)
            Log.e(TAG, "Found fragment: " + supportFragmentManager.getBackStackEntryAt(entry).id)
            Log.e(TAG, "Found fragment: " + supportFragmentManager.getBackStackEntryAt(entry).name)
        }

    }


    override fun onPause() {
        super.onPause()
        Log.e(TAG,"PAUSE MainActivity")

        //someInt = 666
        if (Util.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "PAUSE, int it")
            //someInt = 666
        }
        Log.e(TAG, "PAUSE, saving")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG,"STOP MainActivity")
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
            Log.e(TAG,"STOP int int it")
            //someInt = 666
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG,"DESTROY MainActivity")
    }

    fun continueBuildApp() {
        inflateFragment("fragment_player")
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
                inflateFragment("fragment_player")
                bool = true
            }
            R.id.nav_favorites -> {
                inflateFragment("fragment_songs")
                bool = true
            }
            R.id.nav_search -> {
                inflateFragment("fragment_songs")
                bool = true
            }
            else -> bool = false
        }
        return bool
    }

    override fun inflateFragment(nameFrag: String) {

        if (nameFrag == "fragment_songs") {
            doFragmentTransaction(songsFragment, getString(R.string.song_frag_tag))
        }
        else if (nameFrag == "fragment_player") {
            doFragmentTransaction(playerFragment, getString(R.string.player_frag_tag))
        }
    }


    fun doFragmentTransaction(fragment: Fragment, tag: String){

        var frag: Fragment? = null
        for ( f in supportFragmentManager.fragments) {
            frag = f
//            Log.e(TAG, "Fragment :")
//            Log.e(TAG, f.toString())
//            Log.e(TAG, f.tag)
//            Log.e(TAG, f.id.toString())
        }
        Log.e(TAG, tag)
        Log.e(TAG, frag?.tag.toString())
        if (frag?.tag == tag) {
            Log.e(TAG, "is equal")
            return
        } else if ( frag == null ){
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.newmain_view, fragment, tag)
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                .addToBackStack(tag)
                //.add(R.id.newmain_view, fragment, tag)
                .replace(R.id.newmain_view, fragment, tag)
                .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("num", 6565);
        Log.e(TAG,"onSaveInstanceState")

        // only 1 fragment present
        var frag: Fragment? = null
        for ( f in supportFragmentManager.fragments) {
            frag = f
        }
        if (frag != null) {
            supportFragmentManager.putFragment(outState, "currentFragment", frag)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.e(TAG,"onRestoreInstanceState")
        Log.e(TAG,"onRestoreInstanceState")
        Log.e(TAG,"onRestoreInstanceState")
        someInt = savedInstanceState.getInt("num")

        if (savedInstanceState != null && savedInstanceState.containsKey("currentFragment") && restoredFragment == null ) {
            restoredFragment = supportFragmentManager.getFragment(savedInstanceState, "currentFragment")
            Log.e(TAG, "--__--_-__-__-__-onCreate bundle Frag $restoredFragment--__--_-__-__-__-")
        } else {
            Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>onCreate no bundlde<<<<<<<<<<<<<<<<<}")
        }


    }

    override fun onSongSelect() {

    }







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

}







