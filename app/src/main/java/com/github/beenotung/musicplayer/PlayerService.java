package com.github.beenotung.musicplayer;

import android.app.IntentService;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import static com.github.beenotung.musicplayer.Playlist.playlist;

/**
 * Created by beenotung on 11/25/16.
 */
public class PlayerService extends IntentService {
    final String TAG = getClass().getName();
    public static final String ACTION_INIT = "INIT";
    //    public static final String ACTION_PLAY = "PLAY";
//    public static final String ACTION_STOP = "STOP";
//    public static final String ACTION_PAUSE = "PAUSE";
//    public static final String ACTION_RESUME = "RESUME";
    final IBinder playerBinder = new PlayerBinder();

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return playerBinder;
    }

    MediaPlayer mediaPlayer;

    public PlayerService() {
        super(PlayerService.class.getName());
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.d(getClass().getName(), "Handle: " + action);
    }

    public boolean isPlaying() {
        try {
            return mediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            return false;
        }
    }


}
