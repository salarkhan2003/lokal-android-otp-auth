@echo off
echo ========================================
echo Passwordless Auth App Installation
echo ========================================
echo.

echo Step 1: Checking connected devices...
adb devices
echo.

echo Step 2: Setting JAVA_HOME...
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10
echo JAVA_HOME set to: %JAVA_HOME%
echo.

echo Step 3: Cleaning previous build...
gradlew.bat clean
echo.

echo Step 4: Building APK...
gradlew.bat assembleDebug
echo.

echo Step 5: Installing on device...
gradlew.bat installDebug
echo.

echo ========================================
echo Alternative Installation Methods:
echo ========================================
echo.
echo Method 1: Using Android Studio
echo - Open this project in Android Studio
echo - Click the green "Run" button
echo - Select your connected device
echo.
echo Method 2: Manual APK Installation
echo - Look for APK in: app\build\outputs\apk\debug\
echo - Use: adb install -r app-debug.apk
echo.
echo Method 3: Direct ADB Install
echo - Run: adb install -r app\build\outputs\apk\debug\app-debug.apk
echo.
echo ========================================
echo Troubleshooting:
echo ========================================
echo - Make sure USB Debugging is enabled
echo - Check if device is authorized (adb devices)
echo - Try different USB cable/port
echo - Restart ADB: adb kill-server && adb start-server
echo.
pause