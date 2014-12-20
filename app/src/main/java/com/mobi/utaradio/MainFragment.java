package com.mobi.utaradio;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.mobi.utaradio.util.FastBlur;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Cameron on 9/24/2014.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    private Typeface quicksand;
    static TextView musicTitle, musicArtist, musicAlbum;
    static ImageView musicAlbumImage;
    private ImageButton btnPlay, btnLike, btnDislike, btnShare;
    static LinearLayout lLayout;
    private ViewSwitcher viewSwitcher;


    private Timer myTimer;
    private MediaPlayer mPlayer;

    static boolean allowAlbumImageRoation = true;    // THIS ALLOWS ROTATION OF THE ALBUM ART
    private Animation rotationAnimation;    //record rotation animation

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
        musicAlbumImage.startAnimation(rotationAnimation);
        /* Set UI Element attributes */
        musicTitle.setTypeface(quicksand);
        musicArtist.setTypeface(quicksand);
        musicAlbum.setTypeface(quicksand);

        musicAlbumImage.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnLike.setOnClickListener(this);
        btnDislike.setOnClickListener(this);
        btnShare.setOnClickListener(this);

        lLayout.getBackground().setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);
        //blur(((BitmapDrawable)musicAlbumImage.getDrawable()).getBitmap() ,lLayout);

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
                if (mPlayer.isPlaying()) {
                    //pause + change button image to pause
                    mPlayer.pause();
                    btnPlay.setImageResource(R.drawable.play);
                } else {
                    //play + change button image to play + updateSongInfo
                    mPlayer.start();
                    btnPlay.setImageResource(R.drawable.pause);
                    updateSongInfo();
                }

                break;
            case R.id.music_like_imagebutton:
                Toast.makeText(v.getContext(), "Like", Toast.LENGTH_SHORT).show();
                break;
            case R.id.music_dislike_imagebutton:
                Toast.makeText(v.getContext(), "Dislike", Toast.LENGTH_SHORT).show();
                break;
            case R.id.music_share_imagebutton:
                String message = "Yo dawg, check out these mad beats!";
                String contextTitle = "Select app to share";

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(shareIntent, contextTitle));


                //ShareDialog shareDialog = new ShareDialog(v.getContext(), matches);
                //shareDialog.show();

                break;
            case R.id.music_album_imageview:
                //this is an easter egg
                if(allowAlbumImageRoation && rotationAnimation.hasEnded()){
                    musicAlbumImage.startAnimation(rotationAnimation);
                }
        }
    }

    /**
     * Pause getting the album info
     */
    @Override
    public void onPause() {
        myTimer.cancel();
        myTimer.purge();
        myTimer = null;
        super.onPause();
    }

    /**
     * Resume getting the album art
     */
    @Override
    public void onResume() {
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateSongInfo();
            }
        }, 0, 10000);
        super.onResume();
    }

    void updateSongInfo() {
        new LoadDataFromXML().execute("http://radio.uta.edu/_php/nowplaying.php");
    }
}
