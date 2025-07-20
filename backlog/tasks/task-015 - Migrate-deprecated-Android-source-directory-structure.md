---
id: task-015
title: Migrate deprecated Android source directory structure
status: In Progress
assignee: []
created_date: '2025-07-19'
updated_date: '2025-07-20'
labels: []
dependencies: []
---

## Description

Update the project to use the new Kotlin Multiplatform Android source set layout V2 by migrating from the deprecated 'Android Style' directory structure to the recommended layout. This ensures compatibility with future Gradle versions and follows current KMP best practices.

## Acceptance Criteria

- [ ] Deprecated warning for androidTest directory is resolved
- [ ] All existing Android instrumented tests continue to work
- [ ] Build completes without Android source directory deprecation warnings
- [ ] No test functionality is lost during migration

## Implementation Plan

1. Analyze current deprecated androidTest directory structure\n2. Create new androidInstrumentedTest directory with proper layout\n3. Move all existing test files from androidTest to androidInstrumentedTest\n4. Update any build configurations if necessary\n5. Verify all tests still run correctly after migration\n6. Confirm deprecation warning is resolved
