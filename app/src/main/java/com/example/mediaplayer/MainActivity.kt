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
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ServiceCompat.stopForeground
import androidx.core.content.ContextCompat
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
import java.io.File

// Media streaming with ExoPlayer
// https://codelabs.developers.google.com/codelabs/exoplayer-intro/#2
// Notification
// https://dev.to/mgazar_/playing-local-and-remote-media-files-on-android-using-exoplayer-g3a
class MainActivity : AppCompatActivity() {

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
//            Log.e(TAG, "----0----")
//            Log.e(TAG, filesDir.toString())
//            Log.e(TAG, cacheDir.toString())
//            Log.e(TAG, "----1----")
//            Log.e(TAG, getExternalFilesDir(null).toString())
//            Log.e(TAG, getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())
//            Log.e(TAG, getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.list()?.joinToString())
//            Log.e(TAG, "----2----")
//            Log.e(TAG, externalMediaDirs.joinToString())
//            Log.e(TAG, Environment.getDataDirectory().toString())
//  //          var files: Array<String> = this.fileList()
//            Log.e(TAG, "----3----")
//            val file = File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "albumName")
//            val file2 = File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "albumName2")
//            if (!file?.mkdirs()) { Log.e(TAG, "Directory not created") }
//            if (!file2?.mkdirs()) {Log.e(TAG, "Directory2 not created") }
            queryShit()
        }
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

    // Android's Request App Permissions - https://developer.android.com/training/permissions/requesting
    // How to Request a Run Time Permission - Android Studio Tutorial
    // https://www.youtube.com/watch?v=SMrB97JuIoM
    fun queryShit() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED) {
            doDankQuery()
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


    fun doDankQuery() {
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


        val query = contentResolver.query(
            songUri,
            null,
            selection,
            null,
            null
        )



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
    }


}
