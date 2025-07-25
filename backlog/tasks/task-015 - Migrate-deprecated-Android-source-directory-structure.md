---
id: task-015
title: Migrate deprecated Android source directory structure
status: Done
assignee: []
created_date: '2025-07-19'
updated_date: '2025-07-20'
labels: []
dependencies: []
---

## Description

Update the project to use the new Kotlin Multiplatform Android source set layout V2 by migrating from the deprecated 'Android Style' directory structure to the recommended layout. This ensures compatibility with future Gradle versions and follows current KMP best practices.

## Acceptance Criteria

- [x] Deprecated warning for androidTest directory is resolved
- [x] All existing Android instrumented tests continue to work
- [x] Build completes without Android source directory deprecation warnings
- [x] No test functionality is lost during migration

## Implementation Plan

1. Analyze current deprecated androidTest directory structure\n2. Create new androidInstrumentedTest directory with proper layout\n3. Move all existing test files from androidTest to androidInstrumentedTest\n4. Update any build configurations if necessary\n5. Verify all tests still run correctly after migration\n6. Confirm deprecation warning is resolved

## Implementation Notes

Successfully migrated from deprecated 'Android Style' source directory layout to the new Kotlin Multiplatform Android source set layout V2. 

**Approach taken:**
1. Created new androidInstrumentedTest directory structure following KMP layout V2 guidelines
2. Moved existing test file from shared/src/androidTest/kotlin to shared/src/androidInstrumentedTest/kotlin
3. Removed the deprecated androidTest directory completely
4. Verified build configuration works without additional source set dependencies

**Files modified:**
- Moved: shared/src/androidTest/kotlin/com/module/notelycompose/platform/PlatformAudioPlayerAndroidTest.kt → shared/src/androidInstrumentedTest/kotlin/com/module/notelycompose/platform/PlatformAudioPlayerAndroidTest.kt
- Updated: shared/build.gradle.kts (briefly tested source set dependencies but ultimately removed as unnecessary)

**Technical decisions:**
- Followed official JetBrains documentation for KMP Android layout V2 migration
- Did not add explicit androidInstrumentedTest source set dependencies as they inherit properly from the default layout
- Verified the migration resolves the deprecation warning while maintaining build functionality

**Outcome:**
- Deprecation warning 'Deprecated Android Style Source Directory' is completely resolved
- Build completes successfully without Android source directory warnings
- Project now follows current KMP best practices and is compatible with future Gradle versions
