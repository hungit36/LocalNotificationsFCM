#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint local_push_notifications_fcm.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'local_push_notifications_fcm'
  s.version          = '0.0.1'
  s.summary          = 'Complement of Local Notifications to allow firebase with all local resources in Flutter.'
  s.description      = <<-DESC
A new Flutter plugin project.
                       DESC
  s.homepage         = 'https://github.com/hungit36/LocalNotificationsFCM'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'hungnguyen.it36@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '9.0'
  s.dependency 'local_notifications'
  s.dependency 'IosAwnCore', '0.7.3'
  #s.dependency 'IosAwnFcmCore'
  s.dependency 'IosAwnFcmDist', '0.7.5'
  s.dependency 'Firebase/CoreOnly'
  s.dependency 'Firebase/Messaging'

  s.platform = :ios, '12.0'
  s.swift_version = '5.3'
  s.static_framework = true

  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'NO',
    'ENABLE_BITCODE' => 'NO',
    'ONLY_ACTIVE_ARCH' => 'YES',
    'APPLICATION_EXTENSION_API_ONLY' => 'NO',
    'BUILD_LIBRARY_FOR_DISTRIBUTION' => 'NO',
    'VALID_ARCHS[sdk=iphonesimulator*]' => 'x86_64',
  }
  
  s.script_phase = {
      :name => 'Copy Flutter phase',
      :script => 'source "${SRCROOT}/../Flutter/flutter_export_environment.sh" && "${FLUTTER_ROOT}/packages/flutter_tools/bin/xcode_backend.sh" build',
      :execution_position => :before_compile
  }
end
