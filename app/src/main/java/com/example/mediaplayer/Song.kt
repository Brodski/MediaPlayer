package com.example.mediaplayer

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class Song(
    var id: Int = -1,
    var title: String = "",
    var artist: String = "",
    var dateCreated: Int = 0,
    var duration: Int = 0,
    var uri: Uri? = null,
    var art: Bitmap? = null
    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readParcelable(Bitmap::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeInt(dateCreated)
        parcel.writeInt(duration)
        parcel.writeParcelable(uri, flags)
        parcel.writeParcelable(art, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}
