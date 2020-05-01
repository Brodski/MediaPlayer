package com.example.mediaplayer

import android.Manifest
import android.app.*
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Log
import java.io.File
import java.util.*

// Building feature-rich media apps with ExoPlayer (Google I/O '18)
// https://www.youtube.com/watch?v=svdq1BWl4r8
class AudioPlayerService : Service() {

    //https://medium.com/google-exoplayer/easy-audio-focus-with-exoplayer-a2dcbbe4640e
//        val audioAttributes = AudioAttributes.Builder()
//            .setUsage(C.USAGE_MEDIA)
//            .setContentType(C.CONTENT_TYPE_MOVIE)
//            .build()
//        exoPlayer!!.setAudioAttributes(audioAttributes, true)
    private val binder: IBinder? = LocalBinder()
    private var uri: Uri? = null
    private var episodeTitle: String? = null
    private var playerView: PlayerView? = null
    public var exoPlayer: SimpleExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null
    //private lateinit var concatenatingMediaSource: ConcatenatingMediaSource

    private val MY_PERM_REQUEST = 1

    private val PLAYBACK_CHANNEL_ID = "playback_channel"
    private val PLAYBACK_NOTIFICATION_ID = 1

    private val ARG_URI = "uri_string"
    private val ARG_TITLE = "title"
    private val ARG_START_POSITION = "start_position"
    private val MEDIA_SESSION_TAG = "hello-world-media"

    companion object {
        const val TAG = "AudioPlayerService"
    }

    // get() will be called everytime we access randomNumber
    val randomNumber: Int get() = Random().nextInt(100)

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }


    override fun onCreate() {
        super.onCreate()
        val context: Context = this
        exoPlayer = SimpleExoPlayer.Builder(this).build()

        val concatenatingMediaSource = buildMedia(context)


        exoPlayer!!.prepare(concatenatingMediaSource)
        exoPlayer!!.playWhenReady = false

        // Setup notification and media session.
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            PLAYBACK_CHANNEL_ID,
            R.string.playback_channel_name, //local name in settings dialog for the user
            PLAYBACK_NOTIFICATION_ID,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): String {
                    return player.currentWindowIndex.toString()
                    //return "title"
                }

                @Nullable
                override fun createCurrentContentIntent(player: Player): PendingIntent? = PendingIntent.getActivity(
                    applicationContext,
                    0,
                    Intent(applicationContext, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT)


                @Nullable
                override fun getCurrentContentText(player: Player): String? {
                    //player.
                    return  player.currentWindowIndex.toString()
                    //return  "text/desciption"
                }

                @Nullable
                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
                    //return getBitmapFromVectorDrawable(applicationContext, R.drawable.ic_queue_music)
                    return getBitmapFromVectorDrawable(context, R.drawable.ic_queue_music)
                }
            },
            object : PlayerNotificationManager.NotificationListener {
//                override fun onNotificationStarted(notificationId: Int, notification: Notification) {
//                    startForeground(notificationId, notification)
//                }
                override fun onNotificationCancelled(notificationId: Int) {
                  //  _playerStatusLiveData.value = PlayerStatus.Cancelled(episodeId)
                    stopSelf()
                }
                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                    if (ongoing) {
                        // Make sure the service will not get destroyed while playing media.
                        startForeground(notificationId, notification)
                    } else {
                        // Make notification cancellable.
                        stopForeground(false)
                    }
                }
            }
        )
        playerNotificationManager!!.setPlayer(exoPlayer)


        mediaSession = MediaSessionCompat(context, MEDIA_SESSION_TAG)
        mediaSession!!.isActive = true
        playerNotificationManager!!.setMediaSessionToken(mediaSession!!.sessionToken)
        mediaSessionConnector = MediaSessionConnector(mediaSession!!)
        mediaSessionConnector?.setQueueNavigator(object : TimelineQueueNavigator(mediaSession!!) {
            override fun getMediaDescription(player: Player, windowIndex: Int ): MediaDescriptionCompat {
                Log.e("AAAAY","BAM!")
//                var x0 = mediaSession
//                var x = concatenatingMediaSource.getMediaSource(windowIndex)
//                var x2 = mediaSession!!.controller.metadata.description
//                Log.e(TAG, "x2.mediaId.toString()")
//                Log.e(TAG, x2.mediaId.toString())
//                Log.e(TAG, x2.description.toString())
//                Log.e(TAG, x2.iconBitmap.toString())
//                Log.e(TAG, x2.iconUri.toString())
//                Log.e(TAG, x2.title .toString())
                //Log.e(TAG, x2.mediaId.toString())

                return mediaHelper(context, mediaSession!!.controller.metadata )
            }
        })
        mediaSessionConnector!!.setPlayer(exoPlayer)
    }



    fun buildMedia(context: Context ): ConcatenatingMediaSource {

        val audioUri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3")
        val mp4VideoUri: Uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        val mp4VideoUri2: Uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")

        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(context, Util.getUserAgent(context, this.getString(R.string.app_name)) )
        var concatenatingMediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()


        var mSongs = queryWithPermissions(context)

        Log.e(TAG, "HERE")
        var s: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mSongs!![0].uri)
        var s2: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mSongs!![1].uri)


        //val ms: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioUri)
