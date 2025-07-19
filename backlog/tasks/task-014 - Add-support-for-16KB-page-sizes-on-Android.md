---
id: task-014
title: Add support for 16KB page sizes on Android
status: To Do
assignee: []
created_date: '2025-07-19'
labels: []
dependencies: []
---

## Description

Implement support for Android devices with 16KB page sizes to ensure compatibility with newer Android devices and future-proof the application. This involves updating native code, build configurations, and testing on devices with different page sizes.

## Acceptance Criteria

- [ ] App builds and runs correctly on devices with 16KB page sizes
- [ ] Native audio processing (Whisper C++) works with 16KB pages
- [ ] SQLDelight database operations handle 16KB page sizes
- [ ] No crashes or performance degradation on 16KB page size devices
- [ ] Build configuration updated to support multiple page sizes
- [ ] Testing performed on both 4KB and 16KB page size devices
- [ ] Documentation updated with page size compatibility notes
