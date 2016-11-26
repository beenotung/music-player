package com.github.beenotung.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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

    public static void startForeground(Service service, int noticeId, String msg) {
        Notification.Builder builder = new Notification.Builder(MainActivity.mainActivity);
        builder.setSmallIcon(android.R.drawable.ic_media_play);
        builder.setTicker("ticker: " + System.currentTimeMillis());
        builder.setContentText(msg);
        Intent notificationIntent = new Intent(service, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(service, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        service.startForeground(noticeId, notification);
    }

    public static interface Supplier<A> {
        A apply();
    }
}
