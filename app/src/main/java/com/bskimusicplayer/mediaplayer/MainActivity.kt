package com.bskimusicplayer.mediaplayer

import android.Manifest
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.SimpleExoPlayer
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
    private var mService: AudioPlayerService? = null
    private lateinit var sharedPreferences: SharedPreferences
    private var increment: Int? = null
    private var slop: Int? = null
    private var skipZone: Int? = null
    private var isKeepScreenOn: Boolean? = null
    companion object { const val TAG = "MainActivity"
                        const val tag ="MainActivity"}
    private var restoredFragment: Fragment? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlayerService.LocalBinder
            mService = binder.getService()
            val pFrag = supportFragmentManager.findFragmentById(R.id.newmain_view)
            if (pFrag is PlayerFragment) {
                pFrag.setPlayer()
                pFrag.getTitleStuff()
            } else if (pFrag is SongsFragment) {
                pFrag.updateRecViewer()
            } else if (pFrag == null) {
                inflateFragment(R.string.player_frag_tag)
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState != null && savedInstanceState.containsKey("currentFragment") && restoredFragment == null ) {
            restoredFragment = supportFragmentManager.getFragment(savedInstanceState, "currentFragment")
        }
        // Dear Android, why should I, the dev, put this line in here. Should be default behavior
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        Log.e(TAG,"Main Activity: running")

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
        isKeepScreenOn = sharedPreferences.getBoolean(resources.getString(R.string.save_state_screen), false)

        if (slop == 0 || skipZone == 0 ){
            val editor = sharedPreferences.edit()
            val defaultSlop = resources.getString(R.string.default_slop).toInt()
            editor.putInt(resources.getString(R.string.save_state_slop), defaultSlop)
            editor.putInt(resources.getString(R.string.save_state_skip_zone), 30)
            editor.putBoolean(resources.getString(R.string.save_state_screen), false)
            editor.commit()
        }

    }

    override fun onStart() {
        super.onStart()
        initPlayer2()
    }

    override fun onResume() {
        super.onResume()
        // onRestoreInstanceState() is called after onStart() & before onResume()
        // restoreFragment is assigned in onRestoreInstanceState()
        if (restoredFragment != null ) {
            supportFragmentManager
                .beginTransaction()
                .addToBackStack(tag) // BAM?
                .replace(R.id.newmain_view, restoredFragment!!, restoredFragment!!.tag)
                .commit()
        } else if (supportFragmentManager.fragments.size == 0) {
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if ( !supportFragmentManager.fragments.isNullOrEmpty()) {
            var frag = supportFragmentManager.fragments.get(0)
            supportFragmentManager.putFragment(outState, "currentFragment", frag)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: ")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: ")
        releasePlayer2()
    }

    private fun initPlayer2() {
        askPermissions()
    }

    private fun releasePlayer2() {
        if (mService != null) {
            this.unbindService(connection)
        }
    }

    private fun startMyService(){
        if (mService == null){
            var intent: Intent = Intent(this, AudioPlayerService::class.java)
            this.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
                startService(intent)
            } else{
                Util.startForegroundService(this, intent)
            }
        }
    }

    fun continueBuildApp2() {
        startMyService()
    }

    // Android's Request App Permissions - https://developer.android.com/training/permissions/requesting
    // How to Request a Run Time Permission - Android Studio Tutorial https://www.youtube.com/watch?v=SMrB97JuIoM
    fun askPermissions() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED) {
            // Has permissions
            continueBuildApp2()
        } else {
            // shouldShowRequestPermissionRationale: false if disabled or "do not ask again"
            // if true, show a dialog that explains why we need permission. shows when user already denied it but trying again
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
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
                    continueBuildApp2()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("You have previously denied permissions")
                        .setMessage("Try again \n\nOr go to app settings, find Bski's Music Player and turn on permissions")
                        .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
//                        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                        .create().show()
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
                frag = SongsFragment()
            }
        }

        if (nameFrag == R.string.player_frag_tag) {
            if (supportFragmentManager.findFragmentByTag(getString(R.string.player_frag_tag)) != null  ){
                frag = supportFragmentManager.findFragmentByTag(getString(R.string.player_frag_tag)) as PlayerFragment
            } else {
                frag = PlayerFragment()
            }
        }

        if (nameFrag == R.string.pref_frag_tag) {
            frag = if (supportFragmentManager.findFragmentByTag(getString(R.string.pref_frag_tag)) !=null  ){
                supportFragmentManager.findFragmentByTag(getString(R.string.pref_frag_tag)) as PreferenceFragment
            } else {
                PreferenceFragment()
            }
        }

        if (frag != null) {
            doFragmentTransaction(frag, getString(nameFrag))
        }
    }

    fun doFragmentTransaction(fragment: Fragment, tag: String){
        var currentFrag: Fragment? = null
        for ( f in supportFragmentManager.fragments) {
            currentFrag = f
        }
        if (currentFrag?.tag == tag) {
            return
        }
        else if ( currentFrag == null ){
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.newmain_view, fragment, tag)
                .commit()
        }
        else {
            supportFragmentManager
                .beginTransaction()
                .addToBackStack(tag)
                .replace(R.id.newmain_view, fragment, tag)
                .commit()
        }
    }

    override fun onSongSelect(index: Int, uri: String) {
        // If we tapped a new item then go to it, else nothing
        if (mService?.exoPlayer?.currentWindowIndex != index) {
            mService?.exoPlayer?.seekTo(index,0)
            mService?.exoPlayer?.playWhenReady = true
            inflateFragment(R.string.player_frag_tag)
        }
    }

    override fun onSettingsSort() {
        mService?.buildMediaAgain()
    }

    override fun togglePlayPause() {
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
        this.inflateFragment(R.string.pref_frag_tag)
    }

    override fun sendEmail() {
        //https://developer.android.com/guide/components/intents-common#ComposeEmail
        val mAddress = arrayOf(resources.getString(R.string.contact_my_email))
        val subject = resources.getString(R.string.contact_subject)
        val msg = resources.getString(R.string.contact_intent_msg)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, mAddress)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

//    override fun plzDoante() {
//        val mIntent = Intent(this@MainActivity, MoneyActivity::class.java)
//        startActivity(mIntent)
//    }

}







