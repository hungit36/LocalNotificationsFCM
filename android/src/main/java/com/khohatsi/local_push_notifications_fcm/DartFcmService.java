package com.khohatsi.local_push_notifications_fcm;

import android.content.Context;

import com.khohatsi.local_push_notifications.LocalNotificationsFlutterExtension;
import com.khohatsi.local_push_notifications_fcm.core.services.LocalFcmService;

public class DartFcmService extends LocalFcmService {
    @Override
    public void initializeExternalPlugins(Context context) throws Exception {
        LocalNotificationsFlutterExtension.initialize();
        LocalNotificationsFcmFlutterExtension.initialize();
    }
}
