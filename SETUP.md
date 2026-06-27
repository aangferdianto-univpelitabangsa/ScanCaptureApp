# Scan Capture — Setup Guide

## 1. Firebase Authentication

1. Open [Firebase Console](https://console.firebase.google.com/) and create a project.
2. Add an **Android app** with package name: `com.example.scancaptureapp`
3. Download **`google-services.json`** and replace `app/google-services.json` (do not use the placeholder file in production).
4. In Firebase Console → **Build → Authentication → Sign-in method**, enable **Email/Password**.
5. Sync Gradle and run the app.

## 2. ML Kit Text Recognition (OCR)

Already configured in `app/build.gradle.kts`:

```kotlin
implementation(libs.mlkit.text.recognition)
```

- Uses **on-device** Latin text recognition (`TextRecognizerOptions.DEFAULT_OPTIONS`).
- First run may download the model; afterwards OCR works **offline**.
- Implementation: `data/repository/OcrRepositoryImpl.kt`

## 3. CameraX

Dependencies in Gradle:

- `camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`

Permissions in `AndroidManifest.xml`:

- `android.permission.CAMERA`

Runtime permission handled in `presentation/screens/camera/CameraScreen.kt` via Accompanist Permissions.

## 4. Room Database

- Database name: `scan_capture_db`
- Entity: `scan_history` (image path, text, timestamp)
- No migration needed for v1.

## 5. Hilt

Application class: `ScanCaptureApplication` (`@HiltAndroidApp`)

Modules:

- `di/DatabaseModule.kt`
- `di/FirebaseModule.kt`
- `di/RepositoryModule.kt`

## 6. Run the app

1. Use a **physical device** or emulator with camera (API 27+).
2. Register a new account → Home → **Open Camera** → capture → crop → OCR runs on Home.
3. **Save to History** stores scan in Room.
4. **Profile** shows Firebase user name/email.

## Bonus features included

- **Dark mode**: follows system theme (`ScanCaptureAppTheme`)
- **PDF export**: Home → Export as PDF (`utils/PdfExporter.kt`)
- **Image crop**: after capture, `CropScreen` before OCR

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Firebase auth fails | Replace `google-services.json`; enable Email/Password |
| Camera black screen | Grant camera permission; use real device |
| No text detected | Better lighting, hold document flat, use crop |
| Build error: google-services | Ensure `app/google-services.json` exists |
