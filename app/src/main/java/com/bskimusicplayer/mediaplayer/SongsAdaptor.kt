package com.bskimusicplayer.mediaplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.song_item.view.*

class SongsAdaptor( private val songList: MutableList<Song>, private val onItemListener: OnItemListener)
    : RecyclerView.Adapter<SongsAdaptor.SongsViewHolder>(), Filterable {
    private val songListFull: ArrayList<Song> = ArrayList(songList)


    // called by rcyc viewr when its time to create a new view holder, return type is ExViewhodler
    override fun onCreateViewHolder( parent: ViewGroup, viewType: Int ): SongsViewHolder {
        // class responsible for turning xml layoutfiles to View Objects is layoutInflater (from android fraimwork)
        // .inflate turns layout to view
        // handle to layout inflator, parent = recycler viwer, .context = our activiy of rec
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return SongsViewHolder(itemView, onItemListener)
    }

    // is called over and over, when scroll and new item, called maybe many times per second
    override fun onBindViewHolder( holder: SongsViewHolder, position: Int ) {

        holder.imageView.setImageBitmap(songList[position].art)
        holder.songMainText.text = songList[position].title
        holder.songSubText.text = songList[position].artist
        holder.uriText.text = songList[position].uri.toString()
        //if (position == 0) { holder.tv1.setBackgroundColor(Color.YELLOW) }
    }

    override fun getItemCount() = songList.size

    class SongsViewHolder(itemView: View, val onItemListener: OnItemListener ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val imageView = itemView.imageId
        val songMainText: TextView = itemView.songMainTextId
        val songSubText: TextView = itemView.songSubTextId
        val uriText = itemView.songUriId

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
           // val x0 = itemView.songMainTextId.text.toString()
            val x = itemView.songUriId.text.toString()
            onItemListener.onItemClick(adapterPosition, x)
        }
    }

    interface OnItemListener {
        fun onItemClick(position: Int, text: String)
    }

    override fun getFilter(): Filter {
     return songFilter
    }

    private val songFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<Song> = ArrayList<Song>()
            val pattern = "[,._'\"\\-\\s]"
            if (constraint ==  null || constraint.isEmpty()) {
                filteredList.addAll(songListFull)
            } else {
                //val filterPattern = constraint.toString().toLowerCase().trim()
                val filterPattern = constraint.toString().toLowerCase().replace(Regex(pattern),"").trim()
//                Log.e("BAM", "filterPattern: $filterPattern")
                songListFull.forEach {
//                    Log.e("BAM", it.artist!!.toLowerCase().replace(Regex(pattern),"").trim())
                    if (it.title != null && it.title != "") {
                        if (it.title!!.toLowerCase().replace(Regex(pattern),"") .trim().contains(filterPattern)) {
                            filteredList.add(it)
                        } else if (it.artist!!.toLowerCase().replace(Regex(pattern),"") .trim().contains(filterPattern)) {
                            filteredList.add(it)
                        }
                    }
                }
            }
            var results: FilterResults = FilterResults()
            results.values = filteredList
            return results
        }
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null) {
                songList.clear()
                songList.addAll(results.values as MutableList<Song>)
                notifyDataSetChanged()
            }

        }
    }
}