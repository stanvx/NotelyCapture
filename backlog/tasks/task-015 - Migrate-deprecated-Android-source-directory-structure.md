---
id: task-015
title: Migrate deprecated Android source directory structure
status: To Do
assignee: []
created_date: '2025-07-19'
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
