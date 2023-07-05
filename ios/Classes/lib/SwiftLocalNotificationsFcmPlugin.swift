import UIKit
import Flutter
import IosAwnCore
import IosAwnFcmCore
import local_push_notifications

public class SwiftLocalNotificationsFcmPlugin:
    NSObject,
    FlutterPlugin,
    LocalFcmListener
{
    private static var _instance:SwiftLocalNotificationsFcmPlugin?

    static let TAG = "LocalNotificationsFcmPlugin"
    
    public var registrar:FlutterPluginRegistrar?
    public var flutterChannel:FlutterMethodChannel?
    public var localNotificationsFcm:LocalNotificationsFcm?
    static var flutterRegistrantCallback: FlutterPluginRegistrantCallback?
    
    public static var shared:SwiftLocalNotificationsFcmPlugin {
        get {
            if _instance == nil { _instance = SwiftLocalNotificationsFcmPlugin() }
            return _instance!
        }
    }
    override init(){}

    public static func register(with registrar: FlutterPluginRegistrar) {

        SwiftLocalNotificationsFcmPlugin.shared
            .initializeFlutterPlugin(
                registrar: registrar,
                channel: FlutterMethodChannel(
                    name: FcmDefinitions.CHANNEL_FLUTTER_PLUGIN,
                    binaryMessenger: registrar.messenger()))
    }
    
    private func initializeFlutterPlugin(registrar: FlutterPluginRegistrar, channel: FlutterMethodChannel) {
        self.registrar = registrar
        self.flutterChannel = channel
        
        SwiftLocalNotificationsFcmPlugin.loadClassReferences()
        
        self.localNotificationsFcm = LocalNotificationsFcm()
        
        localNotificationsFcm!.subscribeOnLocalFcmEvents(listener: self)
        
        registrar.addMethodCallDelegate(self, channel: self.flutterChannel!)
        registrar.addApplicationDelegate(self)
        
        loadExternalExtensions(usingFlutterRegistrar: registrar)
    }
    
    public func loadExternalExtensions(usingFlutterRegistrar registrar:FlutterPluginRegistrar){
        FlutterAudioUtils.extendCapabilities(usingFlutterRegistrar: registrar)
        FlutterBitmapUtils.extendCapabilities(usingFlutterRegistrar: registrar)
        DartBackgroundExecutor.extendCapabilities(usingFlutterRegistrar: registrar)
    }
    
    public static func loadClassReferences(){
        if FcmBackgroundService.backgroundFcmClassType != nil { return }
        FcmBackgroundService.backgroundFcmClassType = DartFcmBackgroundExecutor.self
    }
    
    @objc
    public static func setPluginRegistrantCallback(_ callback: @escaping FlutterPluginRegistrantCallback) {
        flutterRegistrantCallback = callback
    }
    
    public func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [AnyHashable : Any] = [:]
    ) -> Bool {
        return localNotificationsFcm?.enableRemoteNotifications(application) ?? false
    }
    
    public func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ){
        localNotificationsFcm?
            .application(application,
                         didRegisterForRemoteNotificationsWithDeviceToken: deviceToken)
    }
    
    public func application(
        _ application: UIApplication,
        didReceiveRemoteNotification userInfo: [AnyHashable : Any],
        fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void
    ) -> Bool {
        SwiftLocalNotificationsFcmPlugin.loadClassReferences()
        return localNotificationsFcm?
            .application(
                application,
                didReceiveRemoteNotification: userInfo,
                fetchCompletionHandler: { backgroundFetchResult in
                    Logger.d(SwiftLocalNotificationsFcmPlugin.TAG, "didReceiveRemoteNotification completed with \(backgroundFetchResult)")
                    completionHandler(backgroundFetchResult)
                }) ?? false
    }
    
    public func onNewNativeToken(token: String?) {
        self.flutterChannel?
            .invokeMethod(
                FcmDefinitions.CHANNEL_METHOD_NEW_NATIVE_TOKEN,
                arguments: token)
    }

    public func onNewFcmToken(token: String?) {
        self.flutterChannel?
            .invokeMethod(
                FcmDefinitions.CHANNEL_METHOD_NEW_FCM_TOKEN,
                arguments: token)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        
        if localNotificationsFcm == nil {
            let exception:LocalNotificationsException
                = ExceptionFactory
                        .shared
                        .createNewLocalException(
                            className: SwiftLocalNotificationsFcmPlugin.TAG,
                            code: ExceptionCode.CODE_INITIALIZATION_EXCEPTION,
                            message: "Local Notifications FCM is currently not available",
                            detailedCode: ExceptionCode.DETAILED_INITIALIZATION_FAILED+".localNotifications.core")
            
            result(
                FlutterError.init(
                    code: exception.code,
                    message: exception.message,
                    details: exception.detailedCode
                )
            )
            return
        }
        
        do {
                
            switch call.method {
                
                case FcmDefinitions.CHANNEL_METHOD_INITIALIZE:
                    try channelMethodInitialize(call: call, result: result)
                    return
                
                case FcmDefinitions.CHANNEL_METHOD_GET_FCM_TOKEN:
                    try channelMethodGetFcmToken(call: call, result: result)
                    return
                    
                case FcmDefinitions.CHANNEL_METHOD_IS_FCM_AVAILABLE:
                    try channelMethodIsFcmAvailable(call: call, result: result)
                    return
              
                case FcmDefinitions.CHANNEL_METHOD_SUBSCRIBE_TOPIC:
                    try channelMethodSubscribeTopic(call: call, result: result)
                    return
                    
                case FcmDefinitions.CHANNEL_METHOD_UNSUBSCRIBE_TOPIC:
                    try channelMethodUnsubscribeTopic(call: call, result: result)
                    return
                    
                default:
                    throw ExceptionFactory
                        .shared
                        .createNewLocalException(
                            className: SwiftLocalNotificationsFcmPlugin.TAG,
                            code: ExceptionCode.CODE_MISSING_METHOD,
                            message: "method \(call.method) not found",
                            detailedCode: ExceptionCode.DETAILED_MISSING_METHOD+"."+call.method)
            }
            
        } catch let localError as LocalNotificationsException {
            result(
                FlutterError.init(
                    code: localError.code,
                    message: localError.message,
                    details: localError.detailedCode
                )
            )
        } catch {
            let exception =
                ExceptionFactory
                    .shared
                    .createNewLocalException(
                        className: SwiftLocalNotificationsFcmPlugin.TAG,
                        code: ExceptionCode.CODE_UNKNOWN_EXCEPTION,
                        detailedCode: ExceptionCode.DETAILED_UNEXPECTED_ERROR,
                        originalException: error)
            
            result(
                FlutterError.init(
                    code: exception.code,
                    message: exception.message,
                    details: exception.detailedCode
                )
            )
        }
    }
    
    private func channelMethodInitialize(call: FlutterMethodCall, result: @escaping FlutterResult) throws {
        guard let arguments:[String:Any?] = call.arguments as? [String:Any?] else {
            throw ExceptionFactory
                .shared
                .createNewLocalException(
                    className: SwiftLocalNotificationsFcmPlugin.TAG,
                    code: ExceptionCode.CODE_MISSING_ARGUMENTS,
                    message: "arguments are required",
                    detailedCode: ExceptionCode.DETAILED_REQUIRED_ARGUMENTS+".arguments")
        }
        
        let silentHandle:Int64 = arguments[FcmDefinitions.SILENT_HANDLE] as? Int64 ?? 0
        let dartBgHandle:Int64 = arguments[FcmDefinitions.DART_BG_HANDLE] as? Int64 ?? 0
        
        let debug:Bool = arguments[FcmDefinitions.DEBUG_MODE] as? Bool ?? false
        let licenseKeys:[String] = arguments[FcmDefinitions.LICENSE_KEYS] as? [String] ?? []
        
        result(
            try localNotificationsFcm?
                    .initialize(
                        silentHandle: silentHandle,
                        dartBgHandle: dartBgHandle,
                        licenseKeys: licenseKeys,
                        debug: debug) ?? false
        )
    }
    
    private func channelMethodIsFcmAvailable(call: FlutterMethodCall, result: @escaping FlutterResult) throws {
        try localNotificationsFcm?.isFcmAvailable(whenFinished: { success, localException in
            if localException == nil {
                result(success)
            } else {
                result(
                    FlutterError.init(
                        code: localException!.code,
                        message: localException!.message,
                        details: localException!.detailedCode
                    )
                )
            }
        })
    }
    
    private func channelMethodGetFcmToken(call: FlutterMethodCall, result: @escaping FlutterResult) throws {
        localNotificationsFcm?
            .requestFirebaseToken(whenFinished: { token, localException in
                if localException == nil {
                    result(token)
                } else {
                    result(
                        FlutterError.init(
                            code: localException!.code,
                            message: localException!.message,
                            details: localException!.detailedCode
                        )
                    )
                }
            })
    }
    
    private func channelMethodSubscribeTopic(call: FlutterMethodCall, result: @escaping FlutterResult) throws {
        let arguments:[String:Any?] = call.arguments as? [String:Any?] ?? [:]
        
        guard let topic:String = arguments[FcmDefinitions.NOTIFICATION_TOPIC] as? String else {
            throw ExceptionFactory
                .shared
                .createNewLocalException(
                    className: SwiftLocalNotificationsFcmPlugin.TAG,
                    code: ExceptionCode.CODE_MISSING_ARGUMENTS,
                    message: "topic name is required",
                    detailedCode: ExceptionCode.DETAILED_REQUIRED_ARGUMENTS+".topic")
        }
        
        localNotificationsFcm?
            .subscribe(
                onTopic: topic,
                whenFinished: { success, localException in
                    if localException == nil {
                        result(success)
                    } else {
                        result(
                            FlutterError.init(
                                code: localException!.code,
                                message: localException!.message,
                                details: localException!.detailedCode
                            )
                        )
                    }
                })
    }
    
    private func channelMethodUnsubscribeTopic(call: FlutterMethodCall, result: @escaping FlutterResult) throws {
        let arguments:[String:Any?] = call.arguments as? [String:Any?] ?? [:]
        
        guard let topic:String = arguments[FcmDefinitions.NOTIFICATION_TOPIC] as? String else {
            throw ExceptionFactory
                .shared
                .createNewLocalException(
                    className: SwiftLocalNotificationsFcmPlugin.TAG,
                    code: ExceptionCode.CODE_MISSING_ARGUMENTS,
                    message: "topic name is required",
                    detailedCode: ExceptionCode.DETAILED_REQUIRED_ARGUMENTS+".topic")
        }
        
        localNotificationsFcm?
            .unsubscribeTopic(
                onTopic: topic,
                whenFinished: { success, localException in
                    if localException == nil {
                        result(success)
                    } else {
                        result(
                            FlutterError.init(
                                code: localException!.code,
                                message: localException!.message,
                                details: localException!.detailedCode
                            )
                        )
                    }
                })
    }
}
