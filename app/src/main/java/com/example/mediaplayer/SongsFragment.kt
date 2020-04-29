package com.example.mediaplayer

import android.content.Context
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_items.*
import java.io.IOException
import java.lang.RuntimeException

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SongsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SongsFragment : Fragment(),  SongsAdaptor.OnItemListener {

    private lateinit var textView: TextView
    private lateinit var songList: MutableList<Song>
    private lateinit var recycler_songs: RecyclerView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(TAG,"onAttach Items")
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG,"onStart Items")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG,"Created Items")
    }

    override fun onDetach() {
        super.onDetach()
        Log.e(TAG,"Detached Items")
    }

    override fun onDestroyView(){
        super.onDestroyView()
        Log.e(TAG,"Destroyed Items")
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Log.e(TAG,"onCreateView Items")
        var view: View = inflater.inflate(R.layout.fragment_items, container, false)
        textView = view.findViewById(R.id.fragTextId)

        var bundle: Bundle? = this.arguments
        var x: String = textView.text.toString()
        if (bundle != null) {
            if (bundle.containsKey("keyOther")){
                x += bundle?.getString("keyOther").toString()
            }
        }

        songList = ArrayList<Song>()
        songList.add(Song(R.drawable.ic_audiotrack, "Bruce Takara", "Tonight (Blue Note remix)"))
        songList.add(Song(R.drawable.ic_queue_music, "Tim Jones ft Domion", "Let me get around"))
        songList.add(Song(R.drawable.ic_search_black_24dp, "Joe ft Nas", "Get to know me"))
        songList.add(Song(R.drawable.ic_audiotrack, "Bruce Takara", "Tonight (Blue Note remix)"))
        songList.add(Song(R.drawable.ic_queue_music, "Tim Jones ft Domion", "Let me get around"))
        songList.add(Song(R.drawable.ic_search_black_24dp, "Joe ft Nas", "Get to know me"))
        songList.add(Song(R.drawable.ic_audiotrack, "Bruce Takara", "Tonight (Blue Note remix)"))
        songList.add(Song(R.drawable.ic_queue_music, "Tim Jones ft Domion", "Let me get around"))
        songList.add(Song(R.drawable.ic_search_black_24dp, "Joe ft Nas", "Get to know me"))
        songList.add(Song(R.drawable.ic_audiotrack, "Bruce Takara", "Tonight (Blue Note remix)"))
        songList.add(Song(R.drawable.ic_queue_music, "Tim Jones ft Domion", "Let me get around"))
        songList.add(Song(R.drawable.ic_search_black_24dp, "Joe ft Nas", "Get to know me"))
        songList.add(Song(R.drawable.ic_audiotrack, "Bruce Takara", "Tonight (Blue Note remix)"))
        songList.add(Song(R.drawable.ic_queue_music, "Tim Jones ft Domion", "Let me get around"))
        songList.add(Song(R.drawable.ic_search_black_24dp, "Joe ft Nas", "Get to know me"))

        //Log.e(TAG, view.toString())
        recycler_songs = view.findViewById(R.id.recycler_songs)
        recycler_songs.adapter = SongsAdaptor(songList, this)
        //recycler_songs.layoutManager = LinearLayoutManager(activity)
        recycler_songs.layoutManager = LinearLayoutManager(context)
        recycler_songs.setHasFixedSize(true)

        val btn:Button = view.findViewById(R.id.btnA) as Button
        btn.setOnClickListener { doSomething(it) }
        return view
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

        fun doSomething(v: View) {
            Log.e(TAG, "Clicked in ITEMS Fragment")
        }
    }



    override fun onItemClick(postion: Int) {

        // var mIntent: Intent = Intent(this, SomeActiviytlol.class)
        // startActivity(intent)
        //
        // to pass the item mList[1[ to activity
        //mIntent.putExtra("someKey", mList[postion]) //the item at postion must implent Parcelable
        // at SomActicitylol
//        if (intent.hasExtra("someKey")) {
//            var xx: Item4list = intent.getParcelableExtra("someKey")
//        }
        Log.e(TAG, songList[postion]?.mainText)
        Log.e(TAG, songList[postion]?.subText)
        Log.e(TAG, songList[postion]?.imageResource.toString())

        Toast.makeText(activity, "BITCH! $postion", Toast.LENGTH_SHORT).show()
    }
}
