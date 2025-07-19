# Notely Capture

[![Kotlin](https://img.shields.io/badge/kotlin-2.1.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.8.0-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)

A Personalised, cross-platform note-taking application with powerful Whisper AI Voice to Text capabilities built with Compose Multiplatform.

This is a Personalised fork of the original [Notely Voice](https://github.com/tosinonikute/NotelyVoice) project, focused on simplifying the UI, adding additional capture methods, focusing on Android development, and integration with Logseq and Obsidian.

Perfect for students capturing lectures, professionals documenting meetings, researchers transcribing interviews, and anyone needing accessible hands-free note-taking with seamless integration to their knowledge management systems.

## Download

> **Note**: This is a Personalised fork. The original Notely Voice app is available on the official stores:

<div style="display:flex;" >
<a href="https://f-droid.org/en/packages/com.module.notelycompose.android">
    <img alt="Get it on F-Droid" height="64" src="https://raw.githubusercontent.com/anwilli5/coin-collection-android-US/main/images/fdroid-repo-badge.png" />
</a>
<a href="https://play.google.com/store/apps/details?id=com.module.notelycompose.android">
    <img alt="Get it on Google Play" height="64" src="https://raw.githubusercontent.com/anwilli5/coin-collection-android-US/main/images/google-play-badge.png" />
</a>
<a href="https://apps.apple.com/us/app/notely-voice-speech-to-text/id6745835691">
    <img alt="Available at Appstore" height="64" src="https://dbsqho33cgp4y.cloudfront.net/github/app-store-badge.png" />
</a>
</div>

## Personalisation Features

### Enhanced Integrations
🔗 **Logseq Integration** - Export notes directly to Logseq  
📝 **Obsidian Integration** - Seamless workflow with Obsidian vaults  
🎯 **Android Focus** - Optimized primarily for Android devices  

### Simplified UI
✨ **Streamlined Interface** - Cleaner, more focused user experience  
🚀 **Performance** - Reduced complexity for better performance

## Screenshots

<img src="https://github.com/tosinonikute/NotelyVoice/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" alt="screenshot2" width="250"> <img src="https://github.com/tosinonikute/NotelyVoice/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" alt="screenshot2" width="250"> <img src="https://github.com/tosinonikute/NotelyVoice/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" alt="screenshot3" width="250">

<img src="https://github.com/tosinonikute/NotelyVoice/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/7.png" alt="screenshot2" width="250"> <img src="https://github.com/tosinonikute/NotelyVoice/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" alt="screenshot2" width="250"> <img src="https://github.com/tosinonikute/NotelyVoice/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" alt="screenshot3" width="250">

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

# Manual sync
git fetch upstream
git merge upstream/main
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
Contributions are welcome! Please follow these steps:

- Fork the repository.
- Create a new branch for your feature or bug fix.
- Submit a pull request with a clear description of your changes.

**For upstream contributions:** Bug fixes and generic features should be contributed to the original [Notely Voice](https://github.com/tosinonikute/NotelyVoice) project.

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
