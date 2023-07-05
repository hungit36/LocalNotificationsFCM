package com.khohatsi.local_push_notifications_fcm;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import io.flutter.view.FlutterCallbackInformation;
import com.khohatsi.local_push_notifications.core.LocalNotificationsExtension;
import com.khohatsi.local_push_notifications.core.exceptions.LocalNotificationsException;
import com.khohatsi.local_push_notifications.core.exceptions.ExceptionCode;
import com.khohatsi.local_push_notifications.core.exceptions.ExceptionFactory;
import com.khohatsi.local_push_notifications.core.utils.MapUtils;

import com.khohatsi.local_push_notifications_fcm.core.LocalNotificationsFcm;
import com.khohatsi.local_push_notifications_fcm.core.FcmDefinitions;
import com.khohatsi.local_push_notifications_fcm.core.listeners.LocalFcmSilentListener;
import com.khohatsi.local_push_notifications_fcm.core.listeners.LocalFcmTokenListener;
import com.khohatsi.local_push_notifications_fcm.core.models.SilentDataModel;
import com.khohatsi.local_push_notifications_fcm.core.managers.FcmDefaultsManager;

/**
 * LocalPushNotificationsFcmPlugin
 */
public class LocalPushNotificationsFcmPlugin
        implements
            FlutterPlugin,
            MethodCallHandler
{
    private static final String TAG = "LocalPushNotificationsFcmPlugin";

    private MethodChannel pluginChannel;
    private LocalNotificationsFcm localNotificationsFcm;

    public static boolean isInitialized = false;
    private WeakReference<Context> wContext;

    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());

    private final LocalFcmTokenListener fcmTokenListener = new LocalFcmTokenListener() {
        @Override
        public void onNewFcmTokenReceived(@NonNull String token) {
            if(pluginChannel != null)
                uiThreadHandler.post(
                        () -> pluginChannel.invokeMethod(
                                FcmDefinitions.CHANNEL_METHOD_NEW_FCM_TOKEN, token));
            else
                ExceptionFactory
                        .getInstance()
                        .registerNewLocalException(
                                TAG,
                                ExceptionCode.CODE_INITIALIZATION_EXCEPTION,
                                "Theres no valid flutter channel available to receive the new fcm token",
                                ExceptionCode.DETAILED_REQUIRED_ARGUMENTS+".onNewFcmTokenReceived");
        }

        @Override
        public void onNewNativeTokenReceived(@NonNull String token) {
            if(pluginChannel != null)
                uiThreadHandler.post(
                        () -> pluginChannel.invokeMethod(
                                FcmDefinitions.CHANNEL_METHOD_NEW_NATIVE_TOKEN, token));
            else
                ExceptionFactory
                        .getInstance()
                        .registerNewLocalException(
                                TAG,
                                ExceptionCode.CODE_INITIALIZATION_EXCEPTION,
                                "Theres no valid flutter channel available to receive the new native token",
                                ExceptionCode.DETAILED_REQUIRED_ARGUMENTS+".onNewNativeTokenReceived");
        }
    };

    private final LocalFcmSilentListener localFcmSilentListener = new LocalFcmSilentListener() {
        @Override
        public void onNewSilentDataReceived(@NonNull SilentDataModel silentReceived) throws LocalNotificationsException {

            final Context context = wContext.get();
            final Map<String, Object> silentData = silentReceived.toMap();

            if(pluginChannel != null)
                pluginChannel.invokeMethod(
                        FcmDefinitions.CHANNEL_METHOD_SILENCED_CALLBACK,
                        new HashMap<String, Object>() {
                            {
                                put(FcmDefinitions.SILENT_HANDLE, FcmDefaultsManager.getSilentCallbackDispatcher(context));
                                put(FcmDefinitions.NOTIFICATION_SILENT_DATA, silentData);
                            }
                        });
            else
                ExceptionFactory
                        .getInstance()
                        .registerNewLocalException(
                                TAG,
                                ExceptionCode.CODE_INITIALIZATION_EXCEPTION,
                                "Theres no valid flutter channel available to receive the new silent data",
                                ExceptionCode.DETAILED_REQUIRED_ARGUMENTS+".onNewSilentDataReceived");
        }
    };

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        AttachLocalNotificationsFcmPlugin(
            flutterPluginBinding.getApplicationContext(),
            new MethodChannel(
                flutterPluginBinding.getBinaryMessenger(),
                FcmDefinitions.CHANNEL_FLUTTER_PLUGIN
            )
        );
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        detachLocalNotificationsFCMPlugin(
                binding.getApplicationContext());
    }

    private void AttachLocalNotificationsFcmPlugin(@NonNull Context context, @NonNull MethodChannel channel) {
        pluginChannel = channel;
        pluginChannel.setMethodCallHandler(this);

        try {

            LocalNotificationsFcmFlutterExtension.initialize();
            if(localNotificationsFcm == null)
                localNotificationsFcm = new LocalNotificationsFcm(context);

            localNotificationsFcm
                    .subscribeOnLocalFcmTokenEvents(fcmTokenListener)
                    .subscribeOnLocalSilentEvents(localFcmSilentListener);

            wContext = new WeakReference<>(context);

            if (LocalNotificationsFcm.debug)
                Log.d(TAG, "Local Notifications FCM attached for Android " + Build.VERSION.SDK_INT);

        } catch (LocalNotificationsException ignored) {
        } catch (Exception exception) {
            ExceptionFactory
                    .getInstance()
                    .registerNewLocalException(
                            TAG,
                            ExceptionCode.CODE_UNKNOWN_EXCEPTION,
                            "An exception was found while attaching local notifications plugin",
                            exception);
        }
    }

    private void detachLocalNotificationsFCMPlugin(@NonNull Context context) {
        pluginChannel.setMethodCallHandler(null);
        pluginChannel = null;

        if (localNotificationsFcm != null) {
            localNotificationsFcm
                    .unsubscribeOnLocalFcmTokenEvents(fcmTokenListener)
                    .unsubscribeOnLocalSilentEvents(localFcmSilentListener);

            localNotificationsFcm.dispose();
            localNotificationsFcm = null;
        }

        if (LocalNotificationsFcm.debug)
            Log.d(TAG, "Local Notifications FCM detached from Android " + Build.VERSION.SDK_INT);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {

        if (localNotificationsFcm == null) {
            LocalNotificationsException localException =
                    ExceptionFactory
                            .getInstance()
                            .createNewLocalException(
                                    TAG,
                                    ExceptionCode.CODE_INITIALIZATION_EXCEPTION,
                                    "Local notifications FCM is currently not available",
                                    ExceptionCode.DETAILED_INITIALIZATION_FAILED+".localNotificationsFcm.core");
            result.error(
                    localException.getCode(),
                    localException.getMessage(),
                    lcoalException.getDetailedCode());
            return;
        }

        try {
            switch (call.method) {

                case FcmDefinitions.CHANNEL_METHOD_INITIALIZE:
                    channelMethodInitialize(call, result);
                    break;

                case FcmDefinitions.CHANNEL_METHOD_IS_FCM_AVAILABLE:
                    channelMethodIsFcmAvailable(call, result);
                    break;

                case FcmDefinitions.CHANNEL_METHOD_GET_FCM_TOKEN:
                    channelMethodGetFcmToken(call, result);
                    break;

                case FcmDefinitions.CHANNEL_METHOD_SUBSCRIBE_TOPIC:
                    channelMethodSubscribeToTopic(call, result);
                    break;

                case FcmDefinitions.CHANNEL_METHOD_UNSUBSCRIBE_TOPIC:
                    channelMethodUnsubscribeFromTopic(call, result);
                    break;

                default:
                    result.notImplemented();
            }

        } catch (LocalNotificationsException localException) {
            result.error(
                    localException.getCode(),
                    localException.getMessage(),
                    localException.getDetailedCode());

        } catch (Exception exception) {
            LocalNotificationsException lcoalException =
                    ExceptionFactory
                            .getInstance()
                            .createNewLocalException(
                                    TAG,
                                    ExceptionCode.CODE_UNKNOWN_EXCEPTION,
                                    ExceptionCode.DETAILED_UNEXPECTED_ERROR+"."+exception.getClass().getSimpleName(),
                                    exception);

            result.error(
                    localException.getCode(),
                    localException.getMessage(),
                    localException.getDetailedCode());
        }
    }

    private void channelMethodInitialize(@NonNull MethodCall call, @NonNull final Result result) throws LocalNotificationsException {
        if(isInitialized) {
            result.success(true);
            return;
        }

        Map<String, Object> arguments = call.arguments();
        if(arguments == null)
            throw ExceptionFactory
                    .getInstance()
                    .createNewLocalException(
                            TAG,
                            ExceptionCode.CODE_MISSING_ARGUMENTS,
                            "Arguments are missing",
                            ExceptionCode.DETAILED_REQUIRED_ARGUMENTS);

        Object callbackSilentObj = arguments.get(FcmDefinitions.SILENT_HANDLE);
        Object callbackDartObj = arguments.get(FcmDefinitions.DART_BG_HANDLE);
        Object licenseKeysObject = arguments.get(FcmDefinitions.LICENSE_KEYS);
        Object debugObject = arguments.get(FcmDefinitions.DEBUG_MODE);

        boolean debug = debugObject != null && (boolean) debugObject;
        long silentCallback = callbackSilentObj == null ? 0L : ((Number) callbackSilentObj).longValue();
        long dartCallback = callbackDartObj == null ? 0L : ((Number) callbackDartObj).longValue();
        List<String> licenseKeys = licenseKeysObject != null ? (List<String>) licenseKeysObject : null;

        if(FlutterCallbackInformation.lookupCallbackInformation(silentCallback) == null){
            throw ExceptionFactory
                    .getInstance()
                    .createNewLocalException(
                            TAG,
                            ExceptionCode.CODE_INVALID_ARGUMENTS,
                            "Silent push callback is not static or global",
                            ExceptionCode.DETAILED_INVALID_ARGUMENTS+".fcm.background.silentCallback");
        }

        if(FlutterCallbackInformation.lookupCallbackInformation(dartCallback) == null){
            throw ExceptionFactory
                    .getInstance()
                    .createNewLocalException(
                            TAG,
                            ExceptionCode.CODE_INVALID_ARGUMENTS,
                            "Dart fcm callback is not static or global",
                            ExceptionCode.DETAILED_INVALID_ARGUMENTS+".fcm.background.dartCallback");
        }

        boolean success =
            localNotificationsFcm.initialize(
                    licenseKeys,
                    dartCallback,
                    silentCallback,
                    debug
            ) &&
            lcoalNotificationsFcm.enableFirebaseMessaging();

        isInitialized = success;
        result.success(success);
    }

    private void channelMethodSubscribeToTopic(
            @NonNull MethodCall call,
            @NonNull final Result result
    ) throws LocalNotificationsException {
        ensureGooglePlayServices();
        String topicReference = null;

        @SuppressWarnings("unchecked")
        Map<String, Object> data = MapUtils.extractArgument(call.arguments(), Map.class).orNull();
        if (data != null)
            topicReference = (String) data.get(FcmDefinitions.NOTIFICATION_TOPIC);

        if (topicReference == null)
            throw ExceptionFactory
                    .getInstance()
                    .createNewLocalException(
                            TAG,
                            ExceptionCode.CODE_INVALID_ARGUMENTS,
                            "Topic name is required",
                            ExceptionCode.DETAILED_INVALID_ARGUMENTS+".fcm.subscribe.topicName");

        if(localNotificationsFcm != null) {
            localNotificationsFcm
                    .subscribeOnFcmTopic(topicReference);
            result.success(true);
        }
        else
            result.success(false);
    }

    private void channelMethodUnsubscribeFromTopic(
            @NonNull MethodCall call,
            @NonNull final Result result
    ) throws LocalNotificationsException {
        ensureGooglePlayServices();
        String topicReference = null;

        @SuppressWarnings("unchecked")
        Map<String, Object> data = MapUtils.extractArgument(call.arguments(), Map.class).orNull();
        if (data != null)
            topicReference = (String) data.get(FcmDefinitions.NOTIFICATION_TOPIC);

        if (topicReference == null)
            throw ExceptionFactory
                    .getInstance()
                    .createNewLocalException(
                            TAG,
                            ExceptionCode.CODE_INVALID_ARGUMENTS,
                            "Topic name is required",
                            ExceptionCode.DETAILED_INVALID_ARGUMENTS+".fcm.subscribe.topicName");

        if(localNotificationsFcm != null) {
            localNotificationsFcm
                    .unsubscribeOnFcmTopic(topicReference);
            result.success(true);
        }
        else
            result.success(false);
    }

    private void channelMethodIsFcmAvailable(
            @NonNull MethodCall call,
            @NonNull final Result result
    ) throws LocalNotificationsException {
        ensureGooglePlayServices();
        try {
            result.success(localNotificationsFcm.enableFirebaseMessaging());
        } catch (Exception e) {
            Log.w(TAG, "FCM could not enabled for this project.", e);
            result.success(false);
        }
    }

    private void channelMethodGetFcmToken(
            @NonNull MethodCall call,
            @NonNull final Result result
    ) throws LocalNotificationsException {
        ensureGooglePlayServices();

        if(localNotificationsFcm != null)
            localNotificationsFcm.requestFcmCode(new LocalFcmTokenListener() {
                @Override
                public void onNewFcmTokenReceived(@NonNull String token) {
                    result.success(token);
                }
                @Override
                public void onNewNativeTokenReceived(@NonNull String token) {
                }
            });
    }

    private boolean isGooglePlayServicesNotAvailable() {
        return localNotificationsFcm == null ||
                !lcoalNotificationsFcm.isGooglePlayServicesAvailable(wContext.get());
    }

    private void ensureGooglePlayServices() throws LocalNotificationsException {
        if(isGooglePlayServicesNotAvailable())
            throw ExceptionFactory
                    .getInstance()
                    .createNewLocalException(
                            TAG,
                            ExceptionCode.CODE_MISSING_ARGUMENTS,
                            "Google play services is not available on this device",
                            ExceptionCode.DETAILED_REQUIRED_ARGUMENTS+".fcm.subscribe.topicName");
    }
}
