package com.bskimusicplayer.mediaplayer

class SongCreatedComparable(): Comparator<Song> {
    override fun compare(song1: Song, song2: Song): Int {
        return song2.dateCreated - song1.dateCreated
    }
}



class SongArtistComparable(): Comparator<Song> {
    override fun compare(song1: Song, song2: Song): Int {
        if (song2.artist != "" && song1.artist != "") {
            shit(song2.artist, song1.artist)
            return song2.artist.toUpperCase().compareTo(song1.artist.toUpperCase())
        }
        else if (song2.artist != "" && song1.artist == "") {
            shit(song2.artist, song1.title)
            return song2.artist.toUpperCase().compareTo(song1.title.toUpperCase())
        }
        else if (song2.artist == "" && song1.artist != "") {
            shit(song2.title, song1.artist)
            return song2.title.toUpperCase().compareTo(song1.artist.toUpperCase())
        }
        else {
            shit(song2.title, song1.title)
            return song2.title.toUpperCase().compareTo(song1.title.toUpperCase())
        }
    }
    fun shit(s2:String, s1:String){
        println("------------------")
        println("${s2.toUpperCase().take(6)} > ${s1.toUpperCase().take(6)} ")
        if ( s2.toUpperCase()>s1.toUpperCase()){
            println(s2.toUpperCase())
        } else {
            println(s1.toUpperCase())
        }
    }
}

class SongTitleComparable(): Comparator<Song> {
    override fun compare(song1: Song, song2: Song): Int {
        println("song1 artist: |${song1.artist}| - ${song1.artist.length}" )
        println("song1 title: |${song1.title}| - ${song1.title.length}" )
        println("song2 artist: |${song2.artist}| - ${song2.artist.length}" )
        println("song2 title: |${song2.title}| - ${song2.title.length}" )
        if (song2.title != "" && song1.title != "") {
            println("song2 title: ${song2.title} " )
            println("song1 title: ${song1.title} " )
            return song2.title.toUpperCase().compareTo(song1.title.toUpperCase())
        }
        else if (song2.title != "" && song1.title == "") {
            println("song2 title: ${song2.title} " )
            println("song1 title: ${song1.title} " )
            return song2.title.toUpperCase().compareTo(song1.artist.toUpperCase())
        }
        else if (song2.title == "" && song1.title != "") {
            println("song2 title: ${song2.artist} " )
            println("song1 title: ${song1.title} " )
            return song2.artist.toUpperCase().compareTo(song1.artist.toUpperCase())
        }
        else {
            println("song2 artist: ${song2.artist} " )
            println("song1 artist: ${song1.artist} " )
            return song2.artist.toUpperCase().compareTo(song1.artist.toUpperCase())
        }
    }
}