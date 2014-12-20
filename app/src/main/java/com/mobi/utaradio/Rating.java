package com.mobi.utaradio;

/**
 * Created by Zedd on 12/20/2014.
 */
public class Rating {

    static int LIKED = 1;
    static int DISLIKED = -1;
    private String songName, artistName;
    private int rating;
    private boolean filled = false;     //this indicate if the rating has data to publish or not

    public Rating(String songName, String artistName, int rating) {
        this.songName = songName;
        this.artistName = artistName;
        this.rating = rating;
        filled = true;
    }

    /**
     * this resets the filled to false indicating rating was already published
     */
    public void reset() {
        this.songName = null;
        this.artistName = null;
        this.rating = 0;
        filled = false;
    }

    public boolean isFilled() {
        return filled;
    }

    public String getSongName() {
        return songName;
    }


    public String getArtistName() {
        return artistName;
    }


    public int getRating() {
        return rating;
    }


}
