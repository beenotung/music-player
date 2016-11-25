package com.github.beenotung.musicplayer;

import android.support.annotation.Nullable;

import java.io.File;
import java.util.HashMap;

/**
 * Created by beenotung on 11/25/16.
 */
public class Utils {
    public static boolean hasNull(@Nullable Object... xs) {
        for (Object x : xs) {
            if (x == null)
                return true;
        }
        return false;
    }

    static HashMap<String, Long> sizeCache = new HashMap<>();

    public static long size(File file) {
        if (sizeCache.containsKey(file.getAbsolutePath()))
            return sizeCache.get(file.getAbsolutePath());
        long acc = 0;
        if (file.isFile())
            acc = file.length();
        File[] files = file.listFiles();
        if (files != null)
            for (File file1 : files) {
                acc += size(file1);
            }
        sizeCache.put(file.getAbsolutePath(), acc);
        return acc;
    }
}
