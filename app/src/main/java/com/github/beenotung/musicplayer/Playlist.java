package com.github.beenotung.musicplayer;

import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by beenotung on 11/25/16.
 */
public class Playlist {
    public static Playlist playlist = new Playlist();

    private Playlist() {
    }

    public static class Song {
        static final String TAG = Song.class.getName();
        String path;
        String filename; // without extension
        @Nullable
        String title = null;
        boolean isSelected = false;
        boolean hide = false;

        @Nullable
        public static String guessCharset(@NonNull String s) {
            UniversalDetector detector = new UniversalDetector(null);
            byte[] bytes = s.getBytes();
            detector.handleData(bytes, 0, bytes.length);
            detector.dataEnd();
            return detector.getDetectedCharset();
        }

        /* reference : http://stackoverflow.com/questions/229015/encoding-conversion-in-java */
        public static String changeCharset(String charsetName, String data) throws CharacterCodingException {
            Charset charset = Charset.forName(charsetName);
            CharsetDecoder decoder = charset.newDecoder();
            CharsetEncoder encoder = charset.newEncoder();

            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(data));

            CharBuffer cbuf = decoder.decode(bbuf);
            return cbuf.toString();
        }

        public Song(File file) {
            this.path = file.getAbsolutePath();
            this.filename = file.getName();
            int idx = filename.lastIndexOf('.');
            if (idx >= 0) {
                this.filename = this.filename.substring(0, idx);
            }
        }

        /* deferred task, to speed up launch speed */
        void checkName() {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path);
            this.title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            try {
                if (this.title != null) {
                    if (this.title.contains(this.filename) || this.filename.contains(this.title)) {
                        this.filename = this.title.length() < this.filename.length()
                                ? this.title
                                : this.filename;
                        this.title = null;
                    }
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "odd things happened", e);
                this.title = null;
            }
            if (this.title != null) {
                title = title.replaceAll("\uFFFD", "\"");
                String charset = guessCharset(this.title);
                if (charset == null) {
                    this.title = null;
                } else {
                    try {
                        this.title = changeCharset(charset, this.title);
                    } catch (Exception e) {
                        this.title = null;
                    }
                }
            }
        }

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

    final Object songsLock = new Object();
    boolean checkedName = false;
    ArrayList<Song> songs = new ArrayList<>();
    private int idx = 0;

    @Nullable
    public String currentSongName() {
        if (songs.size() == 0)
            return null;
        Song song = songs.get(idx());
        return song.title == null
                ? song.filename
                : song.title;
    }

    @Nullable
    public String currentSongPath() {
        if (songs.size() == 0)
            return null;
        return songs.get(idx()).path;
    }

    @Nullable
    public Song currentSong() {
        if (songs.size() == 0)
            return null;
        return songs.get(idx());
    }

    int idx() {
        if (songs.size() == 0)
            return -1;
        return idx % songs.size();
    }

    void idx(int newVal) {
        if (newVal != idx) {
            idx = newVal % songs.size();
        }
    }

    void reset() {
        synchronized (songsLock) {
            checkedName = false;
            songs.clear();
        }
    }

    final String TAG_ADD = getClass().getName() + ":ADD";

    private void addFromFolder(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null)
                for (File f : files) {
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
                        Song song = new Song(file);
                        songs.add(song);
                        break;
                    default:
                }
            }
        }
    }

    private void addFromFolder(String folder) {
        addFromFolder(new File(folder));
    }

    void scanSongs(ArrayList<Folder> folders) {
        synchronized (songsLock) {
            checkedName = false;
            songs.clear();
            for (Folder folder : folders) {
                playlist.addFromFolder(folder.path);
            }
        }
    }

    boolean isEmpty() {
        synchronized (songsLock) {
            return songs.isEmpty();
        }
    }

    void replaceSongs(ArrayList<Song> xs) {
        synchronized (songsLock) {
            songs = xs;
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

    /**
     * @deprecated slow
     * */
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
