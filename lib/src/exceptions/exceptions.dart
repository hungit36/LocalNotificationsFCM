import 'dart:core';

class DartCallbackException implements Exception {
  String msg;
  DartCallbackException(this.msg);
}

class LocalNotificationsFcmException implements Exception {
  String msg;
  LocalNotificationsFcmException(this.msg);
}
