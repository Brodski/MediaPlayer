package com.example.mediaplayer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import javax.security.auth.login.LoginException

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SongsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SongsFragment : Fragment(),  SongsAdaptor.OnItemListener {

    private var songListFull: List<Song>? = null
    private var songList: List<Song>? = null
    private var bottomNav: BottomNavigationView? = null
    private lateinit var recycler_songs: RecyclerView
    private lateinit var adaptor: SongsAdaptor
    private lateinit var mService: AudioPlayerService

    private var listener: SongsFragListener? = null
    interface SongsFragListener {
        fun onSongSelect(index: Int, text: String)
        fun onOptionsSort(sortBy: Int)
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
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Log.e(TAG,"onCreateView Items")
        var view: View = inflater.inflate(R.layout.fragment_items, container, false)
        //bottomNav = activity?.findViewById(R.id.bottom_navigationId)
        var nav: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigationId)
        var songsIcon: MenuItem =  nav.menu.findItem(R.id.nav_favorites)
        songsIcon.isChecked = true

        var bundle: Bundle? = this.arguments

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        setHasOptionsMenu(true)
        toolbar.title = "Cool Player"
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        recycler_songs = view.findViewById(R.id.recycler_songs)


        //private val songListFull: ArrayList<Song> = ArrayList(songList)
        songList = mService.querySongs(requireActivity())
        songListFull = songList?.map{ it.copy() }

        adaptor = SongsAdaptor((songList as MutableList<Song>?)!!, this)
        recycler_songs.adapter = adaptor
        recycler_songs.layoutManager = LinearLayoutManager(context)
        recycler_songs.setHasFixedSize(true)

//        songList.add(Song(R.drawable.ic_audiotrack, "Bruce Takara", "Tonight (Blue Note remix)"))
//        songList.add(Song(R.drawable.ic_queue_music, "Tim Jones ft Domion", "Let me get around"))
//        songList.add(Song(R.drawable.ic_search_black_24dp, "Joe ft Nas", "Get to know me"))


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
            Log.e(TAG, "Found fragment: " + requireFragmentManager().backStackEntryCount.toString())
            for (entry in 0 until requireFragmentManager().backStackEntryCount) {
                Log.e(TAG, "Found fragment: " + requireFragmentManager().getBackStackEntryAt(entry).id)
                Log.e(TAG, "Found fragment: " + requireFragmentManager().getBackStackEntryAt(entry).name)
            }
        }
    }

    fun doButtonB(v: View) {
    //    Log.e(TAG, mService.randomNumber.toString())
        Log.e(TAG, "Clicked Button B")

    }


    override fun onItemClick(position: Int, text: String) {

        Log.e(TAG, "vvvvvvvvvvvvvvvvvvvvvvvvvv")
        Log.e(TAG, "onItemClick text/uri: $text")
        var playerFragment = fragmentManager?.findFragmentByTag(getString(R.string.player_frag_tag))
        if (playerFragment == null) {
            playerFragment = PlayerFragment.newInstance("pp1", "pp2")
        }

        songListFull?.forEachIndexed { idx, element ->
            if (element.uri.toString() == text) {
                listener?.onSongSelect(idx, text)
                Log.e(TAG, "onItemClick: FOUND IT ${element.uri.toString()}")
                Log.e(TAG, "onItemClick: FOUND IT $text")
                Log.e(TAG, "onItemClick: FOUND IT $idx")
                return@forEachIndexed
            }
        }
        //listener?.onSongSelect(position, text)

//        val intent = Intent("custom-event-name")
//        intent.putExtra("message", "This is SONGSONGSONGSONG message!")
//        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)

//        var bundle  = Bundle()
//        bundle.putString( "keyOther2", "message")
//        bundle.putParcelable(getString(R.string.song_bundle), songList?.get(position))
//        bundle.putInt(getString(R.string.song_position), position)
//        playerFragment.arguments = bundle
//        fragmentManager
//            ?.beginTransaction()
//            ?.replace(R.id.newmain_view, playerFragment)
//            ?.addToBackStack("From song")
//            ?.commit()

    //    bottomNav?.selectedItemId = R.id.nav_home //Go to media player

        if (songList != null) {
            Log.e(TAG, songList!![position].mainText.toString())
            Log.e(TAG, songList!![position].subText.toString())
            Log.e(TAG, songList!![position].uri.toString())
        }

        Toast.makeText(activity, "CLICKED! $position", Toast.LENGTH_SHORT).show()
        UIUtil.hideKeyboard(requireActivity())
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //return super.onCreateOptionsMenu(menu)
        Log.e(TAG, "+++ onCreateOptionsMenu +++")
//        val frags = fragmentManager!!.fragments
//        frags?.forEach {
//            Log.e(TAG, "onCreateOptionsMenu: ${it.tag}")
//            Log.e(TAG, "onCreateOptionsMenu: ${it.toString()}")
//        }

        inflater.inflate(R.menu.menu_songs, menu)
        val searchItem = menu.findItem(R.id.search_bar_songs)
        val searchView = searchItem.actionView as SearchView

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        searchView.setOnQueryTextListener( object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adaptor.filter.filter(newText)
                return false
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.e(TAG, "clicked id: ${item.itemId}")
        Log.e(TAG, "clicke title: ${item.title}")
        when (item.itemId){
            R.id.sortAlphAscId -> {
                item.isChecked = true
                Log.e(TAG, "sortAlphAscId")
                listener?.onOptionsSort(R.string.sort_alpha_asc)
                return false
            }
            R.id.sortAlphDescId -> {
                item.isChecked = true
                Log.e(TAG, "sortAlphDescId")
                listener?.onOptionsSort(R.string.sort_alpha_des)
                return false}
            R.id.sortDateRecentId -> {
                item.isChecked = true
                Log.e(TAG, "sortDateRecentId")
                listener?.onOptionsSort(R.string.sort_created_recently)
                return false
            }
            R.id.sortDateOldId -> {
                item.isChecked = true
                Log.e(TAG, "sortDateOldId")
                listener?.onOptionsSort(R.string.sort_created_oldest)
                return false
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
        //return super.onOptionsItemSelected(item)
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
