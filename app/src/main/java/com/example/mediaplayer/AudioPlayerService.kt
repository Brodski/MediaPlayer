package com.example.mediaplayer

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Binder
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import kotlin.math.log

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

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    //  val randomNumber: Int get() = Random().nextInt(100)
    public var exoPlayer: SimpleExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    private val MY_PERM_REQUEST = 1

    private val PLAYBACK_CHANNEL_ID = "playback_channel"
    private val PLAYBACK_NOTIFICATION_ID = 1

    private val ARG_URI = "uri_string"
    private val ARG_TITLE = "title"
    private val ARG_START_POSITION = "start_position"
    private val MEDIA_SESSION_TAG = "hello-world-media"
    var songList: List<Song>? = null

    private lateinit var mContext: Context

    companion object {
        const val TAG = "AudioPlayerService"
    }

    override fun onCreate() {
        super.onCreate()
       // val context: Context = this
        mContext = this
        //LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter("custom-event-name"));

        exoPlayer = SimpleExoPlayer.Builder(this).build()

        buildMedia()
//        if (songList?.size!! < 1) { concatenatingMediaSource = goofydebugging(mContext) }

        // Setup notification and media session.
//        exoPlayer!!.prepare(concatenatingMediaSource)
//        exoPlayer!!.seekTo(0, 0)
        exoPlayer!!.playWhenReady = false

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
//            context,
            mContext,
            PLAYBACK_CHANNEL_ID,
            R.string.playback_channel_name, //local name in settings dialog for the user
            R.string.playback_channel_description,
            PLAYBACK_NOTIFICATION_ID,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): String {
                    return songList?.get(player.currentWindowIndex)?.title.toString()
                }

                @Nullable
                override fun getCurrentContentText(player: Player): String? {
                    return songList?.get(player.currentWindowIndex)?.artist
                }

                @Nullable
                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {

                    return songList?.get(player.currentWindowIndex)?.art
                    //return getBitmapFromVectorDrawable(context, R.drawable.ic_queue_music)
                }

                @Nullable
                override fun createCurrentContentIntent(player: Player): PendingIntent? =
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        Intent(applicationContext, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

            },
            object : PlayerNotificationManager.NotificationListener {

                override fun onNotificationCancelled(notificationId: Int) {
                    //  _playerStatusLiveData.value = PlayerStatus.Cancelled(episodeId)
                    stopSelf()
                }

                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
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

        // The below syncs the foreground player with t he player
        mediaSession = MediaSessionCompat(mContext, MEDIA_SESSION_TAG)
        mediaSession!!.isActive = true

        playerNotificationManager!!.setMediaSessionToken(mediaSession!!.sessionToken) // Lock screen
        mediaSessionConnector = MediaSessionConnector(mediaSession!!)

        // Sync playlist with the queue
        mediaSessionConnector?.setQueueNavigator(object : TimelineQueueNavigator(mediaSession!!) {
            override fun getMediaDescription( player: Player, windowIndex: Int ): MediaDescriptionCompat {
                return mediaHelper(windowIndex, songList?.get(windowIndex))
            }
        })
        mediaSessionConnector!!.setPlayer(exoPlayer)
    }


    fun buildMedia() {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory( mContext, Util.getUserAgent(mContext, this.getString(R.string.app_name)) )
        var concatenatingMediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()
        val sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        val sortBy = sharedpreferences.getString(resources.getString(R.string.sort_keys), resources.getString(R.string.sort_recent_most))

        //Sync playing item with updated list
        var prevPlayingIndex = exoPlayer?.currentWindowIndex ?: 0
        var playTime = exoPlayer?.currentPosition ?: 0
        var isPlaying = exoPlayer?.playWhenReady ?: false
        var prevSong: String = ""
        var prevUri: Uri? = null
        Log.e(TAG, "build2: ------------------ 1  ----------------------")
        Log.e(TAG, "buildMedia: ${exoPlayer?.currentWindowIndex}")
        Log.e(TAG, "buildMedia: playTime ${playTime}")
        Log.e(TAG, "buildMedia: isPlaying ${isPlaying}")
        if (exoPlayer != null && !songList.isNullOrEmpty() ) {
            prevUri = songList!![prevPlayingIndex].uri
            prevSong = songList!![prevPlayingIndex].title
        }

        songList = querySongs(mContext)
        sortBy?.let {songList = sortSongs(sortBy) }
        songList?.forEach { it ->
            var media: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).setTag(it.uri.toString()).createMediaSource(it.uri)
            concatenatingMediaSource.addMediaSource(media)
        }

        exoPlayer?.prepare(concatenatingMediaSource)
        var updatedIndex = 0
        prevUri?.let { updatedIndex = updateCurrentIndex(prevUri)}

        exoPlayer?.seekTo(updatedIndex, playTime)
        exoPlayer?.playWhenReady = isPlaying
        return
    }

    fun updateCurrentIndex(prevUri: Uri): Int {
        var updatedIndex = 0
        run loop@ {
            songList?.forEachIndexed { index, song ->
                Log.e(TAG, "build2: song.uri ${song.uri}")
                if (song.uri == prevUri) {
                    updatedIndex = index
                    Log.e(TAG, "build2: FOUND!")
                    Log.e(TAG, "build2: updatedIndex $updatedIndex")
                    return@loop
                }
            }
        }
        return updatedIndex

    }

    fun build2(){
        val sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        val sortBy = sharedpreferences.getString(getString(R.string.sort_keys),"")

        var prevWindowIndex = exoPlayer?.currentWindowIndex ?: 0
        var prevSong: String = ""
        var prevUri: Uri? = null

        Log.e(TAG, "build2: ------------------ 1  ----------------------")
        if (exoPlayer != null && !songList.isNullOrEmpty() ) {
            prevUri = songList!![prevWindowIndex].uri
            prevSong = songList!![prevWindowIndex].title
        }
        // Setup notification and media session.
        Log.e(TAG, "build2: prev Idx $prevWindowIndex")
        Log.e(TAG, "build2: prevSong $prevSong")
        Log.e(TAG, "build2: prevUri $prevUri")

//        var concatenatingMediaSource = buildMedia()
//        exoPlayer!!.prepare(concatenatingMediaSource)
        Log.e(TAG, "build2: ------------------ 2 ----------------------")
        var updatedIndex = 0
        run loop@ {
            songList?.forEachIndexed { index, song ->
                Log.e(TAG, "build2: song.uri ${song.uri}")
                if (song.uri == prevUri) {
                    updatedIndex = index
                    Log.e(TAG, "build2: FOUND!")
                    Log.e(TAG, "build2: updatedIndex $updatedIndex")
                    return@loop
                }
            }
        }

        Log.e(TAG, "build2: updatedIndex $updatedIndex")
        exoPlayer!!.seekTo(updatedIndex, 0)
        exoPlayer!!.playWhenReady = true
    }

    fun sortSongs(sortBy: String?): MutableList<Song>? {
        Log.e(TAG, "sortSongs: going in")
        Log.e(TAG, "sortby $sortBy")

       val sortedList = when (sortBy) {
            getString(R.string.sort_artist_asc) ->  (songList?.sortedWith(SongArtistComparable()))?.reversed()  as MutableList<Song>?
            getString(R.string.sort_artist_desc) -> songList?.sortedWith(SongArtistComparable()) as MutableList<Song>?
            getString(R.string.sort_title_asc) -> (songList?.sortedWith(SongTitleComparable()) )?.reversed() as MutableList<Song>?
            getString(R.string.sort_title_desc) -> songList?.sortedWith(SongTitleComparable()) as MutableList<Song>?
            getString(R.string.sort_recent_most) -> songList?.sortedWith(SongCreatedComparable() ) as MutableList<Song>?
            getString(R.string.sort_recent_least) -> (songList?.sortedWith(SongCreatedComparable()) )?.reversed() as MutableList<Song>?
            else ->  songList?.sortedWith(comparator = SongCreatedComparable() ) as MutableList<Song>?
        }
        sortedList?.forEach { android.util.Log.e(TAG, "sortSongs: ${it.title} -- ${it.dateCreated} - ${it.artist}") }
        return sortedList

    }

    fun querySongs(context: Context): MutableList<Song>? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED) {
//            Log.e(TAG, "Permission already granted")
            return actualQuerySongs(context)
        } else {
//            Log.e(TAG, "Read Permission not granted")
            return null
        }
    }

    private fun actualQuerySongs(context: Context): MutableList<Song> {
        val songList = mutableListOf<Song>()
        val songUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.IS_ALARM,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            MediaStore.Audio.Media.IS_RINGTONE
        )
        val selection = "${MediaStore.Audio.Media.IS_ALARM} != 1 AND " +
                "${MediaStore.Audio.Media.IS_NOTIFICATION} != 1 AND " +
                "${MediaStore.Audio.Media.IS_RINGTONE} != 1"

        val query = context.contentResolver.query(songUri, projection, selection, null, null)

        query?.use { cursor ->
            //Log.e(TAG, "000000000000000000000000")
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val isAlarmC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_ALARM)
            val isNotifC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_NOTIFICATION)
            val isRingC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE)

            val dateAddedC = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val durationC = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                //Log.e(TAG, "+++++++++++++++++++++++++++")
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
//                var artist = cursor.getString(artistColumn)
                val isAlarmC = cursor.getString(isAlarmC)
                val isNotif = cursor.getString(isNotifC)
                val isRing = cursor.getString(isRingC)
                var dateAdded = cursor.getInt(dateAddedC)
                var dur = cursor.getInt(durationC)
                val artist = if (cursor.getString(artistColumn) == "<unknown>"){
                    ""
                } else {
                    cursor.getString(artistColumn)
                }
                val audioUri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(context, audioUri)
                var rawArt: ByteArray? = mmr.embeddedPicture
                //var rawArt: ByteArray? = null
                val bfo = BitmapFactory.Options()
                var art: Bitmap? = if (rawArt != null) {
                    BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
                } else {
                    //BitmapFactory.decodeResource(resources, R.drawable.music_note_icon)
                    getBitmapFromVectorDrawable(context, R.drawable.ic_music_note)
                }
