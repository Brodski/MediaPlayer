package com.bskimusicplayer.mediaplayer

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener


// Media streaming with ExoPlayer
// https://codelabs.developers.google.com/codelabs/exoplayer-intro/#2
// Notification
// https://dev.to/mgazar_/playing-local-and-remote-media-files-on-android-using-exoplayer-g3a
class MainActivity : AppCompatActivity(), PlayerFragment.PlayerFragListener, SongsFragment.SongsFragListener {


    private val MY_PERM_REQUEST = 1

    private var playerView: PlayerView? = null
    private var player: SimpleExoPlayer? = null
    private var mBound = false
    private var someInt: Int = 0

    private var mService: AudioPlayerService? = null

    private var playWhenReady = true
    private var currentWindow = 0
    private var playBackPosition: Long = 0

    private lateinit var sharedPreferences: SharedPreferences
    private var increment: Int? = null
    private var slop: Int? = null
    private var skipZone: Int? = null

    companion object { const val TAG = "MainActivity"
                        const val tag ="MainActivity"}


    private var restoredFragment: Fragment? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlayerService.LocalBinder
            mService = binder.getService()
            mBound = true

            Log.e(TAG, "``````onServiceConnected: setting``````")
            val pFrag = supportFragmentManager.findFragmentById(R.id.newmain_view)
            Log.e(TAG, "onServiceConnected: $pFrag")

