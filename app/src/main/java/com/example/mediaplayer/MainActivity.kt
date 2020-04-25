package com.example.mediaplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ServiceCompat.stopForeground
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.accessibility.AccessibilityViewCommand
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SampleStream
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.File

// Media streaming with ExoPlayer
// https://codelabs.developers.google.com/codelabs/exoplayer-intro/#2
// Notification
// https://dev.to/mgazar_/playing-local-and-remote-media-files-on-android-using-exoplayer-g3a
class MainActivity : AppCompatActivity() {

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

    companion object { const val TAG = "MainActivity" }


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlayerService.LocalBinder
            mService = binder.getService()
            mBound = true

            playerView = findViewById(R.id.video_view)
            playerView?.player = mService.exoPlayer
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
        }
    }

    private fun initializePlayer() {
//        playerView = findViewById(R.id.video_view)
        player = SimpleExoPlayer.Builder(this).build()
        var mp4VideoUri: Uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        var videoSource: MediaSource = buildMediaSource(mp4VideoUri)
//        playerView?.player = player

        playerView?.player = mService.exoPlayer
        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playBackPosition)
        player?.prepare(videoSource, false, false)

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

//        playerView = findViewById(R.id.video_view)
//        playerView?.player = mService.exoPlayer


        // https://stackoverflow.com/questions/23017767/communicate-with-foreground-service-android
        var intent: Intent = Intent(this, AudioPlayerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Util.startForegroundService(this, intent)
    }

    @SuppressLint("NewApi")
    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
        //    initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        //  hideSystemUi();
        if (Util.SDK_INT < Build.VERSION_CODES.N || player == null) {
       //     initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < Build.VERSION_CODES.N) {
     //       releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()

        unbindService(connection)
        mBound = false

        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
      //      releasePlayer()
        }
    }

    @SuppressLint("NewApi")
    fun onButtonClick(v: View){
        if (mBound) {
            val num = mService.randomNumber
            Toast.makeText(this,"num: $num", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "----0----")
            Log.e(TAG, filesDir.toString())
            Log.e(TAG, cacheDir.toString())
            Log.e(TAG, "----1----")
            Log.e(TAG, getExternalFilesDir(null).toString())
            Log.e(TAG, getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())
            Log.e(TAG, getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.list()?.joinToString())
            Log.e(TAG, "----2----")
            Log.e(TAG, externalMediaDirs.joinToString())
            Log.e(TAG, Environment.getDataDirectory().toString())
  //          var files: Array<String> = this.fileList()
            Log.e(TAG, "----3----")
//            Log.e(TAG, Environment.getExternalStorageState())
//            Log.e(TAG, Environment.getDownloadCacheDirectory().toString())


            val file = File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "albumName")
            val file2 = File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "albumName2")
            if (!file?.mkdirs()) {
                Log.e(TAG, "Directory not created")
            }
            if (!file2?.mkdirs()) {
                Log.e(TAG, "Directory2 not created")
            }


            queryShit()

        }

    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERM_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun queryShit() {
        //val musicList = mutableListOf<>()
        val projection = arrayOf(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        )

        val songUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        Log.e(TAG, "MediaStore.Audio.Media.CONTENT_TYPE")
        Log.e(TAG, MediaStore.Audio.Media.CONTENT_TYPE)
        Log.e(TAG, "MediaStore.Audio.Media.EXTERNAL_CONTENT_URI")
        Log.e(TAG, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())
        //val cR: ContentResolver = contentResolver
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "READ EXTENRSL NOT SWERAGNTED")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
               // poopy
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERM_REQUEST)
            }
        }

        val query = contentResolver.query(
            songUri,
            null,
            null,
            null,
        null
        )
        Log.e(TAG, "000000000000000000000000")

        query?.use {cursor ->
//            Log.e(TAG, "==========================")
//            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
//            val x = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
//            val x2 = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
//            val x3 = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
//            val x4 = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
//            val x5 = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
//            val x6 = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
////            val contentUri: Uri = ContentUris.withAppendedId(
////                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
////                id)
//            Log.e(TAG, idColumn.toString())
//            Log.e(TAG, x.toString())
//            Log.e(TAG, x2.toString())
//            Log.e(TAG, x3.toString())
//            Log.e(TAG, x4.toString())
//            Log.e(TAG, x5.toString())
//            Log.e(TAG, x6.toString())
            while (cursor.moveToNext())
                Log.e(TAG, "==========================")
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val x = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val x2 = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val x3 = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val x4 = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val x5 = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
            val x6 = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
//            val contentUri: Uri = ContentUris.withAppendedId(
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                id)
            Log.e(TAG, idColumn.toString())
            Log.e(TAG, x.toString())
            Log.e(TAG, x2.toString())
            Log.e(TAG, x3.toString())
            Log.e(TAG, x4.toString())
            Log.e(TAG, x5.toString())
            Log.e(TAG, x6.toString())
        }

    }


}
