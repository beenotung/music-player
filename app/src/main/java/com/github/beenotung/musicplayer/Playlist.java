package com.github.beenotung.musicplayer;

import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

    public String currentSongName() {
        if (songs.size() == 0)
            return null;
        return songs.get(idx()).name;
    }

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
                        break;
                    default:
                }
            }
        }
    }

    void addFromFolder(String folder) {
        addFromFolder(new File(folder));
    }

    synchronized void scanSongs(ArrayList<Folder> folders) {
        playlist.reset();
        for (Folder folder : folders) {
            playlist.addFromFolder(folder.path);
        }
    }

    /**
     * @deprecated slow
     * */
    synchronized void scanSongs(ArrayList<Folder> folders, SharedPreferences sharedPreferences) {
        Set<String> ss = sharedPreferences.getStringSet("hidden_ids", new HashSet<String>());
        Set<Long> ids = new HashSet<>();
        for (String x : ss) {
            ids.add(Long.parseLong(x));
        }
        playlist.reset();
        for (Folder folder : folders) {
            playlist.addFromFolder(folder.path);
        }
        ArrayList<Song> xs = new ArrayList<>();
        for (Song song : songs) {
            try {
                if (!ids.contains(song.id())) {
                    xs.add(song);
                }
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
                xs.add(song);
            }
        }
        songs = xs;
    }

    void hideFiles(Set<Long> ids, SharedPreferences sharedPreferences) throws IOException, NoSuchAlgorithmException {
        HashSet<String> ss = new HashSet<>();
        for (Long id : ids) {
            ss.add(Long.toString(id));
        }
        sharedPreferences.edit().putStringSet("hidden_ids", ss).apply();
        ArrayList<Song> xs = new ArrayList<>();
        for (Song song : songs) {
            if (!ids.contains(song.id())) {
                xs.add(song);
            }
        }
        songs = xs;
    }
}
