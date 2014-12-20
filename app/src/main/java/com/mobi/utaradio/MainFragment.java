package com.mobi.utaradio;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Cameron on 9/24/2014.
 * Last updated 12/20/2014 by Zedd
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    private Typeface quicksand;
    static TextView musicTitle, musicArtist, musicAlbum;
    static ImageView musicAlbumImage;
    private ImageButton btnPlay, btnShare;
    static ImageButton btnLike, btnDislike;
    static LinearLayout lLayout;
    private ViewSwitcher viewSwitcher;

    private Timer myTimer;
    private MediaPlayer mPlayer;
    private Animation jump; //This is a simple jump animation, gives nice feedback

    static boolean allowAlbumImageRoation = true;    // THIS ALLOWS ROTATION OF THE ALBUM ART
    private Animation rotationAnimation;    //record rotation animation

    //this is used to contain rating information
    static Rating rating;       //it needs to be static

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        /* Link to UI Elements */
        quicksand = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Quicksand-Regular.ttf");
        lLayout = (LinearLayout) rootView.findViewById(R.id.music_layout);
        musicTitle = (TextView) rootView.findViewById(R.id.music_song_textview);
        musicTitle.setSelected(true);
        musicArtist = (TextView) rootView.findViewById(R.id.music_artist_textview);
        musicAlbum = (TextView) rootView.findViewById(R.id.music_album_textview);
        btnPlay = (ImageButton) rootView.findViewById(R.id.music_play_imagebutton);
        btnLike = (ImageButton) rootView.findViewById(R.id.music_like_imagebutton);
        btnDislike = (ImageButton) rootView.findViewById(R.id.music_dislike_imagebutton);
        btnShare = (ImageButton) rootView.findViewById(R.id.music_share_imagebutton);

        //view switcher and its animation
        viewSwitcher = (ViewSwitcher) rootView.findViewById(R.id.viewswitcher);
        Animation fadeIn = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.fade_out);
        viewSwitcher.setInAnimation(fadeIn);
        viewSwitcher.setOutAnimation(fadeOut);

        //setting the album image and its animation
        musicAlbumImage = (ImageView) rootView.findViewById(R.id.music_album_imageview);
        rotationAnimation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate);
        btnPlay.startAnimation(rotationAnimation);
        /* Set UI Element attributes */
        musicTitle.setTypeface(quicksand);
        musicArtist.setTypeface(quicksand);
        musicAlbum.setTypeface(quicksand);

        musicAlbumImage.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnLike.setOnClickListener(this);
        btnDislike.setOnClickListener(this);
        btnShare.setOnClickListener(this);

        //add a red hue to the background, christmas theme
        lLayout.getBackground().setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);

        //initializing jump animation used later
        jump = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.jump);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource("rtsp://webmedia-2.uta.edu:1935/uta_radio/live");
            mPlayer.prepareAsync(); //Built-in media player AsyncTask
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (mp == mPlayer) {
                        mPlayer.start();    //starting the player
                        btnPlay.setImageResource(R.drawable.pause); //showing the pause button
                        viewSwitcher.showNext();    //moving to the player controls
                    }
                }
            });

            //DEVELOPMENT CHOICE: timer waits 10 seconds to get the album art
            myTimer = new Timer();
