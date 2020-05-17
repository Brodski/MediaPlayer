package com.bskimusicplayer.mediaplayer

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
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util

// Building feature-rich media apps with ExoPlayer (Google I/O '18)
// https://www.youtube.com/watch?v=svdq1BWl4r8
class AudioPlayerService : Service() {

    private val binder: IBinder? = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    public var exoPlayer: SimpleExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    private val PLAYBACK_CHANNEL_ID = "playback_channel"
    private val PLAYBACK_NOTIFICATION_ID = 1
    private val MEDIA_SESSION_TAG = "hello-world-media"
    var songList: List<Song>? = null
    private lateinit var mContext: Context
    val TAG = "AudioPlayerService"


    override fun onCreate() {
        super.onCreate()

        mContext = this
        //LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter("custom-event-name"));

        exoPlayer = SimpleExoPlayer.Builder(this).build()
        exoPlayer!!.playWhenReady = false

        buildMediaStartUp()

        initializeNotificationManager()

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

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
        exoPlayer?.setAudioAttributes(audioAttributes, true)


    }

    private fun initializeNotificationManager() {
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
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
                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback ): Bitmap? {
                    return songList?.get(player.currentWindowIndex)?.art
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
                    stopSelf()
                }

                override fun onNotificationPosted( notificationId: Int, notification: Notification, ongoing: Boolean) {
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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        val skipIncrement = sharedPreferences.getString(resources.getString(R.string.save_state_increment),resources.getString(R.string.default_increment))?.toLong() ?: 15000
        playerNotificationManager?.setFastForwardIncrementMs(skipIncrement)
        playerNotificationManager?.setRewindIncrementMs(skipIncrement)
        playerNotificationManager!!.setPlayer(exoPlayer)

    }

    private fun buildMediaStartUp() {

        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory( mContext, Util.getUserAgent(mContext, this.getString(R.string.app_name)) )
        var concatenatingMediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()
        val sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        val sortBy = sharedpreferences.getString(resources.getString(R.string.save_state_sort_key), resources.getString(R.string.sort_recent_most))
        val idxWhenStopped= sharedpreferences.getInt(resources.getString(R.string.save_state_song_index_key), 0)
        val playPosition = sharedpreferences.getLong(resources.getString(R.string.save_state_song_playtime), 0)

//        Log.e(TAG, "buildMediaStartUp: ==========================================")
//        Log.e(TAG, "buildMediaStartUp: building at sortBy $sortBy")
//        Log.e(TAG, "buildMediaStartUp: building at idx $idxWhenStopped")
//        Log.e(TAG, "buildMediaStartUp: building at playPosition $playPosition")
//        Log.e(TAG, "buildMediaStartUp: ==========================================")
        // Get songs/media on phone, sort, and set
        songList = querySongs(mContext)
        if (songList.isNullOrEmpty() ) {
            if (songList?.size!! < 2) {
                songList = goofydebugging()
                Toast.makeText(mContext, "You need to have at least 2 song on your application\nConnecting to two remote files online\nRestart app required", Toast.LENGTH_LONG).show()
                return
            }
        }
        songList = sortSongs(sortBy)
        songList?.forEach { it ->
            var media: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).setTag(it.uri.toString()).createMediaSource(it.uri)
            concatenatingMediaSource.addMediaSource(media)
        }

