library local_push_notifications_fcm;

import 'package:local_push_notifications_fcm/src/models/fcm_silent_data.dart';

export 'src/local_notifications_fcm_core.dart';
export 'src/models/fcm_silent_data.dart';

/// Method structure to listen to an incoming action with dart
typedef PushTokenHandler = Future<void> Function(String token);

/// Method structure to listen to an notification event with dart
typedef FcmSilentDataHandler = Future<void> Function(FcmSilentData silentData);
