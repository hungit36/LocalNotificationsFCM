# Local Notifications FCM

Local Notifications add-on to send push notifications using FCM (Firebase Cloud Messaging), with all local notifications features.

### Features

- Create **Push Notifications** for Android and iOS using Flutter and Firebase Cloud Messaging services (FCM).
- Enable all Local Notifications **images**, **layouts**, **emoticons**, **buttons** and **sounds** on push notifications.
- Dismiss and cancel notifications remotely by Notification ID, Group ID or Channel ID.
- Use Firebase console (servless) or your backend server to send push notifications
- Get the Firebase Device Token and the Native Device Token.
- Execute remote background instructions using silent push notifications.
- Send messages to multiple devices using topic subscription.

<br>


*Exemplification of how you can send push notifications using **local_push_notifications***

<br>
<br>

# ⚠️ ATTENTION ⚠️ <br> Users from `firebase_messaging` plugin

This plugin contains all features available in `firebase_messaging` plugin + all Local Notification features. Because of this, `local_push_notifications` plugin is incompatible with `firebase_messaging`, as both plugins will compete each other to acquire global notification resources.

So, you **MUST not use** `firebase_messaging` with `local_push_notifications`. All other Firebase plugins are compatible with local notification plugins.
    
To migrate **firebase_messaging** to **local_push_notifications**, please take a look at:  
[How to migrate firebase_messaging plugin](#how-to-migrate-firebase_messaging-plugin).


<br>
    
# Next steps

- Include Web support
- Include Windows support
- Include MacOS support
- Include Linux support
    
<br>
<br>
<br>


# 📝 Important initial notes

1. This plugin is an add-on of [Local Notifications](https://pub.dev/packages/local_push_notifications) plugin and, because of it, depends on it.
2. Push notifications, like local notifications, are not 100% reliable. That means your notification may be delayed or denied due to battery saving modes, background processing blocking, internet connection failures, etc. Keep this in mind when designing your business logic.
3. On iOS, push notifications are only delivered to real devices, but all other features can be tested using simulators.
4. To check what is happening with your notification while the app is terminated, you need to read all logs from the device. To do this on Android, use LogCat from Android Studio. On iOS, use the "Console.app" program available on MacOS.
5. If your app was forced to stop on any platform, all notifications will no longer be delivered until your app is reopened.

<br>
<br>


# 🛠 Getting Started

In this section, you going to configue your Android and iOS project to use all features available in **local_push_notifications**:

<br>

## *Plugin Dependencies*

Add the plugins bellow as a dependency in your `pubspec.yaml` file:
```yaml
  firebase_core: ^1.24.1
  local_push_notifications: any
  local_push_notifications_fcm: any
```
OBS: Always certificate to use the last versions of all these plugins.

<br>
<br>

## 🤖 *Android Configuration*

1 - You MUST apply Google Play Services in your project to use FCM services. To do that, first you need to import `com.google.gms:google-services` package into your Android project, adding the line bellow in your `build.gralde` file, located at "android" folder. (Certifies to use the latest version)

```gradle
buildscript {
    ...
    dependencies {
        ...
        classpath 'com.google.gms:google-services:4.3.10'
        ...
    }
    ...
}
```

2 - Them, you need to apply google play services, adding the line bellow at the end of `build.gralde` file, located at "android/app" folder.

```gradle
apply plugin: 'com.google.gms.google-services'
```

Now, your Android project is configured to use `local_push_notifications`. *Local!!*

<br>
<br>

## 🍎 *iOS Configuration*

1 - First, ensure to have the last XCode version available instaled with at least Swift 5.5.7 (XCode version 14.A400).

![image](https://user-images.githubusercontent.com/40064496/194728638-9cada1b9-4f2d-4a30-9fb3-3bb448f36017.png)

<br>

2 - Run the command `pod install` inside your iOS project folder.

OBS: In case it returns some version conflict, run `pod repo update` to update your local repository and then rename/erase the file "Podfile.lock" inside your iOS folder. For last, try to execute the command `pod install` once again.

![image](https://user-images.githubusercontent.com/40064496/194728843-5a5fd0a1-8540-4186-95e5-441fe5ebfd2f.png)

<br>

3 - To be able to add pictures and buttons on iOS push notifications, its necessary to create a *Notification target extension* using XCode. Target extensions are a type of lightweight application capable to run on background for specific tasks.

To do that, you need to open your Flutter project using XCode.
Go to your iOS project folder and open the file *Runner.xcfworkspace*.



With your project opened, go to "*File -> New -> Target*"

![image](https://user-images.githubusercontent.com/40064496/194728921-ae8c464c-a53b-4d93-a71a-ea417fc620ce.png)

... and chose "Notification Service Extension"

![image](https://user-images.githubusercontent.com/40064496/194729005-3f645f12-f782-43fc-b686-6bad5a2f65e5.png)

... and add a name to your target extension, ending with "ServiceExtension".

![image](https://user-images.githubusercontent.com/40064496/194729045-7956c95f-26ec-4f4a-9649-5f1bb0fbc575.png)

<br>

4 - Now, you need to include `Flutter` and `Local Notifications FCM` libraries to your *Notification Service Extension*. To do that you need to modify your "PodFile", adding the lines bellow at the end of the file, replacing the 2 mentions of `MyAppServiceExtension` by your service extension name:

```Ruby
################  Local Notifications FCM pod mod  ###################
local_fcm_pod_file = File.expand_path(File.join('plugins', 'local_push_notifications', 'ios', 'Scripts', 'LocalFcmPodFile'), '.symlinks')
require lcoal_fcm_pod_file
target 'MyAppServiceExtension' do
  use_frameworks!
  use_modular_headers!
  
  install_local_fcm_ios_pod_target File.dirname(File.realpath(__FILE__))
end
update_local_fcm_service_target('MyAppServiceExtension', File.dirname(File.realpath(__FILE__)), flutter_root)
################  Local Notifications FCM pod mod  ###################
```
Them execute the command `pod install` to update your target extension. 

<br>

5 - At this point, your target extension is able to use local notifications library. Inside your Target extension folder, edit the file `NotificationService.swift`, replacing the class `UNNotificationServiceExtension` by `DartLocalServiceExtension` and erasing all Notification Service content. The final file should look like this:

```Swift
import UserNotifications
import local_push_notifications

@available(iOS 10.0, *)
class NotificationService: DartLocalServiceExtension {

}
```

<br>


6 - Also to build the app correctly, you need to ensure to set some `build settings` options for each of your app targets. In your project view, click on *Runner -> Target Runner -> Build settings*...  

![image](https://user-images.githubusercontent.com/40064496/195867655-c6cfc753-274e-4eff-b435-3d26e46e3b8a.png)

... and set the following options:

In *Runner* Target:
* Build libraries for distribution => NO
* Only safe API extensions => NO
* iOS Deployment Target => 11 or greater

In your *NotificationServiceExtension* Target:
* Build libraries for distribution => NO
* Only safe API extensions => YES
* iOS Deployment Target => 11 or greater

<br>

7 - Lastly, you need to add 3 capabilities to your XCode projec, specially "App Groups", allowing your target extensions to share data with each other.
 
To do this, run your application and search on debug console for the application group name automatically generated by Local Notifications. This name is unique for each application.

![image](https://user-images.githubusercontent.com/113704924/194730007-3e278bd3-a24d-481f-a99b-ae18a25ae36a.png)

Them, open your XCode project, go to Targets -> Runner -> Singing and Capabilities -> Click on "+" icon and add "App Groups", "Push Notifications" and "Remote", checking "background fetching" and "remote notification" options.

![image](https://user-images.githubusercontent.com/113704924/194729763-adfb1d42-9bba-4aa5-908f-91eae574735e.png)

On "App Groups", you need to add the app group name automatically generated by local notifications. This app group name MUST start with "group.". Them add the same "App Group" capability with the same app group name on your notification target extension.

![image](https://user-images.githubusercontent.com/113704924/194729936-eb5877b9-0ed1-43d5-879a-07a4f13f253e.png)

<br>

Now, your iOS project is configured to use `local_push_notifications`. *Local!!* (phew!)

<br>


## 📝 Important notes

1. Push notifications on iOS DO NOT support IDs/Identifiers.
2. Push notifications are only delivered to real devices, all other features can be tested using simulators.
3. Push notifications are always delivered, even in a catastrophic error in your configuration, json package or notification service extension. In these cases, your notification is displayed as simply as possible. To allow you to avoid such situation, local sets these notifications with category `"INVALID"`, and you can filter this category editing your info.plist, but you need to ask Apple for permission to do it. To know more about, please check [com.apple.developer.usernotifications.filtering](https://developer.apple.com/documentation/bundleresources/entitlements/com_apple_developer_usernotifications_filtering).
3. If you do not set `"mutable_content"` to true, your notification will not be delivered to NSE (Notification Service Extension) and modified to have lcoal notification customizations. Instead, it will be delivered only with originals title and body content.
4. If you do not set `"content_available"` to true, your silent push notification will not be delivered.
5. Is recommended to set the badge value in the notification section at same time as in data, because in NSE failures, iOS will reade the badge value present in notification section. Meantime, Android FCM library does not delivery badge value inside notification section, so you need to set it inside data section.
6. You must not use silent push notifications to often, otherwise Apple will starts to block your device to run on background. You can use "Console.app" on MacOS to get the device's logs and check if your app is getting blocked.
7. Your push notifications can be denied on iOS if your users don't open your app for a long time. This limitation is canceled as soon as your user reopen your application manually.

<br>
<br>


# 📋 Creating a Firebase Project to send Push Notifications

To send push notifications using FCM (Firebase Cloud Message) you need to have an Firebase account with `Cloud Messaging` enable and copy the google-services.info (iOS) and google-services.json (Android) files at correct places. Also, you need to send to Firebase the APNs certificates to enable the comunication between Firebase and Apple servers. In this section we going to explain to you how to do all of that.

First, you need to create an Firebase account and activate the Firebase service called `Cloud Messaging`:

1 - Sign in Firebase at https://firebase.google.com  
2 - Click Go to console  
3 - Click + Add project and follow the prompts to create a project. You can name your project anything you want  
4 - Once the project is created, the project configuration page is automatically open. Search for the link `Cloud Messaging` and add the service into your Firebase Account  

After activate the service, a new configuration page will be displayed. This process can be repeated and edited if necessary at any time, for all platforms that you need.

In this configuration page, set correctly your app id and, on second section, download the file called google-services.info (iOS) or google-services.json (Android). The google-services.info must be placed inside the folder "ios/Runner" folder and the google-services.json must be placed inside "android/app/src" folder.

<br>

## Adding APNs Certificate into your Firebase account

To allow Firebase send push notifications to iOS devices, its necessary to do an extra step to create the APNs certificate.

<br>

1 - Go to https://developer.apple.com, sing into an Apple Account and go to "Certificates" and click on "+" button.  
2 - Go to Services section and check **Apple Push Notification service SSL** and click on "Continue"  
3 - Insert the same iOS App ID configured in your Firebase Cloud Messaging.   
4 - In your MacOS machine, Launch Keychain Access app, located in /Applications/Utilities.   
5 - Choose Keychain Access > Certificate Assistant > Request a Certificate from a Certificate Authority.   
6 - In the Certificate Assistant dialog, enter all informations needed, leaving the CA Email Address field empty.  
7 - Choose “Saved to disk,” then click Continue.  
8 - Now, go back to the online certificate process and upload the certificate generated in your local machine to Apple  
9 - Attention: Download the file and store it in a safe place. This file can be donwload only once.  
10 - Lastly, upload this last certificate to Firebase in Project View -> Cloud Messaging -> Apple App Configurations. 

<br> 

And that's it! Now your Firebase is fully configured to send push notifications to iOS and Android.

<br> 
<br> 

# 📫 Requesting Firebase Token

There is two ways to send push notifications to a device:

* Getting the FCM token generated by Firebase. This token is unique for each user, device and app installed. Also, this token can be renewed by Firebase at any time.
* Sending a multicast message to a topic where the devices are subscribed on.

So, to send notifications, first you need to initialize `Firebase`, `LocalNotificationsFcm` and them request the FCM token. Is not always possible to generate the FCM token and this process depends on Internet connection.

```Dart

  //  *********************************************
  ///     INITIALIZATION METHODS
  ///  *********************************************

  static Future<void> initializeRemoteNotifications({
    required bool debug
  }) async {
    await Firebase.initializeApp();
    await LocalNotificationsFcm().initialize(
        onFcmSilentDataHandle: NotificationController.mySilentDataHandle,
        onFcmTokenHandle: NotificationController.myFcmTokenHandle,
        onNativeTokenHandle: NotificationController.myNativeTokenHandle,
        // This license key is necessary only to remove the watermark for
        // push notifications in release mode. To know more about it, please
        // visit http://khohatsi.com/prices
        licenseKey: null,
        debug: debug);
  }

  //  *********************************************
  ///     REMOTE NOTIFICATION EVENTS
  ///  *********************************************

  /// Use this method to execute on background when a silent data arrives
  /// (even while terminated)
  @pragma("vm:entry-point")
  static Future<void> mySilentDataHandle(FcmSilentData silentData) async {
    print('"SilentData": ${silentData.toString()}');

    if (silentData.createdLifeCycle != NotificationLifeCycle.Foreground) {
      print("bg");
    } else {
      print("FOREGROUND");
    }

    print("starting long task");
    await Future.delayed(Duration(seconds: 4));
    final url = Uri.parse("http://google.com");
    final re = await http.get(url);
    print(re.body);
    print("long task done");
  }

  /// Use this method to detect when a new fcm token is received
  @pragma("vm:entry-point")
  static Future<void> myFcmTokenHandle(String token) async {
    debugPrint('FCM Token:"$token"');
  }

  /// Use this method to detect when a new native token is received
  @pragma("vm:entry-point")
  static Future<void> myNativeTokenHandle(String token) async {
    debugPrint('Native Token:"$token"');
  }
}
```

<br>

To request the FCM token, you can use `await` to wait for token be returned by the method `requestFirebaseAppToken` or intercept the token with your static method `myFcmTokenHandle`.

```Dart

  // Request FCM token to Firebase
  Future<String> getFirebaseMessagingToken() async {
    String firebaseAppToken = '';
    if (await LocalNotificationsFcm().isFirebaseAvailable) {
      try {
        firebaseAppToken = await LocalNotificationsFcm().requestFirebaseAppToken();
      }
      catch (exception){
        debugPrint('$exception');
      }
    } else {
      debugPrint('Firebase is not available on this project');
    }
    return firebaseAppToken;
  }

```

Send this token to your backend server and this way you got the "device address" to send notifications. Its recommended to also send the native token to your server, as not all push services are available by Cloud Messaging.

<br><br>


# 📣 Sending Push Notifications

## Using Firebase portal

<br>

To send notifications using Firebase console, you can check this excelent tutorial about how to send push notifications on Firebase Console:
[Complete guide on sending Push using Firebase Cloud Messaging Console](https://enappd.com/blog/sending-push-using-firebase-console/35/)

<br>

## Using your backend server / Postman

<br>

To send push notifications in local notifications with all features available, you must use the standard below (Android and iOS sections are optional):

```Json
{
    "to" : "{{fcm_token_ios}}",
    "priority": "high",
    "mutable_content": true,
    "notification": {
        "badge": 42,
        "title": "Huston! The eagle has landed!",
        "body": "A small step for a man, but a giant leap to Flutter's community!"
    },
    "data" : {
        "content": {
            "id": 1,
            "badge": 42,
            "channelKey": "alerts",
            "displayOnForeground": true,
            "notificationLayout": "BigPicture",
            "largeIcon": "https://br.web.img3.acsta.net/pictures/19/06/18/17/09/0834720.jpg",
            "bigPicture": "https://www.dw.com/image/49519617_303.jpg",
            "showWhen": true,
            "autoDismissible": true,
            "privacy": "Private",
            "payload": {
                "secret": "Local Notifications Rocks!"
            }
        },
        "actionButtons": [
            {
                "key": "REDIRECT",
                "label": "Redirect",
                "autoDismissible": true
            },
            {
                "key": "DISMISS",
                "label": "Dismiss",
                "actionType": "DismissAction",
                "isDangerousOption": true,
                "autoDismissible": true
            }
        ],
        "Android": {
            "content": {
                "title": "Android! The eagle has landed!",
                "payload": {
                    "android": "android custom content!"
                }
            }
        },
        "iOS": {
            "content": {
                "title": "Jobs! The eagle has landed!",
                "payload": {
                    "ios": "ios custom content!"
                }
            },
            "actionButtons": [
                {
                    "key": "REDIRECT",
                    "label": "Redirect message",
                    "autoDismissible": true
                },
                {
                    "key": "DISMISS",
                    "label": "Dismiss message",
                    "actionType": "DismissAction",
                    "isDangerousOption": true,
                    "autoDismissible": true
                }
            ]
        }
    }
}
```

To send silent push notifications, you must not use "notification" section and you need to use "content_available" instead of "mutable_content":

```Json
{
    "to" : "{{fcm_token_ios}}",
    "content_available": true,
    "priority": "high",
    "data" : {
        "data1": "fb787aa2-6387-4f65-a5a2-125f96ab4c14",
        "data2": "call_voice",
        "data3": "3c3079b7-ab5e-48a5-8c61-b64ebb4910a9",
        "data4": "5469846578",
    }
}
```

It's not required to have a real server to send push notifications. You can use REST programs to emulate your backend sending push notifications, like [Postman](https://www.postman.com).

<br>
<br>

# 🚚 How to migrate `firebase_messaging` plugin.

To migrate from `firebase_messaging` you just need to replace firebase methods by its equivalents on Local Notifications and Local Notifications FCM:

* `FirebaseMessaging.onMessageOpenedApp` -> `LocalNotifications.getInitialNotificationAction()`

* `FirebaseMessaging.onMessage` -> `static Future <void> onActionReceivedMethod(ReceivedAction receivedAction)`

* `FirebaseMessaging.onBackgroundMessage` -> `static Future<void> mySilentDataHandle(FcmSilentData silentData)`

* `FirebaseMessaging.requestPermission` -> `LocalNotifications().requestPermissionToSendNotifications()`


To access non-static resources inside the static methods, you can use some design patterns:

* Singleton classes
* Static Flutter Navigator Key defined in MaterialApp widget


To switch the execution from background isolate to the main isolate of your application, where you have a valid context to redirect the users, use `ReceivePort` and `SendPort` classes.

```Dart

```
(work in progress)
<br>
<br>
