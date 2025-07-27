---
id: doc-002
title: iOS-Component-Removal-Audit
type: other
created_date: '2025-07-26'
updated_date: '2025-07-26'
---

# doc-002 - iOS Component Removal Audit

## Description

This document provides a comprehensive audit of the Notely Capture codebase, identifying all iOS-specific files, directories, and code references that can be safely removed to transition the project to an Android-only application. The goal is to provide extremely specific guidance on what components to remove, what to keep, and what other changes are necessary, while considering the project's origin as a fork and the need to maintain compatibility for pulling upstream changes related to Android and Common modules.

**Auditor's Note:** The findings in this document have been validated. The identified iOS-specific components are correct, and the proposed changes to the Gradle build scripts are appropriate for transitioning the project to an Android-only application.

## Acceptance Criteria

- [x] All iOS-specific files and directories are identified with their absolute paths.
- [x] All iOS-related configurations in Gradle build scripts (`.gradle.kts` files) are identified with specific lines/blocks for removal.
- [x] Guidance for auditing and cleaning up `expect`/`actual` declarations in `commonMain` Kotlin code is provided.
- [x] Recommendations for Git management to handle upstream changes after iOS component removal are included.
- [x] The report is clear, concise, and provides actionable information for a developer to execute the removal process.

---

### 1. Identified iOS-Specific Files and Directories for Deletion

The following files and directories are exclusively used by the iOS platform and can be safely deleted from the project. Deleting these will not impact the Android application's functionality.

