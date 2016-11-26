package com.github.beenotung.musicplayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by beenotung on 11/25/16.
 */
public class Folder {
    private File file;
    String title;
    String path;
    int id;
    boolean checked = false;

    private long size = -1;

    File file() {
        if (this.file == null) {
            this.file = new File(this.path);
        }
        return this.file;
    }

    long size() {
        if (size == -1) {
            size = Utils.size(this.file());
        }
        return size;
    }

    public Folder() {
    }

    public Folder(int id, File file) {
        this.file = file;
        this.id = id;
        this.path = file.getAbsolutePath();
        if (this.path.equals("/")) {
            this.title = "root";
        } else {
            this.title = file.getName();
        }
    }

    @Override
    public String toString() {
        JSONObject res = new JSONObject();
        try {
            res.put("title", title);
            res.put("path", path);
            res.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
        return res.toString();
    }
}
