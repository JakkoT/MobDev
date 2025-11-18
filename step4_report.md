# Step 4 Report: Testing, APK Build, Bugs and Limitations

## 1. Testing Strategy

## 2. Build Process for APK

The application is built using the Gradle build system with Kotlin DSL.

- **Build Command:** The release APK is generated using the standard Gradle task:

  ```bash
  ./gradlew assembleRelease
  ```

- **Configuration:**
  - **Target SDK:** 36
  - **Build Types:** A `release` build type is configured.
- **Output:** The resulting APK is located at `app/release/app-release.apk`.

## 3. Known Bugs or Limitations

- **Test Coverage:** While key components like `ItemViewModel` and `LoginActivity` have associated tests, coverage does not extend to all ViewModels, Fragments, and Repositories.
