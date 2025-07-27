# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Notely Capture** is an Android note-taking application built with Kotlin and Compose Multiplatform. It features advanced speech-to-text capabilities powered by OpenAI's Whisper AI for voice notes and audio transcription.

This is a personalized fork of the original "Notely Voice" project, focused on simplifying the UI, adding additional capture methods, optimized for Android development, and integration with Logseq and Obsidian.

## Common Development Commands

### Gradle Build Commands
```bash
# Build debug APK for Android
./gradlew assembleDebug

# Build release APK for Android  
./gradlew assembleRelease

# Install debug build on connected device
./gradlew installDebug

# Clean build artifacts
./gradlew clean

# Build Android target
./gradlew build

# Run tests
./gradlew testDebugUnitTest
./gradlew testReleaseUnitTest

# Lint and code quality checks
./gradlew ktlintCheck
./gradlew ktlintFormat

# Build specific modules
./gradlew :shared:build
./gradlew :core:audio:build
./gradlew :lib:build

# Run single test file (example)
./gradlew :shared:testDebugUnitTest --tests "*NoteViewModelTest"
```

### Project Setup
```bash
# Clone the original repository
git clone https://github.com/tosinonikute/NotelyVoice.git

# Or clone your personalized fork
git clone https://github.com/stanvx/NotelyCapture.git
chmod +x ./gradlew
./gradlew sync
```

### Upstream Synchronization

**Quick Method (Recommended):**
```bash
# Use the interactive sync helper script
./upstream-sync.sh
```

**Manual Method:**
```bash
# Add original repository as upstream (if cloned from fork)
git remote add upstream https://github.com/tosinonikute/NotelyVoice.git

# Fetch latest changes from upstream
git fetch upstream

# Merge upstream changes (be careful with naming conflicts)
git merge upstream/main

# The .gitattributes file will help handle merge conflicts for renamed files
```

### 16KB Page Size Compliance

**Google Play Store Requirement**: Starting November 1st, 2025, all new apps and updates targeting Android 15+ devices must support 16 KB page sizes.

#### Current Compliance Status
✅ **COMPLIANT** - This project has been updated to support 16KB page sizes across all Android device configurations.

#### Implementation Details
The following changes have been implemented to ensure compliance:

**Build Configuration:**
- **NDK Version**: r27.0.12077973 (supports 16KB by default)
- **Android Gradle Plugin**: 8.11.1+ (meets minimum requirement)
- **Target SDK**: 36 (Android 15+)

**Native Library Configuration:**
- CMake linker flags: `-Wl,-z,max-page-size=16384`
- NDK r27 compilation flag: `-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON`
- Uncompressed native libraries in APK packaging

**Modified Files:**
- `gradle.properties` - Added NDK r27 flexible page size support
- `shared/build.gradle.kts` - Configured uncompressed native libraries
- `lib/src/main/jni/whisper/CMakeLists.txt` - Added 16KB page size linker flags

#### Testing 16KB Page Size Support

**Verify Compliance on Device/Emulator:**
```bash
# Check current page size on connected device
adb shell getconf PAGE_SIZE

# Expected output for 16KB compliance: 16384
# Output of 4096 indicates 4KB page size (still supported but not the test target)
```

**Test on Android 15+ Emulator:**
1. Create Android 15 emulator with 16KB page size support
2. Install and test the application: `./gradlew installDebug`
3. Verify app functionality with both audio recording and transcription features
4. Confirm no crashes related to memory alignment issues

**Build Verification Commands:**
```bash
# Clean build with 16KB compliance
./gradlew clean
./gradlew assembleDebug
./gradlew assembleRelease

# Verify native libraries are built with correct alignment
# Check build logs for successful compilation with 16KB flags
```

#### Compliance Benefits
- ✅ **Future-proof**: Ready for Google Play Store requirement
- ✅ **Device Compatibility**: Supports both 4KB and 16KB page size devices  
- ✅ **Performance**: Optimized memory alignment for Android 15+ devices
- ✅ **Backward Compatibility**: Maintains support for existing Android devices