//        val ms2: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri)
//        val ms3: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri2)



        concatenatingMediaSource.addMediaSource(s)
        concatenatingMediaSource.addMediaSource(s2)

        //concatenatingMediaSource.addMediaSource(ms)
//        concatenatingMediaSource.addMediaSource(ms2)
//        concatenatingMediaSource.addMediaSource(ms3)
        return concatenatingMediaSource
    }


    fun queryWithPermissions(context: Context): List<Song>? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED) {
        //    recycler_songs.adapter = SongsAdaptor(songList, this)
            Log.e(TAG, "Permission already granted")
            return queryActually(context)
        } else {
            Log.e(TAG, "Read Permission not granted")
            return null
        }
    }


    private fun queryActually(context: Context): MutableList<Song> {
        val songList = mutableListOf<Song>()

        val songUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.IS_ALARM,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            MediaStore.Audio.Media.IS_RINGTONE
        )

        val selection = "${MediaStore.Audio.Media.IS_ALARM} != 1 AND " +
                "${MediaStore.Audio.Media.IS_NOTIFICATION} != 1 AND " +
                "${MediaStore.Audio.Media.IS_RINGTONE} != 1"

        val query = applicationContext.contentResolver.query(songUri, projection, selection, null, null )

        query?.use { cursor ->
            Log.e(TAG, "000000000000000000000000")
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val isAlarmC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_ALARM)
            val isNotifC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_NOTIFICATION)
            val isRingC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE)
            while (cursor.moveToNext()) {
                Log.e(TAG, "+++++++++++++++++++++++++++")
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val isAlarmC = cursor.getString(isAlarmC)
                val isNotif = cursor.getString(isNotifC)
                val isRing = cursor.getString(isRingC)
                val audioUri: Uri = ContentUris.withAppendedId( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id )

                Log.e(TAG, "id $id")
                Log.e(TAG, "audioUri $audioUri")
                Log.e(TAG, "title $title")
                Log.e(TAG, "artist $artist")
                Log.e(TAG, "is isAlarmC $isAlarmC")
                Log.e(TAG, "is isNotif $isNotif")
                Log.e(TAG, "is isRing $isRing")
                songList.add( Song(uri = audioUri, mainText = title, subText = artist, imageResource = R.drawable.ic_search_black_24dp))
            }
        }
        Log.e(TAG, "SongList")
        songList.forEach { Log.e(TAG, it.toString()) }
        return songList
    }





    private fun mediaHelper(context: Context,  metaData: MediaMetadataCompat): MediaDescriptionCompat {
        var extras: Bundle? = null
        var bitmap: Bitmap? = getBitmapFromVectorDrawable(context, R.drawable.ic_queue_music)
        extras?.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
        extras?.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)

        return MediaDescriptionCompat.Builder()
            .setMediaId("123")
            .setIconBitmap(bitmap)
            .setTitle("DANK TITLE!")
            .setDescription("REAL DESCRIP")
            .setExtras(extras)
            .build()

    }

    // https://dev.to/mgazar_/playing-local-and-remote-media-files-on-android-using-exoplayer-g3a
    @MainThread
    private fun getBitmapFromVectorDrawable(context: Context, @DrawableRes drawableId: Int): Bitmap? {
        return ContextCompat.getDrawable(context, drawableId)?.let {
            val drawable = DrawableCompat.wrap(it).mutate()
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        }
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSessionConnector?.setPlayer(null)
        playerNotificationManager?.setPlayer(null)
        exoPlayer!!.release()
        exoPlayer = null

        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.e(TAG, "TASK REMOVED")

        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }







}

//        val readOnlyMode = "r"
//        applicationContext.contentResolver.openFileDescriptor(uriList[0], readOnlyMode).use { pfd ->
//            Log.e(TAG, "ah shit")
//        }





//private fun buildMediaSource(uri: Uri): MediaSource {
//    val dataSourceFactory: DataSource.Factory =
//        DefaultDataSourceFactory(this, Util.getUserAgent(this, this.getString(R.string.app_name)) )
//
//    val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
//    val audioUri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3")
//
//    val mediaSource2: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioUri)
//    val cs = ConcatenatingMediaSource()
//    cs.addMediaSource(videoSource)
//    cs.addMediaSource(mediaSource2)
//    return cs
//}