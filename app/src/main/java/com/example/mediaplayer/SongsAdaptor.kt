package com.example.mediaplayer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.song_item.view.*


// songsList = songsList
// type argument <>
class SongsAdaptor(private val songList: List<Song>, private val onItemListener: OnItemListener) : RecyclerView.Adapter<SongsAdaptor.SongsViewHolder>() {

    // ctrl I
    // called by rcyc viewr when its time to create a new view holder, return type is ExViewhodler
    override fun onCreateViewHolder( parent: ViewGroup, viewType: Int ): SongsViewHolder {
        // class responsible 4 turning xml layoutfiles to View Objects is layoutInflater (from android fraimwork)
        // .inflate turns layout to view
        // handle to layout inflator, parent = recycler viwer, .context = our activiy of rec
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return SongsViewHolder(itemView, onItemListener)
    }

    // is called over and over, when scroll and new item, called maybe many times per second
    override fun onBindViewHolder( holder: SongsViewHolder, position: Int ) {
        val currentItem = songList[position]

        holder.imageView.setImageBitmap(currentItem.art)
        holder.songMainText.text = currentItem.mainText
        holder.songSubText.text = currentItem.subText

//        if (position == 0) { holder.tv1.setBackgroundColor(Color.YELLOW) }

    }
    override fun getItemCount() = songList.size


    // row
    class SongsViewHolder(itemView: View, val onItemListener: OnItemListener ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        // view caching functioanlty added by us.. pass rows
        //synthetic propterty is kotlin
        val imageView = itemView.imageId // == to findviewbyId(R.id.imageId)
        val songMainText: TextView = itemView.songMainTextId
        val songSubText: TextView = itemView.songSubTextId

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onItemListener.onItemClick(adapterPosition)
        }
    }

    interface OnItemListener {
        fun onItemClick(postion: Int)
    }
}