//        Parameters
//        task  the task to schedule.
//        delay  amount of time in milliseconds before first execution.
//        period  amount of time in milliseconds between subsequent executions.
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateSongInfo();
                }
            }, 0, 10000);
        } catch (IllegalArgumentException e) {
            Log.d("DEBUG", e.toString());
        } catch (SecurityException e) {
            Log.d("DEBUG", e.toString());
        } catch (IllegalStateException e) {
            Log.d("DEBUG", e.toString());
        } catch (IOException e) {
            Log.d("DEBUG", e.toString());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_play_imagebutton:
                v.startAnimation(jump); //make the view jump
                if (mPlayer.isPlaying()) {
                    //pause + change button image to pause
                    mPlayer.pause();
                    btnPlay.setImageResource(R.drawable.play); //we set the image of the next state
                    enableLoadingContent(false);   //we disable loading song content
                } else {
                    //play + change button image to play + updateSongInfo
                    mPlayer.start();
                    btnPlay.setImageResource(R.drawable.pause); //we set the image of the next state
                    enableLoadingContent(true);     //we enable loading content
                }
                break;
            case R.id.music_like_imagebutton:
                v.startAnimation(jump); //make the view jump
                //Toast.makeText(v.getContext(), "Like", Toast.LENGTH_SHORT).show();
                setTrackLiked(true);
                break;
            case R.id.music_dislike_imagebutton:
                v.startAnimation(jump); //make the view jump
                //Toast.makeText(v.getContext(), "Dislike", Toast.LENGTH_SHORT).show();
                setTrackLiked(false);
                break;
            case R.id.music_share_imagebutton:
                String message = "Yo dawg, check out these mad beats!";
                String contextTitle = "Select app to share";

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(shareIntent, contextTitle));
                break;
            case R.id.music_album_imageview:
                //this is an easter egg
                if (allowAlbumImageRoation && rotationAnimation.hasEnded()) {
                    musicAlbumImage.startAnimation(rotationAnimation);
                }
        }
    }

    /**
     * This method disables and enables the loading of new song titles and images
     * it does that by enabling and disabling the 10 second timer
     */
    public void enableLoadingContent(boolean enabled) {
        if (enabled) { //we make a new timer
            myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateSongInfo();
                }
            }, 0, 10000);
        } else {
            // we kill the current timer
           if(myTimer != null) {
               myTimer.cancel();
               myTimer.purge();
               myTimer = null;
           }
        }
    }

    /**
     * Pause getting the album info
     */
    @Override
    public void onPause() {
        enableLoadingContent(false);
        super.onPause();
    }

    /**
     * Resume getting the album art
     */
    @Override
    public void onResume() {
        enableLoadingContent(true);
        super.onResume();
    }

    void updateSongInfo() {
        new LoadDataFromXML().execute("http://radio.uta.edu/_php/nowplaying.php");
    }

    /**
     * We add a the liked music to a list and we increment a counter of the like music
     * @param liked a boolean value
     */
    public void setTrackLiked(boolean liked) {
        if (liked) {
            //HANDLE ui colors
            btnLike.setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            btnDislike.setColorFilter(null);
            //we make a new rating object and set it as liked
            rating = new Rating(musicTitle.getText().toString(),
                                musicArtist.getText().toString(),
                                Rating.LIKED);
        } else {
            //HANDLE ui colors
            btnDislike.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
            btnLike.setColorFilter(null);
            //we make a new rating object and set it as disliked
            rating = new Rating(musicTitle.getText().toString(),
                    musicArtist.getText().toString(),
                    Rating.DISLIKED);
        }
    }

    /*
    this is purely cosmetic, removes the color filter from the buttons
 */
    static void resetTrackLiked(){
        btnDislike.setColorFilter(null);
        btnLike.setColorFilter(null);
    }

    /**
     * This publish the rating to the data base
     * Data is only published once the song is changed!
     * Called only when new songName and artistName is obtained
     * It published the data in the Rating Object
     */
    static void publishRating(){
        if(rating != null && rating.isFilled())
        {
            Log.d("DEBUG", "I am rating a song");
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Ratings");
            query.whereEqualTo("songName", rating.getSongName());
            query.whereEqualTo("artistName", rating.getArtistName());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if(e == null)
                    {
                        if(parseObjects.isEmpty()){
                            //ok object is NOT already in the database
                            //we make a new object
                            //rating is ready to be published
                            ParseObject songRating = new ParseObject("Ratings");
                            if(rating.getSongName() != null){
                                songRating.put("songName", rating.getSongName());
                            }
                            if(rating.getArtistName() != null){
                                songRating.put("artistName", rating.getArtistName());
                            }
                            songRating.increment("rating", rating.getRating());
                            songRating.saveInBackground();   //save the new object to the database
                            rating.reset(); //reset the rating indicating its published
                        } else {
                            //ok object is already in the database
                            //we edit the object
                            parseObjects.get(0).increment("rating", rating.getRating());
                            parseObjects.get(0).saveInBackground(); //save the results back
                            rating.reset(); //reset the rating indicating its published

                        }
                    } else { //database error
                        Log.d("DEBUG", "Database Error:\n" + e.toString());
                    }

                }
            });


        } else {
            Log.d("DEBUG", "No stuff to rate");
        }
    }
}
