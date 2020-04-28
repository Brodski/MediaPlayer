package com.example.mediaplayer

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.C
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
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Log
import java.text.SimpleDateFormat
import java.util.*

// Building feature-rich media apps with ExoPlayer (Google I/O '18)
// https://www.youtube.com/watch?v=svdq1BWl4r8
class AudioPlayerService : Service() {

    private val binder: IBinder? = LocalBinder()

    private var episodeTitle: String? = null
    private var playerView: PlayerView? = null
    public var exoPlayer: SimpleExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    private val PLAYBACK_CHANNEL_ID = "playback_channel"
    private val PLAYBACK_NOTIFICATION_ID = 1

    private val ARG_URI = "uri_string"
    private val ARG_TITLE = "title"
    private val ARG_START_POSITION = "start_position"
    private val MEDIA_SESSION_TAG = "hello-world-media"

    companion object {
        const val TAG = "AudioPlayerService"
    }

    override fun onCreate() {
        super.onCreate()
        val context: Context = this
        exoPlayer = SimpleExoPlayer.Builder(this).build()

        //https://medium.com/google-exoplayer/easy-audio-focus-with-exoplayer-a2dcbbe4640e
//        val audioAttributes = AudioAttributes.Builder()
//            .setUsage(C.USAGE_MEDIA)
//            .setContentType(C.CONTENT_TYPE_MOVIE)
//            .build()
//        exoPlayer!!.setAudioAttributes(audioAttributes, true)

        val audioUri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3")
        val mp4VideoUri: Uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(context, Util.getUserAgent(context, this.getString(R.string.app_name)) )

        var concatenatingMediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()

        val ms: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioUri)
        val ms2: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri)
        concatenatingMediaSource.addMediaSource(ms)
        concatenatingMediaSource.addMediaSource(ms2)

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
                    //return player.currentWindowIndex.toString() + "title"
                    return "title"
                }

                @Nullable
                override fun createCurrentContentIntent(player: Player): PendingIntent? = PendingIntent.getActivity(
                    applicationContext,
                    0,
                    Intent(applicationContext, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT)


                @Nullable
                override fun getCurrentContentText(player: Player): String? {
                    // return  player.currentWindowIndex.toString() + "text/desciption"
                    return  "text/desciption"
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
                    return mediaHelper(context, null)
                }
            })
        mediaSessionConnector!!.setPlayer(exoPlayer)

//        //playerNotificationManager.setPlayer(exoPlayer)
//        playerNotificationManager?.setMediaSessionToken(mediaSession!!.sessionToken)

    }

    private fun mediaHelper(context: Context, someshit: Unit?): MediaDescriptionCompat {
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

    // https://stackoverflow.com/questions/19568315/how-to-handle-code-when-app-is-killed-by-swiping-in-android/26882533#26882533
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.e(TAG, "TASK REMOVED")

        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //return super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }


    // get() will be called everytime we access randomNumber
   val randomNumber: Int get() = Random().nextInt(100)

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

}