# Notely Capture

[![Kotlin](https://img.shields.io/badge/kotlin-2.1.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.8.0-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)

A Personalised, cross-platform note-taking application with powerful Whisper AI Voice to Text capabilities built with Compose Multiplatform.

This is a Personalised fork of the original [Notely Voice](https://github.com/tosinonikute/NotelyVoice) project, focused on simplifying the UI, adding additional capture methods, focusing on Android development, and integration with Logseq and Obsidian.

Perfect for students capturing lectures, professionals documenting meetings, researchers transcribing interviews, and anyone needing accessible hands-free note-taking with seamless integration to their knowledge management systems.

## Download

### 📦 GitHub Releases
Download the latest APK files directly from [GitHub Releases](https://github.com/stanvx/NotelyCapture/releases):

- **Debug APK**: `notely-capture-vX.X.X-debug.apk` - For testing and development
- **Release APK**: `notely-capture-vX.X.X-release.apk` - For production use

> **Installation**: Enable "Install from unknown sources" in Android settings, then install the downloaded APK file.

## Personalisation Features

### Enhanced Integrations
🔗 **Logseq Integration** - Export notes directly to Logseq  
📝 **Obsidian Integration** - Seamless workflow with Obsidian vaults  
🎯 **Android Focus** - Optimized primarily for Android devices  

### Simplified UI
✨ **Streamlined Interface** - Cleaner, more focused user experience  
🚀 **Performance** - Reduced complexity for better performance

## Screenshots

<img src="https://github.com/stanvx/NotelyCapture/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" alt="screenshot2" width="250"> <img src="https://github.com/stanvx/NotelyCapture/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" alt="screenshot2" width="250"> <img src="https://github.com/stanvx/NotelyCapture/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" alt="screenshot3" width="250">

<img src="https://github.com/stanvx/NotelyCapture/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/7.png" alt="screenshot2" width="250"> <img src="https://github.com/stanvx/NotelyCapture/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" alt="screenshot2" width="250"> <img src="https://github.com/stanvx/NotelyCapture/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" alt="screenshot3" width="250">

## Features

### Note-Taking
✏️ **Rich Text Editing** - Format your notes with:
- Headers and sub-headers
- Title styling
- Bold, italic, and underline text
- Text alignment (left, right, center)

🔍 **Simple Search** - Find your notes instantly with text search  
📊 **Smart Filtering** - Filter notes by type (Starred, Voice Notes, Recent)  
📂 **Organization** - Categorize notes with folders and tags

### Speech Recognition
🎙️ **Advanced Speech-to-Text** - Convert speech to text with high accuracy  
🌐 **Offline Capability** - Speech recognition works without an internet connection  
🔄 **Seamless Integration** - Dictate directly into notes or transcribe audio recordings  
🎧 **Audio Recording** - Record voice notes and play them back within the app  
🎧 **Unlimited Transcriptions** - Transcribe unlimited voice notes to multiple languages

### General
🌓 **Theming** - Switch between dark and light themes based on your preference  
💻 **Cross-Platform** - Seamless experience across Android & iOS  
📱 **Share Audio Functionality** - Share audios recorded on the App to Messages, WhatsApp, Files, Google Drive etc  
📱 **Share Texts** - Share texts on the App to Messages, WhatsApp, Files, Google Drive etc

## Speech Recognition Technology

- **OpenAI Whisper** - State-of-the-art open-source automatic speech recognition
  - Robust multilingual speech recognition with support for over 50 languages
  - Trained on 680,000 hours of multilingual and multitask supervised data
  - Excellent performance across diverse audio conditions and accents
  - Can run locally without internet dependency once model is downloaded

- **Cross-Platform Compatibility** - Designed for versatile deployment
  - Consistent speech recognition quality across different operating systems
  - Flexible model sizes from tiny (39 MB) to large (1550 MB) based on accuracy needs
  - Advanced noise robustness and speaker independence
  - Perfect for applications requiring high-quality transcription in various environments

## Built With 🛠

- **[Kotlin](https://kotlinlang.org/)** - Official programming language for Android development
- **[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)** - UI toolkit for building native applications
- **[Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)** - For asynchronous programming
- **[Clean Architecture](https://developer.android.com/topic/architecture)** - Ensures scalability and testability
- **[ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)** - Stores and manages UI-related data
- **[Koin](https://insert-koin.io/docs/quickstart/android/)** - Dependency injection for Android
- **[Material 3](https://m3.material.io/)** - Design system for modern UI
- **[Whisper AI](https://openai.com/index/whisper/)** - Human level robustness speech recognition
- **Native Compose Navigation** - No third-party navigation libraries
- **Custom Text Editor** - Built from scratch without external editing libraries

## Architecture

Notely is built with Clean Architecture principles, separating the app into distinct layers:

- **UI Layer**: Compose UI components
- **Presentation Layer**: Platform Independent ViewModels
- **Domain Layer**: Business logic and use cases
- **Data Layer**: Repositories and data sources

<img src="assets/layered_architecture_diagram.png" alt="Logo" width="70%">

## Project Structure
`shared/`: Contains shared business logic and UI code.

`androidApp/`: Contains Android-specific code.

`iosApp/`: Contains iOS-specific code.

## Fork Management

### Upstream Synchronization
This fork maintains sync with the original [Notely Voice](https://github.com/tosinonikute/NotelyVoice) repository:

```bash
# Quick sync using the helper script
./upstream-sync.sh
```

### Personalisation Strategy
- **Package names unchanged** for store compatibility  
- **Smart merge rules** via `.gitattributes` preserve customizations
- **Automated conflict resolution** for UI strings and metadata
- **Roadmap tracking** in this README

### Planned Personalisations
- **UI Simplification**: Streamlined navigation and reduced complexity
- **Additional Capture Methods**: Quick voice memo widget, background recording
- **Android Focus**: Material You theming, Android-specific optimizations  
- **Knowledge Management**: Logseq export, Obsidian vault sync, markdown optimization

## Contributing

### Contributing to this Fork

Contributions to Notely Capture are welcome! Please follow the feature branch workflow:

#### Development Process
1. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Develop your feature** following the project guidelines in `CLAUDE.md`

3. **Submit a Pull Request** within this fork:
   ```bash
   gh pr create --base main --head feature/your-feature-name --repo stanvx/NotelyCapture
   ```

4. **Follow the PR template** with clear description of changes, testing checklist, and impact assessment

#### Release Process

To create a new release with APK distribution:

1. **Update version** in `shared/build.gradle.kts`:
   ```kotlin
   versionCode = 16
   versionName = "1.1.5"
   ```

2. **Create and push a version tag**:
   ```bash
   git add .
   git commit -m "feat: bump version to 1.1.5"
   git tag v1.1.5
   git push origin main
   git push origin v1.1.5
   ```

3. **Automatic release creation**: GitHub Actions will automatically:
   - Build debug and release APKs
   - Create a GitHub release with changelog
   - Upload APK files as downloadable assets

#### Branch Naming Conventions
- **Features**: `feature/description` (e.g., `feature/logseq-integration`)
- **Bug fixes**: `fix/description` (e.g., `fix/audio-recording-crash`)
- **Refactoring**: `refactor/description`
- **Documentation**: `docs/description`

### Contributing to Upstream

**For upstream contributions:** Generic bug fixes and widely applicable features should be contributed to the original [Notely Voice](https://github.com/tosinonikute/NotelyVoice) project.

**Personalization-specific features** (Logseq integration, Android-focused optimizations, UI simplifications) belong in this fork.

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- XCode 16.1
- JDK 11 or higher
- Kotlin 2.0.21 or higher

### Installation

1. Clone the repository
   ```sh
   git clone https://github.com/stanvx/NotelyCapture.git
   ```

2. Open the project in Android Studio
3. Sync the project with Gradle files
4. Run the app on an emulator or physical device

### License

```
Copyright (C) 2025 Notely Capture (forked from NotelyVoice)

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

## Original Project

This is a fork of the original [Notely Voice](https://github.com/tosinonikute/NotelyVoice) project by [tosinonikute](https://github.com/tosinonikute). Please consider supporting the original project and contributing to the upstream repository.
```
