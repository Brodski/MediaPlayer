package com.example.mediaplayer

import android.Manifest
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import android.nfc.Tag
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private val MY_PERM_REQUEST = 1

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

        val btn: Button = view.findViewById(R.id.btnA) as Button
        btn.setOnClickListener { doSomething(it) }

        val btnB: Button = view.findViewById(R.id.btnB) as Button
        btnB.setOnClickListener { v -> getSongs(v)  }
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

        Toast.makeText(activity, "CLICKED! $postion", Toast.LENGTH_SHORT).show()
    }

    fun getSongs(v: View) {
        Log.e(TAG, "Clicked in get Songs")
        queryWithPermissions()
    }


    fun queryWithPermissions() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED) {
            val audioList = queryActually()
            Log.e(TAG, "Permission already granted")
            //Toast.makeText(this, "Already granted", Toast.LENGTH_SHORT).show()
        } else {

            Log.e(TAG, "Read Permission not granted")
            //Toast.makeText(this, "READ EXTERNAL NOT GRANTED", Toast.LENGTH_SHORT).show()
            //requestStoragePermission()
            // if true, show a dialog that explains why we need permission. shows when user already denied it but trying again
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(context)
                    .setTitle("Read External Storage permission required")
                    .setMessage("Allows read access audio & video files")
                    .setPositiveButton("Agree", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERM_REQUEST)
                        }
                    })
                    .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            dialog?.dismiss()
                        }
                    })
                    .create().show()

            } else {
                //this?
                ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERM_REQUEST)
            }
        }
    }


    fun queryActually(): MutableList<MainActivity.AudioFile> {
        val audioList = mutableListOf<MainActivity.AudioFile>()

//        var projection: Array<String> = arrayOf (
//            MediaStore.Audio.Media._ID,
//            MediaStore.Audio.Media.ARTIST,
//            MediaStore.Audio.Media.TITLE,
//            MediaStore.Audio.Media.DISPLAY_NAME,
//            MediaStore.Audio.Media.DURATION
//        )

        val songUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = "${MediaStore.Audio.Media.IS_ALARM} != 1 AND " +
                "${MediaStore.Audio.Media.IS_NOTIFICATION} != 1 AND " +
                "${MediaStore.Audio.Media.IS_RINGTONE} != 1"

        val query = activity!!.contentResolver.query(songUri, null, selection, null, null )

        query?.use { cursor ->
            Log.e(TAG, "000000000000000000000000")
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val dateAddedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val mimeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
            val isMusicC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
            val isAlarmC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_ALARM)
            val isNotifC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_NOTIFICATION)
            val isPodC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_PODCAST)
            val isRingC = cursor.getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE)
            Log.e(TAG, cursor.count.toString())
            while (cursor.moveToNext()) {
                Log.e(TAG, "==========================")
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val album = cursor.getString(albumColumn)
                val artist = cursor.getString(artistColumn)
                val dateAdded = cursor.getString(dateAddedColumn)
                val mime = cursor.getString(mimeColumn)
                val isMusic = cursor.getString(isMusicC)
                val isAlarmC = cursor.getString(isAlarmC)
                val isNotif = cursor.getString(isNotifC)
                val isPod = cursor.getString(isPodC)
                val isRing = cursor.getString(isRingC)
                val audioUri: Uri = ContentUris.withAppendedId( MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id )
                //  Log.e(TAG, contentUri.toString())
//                Log.e(TAG, "id $id")
                Log.e(TAG, "audioUri $audioUri")
                Log.e(TAG, "title $title")
                Log.e(TAG, "album $album")
                Log.e(TAG, "artist $artist")
//                Log.e(TAG, "mime $mime")
//                Log.e(TAG, "is isMusic $isMusic")
//                Log.e(TAG, "is isAlarmC $isAlarmC")
//                Log.e(TAG, "is isNotif $isNotif")
//                Log.e(TAG, "is isPod $isPod")
//                Log.e(TAG, "is isRing $isRing")
                audioList.add(MainActivity.AudioFile(audioUri, title, artist))
            }
        }
        Log.e(TAG, "audioList")
        audioList.forEach { Log.e(TAG, it.toString()) }
        return audioList
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERM_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //Toast.makeText(this,":) Permission granted!", Toast.LENGTH_SHORT).show()
                    Log.e(TAG,":) Permission granted!")
                } else {
                    //Toast.makeText(this,":( Permission not granted.", Toast.LENGTH_SHORT).show()
                    Log.e(TAG,":( Permission not granted.")
                }
                return
            }
            // Add other 'when' lines to check for other  permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }




}
