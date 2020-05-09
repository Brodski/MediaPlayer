package com.example.mediaplayer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import android.view.View
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

    private  var someInt: Int = 0

//    private lateinit var mService: AudioPlayerService
    private var mService: AudioPlayerService? = null

    private var playWhenReady = true
    private var currentWindow = 0
    private var playBackPosition: Long = 0

    companion object { const val TAG = "MainActivity"
                        const val tag ="MainActivity"}


    private var restoredFragment: Fragment? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlayerService.LocalBinder
            mService = binder.getService()


            Log.e(TAG, "``````onServiceConnected: setting``````")
            Log.e(TAG, "``````onServiceConnected: setting``````")
            Log.e(TAG, "``````onServiceConnected: setting``````")
            Log.e(TAG, "``````onServiceConnected: setting``````")
       //     playerFragment.setPlayer()
            val pFrag = supportFragmentManager.findFragmentById(R.id.newmain_view)
            if (pFrag is PlayerFragment) {
                Log.e(TAG, "onServiceConnected: PlayerFrag Currenlty")
                pFrag.setPlayer()
            } else {
                Log.e(TAG, "onServiceConnected: some other fragmetn")
                
            }
            Log.e(TAG, "onServiceConnected: $pFrag")
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
        }
    }

    override fun onSongSelect(index: Int, uri: String) {
        Log.e(TAG, "onSongSelect: recived uri $uri index: $index" )
//        val pFrag = supportFragmentManager.findFragmentById(R.id.newmain_view)
//        playerFragment.playAtIndex(index, text)

        if (mService == null ) {
            Log.e(TAG, "onSongSelect: we aint find shit, mService is null")
            Log.e(TAG, "onSongSelect: we aint find shit, mService is null")
            Log.e(TAG, "onSongSelect: we aint find shit, mService is null")
            Log.e(TAG, "onSongSelect: we aint find shit, mService is null")
            Log.e(TAG, "onSongSelect: we aint find shit, mService is null")
            Log.e(TAG, "onSongSelect: we aint find shit, mService is null")
            Log.e(TAG, "onSongSelect: we aint find shit, mService is null")
        }
        Log.e(TAG, "here in player fragment, playing at index $index")
        Log.e(TAG, "here in player fragment, playing at index $uri")
        Log.e(TAG, mService?.exoPlayer?.currentWindowIndex.toString())
        //val wtf = mService.exoPlayer.contentPosition
        //val wtf2 = mService.exoPlayer
        mService?.exoPlayer?.seekTo(index,0)
        mService?.exoPlayer?.playWhenReady = true
    }

    override fun onOptionsSort(sortBy: Int) {
        Log.e(TAG, "onOptionsSort: sortBy $sortBy")
        if (mService == null) {
            Log.e(TAG, "onOptionsSort: mService is null as shit")
            Log.e(TAG, "onOptionsSort: mService is null as shit")
            Log.e(TAG, "onOptionsSort: mService is null as shit")
            Log.e(TAG, "onOptionsSort: mService is null as shit")
            Log.e(TAG, "onOptionsSort: mService is null as shit")
            Log.e(TAG, "onOptionsSort: mService is null as shit")
            Log.e(TAG, "onOptionsSort: mService is null as shit")
            Log.e(TAG, "onOptionsSort: mService is null as shit")
        }
//        playerFragment.sortItemsBy(sortBy)
        mService?.buildMedia(sortBy)
    }

    override fun getPlayer(): SimpleExoPlayer? {
        return mService?.exoPlayer
    }

    override fun onPlayerSent(num: Int) {
        Log.e(tag, "$num")
//        playerFragment.talkService2()
        //mService.makeStuff(65)

    }


    private fun initPlayer2() {
        Log.e(TAG, "initPlayer2: init player")
        // Google' Building feature-rich media apps with ExoPlayer - https://www.youtube.com/watch?v=svdq1BWl4r8
        // https://stackoverflow.com/questions/23017767/communicate-with-foreground-service-android
        var intent: Intent = Intent(this, AudioPlayerService::class.java)
        this.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Util.startForegroundService(this, intent)
    }

    private fun releasePlayer2() {
        Log.e(TAG, "releasePlayer2: Releasing some shit")
        if (mService != null) {
            var intent: Intent = Intent(this, AudioPlayerService::class.java)
            this.unbindService(connection)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e(TAG,"CREATED MainActivity")

        initPlayer2()

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

    }

    override fun onStart() {
        super.onStart()
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
        if (restoredFragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.newmain_view, restoredFragment!!, restoredFragment!!.tag)
                .commit()
        } else {
            askPermissions()
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
            continueBuildApp()
        } else {

            Log.e(TAG, "READ EXTERNAL NOT GRANTED")
            // shouldShowRequestPermissionRationale: false if disabled or "do not ask again"
            // if true, show a dialog that explains why we need permission. shows when user already denied it but trying again
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERM_REQUEST)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERM_REQUEST)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERM_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    Log.e(TAG,":) Permission granted!")
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
            R.id.nav_search -> {
                Log.e(TAG, "onNavClick: search")
                inflateFragment(R.string.pref_frag_tag)
                bool = true
            }
            else -> bool = false
        }
        return bool
    }

    var mState: Bundle = Bundle()
    fun saveStatePro(nameFrag: Int) {
        var frag: Fragment? = null
        for ( f in supportFragmentManager.fragments) {
            Log.e(TAG, "onSaveInstanceState: $f")
            frag = f
        }
        if (frag != null) {
            supportFragmentManager.putFragment(mState, getString(nameFrag), frag)
        }
    }

    fun inflateFragment(nameFrag: Int) {
    //    saveStatePro(nameFrag)
        var songsFragment: SongsFragment
        val playerFragment :PlayerFragment
        var preferenceFragment: PreferenceFragment
        var frag: Fragment? = null

        if (nameFrag == R.string.song_frag_tag) {
            if (supportFragmentManager.findFragmentByTag(getString(R.string.song_frag_tag)) !=null  ) {
//                songsFragment = supportFragmentManager.findFragmentByTag(getString(R.string.song_frag_tag)) as SongsFragment
                frag = supportFragmentManager.findFragmentByTag(getString(R.string.song_frag_tag)) as SongsFragment
            } else {
                frag = SongsFragment.newInstance("pp1", "pp2")
            }
        }

        if (nameFrag == R.string.player_frag_tag) {
            if (supportFragmentManager.findFragmentByTag(getString(R.string.player_frag_tag)) != null  ){
                frag = supportFragmentManager.findFragmentByTag(getString(R.string.player_frag_tag)) as PlayerFragment
            } else {
//                playerFragment = PlayerFragment.newInstance("pp1", "pp2")
                frag = PlayerFragment.newInstance("pp1", "pp2")
            }
        }

        if (nameFrag == R.string.pref_frag_tag) {
            frag = if (supportFragmentManager.findFragmentByTag(getString(R.string.pref_frag_tag)) !=null  ){
                supportFragmentManager.findFragmentByTag(getString(R.string.pref_frag_tag)) as PreferenceFragment
            } else {
                PreferenceFragment.newInstance("pp1", "pp2")
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
        Log.e(TAG, "doFragmentTransaction: ${fragment}")
        Log.e(TAG, "doFragmentTransaction: ${fragment.tag}")
        Log.e(TAG, "doFragmentTransaction: ${tag}")
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
            Log.e(TAG,"XXXXXXXXXXXXXXXX onSaveInstanceState XXXXXXXXXXXXXXXX")
            Log.e(TAG,"XXXXXXXXXXXXXXXX onSaveInstanceState XXXXXXXXXXXXXXXX")
            Log.e(TAG,"XXXXXXXXXXXXXXXX onSaveInstanceState XXXXXXXXXXXXXXXX")
            Log.e(TAG,"XXXXXXXXXXXXXXXX onSaveInstanceState XXXXXXXXXXXXXXXX")
            frag = f
        }
        if (frag != null) {
            supportFragmentManager.putFragment(outState, "currentFragment", frag)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.e(TAG,"onRestoreInstanceState")


        if (savedInstanceState.containsKey("currentFragment") && restoredFragment == null ) {
            restoredFragment = supportFragmentManager.getFragment(savedInstanceState, "currentFragment")
            Log.e(TAG, ">>>>>>>>>>>>>>>>>>> onCreate bundle Frag $restoredFragment <<<<<<<<<<<<<<<<<")
        } else {
            Log.e(TAG, ">>>>>>>>>>>>>>>>>>>> onCreate no bundlde<<<<<<<<<<<<<<<<<}")
        }
//        if (savedInstanceState.containsKey("currentFragment") && restoredFragment == null ) {
//            restoredFragment = supportFragmentManager.getFragment(savedInstanceState, "currentFragment")
//            Log.e(TAG, ">>>>>>>>>>>>>>>>>>> onCreate bundle Frag $restoredFragment <<<<<<<<<<<<<<<<<")
//        } else {
//            Log.e(TAG, ">>>>>>>>>>>>>>>>>>>> onCreate no bundlde<<<<<<<<<<<<<<<<<}")
//        }
    }

    fun editSettings(view: View){
        val sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedpreferences.edit()
        editor.putString("dropdown", "1")
        editor.putString("list_example", "")
        editor.commit()
    }

    fun getSettings(view: View) {
        Log.e(TAG, "getSettings: getting")
        val sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val z1 = sharedpreferences.getBoolean("checkbox", true)
        val z2 = sharedpreferences.getString("dropdown","")
        val z3 = sharedpreferences.getString("list_example","")
        Log.e(TAG, "getSettings: $z1")
        Log.e(TAG, "getSettings: $z2")
        Log.e(TAG, "getSettings: $z3")
    }

    fun bottonRight(view: View) {
        Log.e(TAG, "clicked btton right")
        Log.e(TAG, mService?.exoPlayer?.currentWindowIndex.toString())

        Log.e(TAG,"Broadcasting message")
        val intent = Intent("custom-event-name")
        // You can also include some extra data.
        // You can also include some extra data.
        intent.putExtra("message", "This is my message!")
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }

    fun talkToMain(){
        Log.e(TAG, "hi i'm in main")
        Log.e(TAG, mService?.exoPlayer?.currentWindowIndex.toString())
    }

    fun saveThingy() {
        var frag: Fragment? = null
        Log.e(TAG, "Found fragment: " + supportFragmentManager.backStackEntryCount.toString())
        for (entry: Int in 0 until supportFragmentManager.backStackEntryCount) {
            val ff = supportFragmentManager.getBackStackEntryAt(entry)
            Log.e(TAG, "Found fragment: " + supportFragmentManager.getBackStackEntryAt(entry).id)
            Log.e(TAG, "Found fragment: " + supportFragmentManager.getBackStackEntryAt(entry).name)
        }

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
}