            if (pFrag is PlayerFragment) {
                Log.e(TAG, "onServiceConnected: PlayerFrag Currenlty showing $pFrag")
                pFrag.setPlayer()
                pFrag.getTitleStuff()
            } else if (pFrag is SongsFragment) {
                pFrag.updateRecViewer()
            } else {
                Log.e(TAG, "onServiceConnected: some other fragmetn")
            }
            when {
                pFrag is PlayerFragment -> Log.e(TAG, "onServiceConnected: playerfrag")
                pFrag is PreferenceFragment -> Log.e(TAG, "onServiceConnected: preferencefrag")
                pFrag is SongsFragment -> Log.e(TAG, "onServiceConnected: songs frag")
                else -> Log.e(TAG, "onServiceConnected: ????")

            }
//            playerView?.player = mService.exoPlayer
//            playerView?.showController()
//          playerFragment.configPlayer(mService.exoPlayer)
            //Log.e(TAG, mService.exoPlayer?.currentWindowIndex.toString())
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e(TAG, "Disconnected Service :o")
            mService = null
            mBound = false
        }
    }

    override fun sendEmail() {
        //https://developer.android.com/guide/components/intents-common#ComposeEmail
        Log.e(TAG, "onOptionsItemSelected: support/donate click")
        val mAddress = arrayOf(resources.getString(R.string.contact_my_email))
        val subject = resources.getString(R.string.contact_subject)
        val msg = resources.getString(R.string.contact_intent_msg)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, mAddress)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            //     Intent.createChooser(this, msg)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun plzDoante() {
        Log.e(TAG, "plzDonate")
        val mIntent = Intent(this@MainActivity, MoneyActivity::class.java)
        mIntent.putExtra("fav_num", 69)
        startActivity(mIntent)
    }

    override fun onSongSelect(index: Int, uri: String) {
        Log.e(TAG, "currenlty playing " + mService?.exoPlayer?.currentWindowIndex.toString())
        Log.e(TAG, "onSongSelect: recived uri $uri index: $index" )

        // If we tapped a new item then go to it, else nothing
        if (mService?.exoPlayer?.currentWindowIndex != index) {
            Log.e(TAG, "onSongSelect: Chaning song")
            mService?.exoPlayer?.seekTo(index,0)
            mService?.exoPlayer?.playWhenReady = true
            inflateFragment(R.string.player_frag_tag)
        }
    }

    override fun onSettingsSort() {
//        mService?.build2()
        mService?.buildMediaAgain()
    }

    override fun togglePlayPause() {
        Log.e(TAG, "togglePlayPause: ${!(mService?.exoPlayer?.playWhenReady)!!}")
        if (mService != null ) {
            if (mService!!.exoPlayer != null) {
                mService?.exoPlayer?.playWhenReady = !(mService?.exoPlayer?.playWhenReady)!!
            }
        }


    }

    override fun getSongTitle(): String {
        var title: String = ""
        if (mService?.exoPlayer != null ) {
            title = mService?.songList?.get(mService?.exoPlayer?.currentWindowIndex!!)?.title.toString()
        }
        return title
    }

    override fun getSongArtist(): String {
        var artist: String = ""
        if (mService?.exoPlayer != null ) {
            artist = mService?.songList?.get(mService?.exoPlayer?.currentWindowIndex!!)?.artist.toString()
        }
        return artist

    }

    override fun getPlayer(): SimpleExoPlayer? {
        return mService?.exoPlayer
    }

    // could be better
    override fun skipForward(rewind: Boolean) {
        increment = sharedPreferences.getString(resources.getString(R.string.save_state_increment), "15000")?.toInt() ?: 15000
        mService?.exoPlayer?.currentPosition?.also { playPosition ->
            mService?.exoPlayer?.seekTo(playPosition + increment!!)
        }
    }

    // could be better
    override fun skipRewind() {
        increment = sharedPreferences.getString(resources.getString(R.string.save_state_increment), "15000")?.toInt() ?: 15000
        mService?.exoPlayer?.currentPosition?.also { playPosition ->
            mService?.exoPlayer?.seekTo(playPosition - increment!!)
        }
    }

    override fun getPlaylist(): MutableList<Song>? {
        return mService?.songList?.toMutableList()
    }

    override fun isPlaying(): Boolean? {
        return mService?.exoPlayer?.playWhenReady
    }

    override fun isService(): Boolean {
        if (mService != null) {
            return true
        }
        return false
    }

    override fun handleSettingsClick() {
        Log.e(TAG, "handleSettingsClick: handeling...")
        this.inflateFragment(R.string.pref_frag_tag)
        Log.e(TAG, "handleSettingsClick: handed! :)")
    }


    private fun initPlayer2() {
        Log.e(TAG, "initPlayer2: init player")
        Log.e(TAG, "initPlayer2: mbound $mBound")
        Log.e(TAG, "initPlayer2: service !null ${(mService != null)}")
        Log.e(TAG, "initPlayer2: mService $mService")
        askPermissions()
        // Google' Building feature-rich media apps with ExoPlayer - https://www.youtube.com/watch?v=svdq1BWl4r8
        // https://stackoverflow.com/questions/23017767/communicate-with-foreground-service-android


//        var intent: Intent = Intent(this, AudioPlayerService::class.java)
//        this.bindService(intent, connection, Context.BIND_AUTO_CREATE)
//        Util.startForegroundService(this, intent)
    }

    private fun releasePlayer2() {
        Log.e(TAG, "releasePlayer2: Releasing some shit")
        if (mService != null) {
//            var intent: Intent = Intent(this, AudioPlayerService::class.java)
            this.unbindService(connection)
            mBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        var tv: TextView = findViewById(R.id.scrollText)
//        tv.isSelected = true
        Log.e(TAG,"CREATED MainActivity")
        if (savedInstanceState != null && savedInstanceState.containsKey("currentFragment") && restoredFragment == null ) {
            restoredFragment = supportFragmentManager.getFragment(savedInstanceState, "currentFragment")
            Log.e(TAG, "found some fragment $restoredFragment")
        }
//        initPlayer2()

        var bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigationId)
        bottomNav.setOnNavigationItemSelectedListener { onNavClick(it) }

        KeyboardVisibilityEvent.setEventListener( this, object : KeyboardVisibilityEventListener {
            override fun onVisibilityChanged(isOpen: Boolean) {
                if (isOpen) {
                    bottomNav.visibility = View.GONE
                } else {
                    bottomNav.visibility = View.VISIBLE
                }
            }
        })
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        slop = sharedPreferences.getInt(resources.getString(R.string.save_state_slop), 0)
        skipZone = sharedPreferences.getInt(resources.getString(R.string.save_state_skip_zone), 0)

        if (slop == 0 || skipZone == 0 ){
            val editor = sharedPreferences.edit()
            val defaultSlop = resources.getString(R.string.default_slop).toInt()
            editor.putInt(resources.getString(R.string.save_state_slop), defaultSlop)
            editor.putInt(resources.getString(R.string.save_state_skip_zone), 30)
            editor.commit()
        }
        Log.e(TAG, "buildMediaStartUp: slop $slop")
        Log.e(TAG, "buildMediaStartUp: save_state_skip_zone $skipZone")
    }

    override fun onStart() {
        super.onStart()
        initPlayer2()

        Log.e(TAG,"START MainActivity")
        //Log.e(TAG, someInt.toString())
     //   initPlayer2()
     //   mService.queryWithPermissions(this)
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG,"RESUME MainActivity")
        // onRestoreInstanceState() is called after onStart() & before onResume()
        // restoreFragment is assigned in onRestoreInstanceState()

        if (restoredFragment != null ) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.newmain_view, restoredFragment!!, restoredFragment!!.tag)
                .commit()
        } else if (supportFragmentManager.fragments.size == 0) {
//            askPermissions()
            continueBuildApp()
//            continueBuildApp2()
        }

    }



    override fun onPause() {
        super.onPause()
        Log.e(TAG,"PAUSE MainActivity")

        if (Util.SDK_INT < Build.VERSION_CODES.N) {
//            Log.e(TAG, "PAUSE, int it")
            someInt = 666
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG,"STOP MainActivity")
        if (Util.SDK_INT >= Build.VERSION_CODES.N) {
//            Log.e(TAG,"STOP int int it")
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG,"DESTROY MainActivity")
        releasePlayer2()
    }

    fun continueBuildApp2() {
        var intent: Intent = Intent(this, AudioPlayerService::class.java)
        this.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Util.startForegroundService(this, intent)
        inflateFragment(R.string.player_frag_tag)

    }
    fun continueBuildApp() {
        inflateFragment(R.string.player_frag_tag)

        //val audioList = queryActually()
     //   Log.e(TAG, "Is granted")
    }


    // Android's Request App Permissions - https://developer.android.com/training/permissions/requesting
    // How to Request a Run Time Permission - Android Studio Tutorial https://www.youtube.com/watch?v=SMrB97JuIoM
    fun askPermissions() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED) {
            // Has permissions
//            continueBuildApp()
            continueBuildApp2()

//            var intent: Intent = Intent(this, AudioPlayerService::class.java)
//            this.bindService(intent, connection, Context.BIND_AUTO_CREATE)
//            Util.startForegroundService(this, intent)
//            continueBuildApp()
        } else {

            Log.e(TAG, "READ EXTERNAL NOT GRANTED")
            // shouldShowRequestPermissionRationale: false if disabled or "do not ask again"
            // if true, show a dialog that explains why we need permission. shows when user already denied it but trying again
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERM_REQUEST)
                AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Required to access audio files")
                    .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which -> ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERM_REQUEST) })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                    .create().show()



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
//                    continueBuildApp()
                    continueBuildApp2()
                } else {
                    Log.e(TAG, "onRequestPermissionsResult: nope, closing")
                    AlertDialog.Builder(this)
                        .setTitle("You have previously denied permissions")
                        .setMessage("Try again \nOr go to app settings, find Bski's Music Player and turn on permissions")
                        .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
//                        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                        .create().show()
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
                inflateFragment(R.string.player_frag_tag)
                bool = true
            }
            R.id.nav_favorites -> {
                inflateFragment(R.string.song_frag_tag)
                bool = true
            }
            else -> bool = false
        }
        return bool
    }


    fun inflateFragment(nameFrag: Int) {
        var frag: Fragment? = null

        if (nameFrag == R.string.song_frag_tag) {
            if (supportFragmentManager.findFragmentByTag(getString(R.string.song_frag_tag)) !=null  ) {
                frag = supportFragmentManager.findFragmentByTag(getString(R.string.song_frag_tag)) as SongsFragment
            } else {
                frag = SongsFragment.newInstance()
            }
        }

        if (nameFrag == R.string.player_frag_tag) {
            if (supportFragmentManager.findFragmentByTag(getString(R.string.player_frag_tag)) != null  ){
                frag = supportFragmentManager.findFragmentByTag(getString(R.string.player_frag_tag)) as PlayerFragment
            } else {
                frag = PlayerFragment.newInstance()
            }
        }

        if (nameFrag == R.string.pref_frag_tag) {
            frag = if (supportFragmentManager.findFragmentByTag(getString(R.string.pref_frag_tag)) !=null  ){
                supportFragmentManager.findFragmentByTag(getString(R.string.pref_frag_tag)) as PreferenceFragment
            } else {
                PreferenceFragment.newInstance()
            }
        }

        Log.e(TAG, "inflateFragment: $frag ")
        if (frag != null) {
            doFragmentTransaction(frag, getString(nameFrag))
        }
    }

    fun doFragmentTransaction(fragment: Fragment, tag: String){
       // findFrag()
        var currentFrag: Fragment? = null
        for ( f in supportFragmentManager.fragments) {
            currentFrag = f
        }
        if (currentFrag?.tag == tag) {
            Log.e(TAG, "Current frag is already showing. No change")
            return
        }
        else if ( currentFrag == null ){
            Log.e(TAG, "Initial load to $tag")
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.newmain_view, fragment, tag)
                .commit()
        }
        else {
            Log.e(TAG, "Changing to $tag")
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
        Log.e(TAG,"onSaveInstanceState")

        // only 1 fragment present
        var frag: Fragment? = null
        for ( f in supportFragmentManager.fragments) {

            Log.e(TAG,"XXXXXXXXXXXXXXXX onSaveInstanceState XXXXXXXXXXXXXXXX")
            frag = f
        }
        if (frag != null) {
            supportFragmentManager.putFragment(outState, "currentFragment", frag)
        }
    }



    fun editSettings(view: View){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
//        editor.putString("dropdown", "1")
//        editor.putString("list_example", "2")
      //  editor.putString("save_state_sort_key", "")
        editor.commit()
    }

    fun getSettings(view: View) {
        Log.e(TAG, "getSettings: getting")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val save_state_sort_key = sharedPreferences.getString(resources.getString(R.string.save_state_sort_key),"")
        val save_state_slop = sharedPreferences.getInt(resources.getString(R.string.save_state_slop),0)
        val save_state_increment = sharedPreferences.getString(resources.getString(R.string.save_state_increment),"0")
        val save_state_skip_zone = sharedPreferences.getInt(resources.getString(R.string.save_state_skip_zone),0)
        val slopMax = resources.getString(R.string.slop_max).toInt()
        val default_slop = resources.getString(R.string.default_slop).toInt()
        Log.e(TAG, "slopMax: $slopMax")
        Log.e(TAG, "default_slop: $default_slop")
        Log.e(TAG, "save_state_slop: $save_state_slop")
        Log.e(TAG, "save_state_increment: $save_state_increment")
        Log.e(TAG, "save_state_skip_zone: $save_state_skip_zone")
//        Log.e(TAG, "save_state_sort_key: $save_state_sort_key")

    }

    fun findFrag(){
        Log.e(TAG,"Finding frag")
        Log.e(TAG, "size: ${supportFragmentManager.fragments.size}")
        for (x in supportFragmentManager.fragments){
            Log.e(TAG,"---All Frags---")
            Log.e(TAG, x.toString())
        }
        Log.e(TAG,"---End Frags---")
        Log.e(TAG,"findFrag(player_frag_tag")
        var f = supportFragmentManager.findFragmentByTag(getString(R.string.player_frag_tag))
        Log.e(TAG, f.toString())
        Log.e(TAG, f?.tag.toString())
        Log.e(TAG,".........Exiting Finding frag")
    }


    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        var mp4VideoUri: Uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        //var videoSource: MediaSource = buildMediaSource(mp4VideoUri)

        playerView?.player = mService?.exoPlayer
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

    fun donate(view: View) {
        val intent = Intent(this, MoneyActivity::class.java)
        startActivity(intent)
    }


}