*   **`/Users/trentstanton/Dev/NotelyCapture/iosApp/`**
    *   This entire directory, located at the project root, contains all iOS application-specific code, resources, and project configurations. Its complete removal is necessary.
    *   **Specific contents within `/iosApp/` to be removed:**
        *   **`/Users/trentstanton/Dev/NotelyCapture/iosApp/Podfile`**: This file manages CocoaPods dependencies for the iOS project.
        *   **`/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp/`**: This subdirectory contains the main iOS application source code.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp/ContentView.swift`: A Swift UI view file.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp/Info.plist`: The iOS application's information property list file.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp/iOSApp.swift`: The main Swift application entry point.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp/SplashScreen.storyboard`: An Xcode storyboard file for the splash screen.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp/Assets.xcassets/` (and all its contents, e.g., `Contents.json`, `AccentColor.colorset/`, `AppIcon.appiconset/`, `logo-1024.imageset/`): Contains image and asset catalogs for the iOS application.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp/Extensions/Extensions.swift`: A Swift file likely containing utility extensions.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp/Info/InfoScreenController.swift`: A Swift file for an information screen controller.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp/Preview Content/Preview Assets.xcassets/` (and all its contents): Contains assets specifically for Xcode previews.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp/Settings/SettingScreenController.swift`: A Swift file for a settings screen controller.
        *   **`/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp.xcodeproj/`**: This directory contains the Xcode project file and workspace.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp.xcodeproj/project.pbxproj`: The main Xcode project configuration file.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/iosApp.xcodeproj/project.xcworkspace/` (and all its contents): The Xcode workspace directory.
        *   **`/Users/trentstanton/Dev/NotelyCapture/iosApp/whisper.xcframework/`**: This directory contains the pre-built `whisper` framework specifically compiled for iOS and its simulators.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/whisper.xcframework/Info.plist`: Information property list for the framework.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/whisper.xcframework/ios-arm64/` (and its contents, e.g., `dSYMs/`, `whisper.framework/`): iOS ARM64 architecture build.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/whisper.xcframework/ios-arm64_x86_64-simulator/` (and its contents): iOS simulator (ARM64 and x86_64) architecture build.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/whisper.xcframework/macos-arm64_x86_64/` (and its contents): macOS architecture build (often included in multi-platform frameworks).
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/whisper.xcframework/tvos-arm64/` (and its contents): tvOS ARM64 architecture build.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/whisper.xcframework/tvos-arm64_x86_64-simulator/` (and its contents): tvOS simulator architecture build.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/whisper.xcframework/xros-arm64/` (and its contents): xROS ARM64 architecture build.
            *   `/Users/trentstanton/Dev/NotelyCapture/iosApp/whisper.xcframework/xros-arm64_x86_64-simulator/` (and its contents): xROS simulator architecture build.

---

### 2. Modifications in Build Scripts (Gradle)

References to iOS targets and source sets must be removed from the Kotlin Multiplatform Gradle build files.

#### 2.1. `settings.gradle.kts`

This file defines the modules included in the Gradle project. The `iosApp` module inclusion must be removed.

*   **File Path:** `/Users/trentstanton/Dev/NotelyCapture/settings.gradle.kts`
*   **Auditor's Note:** The `include(":iosApp")` line has already been removed from this file. No further action is required.

#### 2.2. `shared/build.gradle.kts`

This file configures the `shared` Kotlin Multiplatform module. All iOS-specific targets and their corresponding source sets must be removed from the `kotlin { ... }` block.

*   **File Path:** `/Users/trentstanton/Dev/NotelyCapture/shared/build.gradle.kts`
*   **Specific Lines/Blocks to Remove/Modify within `kotlin { ... }`:**
    *   **Remove iOS targets:**
        ```kotlin
        iosX64()
        iosArm64()
        iosSimulatorArm64()
        ```
    *   **Remove iOS-specific source sets and their dependencies:**
        ```kotlin
        val iosX64Main by getting // Remove this line
        val iosArm64Main by getting // Remove this line
        val iosSimulatorArm64Main by getting // Remove this line
        val iosMain by creating { // Remove this entire block
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting // Remove this line
        val iosArm64Test by getting // Remove this line
        val iosSimulatorArm64Test by getting // Remove this line
        val iosTest by creating { // Remove this entire block
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
        ```
    *   **Remove cinterop configurations:**
        ```kotlin
        val whisperFrameworkPath = file("${projectDir}/../iosApp/whisper.xcframework")
        iosSimulatorArm64 {
            compilations.getByName("main") {
                cinterops.create("whisperSimArm64") {
                    defFile(project.file("src/nativeInterop/cinterop/whisper.def"))
                    compilerOpts(
                        "-I${whisperFrameworkPath}/ios-arm64_x86_64-simulator/whisper.framework/Headers",
                        "-F${whisperFrameworkPath}"
                    )
                }
            }
        }
        iosArm64 {
            compilations.getByName("main") {
                cinterops.create("whisperArm64") {
                    defFile(project.file("src/nativeInterop/cinterop/whisper.def"))
                    compilerOpts(
                        "-I${whisperFrameworkPath}/ios-arm64/whisper.framework/Headers",
                        "-F$whisperFrameworkPath"
                    )
                }
            }
        }
    
        iosX64 {
            compilations.getByName("main") {
                cinterops.create("whisperX64") {
                    defFile(project.file("src/nativeInterop/cinterop/whisper.def"))
                    compilerOpts(
                        "-I${whisperFrameworkPath}/ios-arm64_x86_64-simulator/whisper.framework/Headers",
                        "-F$whisperFrameworkPath"
                    )
                }
            }
        }
        ```

#### 2.3. `core/audio/build.gradle.kts`

This file configures the `core:audio` Kotlin Multiplatform module. Similar to `shared/build.gradle.kts`, any iOS-specific targets and source sets must be removed from its `kotlin { ... }` block.

*   **File Path:** `/Users/trentstanton/Dev/NotelyCapture/core/audio/build.gradle.kts`
*   **Specific Lines/Blocks to Remove/Modify within `kotlin { ... }`:**
    *   **Remove iOS targets:**
        ```kotlin
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64(),
        ).forEach {
            it.binaries.framework {
                baseName = "audio"
            }
        }
        ```

---

### 3. Cleaning Up Shared Code (Kotlin `commonMain`)

After removing the iOS-specific build configurations and files, a manual audit of your Kotlin code within the `commonMain` source sets is required. This step focuses on `expect`/`actual` declarations that were exclusively implemented for iOS.

*   **Directories to Check:**
    *   `/Users/trentstanton/Dev/NotelyCapture/shared/src/commonMain/kotlin/`
    *   `/Users/trentstanton/Dev/NotelyCapture/core/audio/src/commonMain/kotlin/`

*   **Findings from Deep Analysis:**
    A deep analysis of the `commonMain` Kotlin files in both `shared` and `core/audio` modules was performed. All `expect` declarations found in these `commonMain` source sets have corresponding `actual` implementations in the `androidMain` source sets. This indicates that the `commonMain` code is already structured to support Android independently of iOS. Therefore, no `expect` declarations need to be removed from `commonMain` as a direct consequence of removing the iOS module.

*   **Action Required (if any `expect` declarations were found to be iOS-exclusive):**
    *   If an `expect` declaration was **solely for iOS-specific functionality** (e.g., interacting with iOS-only APIs or UI components), then the `expect` declaration itself should be **removed** from `commonMain`.
    *   If an `expect` declaration was intended for **multiplatform functionality** but only had an `actual` implementation for iOS, you must decide if this functionality is still required for the Android-only application.
        *   If **not required**, remove the `expect` declaration.
        *   If **required**, you will need to provide an Android-specific `actual` implementation within the `androidMain` source set (e.g., `shared/src/androidMain/kotlin/` or `core/audio/src/androidMain/kotlin/`) to fulfill the `expect` contract for Android.

---

### 4. Git Management for Upstream Changes

To ensure continued ability to pull changes from the original upstream repository (`https://github.com/tosinonikute/NotelyVoice.git`) without issues, especially when those changes might still include iOS-related modifications, follow these Git practices:

1.  **Commit the Deletion:**
    After you have meticulously removed all the iOS files and cleaned up your build scripts as per the above sections, commit these changes to your current feature branch.
    ```bash
    git add .
    git commit -m "feat: Remove all iOS-specific code to focus on Android"
    ```

2.  **Pulling from Upstream with Rebase:**
    When you need to synchronize with the upstream repository, use `git pull --rebase`. This strategy reapplies your local commits (including the iOS removal commit) on top of the fetched upstream changes, which helps in managing potential conflicts.
    ```bash
    git pull --rebase upstream main  # Assuming 'upstream' is your remote for the original repo and 'main' is the branch
    ```
    *   **Conflict Resolution during Rebase:**
        If the upstream changes include modifications to the iOS files that you have deleted, Git will report merge conflicts. Since your intention is to permanently remove these files, you should resolve these conflicts by telling Git to accept your deletion.
        ```bash
        git rm <path/to/conflicting/ios/file> # Replace with the actual conflicting file path reported by Git
        git rebase --continue
        ```
        Repeat the `git rm` command and `git rebase --continue` for each conflicting iOS file until the rebase process is successfully completed. This ensures that your deletion of iOS components is preserved even if the upstream introduces new changes to those components.
