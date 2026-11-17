# Release builds

Spotword ships to Google Play as an Android App Bundle (`.aab`) built from `androidApp`.

## Build configuration

- Application ID `com.artkachenko.spotword`; it cannot change after the first Play upload.
  The Kotlin namespace stays `com.artkachenko.livetextfinder`.
- `versionCode` and `versionName` live in `androidApp/build.gradle.kts`. Every upload needs a
  higher `versionCode`.
- Release builds are shrunk and obfuscated with R8 (`isMinifyEnabled`, `isShrinkResources`).
  Library rules come with CameraX, ML Kit and Compose. `androidApp/proguard-rules.pro` adds
  only rules for demonstrated problems:
  - ML Kit's component registrars are created by reflection; without their no-argument
    constructors, live and imported recognition fail in release builds.
  - Line numbers are kept so Play Console can deobfuscate crashes. The bundle carries the R8
    mapping file automatically.

## Upload key

Play App Signing holds the app signing key; the upload key signs what you send to Play. The
key and its passwords never enter the repository (`keystore.properties`, `*.jks` and
`*.keystore` are ignored by Git).

1. Create the upload key once, outside the repository, and back it up with its passwords:

   ```bash
   keytool -genkeypair -v -keystore ~/keys/spotword-upload.jks -alias upload -keyalg RSA -keysize 4096 -validity 10000
   ```

2. Create `keystore.properties` in the repository root:

   ```properties
   storeFile=/Users/<you>/keys/spotword-upload.jks
   storePassword=...
   keyAlias=upload
   keyPassword=...
   ```

   `storeFile` may be absolute or relative to the repository root. Without this file, release
   builds still succeed but are unsigned and cannot be uploaded.

3. Build the bundle:

   ```bash
   ./gradlew :androidApp:bundleRelease
   ```

   The output is `androidApp/build/outputs/bundle/release/androidApp-release.aab`.

If the upload key is lost, Play support can reset it while Play App Signing is enabled.

## Checking a release build on a device

R8 problems only appear at runtime, so check every new release build on a device or emulator
before uploading: grant the camera, scan text, import an image and share it. A signed APK for
this check can come from `:androidApp:assembleRelease` with the same `keystore.properties`.