        exoPlayer?.prepare(concatenatingMediaSource)
        exoPlayer?.seekTo(idxWhenStopped, playPosition)
        exoPlayer?.playWhenReady = false
        return
    }

    fun buildMediaAgain() {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory( mContext, Util.getUserAgent(mContext, this.getString(R.string.app_name)) )
        var concatenatingMediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()
        val sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        val sortBy = sharedpreferences.getString(resources.getString(R.string.save_state_sort_key), resources.getString(R.string.sort_recent_most))

        //Sync playing item with updated list
        var prevPlayingIndex = exoPlayer?.currentWindowIndex ?: 0
        var playTime = exoPlayer?.currentPosition ?: 0
        var isPlaying = exoPlayer?.playWhenReady ?: false
        var updatedIndex = 0
        var prevSong: String = ""
        var prevUri: Uri? = null

        if (exoPlayer != null && !songList.isNullOrEmpty() ) {
            prevUri = songList!![prevPlayingIndex].uri
            prevSong = songList!![prevPlayingIndex].title
        }

        // Get songs/media on phone, sort, and set
        songList = querySongs(mContext)  //Optional. Maybe I remove??
        songList = sortSongs(sortBy)
        songList?.forEach { it ->
            var media: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).setTag(it.uri.toString()).createMediaSource(it.uri)
            concatenatingMediaSource.addMediaSource(media)
        }

        exoPlayer?.prepare(concatenatingMediaSource)
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

    fun sortSongs(sortBy: String?): MutableList<Song>? {

       val sortedList = when (sortBy) {
            getString(R.string.sort_artist_asc) -> (songList?.sortedWith(SongArtistComparable()))?.reversed()  as MutableList<Song>?
            getString(R.string.sort_artist_desc) -> songList?.sortedWith(SongArtistComparable()) as MutableList<Song>?
            getString(R.string.sort_title_asc) ->  (songList?.sortedWith(SongTitleComparable()) )?.reversed() as MutableList<Song>?
            getString(R.string.sort_title_desc) -> songList?.sortedWith(SongTitleComparable()) as MutableList<Song>?
            getString(R.string.sort_recent_most) -> songList?.sortedWith(SongCreatedComparable() ) as MutableList<Song>?
            getString(R.string.sort_recent_least) -> (songList?.sortedWith(SongCreatedComparable()) )?.reversed() as MutableList<Song>?
            else ->  songList?.sortedWith(comparator = SongCreatedComparable() ) as MutableList<Song>?
        }
//        sortedList?.forEach { android.util.Log.e(TAG, "sortSongs: ${it.title} -- ${it.dateCreated} - ${it.artist}") }
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
    //    val defaultArt = getBitmapFromVectorDrawable(context, R.drawable.ic_music_note_white)
//        Log.e(TAG, "actualQuerySongs: queirying in")
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

//        Log.e(TAG, "actualQuerySongs: geting query")
        val query = context.contentResolver.query(songUri, projection, selection, null, null)

//        Log.e(TAG, "actualQuerySongs: got queiry")
        query?.use { cursor ->
//            Log.e(TAG, "000000000000000000000000")
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val isAlarmC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_ALARM)
            val isNotifC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_NOTIFICATION)
            val isRingC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE)

            val dateAddedC = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val durationC = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
