package com.example.mediaplayer

class SongCreatedComparable(): Comparator<Song> {
    override fun compare(song1: Song, song2: Song): Int {
        return song2.duration - song1.duration
    }
}

class SongArtistComparable(): Comparator<Song> {
    override fun compare(song1: Song, song2: Song): Int {
        println("WE ARE IN")
        println(song2.subText + ":" + song2.mainText)
        println(song1.subText + ":" + song1.mainText)
        println((song1.subText == ""))
        println((song1.mainText == ""))
        if (song2.subText != "" && song1.subText != ""){
            println("11111111111111")
            println((song2.subText != "" && song1.subText != "").toString())
            println("song2.subText " + song2.subText)
            println("song1.subText " + song1.subText)
            println(song2.subText.compareTo(song1.subText))
            return song2.subText.compareTo(song1.subText)
        }
        else if (song2.subText != "" && song1.subText == ""){
            println("22222222222222222")
            println((song2.subText != "" && song1.subText == "").toString())
            println("song2.subText " + song2.subText)
            println("song1.mainText " + song2.subText)
            println( song2.subText.compareTo(song1.mainText))
            return song2.subText.compareTo(song1.mainText)
        }
        else if (song2.subText == "" && song1.subText != ""){
            println("33333333333333333")
            println((song2.subText == "" && song1.subText != "").toString())
            println("song2.mainText " + song2.mainText)
            println("song1.subText " + song1.subText)
            println(song2.mainText.compareTo(song1.subText))
            return song2.mainText.compareTo(song1.subText)
        }
        else {
            println("44444444444444444444")
            println("song2.subText :" +song2.subText )
            println("song2.mainText :" + song2.mainText)
            println("song1.subText :" + song1.subText)
            println("song1.mainText :" + song1.mainText)
            println(song2.mainText.compareTo(song1.mainText))
            return song2.mainText.compareTo(song1.mainText)
        }
    }
}

class SongTitleComparable(): Comparator<Song> {
    override fun compare(song1: Song, song2: Song): Int {
        if (song2.mainText != "" && song1.mainText != "") {
            return song2.mainText.compareTo(song2.mainText)
        }
        else if (song2.mainText != "" && song1.mainText == "") {
            return song2.mainText.compareTo(song2.subText)
        }
        else if (song2.mainText == "" && song1.mainText != "") {
            return song2.subText.compareTo(song2.subText)
        }
        else {
            return 0
        }
    }
}