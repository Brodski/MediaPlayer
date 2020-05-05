package com.example.mediaplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.*
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
class MainActivity : AppCompatActivity(), IMainActivity, SongsFragment.SongsFragListener {

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

    companion object { const val TAG = "MainActivity" }

    private lateinit var songsFragment: SongsFragment
    private lateinit var playerFragment: PlayerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e(TAG,"CREATED MainActivity")

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
     //   mService.queryWithPermissions(this)
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG,"RESUME MainActivity")
    }

    fun saveThingy() {
        var frag: Fragment? = null
        Log.e(TAG, "Found fragment: " + supportFragmentManager.backStackEntryCount.toString())
        for (entry: Int in 0 until supportFragmentManager.backStackEntryCount) {
            val ff = supportFragmentManager.getBackStackEntryAt(entry)
            Log.e(TAG, "Found fragment: " + supportFragmentManager.getBackStackEntryAt(entry).id)
            Log.e(TAG, "Found fragment: " + supportFragmentManager.getBackStackEntryAt(entry).name)
        }

        if (frag != null) {
            Log.e(TAG, "FRAG AINT NULL")
        } else {
            Log.e(TAG, "FRAG NULL")

        }
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

        var frag: Fragment? = null
        Log.e(TAG, "Backstack count: " + supportFragmentManager.backStackEntryCount.toString())




        var frag2: Fragment? = null
//        when (nameFrag) {
//            getString(R.string.player_frag_tag) -> frag2 = supportFragmentManager.findFragmentByTag(getString(R.string.player_frag_tag))
//            getString(R.string.song_frag_tag) -> frag2 = supportFragmentManager.findFragmentByTag(getString(R.string.song_frag_tag))
//        }
//        if (frag2 != null){
//            Log.e(TAG,"FRAG 2 AINT NULL")
//        } else {
//            Log.e(TAG,"FRAG 2 NULL")
//            Log.e(TAG, frag2.toString())
//        }


        if (nameFrag == "fragment_songs") {
            //var fragment: SongsFragment = SongsFragment.newInstance("pp1", "pp2")
            //doFragmentTransaction(fragment, getString(R.string.song_frag_tag))
            doFragmentTransaction(songsFragment, getString(R.string.song_frag_tag))
        }
        else if (nameFrag == "fragment_player") {
            //var fragment: PlayerFragment = PlayerFragment.newInstance("pp1,", "pp2")
            doFragmentTransaction(playerFragment, getString(R.string.player_frag_tag))
        }
    }


    fun doFragmentTransaction(fragment: Fragment, tag: String){

//        supportFragmentManager.beginTransaction().attach(playerFragment)
        var frag: Fragment? = null
        for ( f in supportFragmentManager.fragments) {
            frag = f
            Log.e(TAG, "Fragment :")
            Log.e(TAG, f.toString())
            Log.e(TAG, f.tag)
            Log.e(TAG, f.id.toString())
        }
        Log.e(TAG, tag)
        Log.e(TAG, frag?.tag.toString())
        if ( frag == null ){
            supportFragmentManager
                .beginTransaction()
                //.addToBackStack(tag)
                //.add(R.id.newmain_view, fragment, tag)
                .replace(R.id.newmain_view, fragment, tag)
                .commit()
        } else if (frag?.tag == tag) {
            Log.e(TAG, "is equal")
            return
        } else {
            supportFragmentManager
                .beginTransaction()
                .addToBackStack(tag)
                //.add(R.id.newmain_view, fragment, tag)
                .replace(R.id.newmain_view, fragment, tag)
                .commit()
        }
//        supportFragmentManager
//            .beginTransaction()
//    //        .addToBackStack(tag)
//            .add(R.id.newmain_view, fragment, tag)
//            //.replace(R.id.newmain_view, fragment)
//            .commit()
    }


    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.e(TAG, "MainActivity, saving")
        Log.e(TAG, "MainActivity, saving")
        Log.e(TAG, "MainActivity, saving")
        Log.e(TAG, "MainActivity, saving")
        //supportFragmentManager.putFragment((outState, "myFragments", ))

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







