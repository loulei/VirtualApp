package com.xdja.zs;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.xdja.activitycounter.ActivityCounterManager;

@SuppressLint("OverrideAbstract")
public class NotificationListener extends NotificationListenerService {
    String Tag = "zs_NotificationListener";

    public NotificationListener() {
    }

    private Context mApp = null;
    private PackageManager packageManager = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mApp = getApplicationContext();
        packageManager = mApp.getPackageManager();
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

        Log.e(Tag, "onListenerConnected");
    }

    private boolean isSystemApp(String pkgName)
    {
        boolean ret = false;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("isThirdApp: ");
            stringBuilder.append(pkgName);
            Log.d(Tag, stringBuilder.toString());
            if ((packageManager.getPackageInfo(pkgName, 0).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                ret = true;
            }
            return ret;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        Notification notification = sbn.getNotification();
        String packageName = sbn.getPackageName();
        Bundle extras = notification.extras;
        String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
        int notificationIcon = extras.getInt(Notification.EXTRA_SMALL_ICON);
        Bitmap notificationLargeIcon = ((Bitmap)extras.getParcelable(Notification.EXTRA_LARGE_ICON));
        CharSequence notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence notificationSubText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);

        Log.e(Tag, String.format("onNotificationPosted [Title: %s] [Text: %s] [SubText: %s] [pkgName %s]",
                notificationTitle, notificationText, notificationSubText, packageName));

        if(mApp.getPackageName().equals(packageName))
        {
            Log.e(Tag, String.format("\tthis notify cames from inside, ignore"));

            return;
        }
        Log.e(Tag, "currentSpace "+currentSpace());

        if(isSystemApp(packageName) && currentSpace())
        {
            Log.e(Tag, "\tdelete this notify " + sbn.getKey());
            cancelNotification(sbn.getKey());

            return;
        }

        if(currentSpace())
        {
            Log.e(Tag, "\tsnooze this notify " + sbn.getKey());
            snoozeNotification(sbn.getKey(), 10 * 1000);
        }
    }

    boolean currentSpace() {
        Context context = this;
        Uri CONTENT_URI = Uri.parse("content://com.xdja.engine.provider");

        try {
            Bundle bundle = new Bundle();
            Bundle result = context.getContentResolver().call(CONTENT_URI, "currentSpace", "com.xdja.safetybox", bundle);
            return result.getBoolean("space");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}