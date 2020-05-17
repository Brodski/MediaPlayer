package com.bskimusicplayer.mediaplayer

class SongCreatedComparable(): Comparator<Song> {
    override fun compare(song1: Song, song2: Song): Int {
        return song2.dateCreated - song1.dateCreated
    }
}



class SongArtistComparable(): Comparator<Song> {
    override fun compare(song1: Song, song2: Song): Int {
        if (song2.artist != "" && song1.artist != "") {
            return song2.artist.toUpperCase().compareTo(song1.artist.toUpperCase())
        }
        else if (song2.artist != "" && song1.artist == "") {
            return song2.artist.toUpperCase().compareTo(song1.title.toUpperCase())
        }
        else if (song2.artist == "" && song1.artist != "") {
            return song2.title.toUpperCase().compareTo(song1.artist.toUpperCase())
        }
        else {
            return song2.title.toUpperCase().compareTo(song1.title.toUpperCase())
        }
    }
}

class SongTitleComparable(): Comparator<Song> {
    override fun compare(song1: Song, song2: Song): Int {
        if (song2.title != "" && song1.title != "") {
            return song2.title.toUpperCase().compareTo(song1.title.toUpperCase())
        }
        else if (song2.title != "" && song1.title == "") {
            return song2.title.toUpperCase().compareTo(song1.artist.toUpperCase())
        }
        else if (song2.title == "" && song1.title != "") {
            return song2.artist.toUpperCase().compareTo(song1.artist.toUpperCase())
        }
        else {
            return song2.artist.toUpperCase().compareTo(song1.artist.toUpperCase())
        }
    }
}