//                Log.e(TAG, "id $id")
//                Log.e(TAG, "audioUri $audioUri")
//                Log.e(TAG, "title $title")
//                Log.e(TAG, "artist $artist")
////                Log.e(TAG, "is isAlarmC $isAlarmC")
////                Log.e(TAG, "is isNotif $isNotif")
////                Log.e(TAG, "is isRing $isRing")
//                Log.e(TAG, " dateAdded $dateAdded")
//                Log.e(TAG, " duration $dur")
                songList.add(
                    Song(
                        id = id.toInt(),
                        uri = audioUri,
                        title = title,
                        artist = artist,
                        duration = dur,
                        dateCreated = dateAdded,
                        imageResource = R.drawable.ic_rowing,
                        art = art
                    )
                )
            }
            //https://stackoverflow.com/questions/12931876/uncaught-exception-thrown-by-finalizer-when-opening-mapactivity
            query.close()
        }
//        Log.e(TAG, "SongList")
//        songList.forEach { Log.e(TAG, it.toString()) }

        return songList
    }


    //private fun mediaHelper(context: Context,  metaData: MediaMetadataCompat): MediaDescriptionCompat {
    private fun mediaHelper(windowIndex: Int, song: Song?): MediaDescriptionCompat {
        var extras: Bundle = Bundle()
        //var bitmap: Bitmap? = getBitmapFromVectorDrawable(context, R.drawable.ic_queue_music)
        //var bitmap = metaData.description.iconBitmap
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, song?.art)
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, song?.art)

        return MediaDescriptionCompat.Builder()
            .setMediaId(song?.id.toString())
            .setIconBitmap(song?.art)
            .setTitle(song?.title)
            .setDescription(song?.title)
            .setExtras(extras)
            .build()
    }

    // https://dev.to/mgazar_/playing-local-and-remote-media-files-on-android-using-exoplayer-g3a
    @MainThread
    private fun getBitmapFromVectorDrawable(context: Context, @DrawableRes drawableId: Int): Bitmap? {
        return ContextCompat.getDrawable(context, drawableId)?.let {
            val drawable = DrawableCompat.wrap(it).mutate()
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
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

       // LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)

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

    fun goofydebugging(context: Context): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, this.getString(R.string.app_name))
        )
        val audioUri =
            Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3")
        val mp4VideoUri: Uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        val ms: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioUri)
        val ms2: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri)
        concatenatingMediaSource.addMediaSource(ms)
        concatenatingMediaSource.addMediaSource(ms2)
        val songList2 = mutableListOf<Song>()
        songList2?.add(
            Song(
                id = 123,
                uri = audioUri,
                title = "title",
                artist = "artist",
                duration = 10,
                dateCreated = 100,
                art = getBitmapFromVectorDrawable(context, R.drawable.ic_music_note),
                imageResource = R.drawable.ic_rowing
            )
        )
        if (songList2?.size == 1) {
            songList2?.add(
                Song(
                    id = 123,
                    uri = mp4VideoUri,
                    title = "title vid",
                    artist = "artist vid",
                    duration = 11,
                    dateCreated = 101,
                    art = getBitmapFromVectorDrawable(context, R.drawable.ic_music_note),
                    imageResource = R.drawable.ic_rowing
                )
            )
        }
        songList = songList2
        return concatenatingMediaSource
    }

    fun makeStuff(position: Int){
        Log.e(TAG, "HI HI HI $position")
        Log.e(TAG, "HI HI HI ${exoPlayer?.currentWindowIndex.toString()}")
        Log.e(TAG, "HI HI HI ${exoPlayer?.currentTimeline.toString()}")

        exoPlayer?.seekTo(position, 0)

        Log.e(TAG, "HI HI HI ${exoPlayer?.currentWindowIndex.toString()}")
    }


    // https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
//    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent) {
//            // Get extra data included in the Intent
//            val extras = intent.extras
//            if (extras?.containsKey("message") == true){
//                val message = extras.getString("message")
//                Log.e("receiver", "Got message: $message")
//            }
//            else if (extras?.containsKey(getString(R.string.play_this_position)) == true) {
//                val position = extras.getInt(getString(R.string.play_this_position))
//
//            }
//            Log.e(TAG, "Leaving onReceiver broadcaster")
//        }
//    }

}