### Release Workflow

#### Creating GitHub Releases with APK Distribution

The project has automated GitHub Actions workflows that create releases and distribute APK files:

```bash
# 1. Update version in shared/build.gradle.kts
# Update versionCode and versionName

# 2. Create and push version tag
git add .
git commit -m "feat: bump version to 1.1.5"
git tag v1.1.5
git push origin main
git push origin v1.1.5
```

**Automated Process:**
- **Trigger**: Git tag creation (e.g., `v1.1.5`)
- **Workflow**: `.github/workflows/github-release.yml`
- **Outputs**: 
  - Debug APK: `notely-capture-v1.1.5-debug.apk`
  - Release APK: `notely-capture-v1.1.5-release.apk`
- **Distribution**: Uploaded to GitHub Releases as downloadable assets
- **Changelog**: Auto-generated from commit messages since last tag

#### GitHub Actions Workflows

1. **`github-release.yml`** - Main release workflow
   - Triggered by version tags
   - Builds both debug and release APKs
   - Creates GitHub release with changelog
   - Uploads APK files as assets

2. **`release-signed.yml`** - Enhanced signed release workflow
   - Triggered by GitHub release creation or manual dispatch
   - Builds signed APKs (requires keystore secrets)
   - Also uploads to GitHub releases when triggered by release events

3. **`build-android.yml`** - Manual build workflow
   - Manual trigger for testing builds
   - Creates workflow artifacts (temporary)

#### Keystore Configuration

For signed releases, configure these GitHub repository secrets:
- `KEYSTORE_BASE64`: Base64-encoded keystore file
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias name
- `KEY_PASSWORD`: Key password

## Architecture Overview

### Clean Architecture Layers
- **UI Layer**: Compose screens (`/ui/`)
- **Presentation Layer**: ViewModels (`/presentation/`)
- **Domain Layer**: Use cases, entities, repository interfaces (`/domain/`)
- **Data Layer**: Repository implementations, data sources (`/data/`)

### Key Modules
- **`:shared`** - Main application module (Compose Multiplatform)
- **`:core:audio`** - Audio recording and processing
- **`:lib`** - Whisper C++ integration for speech recognition

### Dependency Injection with Koin
- Uses modular Koin setup with separate modules for different concerns
- Platform-specific modules using `expect/actual` pattern
- Factory pattern for use cases, singleton for repositories

### Key Patterns

#### ViewModel Architecture
- Uses `StateFlow` for reactive state management
- Intent-based user action handling via `onProcessIntent`
- Proper lifecycle management with `viewModelScope`

#### Repository Pattern
- Clear abstraction with interfaces in domain layer
- Implementations in data layer using SQLDelight
- Reactive streams using `CommonFlow` wrapper

#### Navigation
- Type-safe navigation using `@Serializable` sealed interfaces
- Nested navigation graphs for related screens
- Shared ViewModels using parent navigation entry

#### Data Transformation
- Explicit mappers between layers (Data ↔ Domain ↔ Presentation ↔ UI)
- Mappers injected as dependencies
- Composable mappers for complex transformations

#### Platform Abstraction
- `expect/actual` pattern for platform-specific implementations
- Common abstractions in commonMain
- Platform-specific Koin modules for Android/iOS dependencies

## Technology Stack

### Core Framework
- **Kotlin Multiplatform**: 2.2.0
- **Compose Multiplatform**: 1.8.2
- **Coroutines**: 1.10.2

### Key Dependencies
- **Koin**: 4.1.0 (Dependency injection)
- **SQLDelight**: 1.5.5 (Type-safe SQL)
- **DataStore**: 1.1.7 (Preferences)
- **Navigation Compose**: 2.9.0-beta03
- **Material 3**: Design system
- **dotLottie Android**: 0.9.2
- **Rich Text Editor**: 1.0.0-rc13 (Compose rich text editing)
- **OWASP HTML Sanitizer**: Security for rich text content

