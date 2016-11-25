package com.github.beenotung.musicplayer;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by beenotung on 11/25/16.
 */
class Playlist {
    public static Playlist playlist = new Playlist();

    private Playlist() {
    }

    class Song {
        String path;
        String name;
        boolean isSelected = false;
        boolean hide = false;

        long id() throws NoSuchAlgorithmException, IOException {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            FileInputStream inputStream = new FileInputStream(new File(this.path));
            byte[] bs = new byte[8192];
            int read;
            while ((read = inputStream.read(bs)) > 0) {
                md5.update(bs, 0, read);
            }
            bs = md5.digest();
            long res = 0;
            for (byte b : bs) {
                res = res << 8 | b;
            }
            return res;
        }
    }

    ArrayList<Song> songs = new ArrayList<>();
    private int idx = 0;

    public String currentSongPath() {
        if (songs.size() == 0)
            return null;
        return songs.get(idx()).path;
    }

    int idx() {
        if (songs.size() == 0)
            return -1;
        return idx % songs.size();
    }

    void idx(int newVal) {
        if (newVal != idx) {
            idx = newVal % songs.size();
            // TODO change song
        }
    }

    void reset() {
        songs.clear();
    }

    final String TAG_ADD = getClass().getName() + ":ADD";

    void addFromFolder(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFromFolder(f);
            }
        } else {
            String[] xs = file.getName().split(".");
            int idx = file.getName().lastIndexOf('.');
            if (idx > 0) {
                String ext = file.getName().substring(idx + 1);
                switch (ext.toLowerCase()) {
                        /* audio */
                    case "3gp":
                    case "mp4":
                    case "m4a":
                    case "aac":
                    case "ts":
                    case "flac":
                    case "mp3":
                    case "mid":
                    case "xmf":
                    case "mxmf":
                    case "rtttl":
                    case "rtx":
                    case "ota":
                    case "imy":
                    case "ogg":
                    case "mkv":
                    case "wav":
                        /* video */
                    case "webm":
                        Song song = new Song();
                        song.path = file.getAbsolutePath();
                        song.name = file.getName();
                        songs.add(song);
//                        Log.d(TAG_ADD, file.getAbsolutePath());
                        break;
                    default:
                }
            }
        }
    }

    void addFromFolder(String folder) {
        addFromFolder(new File(folder));
    }
}
