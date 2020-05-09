package com.example.mediaplayer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceActivity
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
import androidx.preference.ListPreference
import androidx.preference.PreferenceGroupAdapter
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import javax.security.auth.login.LoginException
import kotlin.reflect.typeOf

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
        fun onOptionsSort()
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
        super.onCreate(savedInstanceState)
        //mService = binder.getService()
        mService = AudioPlayerService()
    }

    override fun onResume() {
        super.onResume()

        //preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Log.e(TAG,"onCreateView SongsFragment")
        var view: View = inflater.inflate(R.layout.fragment_items, container, false)

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
//        songList = mService.songList
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

        songListFull?.forEachIndexed { idx, element ->
            if (element.uri.toString() == text) {
                listener?.onSongSelect(idx, text)
                Log.e(TAG, "onItemClick: FOUND IT ${element.uri.toString()}")
                Log.e(TAG, "onItemClick: FOUND IT $text")
                Log.e(TAG, "onItemClick: FOUND IT $idx")
                return@forEachIndexed
            }
        }
        listener?.onSongSelect(position, text)
        UIUtil.hideKeyboard(requireActivity())

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
        Log.e(TAG, "onCreateOptionsMenu: SongFragment")
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
        val sharedpreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val sortBy = sharedpreferences.getString(resources.getString(R.string.sort_keys),"")

        //TODO - the below is just stupid.
        if ( sortBy == getString(R.string.sort_artist_asc) ){
            menu.findItem(R.id.sortArtistAscId).isChecked = true
        }
        else if ( sortBy == getString(R.string.sort_artist_desc) ){
            menu.findItem(R.id.sortArtistDescId).isChecked = true
        }
        else if ( sortBy == getString(R.string.sort_title_asc) ){
            menu.findItem(R.id.sortTitleAscId).isChecked = true
        }
        else if ( sortBy == getString(R.string.sort_title_desc) ){
            menu.findItem(R.id.sortTitleDescId).isChecked = true
        }
        else if ( sortBy == getString(R.string.sort_recent_most) ){
            menu.findItem(R.id.sortDateRecentId).isChecked = true
        }
        else {
            menu.findItem(R.id.sortDateOldestId).isChecked = true
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun handleSortClick(sortBy: String){
        val sharedpreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor = sharedpreferences.edit()
        Log.e(TAG, "handleSortClick: sortby $sortBy")
        editor.putString(resources.getString(R.string.sort_keys), sortBy)
        editor.commit()
        listener?.onOptionsSort()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.e(TAG, "onOptionsItemSelected clicked id: ${item.itemId}")
        Log.e(TAG, "onOptionsItemSelected clicked title: ${item.title}")

        when (item.itemId){
            R.id.sortArtistAscId -> {
                item.isChecked = true
                Log.e(TAG, "sortArtistAscId")
                handleSortClick(resources.getString(R.string.sort_artist_asc))
                return false
            }
            R.id.sortArtistDescId -> {
                item.isChecked = true
                Log.e(TAG, "sortArtistDescId")
                handleSortClick(resources.getString(R.string.sort_artist_desc))
                return false
            }
            R.id.sortTitleAscId -> {
                item.isChecked = true
                Log.e(TAG, "sortTitleAscId")
                handleSortClick(resources.getString(R.string.sort_title_asc))
                return false
            }
            R.id.sortTitleDescId -> {
                item.isChecked = true
                Log.e(TAG, "sortTitleDescId")
                handleSortClick(resources.getString(R.string.sort_title_desc))
                return false
            }
            R.id.sortDateRecentId -> {
                item.isChecked = true
                Log.e(TAG, "sortDateRecentId")
                handleSortClick(resources.getString(R.string.sort_recent_most))
                return false
            }
            R.id.sortDateOldestId -> {
                item.isChecked = true
                Log.e(TAG, "sortDateOldestId")
                handleSortClick(resources.getString(R.string.sort_recent_least))
                return false
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
        //return super.onOptionsItemSelected(item)
    }
}