### Audio Processing
- **Whisper C++ Library**: Integrated via JNI and C-interop
- **Platform-specific audio**: AVAudioEngine (iOS), MediaRecorder (Android)
- **AudioWaveformExtractor**: Real-time amplitude data extraction
- **AmplitudeCollector**: Audio visualization data collection

### Animation System
- **dotLottie**: Modern Lottie library with full gradient support
- **Audio Reactive Animations**: Real-time amplitude visualization
- **AudioReactiveLottie**: Synchronized Lottie animations with audio amplitude
- **Performance**: Optimized for real-time audio synchronization

### Testing Framework
- **Kotlin Test**: Built-in multiplatform testing
- **Koin Test**: Dependency injection testing utilities
- **Test structure**: `commonTest/`, `androidInstrumentedTest/`, `iosTest/`
- **Security Testing**: Validation for security vulnerabilities
- **Performance Testing**: Typography and memory optimization tests

## Development Guidelines

### Fork Development Workflow

**IMPORTANT: All development work should be done on this fork (Notely Capture), not the upstream repository.**

- **Commits**: All changes go to this fork's repository
- **Pull Requests**: Create PRs within this fork (feature branches → main)
- **Upstream Contributions**: Only contribute generic bug fixes or widely applicable features to the original [Notely Voice](https://github.com/tosinonikute/NotelyVoice) repository
- **Branch Strategy**: Use feature branches for development, merge to `main` when ready

### Code Style
- Follow Kotlin coding conventions
- Use `camelCase` for functions, `PascalCase` for types
- Prefer immutable data structures
- Use explicit typing where it aids clarity
- **Automated formatting**: Run `./gradlew ktlintFormat` before committing
- **Code quality**: Ensure `./gradlew ktlintCheck` passes before submitting PRs

### Development Hooks Integration
The project includes a Makefile for integration with development hooks:
```bash
# Lint specific files (called automatically by hooks)
make lint FILE=path/to/file.kt

# Run tests for specific files
make test FILE=path/to/file.kt

# Check all linting
make lint-all

# Run all tests
make test-all

# Check integration setup
make check-integration
```

### Task Management with Backlog.md
The project uses Backlog.md CLI tool for task management:
```bash
# List tasks
backlog task list --plain

# View task details
backlog task 042 --plain

# Create new task
backlog task create "Task title" -d "Description" --ac "Acceptance criteria"

# Edit task status
backlog task edit 042 -s "In Progress" -a @yourself

# Mark task complete
backlog task edit 042 -s Done --notes "Implementation summary"
```

### Layer Boundaries
- Always respect architectural boundaries: UI → Presentation → Domain → Data
- Use explicit mappers when crossing layer boundaries
- Never bypass layers or create circular dependencies

### Testing
- Write tests for business logic in use cases
- Test ViewModels through their public interfaces
- Use factory functions for test data creation
- Follow the project's testing patterns and conventions
- **Test commands**: `./gradlew testDebugUnitTest` for unit tests
- **Lint commands**: `./gradlew ktlintCheck` and `./gradlew ktlintFormat`
- Tests are located in `shared/src/commonTest/`, `shared/src/androidInstrumentedTest/`, `shared/src/iosTest/`

### Security Considerations
- **HTML Sanitization**: Rich text content is sanitized using OWASP HTML Sanitizer
- **Resource Management**: Proper cleanup of native resources (Whisper models, audio streams)
- **Threading Safety**: Audio operations use appropriate threading patterns
- **Data Validation**: Input validation for audio files and text content

### Platform-Specific Code
- Use `expect/actual` for platform-specific functionality
- Keep platform code minimal and focused
- Prefer shared code over platform-specific implementations

### State Management
- Use `StateFlow` for ViewModel state
- Maintain state immutability
- Handle loading, success, and error states consistently

## Module Structure

The project uses a modular architecture with the following key modules:

- **`:shared`** - Main application module containing all business logic and UI
  - `androidMain/` - Android-specific implementations
  - `commonMain/` - Shared code across platforms
  - `iosMain/` - iOS-specific implementations
  - `commonTest/` - Shared tests
- **`:core:audio`** - Audio processing and recording functionality
- **`:lib`** - Whisper C++ integration and JNI bindings
- **`:iosApp`** - iOS application wrapper

### Source Set Layout (Android-focused)
```
shared/src/
├── androidMain/kotlin/          # Android implementations
├── androidInstrumentedTest/     # Android integration tests
├── commonMain/kotlin/           # Shared business logic & UI
└── commonTest/kotlin/           # Shared unit tests
```

## Prerequisites

- **Android Studio Ladybug** or newer
- **JDK 17** or higher
- **Kotlin 2.2.0** or higher
- **NDK 27.0.12077973** (Android Native Development Kit)

## Key Application Features

### Core Audio Architecture
- **AudioRecorderInteractor**: Core audio recording business logic with platform-specific implementations
- **AudioWaveformExtractor**: Real-time amplitude extraction for visualization
- **AmplitudeCollector**: Audio data collection and processing
- **AudioReactiveLottie**: Synchronized animations with audio amplitude
- **Variable Speed Playback**: 1x, 1.5x, 2x playback speeds for audio notes
- **Background Recording Service**: Android service for continuous audio recording

### Note Management System
- **Clean Architecture**: Separated into Data, Domain, Presentation, and UI layers
- **SQLDelight**: Type-safe database operations with reactive queries
- **Rich Text Editor**: Advanced formatting with toolbar and accessibility features
- **Quick Record**: Streamlined audio capture workflow with quick shortcuts
- **Note Organization**: Filtering by starred, voice notes, recent, with search capabilities
- **Undo/Redo System**: Text editing history management

### Speech Recognition & AI
- **Whisper Integration**: Local speech-to-text with multiple model sizes
- **Offline Transcription**: Works without internet connectivity
- **Multi-language Support**: 50+ languages supported
- **Background Transcription**: Non-blocking transcription processing
- **AI Summarization**: Text summarization using TF-IDF algorithms

### UI Components Structure
- **Design System**: Material 3 with custom theming and expressive design
- **Unified Components**: Shared UI components across the application
- **Material Symbols**: Font-based icon system for consistent styling
- **Responsive Layouts**: Optimized for different screen sizes and orientations
- **Performance Optimizations**: LazyVerticalStaggeredGrid for note lists
- **Animation System**: Motion tokens and unified animations

## Personalization Notes

This fork has been personalized with the following changes:
- **App Name**: Changed from "Notely Voice" to "Notely Capture"
- **Focus**: Primary focus on Android development
- **UI Simplification**: Streamlined user interface design
- **Additional Capture Methods**: Enhanced input capabilities
- **Integration**: Added support for Logseq and Obsidian

### Maintaining Upstream Compatibility

To ensure easy synchronization with the upstream repository:

1. **Package Names**: Keep original package names (`com.module.notelycompose.android`) for compatibility
2. **Core Architecture**: Maintain the existing clean architecture structure
3. **File Structure**: Avoid moving or renaming core files unnecessarily
4. **Git Attributes**: Use `.gitattributes` for automatic conflict resolution on personalized files

### Files Modified for Personalization

- All `strings.xml` files (app name changes)
- `fastlane/metadata/` files (store listings)
- iOS project configuration files
- README.md and documentation files
- This CLAUDE.md file

Keep these files in mind when merging upstream changes, as they may conflict and need manual review.

<!-- BACKLOG.MD GUIDELINES START -->
# Instructions for the usage of Backlog.md CLI Tool

## 1. Source of Truth

- Tasks live under **`backlog/tasks/`** (drafts under **`backlog/drafts/`**).
- Every implementation decision starts with reading the corresponding Markdown task file.
- Project documentation is in **`backlog/docs/`**.
- Project decisions are in **`backlog/decisions/`**.

## 2. Defining Tasks

### **Title**

Use a clear brief title that summarizes the task.

### **Description**: (The **"why"**)

Provide a concise summary of the task purpose and its goal. Do not add implementation details here. It
should explain the purpose and context of the task. Code snippets should be avoided.

### **Acceptance Criteria**: (The **"what"**)

List specific, measurable outcomes that define what means to reach the goal from the description. Use checkboxes (`- [ ]`) for tracking.
When defining `## Acceptance Criteria` for a task, focus on **outcomes, behaviors, and verifiable requirements** rather
than step-by-step implementation details.
Acceptance Criteria (AC) define *what* conditions must be met for the task to be considered complete.
They should be testable and confirm that the core purpose of the task is achieved.
**Key Principles for Good ACs:**

- **Outcome-Oriented:** Focus on the result, not the method.
- **Testable/Verifiable:** Each criterion should be something that can be objectively tested or verified.
- **Clear and Concise:** Unambiguous language.
- **Complete:** Collectively, ACs should cover the scope of the task.
- **User-Focused (where applicable):** Frame ACs from the perspective of the end-user or the system's external behavior.

    - *Good Example:* "- [ ] User can successfully log in with valid credentials."
    - *Good Example:* "- [ ] System processes 1000 requests per second without errors."
    - *Bad Example (Implementation Step):* "- [ ] Add a new function `handleLogin()` in `auth.ts`."

### Task file

Once a task is created it will be stored in `backlog/tasks/` directory as a Markdown file with the format
`task-<id> - <title>.md` (e.g. `task-042 - Add GraphQL resolver.md`).

### Additional task requirements

- Tasks must be **atomic** and **testable**. If a task is too large, break it down into smaller subtasks.
  Each task should represent a single unit of work that can be completed in a single PR.

- **Never** reference tasks that are to be done in the future or that are not yet created. You can only reference
  previous
  tasks (id < current task id).

- When creating multiple tasks, ensure they are **independent** and they do not depend on future tasks.   
  Example of wrong tasks splitting: task 1: "Add API endpoint for user data", task 2: "Define the user model and DB
  schema".  
  Example of correct tasks splitting: task 1: "Add system for handling API requests", task 2: "Add user model and DB
  schema", task 3: "Add API endpoint for user data".

## 3. Recommended Task Anatomy

```markdown
# task‑042 - Add GraphQL resolver

## Description (the why)

Short, imperative explanation of the goal of the task and why it is needed.

## Acceptance Criteria (the what)

- [ ] Resolver returns correct data for happy path
- [ ] Error response matches REST
- [ ] P95 latency ≤ 50 ms under 100 RPS

## Implementation Plan (the how) (added after starting work on a task)

1. Research existing GraphQL resolver patterns
2. Implement basic resolver with error handling
3. Add performance monitoring
4. Write unit and integration tests
5. Benchmark performance under load

## Implementation Notes (only added after finishing work on a task)

- Approach taken
- Features implemented or modified
- Technical decisions and trade-offs
- Modified or added files
```

## 6. Implementing Tasks

Mandatory sections for every task:

- **Implementation Plan**: (The **"how"**) Outline the steps to achieve the task. Because the implementation details may
  change after the task is created, **the implementation plan must be added only after putting the task in progress**
  and before starting working on the task.
- **Implementation Notes**: Document your approach, decisions, challenges, and any deviations from the plan. This
  section is added after you are done working on the task. It should summarize what you did and why you did it. Keep it
  concise but informative.

**IMPORTANT**: Do not implement anything else that deviates from the **Acceptance Criteria**. If you need to
implement something that is not in the AC, update the AC first and then implement it or create a new task for it.

## 2. Typical Workflow

```bash
# 1 Identify work
backlog task list -s "To Do" --plain

# 2 Read details & documentation
backlog task 042 --plain
# Read also all documentation files in `backlog/docs/` directory.
# Read also all decision files in `backlog/decisions/` directory.

# 3 Start work: assign yourself & move column
backlog task edit 042 -a @{yourself} -s "In Progress"

# 4 Add implementation plan before starting
backlog task edit 042 --plan "1. Analyze current implementation\n2. Identify bottlenecks\n3. Refactor in phases"

# 5 Break work down if needed by creating subtasks or additional tasks
backlog task create "Refactor DB layer" -p 42 -a @{yourself} -d "Description" --ac "Tests pass,Performance improved"

# 6 Complete and mark Done
backlog task edit 042 -s Done --notes "Implemented GraphQL resolver with error handling and performance monitoring"
```

### 7. Final Steps Before Marking a Task as Done

Always ensure you have:

1. ✅ Marked all acceptance criteria as completed (change `- [ ]` to `- [x]`)
2. ✅ Added an `## Implementation Notes` section documenting your approach
3. ✅ Run all tests and linting checks
4. ✅ Updated relevant documentation

## 8. Definition of Done (DoD)

A task is **Done** only when **ALL** of the following are complete:

1. **Acceptance criteria** checklist in the task file is fully checked (all `- [ ]` changed to `- [x]`).
2. **Implementation plan** was followed or deviations were documented in Implementation Notes.
3. **Automated tests** (unit + integration) cover new logic.
4. **Static analysis**: linter & formatter succeed.
5. **Documentation**:
    - All relevant docs updated (any relevant README file, backlog/docs, backlog/decisions, etc.).
    - Task file **MUST** have an `## Implementation Notes` section added summarising:
        - Approach taken
        - Features implemented or modified
        - Technical decisions and trade-offs
        - Modified or added files
6. **Review**: self review code.
7. **Task hygiene**: status set to **Done** via CLI (`backlog task edit <id> -s Done`).
8. **No regressions**: performance, security and licence checks green.

⚠️ **IMPORTANT**: Never mark a task as Done without completing ALL items above.

## 9. Handy CLI Commands

| Purpose          | Command                                                                |
|------------------|------------------------------------------------------------------------|
| Create task      | `backlog task create "Add OAuth"`                                      |
| Create with desc | `backlog task create "Feature" -d "Enables users to use this feature"` |
| Create with AC   | `backlog task create "Feature" --ac "Must work,Must be tested"`        |
| Create with deps | `backlog task create "Feature" --dep task-1,task-2`                    |
| Create sub task  | `backlog task create -p 14 "Add Google auth"`                          |
| List tasks       | `backlog task list --plain`                                            |
| View detail      | `backlog task 7 --plain`                                               |
| Edit             | `backlog task edit 7 -a @{yourself} -l auth,backend`                   |
| Add plan         | `backlog task edit 7 --plan "Implementation approach"`                 |
| Add AC           | `backlog task edit 7 --ac "New criterion,Another one"`                 |
| Add deps         | `backlog task edit 7 --dep task-1,task-2`                              |
| Add notes        | `backlog task edit 7 --notes "We added this and that feature because"` |
| Mark as done     | `backlog task edit 7 -s "Done"`                                        |
| Archive          | `backlog task archive 7`                                               |
| Draft flow       | `backlog draft create "Spike GraphQL"` → `backlog draft promote 3.1`   |
| Demote to draft  | `backlog task demote <task-id>`                                        |

## 10. Tips for AI Agents

- **Always use `--plain` flag** when listing or viewing tasks for AI-friendly text output instead of using Backlog.md
  interactive UI.
- When users mention to create a task, they mean to create a task using Backlog.md CLI tool.

<!-- BACKLOG.MD GUIDELINES END -->
