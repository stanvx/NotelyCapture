# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Notely Capture** is a cross-platform mobile note-taking application built with Kotlin Multiplatform and Compose Multiplatform. It features advanced speech-to-text capabilities powered by OpenAI's Whisper AI for voice notes and audio transcription.

This is a personalized fork of the original "Notely Voice" project, focused on simplifying the UI, adding additional capture methods, focusing on Android development, and integration with Logseq and Obsidian.

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

# Build all targets (Android + iOS)
./gradlew build

# Run Android tests
./gradlew testDebug

# Build specific modules
./gradlew :shared:build
./gradlew :core:audio:build
./gradlew :lib:build
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

### iOS Development
```bash
cd iosApp
pod install
open iosApp.xcworkspace
```

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
- **`:iosApp`** - iOS-specific wrapper

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

### Audio Processing
- **Whisper C++ Library**: Integrated via JNI and C-interop
- **Platform-specific audio**: AVAudioEngine (iOS), MediaRecorder (Android)

## Development Guidelines

### Fork Development Workflow

**IMPORTANT: All development work should be done on this fork (Notely Capture), not the upstream repository.**

- **Commits**: All changes go to this fork's repository
- **Pull Requests**: Create PRs within this fork (feature branches → main)
- **Upstream Contributions**: Only contribute generic bug fixes or widely applicable features to the original [Notely Voice](https://github.com/tosinonikute/NotelyVoice) repository
- **Branch Strategy**: Use feature branches for development, merge to `main` when ready

#### Feature Development Process

**MANDATORY: Each feature must be developed on a separate feature branch and merged via Pull Request.**

```bash
# 1. Create and switch to new feature branch
git checkout -b feature/your-feature-name

# 2. Develop your feature with commits
git add .
git commit -m "feat: implement core functionality"
git commit -m "test: add comprehensive tests"
git commit -m "docs: update documentation"

# 3. Push feature branch to origin
git push -u origin feature/your-feature-name

# 4. Create Pull Request within this fork
gh pr create --base main --head feature/your-feature-name --repo stanvx/NotelyCapture \
  --title "feat: Add [feature description]" \
  --body "## Summary
Brief description of the feature

## Changes
- List of key changes
- New functionality added
- Tests included

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed"

# 5. After PR approval and merge, clean up
git checkout main
git pull origin main
git branch -d feature/your-feature-name
git push origin --delete feature/your-feature-name
```

#### Branch Naming Conventions
- **Features**: `feature/description` (e.g., `feature/logseq-integration`)
- **Bug fixes**: `fix/description` (e.g., `fix/audio-recording-crash`)
- **Refactoring**: `refactor/description` (e.g., `refactor/viewmodel-cleanup`)
- **Documentation**: `docs/description` (e.g., `docs/api-documentation`)

**CRITICAL: When creating PRs, always specify the base repository explicitly:**
```bash
# ✅ CORRECT - Create PR within this fork
gh pr create --base main --head feature-branch --repo stanvx/NotelyCapture

# ❌ WRONG - This creates PR against upstream by default
gh pr create --title "..." --body "..."
```

### Code Style
- Follow Kotlin coding conventions
- Use `camelCase` for functions, `PascalCase` for types
- Prefer immutable data structures
- Use explicit typing where it aids clarity

### Layer Boundaries
- Always respect architectural boundaries: UI → Presentation → Domain → Data
- Use explicit mappers when crossing layer boundaries
- Never bypass layers or create circular dependencies

### Testing
- Write tests for business logic in use cases
- Test ViewModels through their public interfaces
- Use factory functions for test data creation
- Follow the project's testing patterns and conventions

### Platform-Specific Code
- Use `expect/actual` for platform-specific functionality
- Keep platform code minimal and focused
- Prefer shared code over platform-specific implementations

### State Management
- Use `StateFlow` for ViewModel state
- Maintain state immutability
- Handle loading, success, and error states consistently

## Prerequisites

- **Android Studio Ladybug** or newer
- **Xcode 16.1** (for iOS development)
- **JDK 17** or higher
- **Kotlin 2.2.0** or higher
- **NDK 27.0.12077973** (Android Native Development Kit)

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

### Upstream Sync Process

**Using the Helper Script (Recommended):**
```bash
# Interactive sync with safety checks and options
./upstream-sync.sh

# The script provides options to:
# 1. Preview changes before merging
# 2. Merge with automatic conflict resolution
# 3. Create test branch for safe experimentation
# 4. Handles .gitattributes merge rules automatically
```

**Manual Process:**
```bash
# Regular sync process
git fetch upstream
git merge upstream/main

# Handle naming conflicts manually if needed
# The .gitattributes file will prefer your changes for UI-related files
# and upstream changes for core functionality

# Review changes before committing
git status
git diff

# Commit the merge
git commit -m "Merge upstream changes while preserving Notely Capture personalization"
```

### Files Modified for Personalization

- All `strings.xml` files (app name changes)
- `fastlane/metadata/` files (store listings)
- iOS project configuration files
- README.md and documentation files
- This CLAUDE.md file

Keep these files in mind when merging upstream changes, as they may conflict and need manual review.