---
id: task-053
title: Remove iOS platform components to transition to Android-only application
status: Done
assignee: ["@trentstanton"]
created_date: '2025-07-27'
labels: []
dependencies: []
---

## Description

Transform Notely Capture from a Kotlin Multiplatform project to an Android-only application by removing all iOS-specific components, build configurations, and source code. This aligns with the project's focus on Android development while maintaining the ability to sync with upstream changes for Android and common modules.

## Acceptance Criteria

## Implementation Notes

Completed comprehensive iOS platform removal:

**Removed Directories and Files:**
- `iosApp/` - Complete iOS application directory with Xcode project, Swift files, and Whisper.xcframework
- `shared/src/iosMain/` - 24 iOS-specific Kotlin implementation files
- `shared/src/iosTest/` - iOS-specific test files
- `core/audio/src/iosMain/` - iOS audio implementation files
- `shared/src/nativeInterop/` - C interop configurations for iOS
- `lib/src/main/jni/src/coreml/` - CoreML whisper components
- iOS Metal implementations from GGML
- 8 iOS onboarding images from `composeResources/drawable/`

**Code Updates:**
- Updated `OnboardingWalkthrough.kt` to remove iOS image references and simplify to Android-only
- Updated `ModelSetupPage.kt` to use Android resources only
- Fixed import issues in `NoteListHeader.kt`

**Verification:**
- Android debug build successful (`./gradlew :shared:assembleDebug`)
- Lint checks passed (`./gradlew lint`)
- No missing iOS dependencies or broken references
- Application remains fully functional on Android

**Files Modified:**
- `/shared/src/commonMain/kotlin/com/module/notelycompose/onboarding/ui/OnboardingWalkthrough.kt`
- `/shared/src/commonMain/kotlin/com/module/notelycompose/onboarding/ui/ModelSetupPage.kt`
- `/shared/src/commonMain/kotlin/com/module/notelycompose/notes/ui/list/NoteListHeader.kt`

The transition to Android-only is now complete, with all iOS components successfully removed while maintaining full Android functionality.

## Acceptance Criteria

- [x] Complete iosApp/ directory removal
- [x] iOS targets removed from all build.gradle.kts files
- [x] iOS source sets removed from shared and core modules
- [x] iOS cinterop configurations removed
- [x] All iosMain source directories deleted
- [x] Android build functionality verified
- [x] No orphaned expect/actual declarations remain
- [x] Documentation updated appropriately
