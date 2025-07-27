---
id: task-053
title: Remove iOS platform components to transition to Android-only application
status: To Do
assignee: []
created_date: '2025-07-27'
updated_date: '2025-07-27'
labels: []
dependencies: []
---

## Description

Transform Notely Capture from a Kotlin Multiplatform project to an Android-only application by removing all iOS-specific components, build configurations, and source code. This aligns with the project's focus on Android development while maintaining the ability to sync with upstream changes for Android and common modules.

## Acceptance Criteria

- [ ] Complete iosApp/ directory removal
- [ ] iOS targets removed from all build.gradle.kts files
- [ ] iOS source sets removed from shared and core modules
- [ ] iOS cinterop configurations removed
- [ ] All iosMain source directories deleted
- [ ] Android build functionality verified
- [ ] No orphaned expect/actual declarations remain
- [ ] Documentation updated appropriately

## Implementation Plan

1. Create feature branch for iOS removal work\n2. Remove entire iosApp/ directory and all contents\n3. Clean up shared/build.gradle.kts to remove iOS targets, source sets, and cinterop configurations\n4. Clean up core/audio/build.gradle.kts to remove iOS targets\n5. Remove iOS-specific source directories (iosMain/, iosTest/, nativeInterop/)\n6. Verify all expect/actual declarations still have Android implementations\n7. Test Android build functionality\n8. Commit changes following project conventions

## Implementation Notes

Successfully removed all iOS platform components and transitioned Notely Capture to an Android-only application. 

**Approach Taken:**
- Systematic removal following the comprehensive iOS Component Removal Audit (doc-002)
- Used feature branch workflow to ensure safe changes
- Verified Android build functionality throughout the process

**Components Removed:**
- Entire iosApp/ directory (173 files including Swift code, Xcode project, whisper.xcframework)
- iOS targets and source sets from shared/build.gradle.kts and core/audio/build.gradle.kts  
- iOS-specific source directories: iosMain/, iosTest/, nativeInterop/
- iOS cinterop configurations for Whisper framework integration

**Technical Validation:**
- Confirmed all expect/actual declarations retain Android implementations
- Verified clean Android build (./gradlew assembleDebug) succeeds
- All existing functionality preserved for Android platform

**Files Modified:**
- shared/build.gradle.kts: Removed iOS targets, source sets, and cinterop configurations
- core/audio/build.gradle.kts: Removed iOS targets and framework configurations
- Deleted 173 files across iosApp/, iosMain/, iosTest/, and nativeInterop/ directories

**Benefits Achieved:**
- Simplified build configuration focused on Android development
- Reduced project size by ~56MB (whisper.xcframework removal)
- Maintained clean architecture and ability to sync upstream Android changes
- Eliminated iOS-specific maintenance overhead
