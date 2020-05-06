package com.example.mediaplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SongsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SongsFragment : Fragment(),  SongsAdaptor.OnItemListener {

    private var counter: Int = 0

    private lateinit var textView: TextView
    private var songList: List<Song>? = null
    private lateinit var recycler_songs: RecyclerView
    private val MY_PERM_REQUEST = 1
    private var bottomNav: BottomNavigationView? = null
    private val mIMainActivity: IMainActivity? = null
//    private val binder: IBinder? = LocalBinder()
    private lateinit var mService: AudioPlayerService

    private var listener: SongsFragListener? = null
    interface SongsFragListener {
        fun onSongSelect(index: Int)
    }
    companion object {
        const val TAG = "SongsFragment"
        @JvmStatic
        fun newInstance(param1: String, param2: String) : SongsFragment {
            val songsFragment = SongsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            songsFragment.arguments = args
            return songsFragment

        }


    }




    override fun onCreate(savedInstanceState: Bundle?) {
        //mService = binder.getService()
        super.onCreate(savedInstanceState)
        mService = AudioPlayerService()
        Log.e(TAG,"ON CREAST SONGS")
        Log.e(TAG,"ON CREAST SONGS")
        Log.e(TAG,"ON CREAST SONGS")
        Log.e(TAG,"ON CREAST SONGS")
//        if (savedInstanceState?.containsKey("counter") == true) {
//            Log.e(TAG,"contains counter ^.^")
//            Log.e(TAG, savedInstanceState.getInt("counter").toString())
//        } else { Log.e(TAG,"does not countain :(") }
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Log.e(TAG,"onCreateView Items")


        var view: View = inflater.inflate(R.layout.fragment_items, container, false)
        bottomNav = activity?.findViewById(R.id.bottom_navigation)


        var bundle: Bundle? = this.arguments


        recycler_songs = view.findViewById(R.id.recycler_songs)

        val zxx = activity?.contentResolver
        songList = mService.querySongs(activity!!)
        //songList = ArrayList<Song>()
//        songList.add(Song(R.drawable.ic_audiotrack, "Bruce Takara", "Tonight (Blue Note remix)"))
//        songList.add(Song(R.drawable.ic_queue_music, "Tim Jones ft Domion", "Let me get around"))
//        songList.add(Song(R.drawable.ic_search_black_24dp, "Joe ft Nas", "Get to know me"))

        recycler_songs.adapter = SongsAdaptor(songList!!, this)
        recycler_songs.layoutManager = LinearLayoutManager(context)
        recycler_songs.setHasFixedSize(true)

        val btn: Button = view.findViewById(R.id.btnA) as Button
        btn.setOnClickListener { doButtonA(it) }

        val btnB: Button = view.findViewById(R.id.btnB) as Button
        btnB.setOnClickListener { v -> doButtonB(v)  }
        return view
    }

    // BUTTON a
    fun doButtonA(v: View) {
        Log.e(TAG, "Clicked Button A")

        if ( fragmentManager != null) {
            Log.e(TAG, "Found fragment: " + fragmentManager!!.backStackEntryCount.toString())
            for (entry in 0 until fragmentManager!!.backStackEntryCount) {
                Log.e(TAG, "Found fragment: " + fragmentManager!!.getBackStackEntryAt(entry).id)
                Log.e(TAG, "Found fragment: " + fragmentManager!!.getBackStackEntryAt(entry).name)
            }
        }
    }

    fun doButtonB(v: View) {
    //    Log.e(TAG, mService.randomNumber.toString())
        val x = fragmentManager?.fragments
        Log.e(TAG, "Clicked Button B")
        Log.e(TAG, "Couter $counter")
        counter = counter + 1

    }


    override fun onItemClick(postion: Int) {

        Log.e(TAG, "vvvvvvvvvvvvvvvvvvvvvvvvvv")
        var playerFragment = fragmentManager?.findFragmentByTag(getString(R.string.player_frag_tag))
        if (playerFragment == null) {
            playerFragment = PlayerFragment.newInstance("pp1", "pp2")
        }

        listener?.onSongSelect(postion)

        val intent = Intent("custom-event-name")
        intent.putExtra("message", "This is SONGSONGSONGSONG message!")
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)

        var bundle  = Bundle()
        bundle.putString( "keyOther2", "message")
        bundle.putParcelable(getString(R.string.song_bundle), songList?.get(postion))
        bundle.putInt(getString(R.string.song_position), postion)
        playerFragment.arguments = bundle

//        fragmentManager
//            ?.beginTransaction()
//            ?.replace(R.id.newmain_view, playerFragment)
//            ?.addToBackStack("From song")
//            ?.commit()

    //    bottomNav?.selectedItemId = R.id.nav_home

        if (songList != null) {
            Log.e(TAG, songList!![postion]?.mainText)
            Log.e(TAG, songList!![postion]?.subText)
        }

        Toast.makeText(activity, "CLICKED! $postion", Toast.LENGTH_SHORT).show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // other class must implement this
        listener = context as SongsFragListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//
//        Log.e(TAG, "SAVING")
//        Log.e(TAG, "SAVING")
//        Log.e(TAG, "SAVING")
//        outState.putInt("counter", counter)
//    }
}
