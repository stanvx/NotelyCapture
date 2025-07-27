---
id: task-053
title: Remove iOS platform components to transition to Android-only application
status: To Do
assignee: []
created_date: '2025-07-27'
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
