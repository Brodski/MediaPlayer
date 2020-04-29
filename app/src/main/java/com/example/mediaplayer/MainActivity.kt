package com.example.mediaplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
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


    private var playerView2: PlayerView? = null
    private var player2: SimpleExoPlayer? = null

    private lateinit var mService: AudioPlayerService
    private var mBound: Boolean = false

    private val context: Context = this

    private var playerNotificationManager: PlayerNotificationManager? = null
    private val CHANNEL_ID = "69 channel"
    private val NOTIFICATION_ID = 420
    private var playWhenReady = true
    private var currentWindow = 0
    private var playBackPosition: Long = 0


    lateinit var fragmentContainer: FrameLayout
    lateinit var btnFrag: Button
    //lateinit var mIMainActivity: IMainActivity

    companion object { const val TAG = "MainActivity" }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e(TAG, "CONNECTED SERVICE")
            val binder = service as AudioPlayerService.LocalBinder
            mService = binder.getService()
            mBound = true

//            playerView = findViewById(R.id.main_view)
//            playerView?.player = mService.exoPlayer

//            playerView2 = findViewById(R.id.main_view2)
//            playerView2?.player = mService.exoPlayer
            inflateFragment("fragment_player", "123!")
        }

//        override fun onServiceDisconnected(arg0: ComponentName?) {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e(TAG, "DISCONNECTED SERVICE")
            mBound = false
        }
    }

    private fun initPlayer2(){
        // Google' Building feature-rich media apps with ExoPlayer - https://www.youtube.com/watch?v=svdq1BWl4r8
        // https://stackoverflow.com/questions/23017767/communicate-with-foreground-service-android
        var intent: Intent = Intent(this, AudioPlayerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Util.startForegroundService(this, intent)

    }

    private fun releasePlayer2() {
        Log.e(TAG,"Release called")
        var intent: Intent = Intent(this, AudioPlayerService::class.java)
        //stopService(intent)
        unbindService(connection)
        mBound = false
    }

    private fun initializePlayer() {
//        playerView = findViewById(R.id.main_view)
        player = SimpleExoPlayer.Builder(this).build()
        var mp4VideoUri: Uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        var videoSource: MediaSource = buildMediaSource(mp4VideoUri)
//        playerView?.player = player

        playerView?.player = mService.exoPlayer
        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playBackPosition)
        player?.prepare(videoSource, false, false)
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

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(this, Util.getUserAgent(this, this.getString(R.string.app_name)) )

        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        val audioUri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3")

        val mediaSource2: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioUri)
        val cs = ConcatenatingMediaSource()
        cs.addMediaSource(videoSource)
        cs.addMediaSource(mediaSource2)
        return cs
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e(TAG,"CREATED MainActivity")

        var bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener {onNavClick(it) }
    }


    override fun onStart() {
        super.onStart()
        Log.e(TAG,"START MainActivity")
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
        //    initializePlayer()
//            initPlayer2()
        }
        inflateFragment("fragment_player", "123!")
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG,"RESUME MainActivity")
        if (Util.SDK_INT < Build.VERSION_CODES.N || player == null) {
       //     initializePlayer()
 //           initPlayer2()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG,"PAUSE MainActivity")
        if (Util.SDK_INT < Build.VERSION_CODES.N) {
     //       releasePlayer()
     //       releasePlayer2()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG,"STOP MainActivity")
//        unbindService(connection)
//        mBound = false

        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
      //      releasePlayer()
      //      releasePlayer2()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.e(TAG,"DESTROY MainActivity")
    }

    fun onButtonClick(v: View){
        if (mBound) {
            val num = mService.randomNumber
            Toast.makeText(this,"num: $num", Toast.LENGTH_SHORT).show()
            queryWithPermissions()
        }
    }


    // Android's Request App Permissions - https://developer.android.com/training/permissions/requesting
    // How to Request a Run Time Permission - Android Studio Tutorial
    // https://www.youtube.com/watch?v=SMrB97JuIoM
    fun queryWithPermissions() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED) {
            val audioList = queryActually()
            Log.e(TAG, "Already granted")
            Toast.makeText(this, "Already granted", Toast.LENGTH_SHORT).show()
        } else {

            Log.e(TAG, "READ EXTERNAL NOT GRANTED")
            Toast.makeText(this, "READ EXTERNAL NOT GRANTED", Toast.LENGTH_SHORT).show()
            //requestStoragePermission()
            // if true, show a dialog that explains why we need permission. shows when user already denied it but trying again
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(this)
                    .setTitle("Read External Storage permission required")
                    .setMessage("Allows read access audio & video files")
                    .setPositiveButton("Agree", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERM_REQUEST)
                        }
                    })
                    .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            dialog?.dismiss()
                        }
                    })
                    .create().show()

            } else {
                //this?
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERM_REQUEST)
            }
        }
    }


    fun queryActually(): MutableList<AudioFile> {
        val audioList = mutableListOf<AudioFile>()

//        var projection: Array<String> = arrayOf (
//            MediaStore.Audio.Media._ID,
//            MediaStore.Audio.Media.ARTIST,
//            MediaStore.Audio.Media.TITLE,
//            MediaStore.Audio.Media.DISPLAY_NAME,
//            MediaStore.Audio.Media.DURATION
//        )

        val songUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = "${MediaStore.Audio.Media.IS_ALARM} != 1 AND " +
            "${MediaStore.Audio.Media.IS_NOTIFICATION} != 1 AND " +
            "${MediaStore.Audio.Media.IS_RINGTONE} != 1"

        val query = contentResolver.query(songUri, null, selection, null, null )

        query?.use { cursor ->
            Log.e(TAG, "000000000000000000000000")
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val dateAddedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val mimeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
            val isMusicC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
            val isAlarmC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_ALARM)
            val isNotifC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_NOTIFICATION)
            val isPodC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_PODCAST)
            val isRingC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE)
            while (cursor.moveToNext()) {
                Log.e(TAG, "==========================")
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val album = cursor.getString(albumColumn)
                val artist = cursor.getString(artistColumn)
                val dateAdded = cursor.getString(dateAddedColumn)
                val mime = cursor.getString(mimeColumn)
                val isMusic = cursor.getString(isMusicC)
                val isAlarmC = cursor.getString(isAlarmC)
                val isNotif = cursor.getString(isNotifC)
                val isPod = cursor.getString(isPodC)
                val isRing = cursor.getString(isRingC)
                val audioUri: Uri = ContentUris.withAppendedId( MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id )
              //  Log.e(TAG, contentUri.toString())
                Log.e(TAG, "id $id")
                Log.e(TAG, "audioUri $audioUri")
                Log.e(TAG, "title $title")
                Log.e(TAG, "album $album")
                Log.e(TAG, "artist $artist")
                Log.e(TAG, "mime $mime")
                Log.e(TAG, "is isMusic $isMusic")
                Log.e(TAG, "is isAlarmC $isAlarmC")
                Log.e(TAG, "is isNotif $isNotif")
                Log.e(TAG, "is isPod $isPod")
                Log.e(TAG, "is isRing $isRing")
                audioList.add( AudioFile(audioUri, title, artist))
            }
        }
        Log.e(TAG, "audioList")
        audioList.forEach { Log.e(TAG, it.toString()) }
        return audioList
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERM_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this,":) Permission granted!", Toast.LENGTH_SHORT).show()
                    Log.e(TAG,":) Permission granted!")
                } else {
                    Toast.makeText(this,":( Permission not granted.", Toast.LENGTH_SHORT).show()
                    Log.e(TAG,":( Permission not granted.")
                }
                return
            }
            // Add other 'when' lines to check for other  permissions this app might request.
            else -> {
                // Ignore all other requests.
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

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_view, fragment)
//          .addToBackStack()
            .commit()
    }

    fun myGoToFragment(v: View){
        //multi fragments
        var songsFragment: SongsFragment = SongsFragment.newInstance("pp1,", "pp2")
        var playerFragment: PlayerFragment = PlayerFragment.newInstance("pp1,", "pp2")
        // this object lets us put the fragment into the layout

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_b, songsFragment)
            .replace(R.id.container_a, playerFragment)
            //.replace(R.id.container_itmes, playerFragment)
            //.addToBackStack(songsFragment.toString())
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
}
