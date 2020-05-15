package com.bskimusicplayer.mediaplayer

class SongCreatedComparable(): Comparator<Song> {
    override fun compare(song1: Song, song2: Song): Int {
        return song2.dateCreated - song1.dateCreated
    }
}

class SongArtistComparable(): Comparator<Song> {
    override fun compare(song1: Song, song2: Song): Int {
        if (song2.artist != "" && song1.artist != "") {
            return song2.artist.compareTo(song1.artist)
        }
        else if (song2.artist != "" && song1.artist == "") {
            return song2.artist.compareTo(song1.title)
        }
        else if (song2.artist == "" && song1.artist != "") {
            return song2.title.compareTo(song1.artist)
        }
        else {
            return song2.title.compareTo(song1.title)
        }
    }
}

class SongTitleComparable(): Comparator<Song> {
    override fun compare(song1: Song, song2: Song): Int {
        println("song1 artist: ${song1.artist} " )
        println("song1 title: ${song1.title} " )
        println("song2 artist: ${song2.artist} " )
        println("song2 title: ${song2.title} " )
        if (song2.title != "" && song1.title != "") {
            println("song2 title: ${song2.title} " )
            println("song1 title: ${song1.title} " )
            return song2.title.compareTo(song1.title)
        }
        else if (song2.title != "" && song1.title == "") {
            println("song2 title: ${song2.title} " )
            println("song1 title: ${song1.title} " )
            return song2.title.compareTo(song1.artist)
        }
        else if (song2.title == "" && song1.title != "") {
            println("song2 title: ${song2.artist} " )
            println("song1 title: ${song1.title} " )
            return song2.artist.compareTo(song1.artist)
        }
        else {
            println("song2 artist: ${song2.artist} " )
            println("song1 artist: ${song1.artist} " )
            return song2.artist.compareTo(song1.artist)
        }
    }
}