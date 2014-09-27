package com.mobi.utaradio;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;
=======
import java.io.IOException;
<<<<<<< HEAD
import java.util.Timer;
import java.util.TimerTask;
=======
>>>>>>> origin/master
>>>>>>> origin/master

/**
 * Created by Cameron on 9/24/2014.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    private Typeface quicksand;
    static TextView musicTitle, musicArtist, musicAlbum;
    static ImageView musicAlbumImage;
    private ImageButton btnPlay, btnLike, btnDislike, btnShare;

    private MediaPlayer mPlayer = new MediaPlayer();

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        /* Link to UI Elements */
        quicksand = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Quicksand-Regular.ttf");
        musicTitle = (TextView) rootView.findViewById(R.id.music_song_textview);
        musicArtist = (TextView) rootView.findViewById(R.id.music_artist_textview);
        musicAlbum = (TextView) rootView.findViewById(R.id.music_album_textview);
        btnPlay = (ImageButton) rootView.findViewById(R.id.music_play_imagebutton);
        btnLike = (ImageButton) rootView.findViewById(R.id.music_like_imagebutton);
        btnDislike = (ImageButton) rootView.findViewById(R.id.music_dislike_imagebutton);
        btnShare = (ImageButton) rootView.findViewById(R.id.music_share_imagebutton);
        musicAlbumImage = (ImageView) rootView.findViewById(R.id.music_album_imageview);

        /* Set UI Element attributes */
        musicTitle.setTypeface(quicksand);
        musicArtist.setTypeface(quicksand);
        musicAlbum.setTypeface(quicksand);

        btnPlay.setOnClickListener(this);
        btnLike.setOnClickListener(this);
        btnDislike.setOnClickListener(this);
        btnShare.setOnClickListener(this);

        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            mPlayer.setDataSource("rtsp://webmedia-2.uta.edu:1935/uta_radio/live");
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //DEVELOPMENT CHOICE: timer waits 10 to get the album art
            Timer myTimer = new Timer();
//        Parameters
//        task  the task to schedule.
//        delay  amount of time in milliseconds before first execution.
//        period  amount of time in milliseconds between subsequent executions.
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new LoadDataFromXML().execute("http://radio.uta.edu/_php/nowplaying.php");
                }
            }, 0, 10000);
        } catch (IllegalArgumentException e) {
            Log.d("DEBUG", e.toString());
        } catch (SecurityException e) {
            Log.d("DEBUG", e.toString());
        } catch (IllegalStateException e) {
            Log.d("DEBUG", e.toString());
        } catch (IOException e){
            Log.d("DEBUG", e.toString());
        }
        try {
            mPlayer.prepare();
            mPlayer.start();
            btnPlay.setImageResource(R.drawable.pause);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.music_play_imagebutton:
                if (mPlayer.isPlaying() )
                {
                    //pause + change button image to pause
                    mPlayer.pause();
                    btnPlay.setImageResource(R.drawable.play);
                } else {
                    //play + change button image to play
                    mPlayer.start();
                    btnPlay.setImageResource(R.drawable.pause);

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
        }
    }
}
