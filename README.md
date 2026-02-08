# SMB Image Viewer

This project is an Android app that connects to an SMB share, navigates into a configured folder, and displays images with swipe navigation.

## Build (local)
1. Install Android Studio and ensure the Android SDK is available.
2. Generate the Gradle wrapper if needed:
   ```bash
   gradle wrapper
   ```
3. Build the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

The APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

## Configure SMB
Update the SMB host/share credentials and folder path in `SmbImageRepository` before running on device.
