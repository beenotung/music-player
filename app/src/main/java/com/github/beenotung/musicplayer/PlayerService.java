package com.github.beenotung.musicplayer;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import static com.github.beenotung.musicplayer.Playlist.playlist;

/**
 * Created by beenotung on 11/25/16.
 */
public class PlayerService extends IntentService implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    public static PlayerService playerService;
    public static final String ACTION_PLAY = PlayerService.class.getCanonicalName() + ".PLAY";
    public static final String ACTION_TOGGLE = PlayerService.class.getCanonicalName() + ".TOGGLE";
    public static final String ACTION_STOP = PlayerService.class.getCanonicalName() + ".STOP";
    final int ID_START = 1;
    final int ID_STOP = 2;
    MediaPlayer mediaPlayer = null;

    public PlayerService() {
        super(PlayerService.class.getName());
        playerService = this;
    }

    MediaPlayer initMediaPlayer() {
        MediaPlayer res = new MediaPlayer();
//        MediaPlayer res = MediaPlayer.create(MainActivity.mainActivity, Uri.fromFile(new File(playlist.currentSongPath())));
        res.setAudioStreamType(AudioManager.STREAM_MUSIC);
        res.setOnPreparedListener(this);
        res.setOnErrorListener(this);
        return res;
    }

    void playSong(String path) throws IOException {
        synchronized (this) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(getClass().getName(), "onHandleIntent: " + intent.getAction());
        if (intent.getAction().equals(ACTION_PLAY)) {
            try {
                mediaPlayer = initMediaPlayer();
                mediaPlayer.setDataSource(playlist.currentSongPath());
                mediaPlayer.prepareAsync();
                String songName = playlist.songs.get(playlist.idx()).name;
                Notification.Builder builder = new Notification.Builder(MainActivity.mainActivity);
                builder.setSmallIcon(android.R.drawable.ic_media_play);
                builder.setTicker("ticker: " + System.currentTimeMillis());
                builder.setContentText("Playing: " + songName);
                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
                builder.setContentIntent(pendingIntent);
                Notification notification = builder.build();
                startForeground(ID_START, notification);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (intent.getAction().equals(ACTION_STOP)) {
            stopForeground(true);
            stopSelf();
        } else if (intent.getAction().equals(ACTION_TOGGLE)) {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
            }
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e(getClass().getName(), "MediaPlayer onPrepared");
        mediaPlayer = mp;
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mp.start();
    }

    void stopSong() {
        mediaPlayer.stop();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(getClass().getName(), "MediaPlayer onError what:" + what + " extra:" + extra);
        mp.reset();
        return false;
    }
}
