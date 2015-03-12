package com.mobi.utaradio;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Cameron on 3/9/15.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private final IBinder musicBind = new MusicBinder();
    private Intent broadcastIntent;

    private boolean hasBeenPrepared = false;

    public static final String BROADCAST_ACTION = "com.mobi.utaradio.broadcast-action";

    public static final int ACTION_PREPARED = 0;
    public static final int ACTION_PLAY = 1;
    public static final int ACTION_PAUSE = 2;

    @Override
    public void onCreate() {
        super.onCreate();

        player = new MediaPlayer();
        broadcastIntent = new Intent(BROADCAST_ACTION);
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Notification rolls in on the start
        Notification notification = new Notification(R.drawable.ic_launcher,
                "UTA Radio is currently playing...", System.currentTimeMillis());

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        //Set dropdown content
        notification.setLatestEventInfo(this,
                "Artist - Song", "UTA Radio is currently playing...", contentIntent);

        startForeground(1, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    public boolean isPlaying() { return player.isPlaying(); }
    public boolean hasBeenPrepared() { return hasBeenPrepared; }
    public void pause() { player.pause(); }
    public void play() { player.start(); }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e("MUSIC", "Completed");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        hasBeenPrepared = true;
        broadcastIntent.putExtra(BROADCAST_ACTION, ACTION_PREPARED);
        sendBroadcast(broadcastIntent);
    }

    public class MusicBinder extends Binder {

        MusicService getService() {
            return MusicService.this;
        }

    }

    private void initMediaPlayer() {
        try {
            player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource("rtsp://webmedia-2.uta.edu:1935/uta_radio/live");
            player.prepareAsync(); //Built-in media player AsyncTask

            player.setOnPreparedListener(this);
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("RADIO STREAM", "IO EXCEPTION COULD NOT CREATE PLAYER : " + e.toString());
        }
    }
}