//            Log.e(TAG, "actualQuerySongs: ${cursor.columnNames}")
//            Log.e(TAG, "actualQuerySongs: ${cursor.count}")
//            cursor.columnNames.forEach { Log.e(TAG, "actualQuerySongs: $it") }
//            cursor.count .forEach { Log.e(TAG, "actualQuerySongs: $it") }
            while (cursor.moveToNext()) {
//                Log.e(TAG, "+++++++++++++++++++++++++++")
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val isAlarmC = cursor.getString(isAlarmC)
                val isNotif = cursor.getString(isNotifC)
                val isRing = cursor.getString(isRingC)
                var dateAdded = cursor.getInt(dateAddedC)
                var dur = cursor.getInt(durationC)
//                var artist = cursor.getString(artistColumn)
                val artist = if (cursor.getString(artistColumn) == "<unknown>"){
                    ""
                } else {
                    cursor.getString(artistColumn)
                }
                val audioUri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
//                Log.e(TAG, "id $id")
//                Log.e(TAG, "audioUri $audioUri")
//                Log.e(TAG, "title $title")
//                Log.e(TAG, "artist $artist")
////                Log.e(TAG, "is isAlarmC $isAlarmC")
////                Log.e(TAG, "is isNotif $isNotif")
////                Log.e(TAG, "is isRing $isRing")
//                Log.e(TAG, " dateAdded $dateAdded")
//                Log.e(TAG, " duration $dur")
                val mmr = MediaMetadataRetriever()
                try {
                    mmr.setDataSource(context, audioUri)
                } catch (e:Exception){
                    Log.e(TAG, "actualQuerySongs: $e")
                    Log.e(TAG, "actualQuerySongs: Probably URI problem after editing/deleting file")
                    continue
                }

                var rawArt: ByteArray? = mmr.embeddedPicture
                //var rawArt: ByteArray? = null
                val bfo = BitmapFactory.Options()
                // art = notification image
                var art: Bitmap? = if (rawArt != null) {
                    BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
                } else {
                    //defaultArt
                    BitmapFactory.decodeResource(resources,R.drawable.ic_music_note_white )
//                    Log.e(TAG, "actualQuerySongs: WHUT")
//                    Log.e(TAG, "actualQuerySongs: WHUT")
//                    Log.e(TAG, "actualQuerySongs: WHUT")
//                    Log.e(TAG, "actualQuerySongs: WHUT")
//                    BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
                }

                songList.add(
                    Song(
                        id = id.toInt(),
                        uri = audioUri,
                        title = title,
                        artist = artist,
                        duration = dur,
                        dateCreated = dateAdded,
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
        //var bitmap: Bitmap? = getBitmapFromVectorDrawable(context, R.drawable.ic_ajskdf)
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
//    @MainThread
    private fun getBitmapFromVectorDrawable(context: Context, @DrawableRes drawableId: Int): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            Log.e(TAG, "getBitmapFromVectorDrawable: PIE!!!!! DRAWABLE!")
            return BitmapFactory.decodeResource(resources,R.drawable.ic_music_note_white )
        }
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

    fun releasePlayer() {
        if (exoPlayer != null) {
            mediaSession?.release()
            mediaSessionConnector?.setPlayer(null)
            playerNotificationManager?.setPlayer(null)
            exoPlayer!!.release()
            exoPlayer = null
        }
    }

    fun saveState() {

        // Save Media State
        var playingIndex = exoPlayer?.currentWindowIndex ?: 0
        var playTime = exoPlayer?.currentPosition ?: 0
        Log.e(TAG, "buildMediaStartUp: +++++++++++++++++++++++++++++++++++++++")
        Log.e(TAG, "saveState: Saving index at $playingIndex")
        Log.e(TAG, "saveState: Saving playTime at $playTime")
        Log.e(TAG, "buildMediaStartUp: +++++++++++++++++++++++++++++++++++++++")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        val editor = sharedPreferences.edit()

        //song Index
        editor.putInt(resources.getString(R.string.save_state_song_index_key), playingIndex)
        //playtime
        editor.putLong(resources.getString(R.string.save_state_song_playtime), playTime)
        editor.commit()
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy: Player Service, destroying")

        saveState()
        releasePlayer()

        Log.e(TAG, "onDestroy: Player Service, destroyed 1/2")
        super.onDestroy()
        Log.e(TAG, "onDestroy: Player Service, destroyed 2/2")
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


    fun goofydebugging(): MutableList<Song> {

        val concatenatingMediaSource = ConcatenatingMediaSource()
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            mContext,
            Util.getUserAgent(mContext, this.getString(R.string.app_name))
        )
        val audioUri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3")
//        val mp4VideoUri: Uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        val mp4VideoUri: Uri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3")
        val ms: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioUri)
        val ms2: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri)
        concatenatingMediaSource.addMediaSource(ms)
        concatenatingMediaSource.addMediaSource(ms2)
        val songList2 = mutableListOf<Song>()
        songList2?.add(
            Song(
                id = 123,
                uri = audioUri,
                title = "Jazz in paris 1",
                artist = "Media Right Productions",
                duration = 10,
                dateCreated = 100,
                art = getBitmapFromVectorDrawable(mContext, R.drawable.ic_audiotrack_white)
            )
        )
        if (songList2?.size == 1) {
            songList2?.add(
                Song(
                    id = 123,
                    uri = mp4VideoUri,
                    title = "Jazz in Paris 2",
                    artist = "Media Right Productions",
                    duration = 11,
                    dateCreated = 101,
                    art = getBitmapFromVectorDrawable(mContext, R.drawable.ic_audiotrack_white)
                )
            )
        }
        return songList2
    }

}
