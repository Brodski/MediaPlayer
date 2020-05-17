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
import kotlin.math.log


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

    companion object { const val TAG = "MainActivity"
                        const val tag ="MainActivity"}


    private var restoredFragment: Fragment? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlayerService.LocalBinder
            mService = binder.getService()
//            Log.e(TAG, "``````onServiceConnected: setting``````")
            val pFrag = supportFragmentManager.findFragmentById(R.id.newmain_view)
//            Log.e(TAG, "onServiceConnected: current frag $pFrag")

            if (pFrag is PlayerFragment) {
                pFrag.setPlayer()
                pFrag.getTitleStuff()
//                Log.e(TAG, "onServiceConnected: DONE")
            } else if (pFrag is SongsFragment) {
                pFrag.updateRecViewer()
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e(TAG, "Disconnected Service :o")
            mService = null
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        Log.e(TAG,"onCreate: CREATED MainActivity")
        if (savedInstanceState != null && savedInstanceState.containsKey("currentFragment") && restoredFragment == null ) {
            restoredFragment = supportFragmentManager.getFragment(savedInstanceState, "currentFragment")
//            Log.e(TAG, "onCreate: found some fragment $restoredFragment")
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

        Log.e(TAG, "onCreate: mService == null? ${mService == null}")
        if (mService == null){

            Log.e(TAG, "onCreate: -=-=-=-= creating intent -=-=-=-=")
            var intent: Intent = Intent(this, AudioPlayerService::class.java)
            Log.e(TAG, "onCreate: -=-=-=-=binding Service -=-=-=-=")
            this.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            Log.e(TAG, "onCreate: -=-=-=-=before startforegorundservice -=-=-=-=")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
                startService(intent)
            } else{
                Util.startForegroundService(this, intent)
            }

            Log.e(TAG, "onCreate: -=-=-=-=after startforegorundservice -=-=-=-=")
    //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    //            startForegroundService(intent)
    //        }
        }
    }

    override fun onStart() {
        super.onStart()
//        Log.e(TAG,"START MainActivity")
        initPlayer2()
    }

    override fun onResume() {
        super.onResume()
//        Log.e(TAG,"RESUME MainActivity")
        // onRestoreInstanceState() is called after onStart() & before onResume()
        // restoreFragment is assigned in onRestoreInstanceState()
        if (restoredFragment != null ) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.newmain_view, restoredFragment!!, restoredFragment!!.tag)
                .commit()
        } else if (supportFragmentManager.fragments.size == 0) {
            continueBuildApp2()
        }
    }

    override fun onPause() {
        super.onPause()
//        Log.e(TAG,"PAUSE MainActivity")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        Log.e(TAG,"onSaveInstanceState")
        if ( !supportFragmentManager.fragments.isNullOrEmpty()) {
            var frag = supportFragmentManager.fragments.get(0)
//            Log.e(TAG, "onSaveInstanceState: putting this frag in $frag")
//            Log.e(TAG, "onSaveInstanceState: putting this frag in ${frag.tag}")
            supportFragmentManager.putFragment(outState, "currentFragment", frag)
        }
    }

    override fun onStop() {
        super.onStop()
//        Log.e(TAG,"STOP MainActivity")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG,"DESTROY MainActivity")
        releasePlayer2()
    }



    private fun initPlayer2() {
//        Log.e(TAG, "initPlayer2: '(mService != null)'  ${(mService != null)}")
        askPermissions()
        // Google' Building feature-rich media apps with ExoPlayer - https://www.youtube.com/watch?v=svdq1BWl4r8
        // https://stackoverflow.com/questions/23017767/communicate-with-foreground-service-android
//        var intent: Intent = Intent(this, AudioPlayerService::class.java)
//        this.bindService(intent, connection, Context.BIND_AUTO_CREATE)
//        Util.startForegroundService(this, intent)
    }

    private fun releasePlayer2() {
//        Log.e(TAG, "releasePlayer2: Releasing some shit")
        Log.e(TAG, "releasePlayer2: is mService null? ${(mService == null)}")
        if (mService != null) {
            this.unbindService(connection)
        }
    }


    fun continueBuildApp2() {

//        var intent: Intent = Intent(this, AudioPlayerService::class.java)
//        this.bindService(intent, connection, Context.BIND_AUTO_CREATE)
//        Util.startForegroundService(this, intent)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent)
//        ContextCompat.startForegroundService(this, intent)
        inflateFragment(R.string.player_frag_tag)

//        }
    }


    // Android's Request App Permissions - https://developer.android.com/training/permissions/requesting
    // How to Request a Run Time Permission - Android Studio Tutorial https://www.youtube.com/watch?v=SMrB97JuIoM
    fun askPermissions() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED) {
            // Has permissions
            continueBuildApp2()
        } else {
//            Log.e(TAG, "READ EXTERNAL NOT GRANTED")
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
                    continueBuildApp2()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("You have previously denied permissions")
                        .setMessage("Try again \nOr go to app settings, find Bski's Music Player and turn on permissions")
                        .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
//                        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                        .create().show()
                }
                return
            }
        }
    }

    fun inflateFragment(nameFrag: Int) {
        var frag: Fragment? = null

        if (nameFrag == R.string.song_frag_tag) {
            if (supportFragmentManager.findFragmentByTag(getString(R.string.song_frag_tag)) !=null  ) {
                frag = supportFragmentManager.findFragmentByTag(getString(R.string.song_frag_tag)) as SongsFragment
            } else {
//                frag = SongsFragment.newInstance()
                frag = SongsFragment()
            }
        }

        if (nameFrag == R.string.player_frag_tag) {
            if (supportFragmentManager.findFragmentByTag(getString(R.string.player_frag_tag)) != null  ){
                frag = supportFragmentManager.findFragmentByTag(getString(R.string.player_frag_tag)) as PlayerFragment
            } else {
//                frag = PlayerFragment.newInstance()
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

    fun doFragmentTransaction(fragment: Fragment, tag: String){
       // findFrag()
        var currentFrag: Fragment? = null
        for ( f in supportFragmentManager.fragments) {
            currentFrag = f
        }
        if (currentFrag?.tag == tag) {
//            Log.e(TAG, "Current frag is already showing. No change")
            return
        }
        else if ( currentFrag == null ){
//            Log.e(TAG, "Initial load to $tag")
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.newmain_view, fragment, tag)
                .commit()
        }
        else {
//            Log.e(TAG, "Changing to $tag")
            supportFragmentManager
                .beginTransaction()
                .addToBackStack(tag)
                //.add(R.id.newmain_view, fragment, tag)
                .replace(R.id.newmain_view, fragment, tag)
                .commit()
        }
    }

    override fun onSongSelect(index: Int, uri: String) {
//        Log.e(TAG, "onSongSelect: recived uri $uri index: $index" )
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
//        Log.e(TAG, "onOptionsItemSelected: email")
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

    override fun plzDoante() {
//        Log.e(TAG, "plzDonate")
        val mIntent = Intent(this@MainActivity, MoneyActivity::class.java)
        startActivity(mIntent)
    }

}







