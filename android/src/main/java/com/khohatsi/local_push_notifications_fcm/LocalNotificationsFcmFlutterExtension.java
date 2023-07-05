package com.khohatsi.local_push_notifications_fcm;

import android.content.Context;

import com.khohatsi.local_push_notifications.DartBackgroundExecutor;
import com.khohatsi.local_push_notifications.core.LocalNotifications;
import com.khohatsi.local_push_notifications.core.LocaleNotificationsExtension;
import com.khohatsi.local_push_notifications.core.logs.Logger;

import com.khohatsi.local_push_notifications_fcm.core.LocalNotificationsFcm;
import com.khohatsi.local_push_notifications_fcm.core.background.FcmBackgroundExecutor;

public class LocalNotificationsFcmFlutterExtension extends LocalNotificationsExtension {
    private static final String TAG = "LocalNotificationsFcmFlutterExtension";

    public static void initialize(){
        if(LocalNotificationsFcm.localFcmExtensions != null) return;
        LocalNotificationsFcm.localFcmExtensions = new LocalNotificationsFcmFlutterExtension();

        if (LocalNotifications.debug)
            Logger.d(TAG, "Flutter FCM extensions attached to Local Notification's core.");
    }

    @Override
    public void loadExternalExtensions(Context context) {
        LocalNotificationsFcm.localFcmServiceClass = DartFcmService.class;
        LocalNotificationsFcm.localFcmBackgroundExecutorClass = FcmBackgroundExecutor.class;
        FcmBackgroundExecutor.setBackgroundExecutorClass(FcmDartBackgroundExecutor.class);
    }
}