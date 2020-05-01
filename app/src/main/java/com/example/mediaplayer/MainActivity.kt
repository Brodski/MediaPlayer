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
    private var mBound: Boolean = false

    private val context: Context = this

    private var playerNotificationManager: PlayerNotificationManager? = null
    private val CHANNEL_ID = "69 channel"
    private val NOTIFICATION_ID = 420
    private var playWhenReady = true
    private var currentWindow = 0
    private var playBackPosition: Long = 0

    //lateinit var mIMainActivity: IMainActivity

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
        Log.e(TAG,"CREATED MainActivity")

        mService = AudioPlayerService()
        Log.e(TAG,mService.randomNumber.toString())

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
        askPermissions()
        //var cs = buildMedia(context)
        //Log.e(TAG, cs.toString())
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
        val num = mService.randomNumber
        Toast.makeText(this,"num: $num", Toast.LENGTH_SHORT).show()
        Log.e(TAG,mService.randomNumber.toString())
    }


    fun continueBuildApp() {
        inflateFragment("fragment_player", "123!")
        //val audioList = queryActually()
        Log.e(TAG, "Is granted")
    }


    fun queryActually(): MutableList<AudioFile> {
        val audioList = mutableListOf<AudioFile>()

        val songUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = "${MediaStore.Audio.Media.IS_ALARM} != 1 AND " +
            "${MediaStore.Audio.Media.IS_NOTIFICATION} != 1 AND " +
            "${MediaStore.Audio.Media.IS_RINGTONE} != 1"

        val query = applicationContext.contentResolver.query(songUri, null, selection, null, null )

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
            val dispCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                Log.e(TAG, "+++++++++++++++++++++++++++")
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
                val disp = cursor.getString(dispCol)
                val audioUri: Uri = ContentUris.withAppendedId( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id )

                //applicationContext.contentResolver.openInputStream(audioUri)
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
                Log.e(TAG, "disp $disp")
                audioList.add( AudioFile(audioUri, title, artist))
            }
        }
        Log.e(TAG, "audioList")
        audioList.forEach { Log.e(TAG, it.toString()) }
        return audioList
    }

    // Android's Request App Permissions - https://developer.android.com/training/permissions/requesting
    // How to Request a Run Time Permission - Android Studio Tutorial
    // https://www.youtube.com/watch?v=SMrB97JuIoM
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
                //this?
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERM_REQUEST)
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERM_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this,":) Permission granted!", Toast.LENGTH_SHORT).show()
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


    fun buildMedia(context: Context ): ConcatenatingMediaSource {

        val audioUri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3")
        val mp4VideoUri: Uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        val mp4VideoUri2: Uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(context, Util.getUserAgent(context, this.getString(R.string.app_name)) )
        var concatenatingMediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()


        var mSongs = queryActually()

        Log.e(TAG, "HERE")
        var s: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mSongs!![0].uri)
//        var s: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse("sleeps in the poop"))
        var s2: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mSongs!![1].uri)

        Log.e(TAG, mSongs[0].uri.toString())
        Log.e(TAG, mSongs[1].uri.toString())
        Log.e(TAG, audioUri.toString())
        Log.e(TAG, Uri.parse("sleeps in the poop").toString())
        Log.e(TAG, s.toString())
        if (s is MediaSource){
            Log.e(TAG, "IT IS IS!!")
        }
        val uri = mSongs[0].uri
        Log.e(TAG, uri?.getScheme().toString())
        //var proj: Array<String> = arrayOf(MediaStore.Audio.Media.DATA.toString() )
        val proj: Array<String> = arrayOf(MediaStore.Audio.Media._ID )
        if (uri != null && "content".equals(uri.getScheme())) {
            val cursor: Cursor? = context.contentResolver.query(uri,  null, null,null, null);
            if ( cursor!!.moveToNext()) {
                Log.e(TAG,"YES")
            } else {
                Log.e(TAG,"NOPE")
            }
            while (cursor!!.moveToNext()) {
                Log.e(TAG, "-0-0-0--")
                val col = cursor!!.getColumnIndex(MediaStore.Audio.Media._ID)
                val id = cursor?.getLong(col)
                val uriImage =Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id)
                Log.e(TAG, uriImage.toString())
            }
            cursor?.close();
        }
//        else {
//            filePath = _uri.getPath();
//        }
//        Log.d("","Chosen path = "+ filePath);

        //concatenatingMediaSource.addMediaSource(s)
        concatenatingMediaSource.addMediaSource(s2)

        val ms: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioUri)
        val ms2: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri)
        val ms3: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri2)

//        concatenatingMediaSource.addMediaSource(ms)
//        concatenatingMediaSource.addMediaSource(ms2)
//        concatenatingMediaSource.addMediaSource(ms3)
        return concatenatingMediaSource
    }
}
