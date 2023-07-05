#import "LocalNotificationsFcmPlugin.h"
#if __has_include(<local_push_notifications_fcm/local_push_notifications_fcm-Swift.h>)
#import <local_push_notifications_fcm/local_push_notifications_fcm-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "local_push_notifications_fcm-Swift.h"
#endif

@implementation LocalNotificationsFcmPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftLocalNotificationsFcmPlugin registerWithRegistrar:registrar];
}
@end
