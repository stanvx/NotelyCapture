# Notely Voice

[![Kotlin](https://img.shields.io/badge/kotlin-2.1.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.8.0-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)

A completely free modern, cross-platform note-taking application with powerful Whisper AI speech recognition capabilities built with Compose Multiplatform

## Download the app
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
</div><br/>

## iOS Screenshots

<img src="https://github.com/user-attachments/assets/fcb2176b-e89c-4aa6-b535-156cb2bfda24" alt="screenshot2" width="250"> <img src="https://github.com/user-attachments/assets/52a47e2e-b1ff-4285-89be-828d6823325d" alt="screenshot2" width="250"> <img src="https://github.com/user-attachments/assets/942a4eb1-04fb-439b-8b3c-937bb6470b98" alt="screenshot3" width="250">

## Android Screenshots

<img src="https://github.com/user-attachments/assets/6f3d3686-a904-4214-bfe6-dab774a2c43b" alt="screenshot2" width="250"> <img src="https://github.com/user-attachments/assets/f7ca46eb-4587-4890-8dd6-0dfff3764602" alt="screenshot2" width="250"> <img src="https://github.com/user-attachments/assets/2205869b-2741-47f4-bdeb-98efadc203c8" alt="screenshot3" width="250">

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
- **[Dagger-Hilt](https://dagger.dev/hilt/)** - Dependency injection for Android
- **[Material 3](https://m3.material.io/)** - Design system for modern UI
- **[Vosk-API](https://alphacephei.com/vosk/)** - Offline speech recognition engine
- **[SFSpeechRecognizer](https://developer.apple.com/documentation/speech/sfspeechrecognizer)** - iOS native speech recognition
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

## Contributing
Contributions are welcome! Please follow these steps:

- Fork the repository.
- Create a new branch for your feature or bug fix.
- Submit a pull request with a clear description of your changes.

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- XCode 16.1
- JDK 11 or higher
- Kotlin 2.0.21 or higher

### Installation

1. Clone the repository
   ```sh
   git clone https://github.com/tosinonikute/NotelyVoice.git
   ```

2. Open the project in Android Studio
3. Sync the project with Gradle files
4. Run the app on an emulator or physical device

### License

```
Copyright (C) 2024 NotelyVoice

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see https://www.gnu.org/licenses/.

SPDX-License-Identifier: GPL-3.0-only
```
