package com.example.mediaplayer

import android.graphics.Bitmap
import android.net.Uri

data class Song(
    var imageResource: Int,
    var mainText: String,
    var subText:String,
    var uri: Uri? = null,
    var art: Bitmap? = null
)