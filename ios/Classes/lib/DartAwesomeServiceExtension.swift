//
//  DartLocalServiceExtension.swift
//  local_notifications
//
//  Created by CardaDev on 29/08/22.
//

import Foundation
import IosAwnCore
import IosAwnFcmCore
import local_push_notifications

open class DartLocalServiceExtension: LocalServiceExtension {
    
    open override func didReceive(
        _ request: UNNotificationRequest,
        withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void
    ){
        SwiftLocalNotificationsFcmPlugin.loadClassReferences()
        super.didReceive(request, withContentHandler: contentHandler)
    }
}
