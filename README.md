# 🎵 SONIQ Music (DwarkaSpay)

A modern, high-performance Android Music Player & Streaming Application built with **Jetpack Compose (Material 3)**, **Media3 ExoPlayer**, and a dedicated **Node.js streaming backend**.

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)

---

## 🌟 Key Highlights & Features

### 🎧 Audio & Media Player
* **ExoPlayer (Media3)**: High-fidelity audio playback engine supporting streaming & local tracks.
* **Background Playback**: `VioraPlaybackService` foreground service with persistent lock screen & notification controls.
* **Audio Focus & Headset Controls**: Auto-pause on incoming calls, seamless reconnection on Bluetooth disconnect.
* **Smart Sleep Timer**: 15m, 30m, 45m, 60m, or end-of-track auto-stop options.
* **Built-in Equalizer**: Multi-band frequency adjustment with custom presets (Bass Boost, Treble, Mid).
* **Queue Management**: Dynamic queue reordering, shuffle, repeat one/all.

### 🎨 Visuals & UI/UX (Jetpack Compose)
* **Design Philosophy**: Deep Midnight & Electric Violet dark aesthetic paired with luxury cream light mode tokens.
* **Dynamic Island / Floating Island**: Interactive mini-island overlay inspired by modern mobile OS designs.
* **Mini-Player**: Smooth floating player docked above the navigation bar with expandable full-screen player sheet.
* **Live Search**: Debounced instant search across both online catalogs (Saavn & iTunes) and local device library.
* **Lyrics View**: Synchronized/scrolling lyrics bottom-sheet integration.

### 📁 Device & Cloud Library
* **Local Storage Scanner**: Auto-scans device audio (`READ_MEDIA_AUDIO`) filtering out ringtones/short notifications.
* **Audio Import**: Direct file picker import with MediaMetadataRetriever ID3 tag parsing.
* **Playlists & Favorites**: Create, edit, and organize custom playlists with persistent state.

### 🔐 Authentication & Profile
* **Firebase Authentication**: Email & password authentication with verification flow.
* **Google Sign-In**: One-tap Google authentication.
* **User Profile**: Custom avatar upload, birthdate, and account management drawer.

### ⚡ Streaming Backend (Node.js)
* **JioSaavn Integration**: Stream decryption (3DES-ECB) supporting up to 320kbps high-quality audio streams.
* **iTunes Search API**: Instant 30s preview metadata fallback.
* **OTP & Mailer Service**: Verification code dispatch system.

---

## 🏗️ Project Architecture

```
DwarkaSpay/
├── app/
│   ├── src/main/java/com/dwarkadhish/dwarkaspay/
│   │   ├── data/
│   │   │   ├── auth/          # Firebase Auth, Google Sign-In helper, validators
│   │   │   ├── local/         # LocalAudioScanner, catalogs, sample tracks
│   │   │   ├── network/       # Retrofit client, Saavn & iTunes REST APIs
│   │   │   ├── preferences/   # Shared preference & theme managers
│   │   │   └── repository/    # MusicRepository central single source of truth
│   │   ├── model/             # Track, Album, Artist, Playlist, PlaybackState
│   │   ├── player/            # AudioPlayerManager, VioraPlaybackService
│   │   ├── ui/
│   │   │   ├── components/    # MiniPlayer, NowPlayingOverlay, Artwork cards
│   │   │   ├── navigation/    # VioraNavGraph, Screen destinations
│   │   │   ├── screens/       # Home, Search, Library, Player, Auth, Settings
│   │   │   └── theme/         # Color, Shape, Type, Material3 theme
│   │   ├── MainActivity.kt
│   │   └── VioraApplication.kt
├── backend/
│   ├── routes/
│   │   ├── auth.js            # Auth & OTP endpoints
│   │   ├── saavn.js           # JioSaavn search & stream decryption
│   │   └── spotify.js         # Spotify API integration
│   ├── utils/
│   │   ├── mailer.js          # Nodemailer setup
│   │   └── otpUtils.js        # OTP generator & storage
│   ├── server.js              # Express app entrypoint
│   └── package.json
└── build.gradle.kts           # Root gradle configuration
```

---

## 🚀 Getting Started

### 1. Prerequisites
* **Android Studio**: Ladybug / Meerkat (AGP 8.x / 9.x compatible)
* **JDK**: Version 17 or 21
* **Android SDK**: API 24 (minSdk) to API 35 (targetSdk)
* **Node.js**: v18+ (for running the streaming backend)

### 2. Backend Setup
```bash
cd backend
npm install
node server.js
```
The server will start on port `3000` (e.g., `http://localhost:3000` or your LAN IP `http://192.168.x.x:3000`).

### 3. Android App Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/ayushgosai/SONIQpura.git
   ```
2. Open the project in Android Studio.
3. Configure your Google Services:
   * Place your `google-services.json` inside the `app/` folder for Firebase Auth.
4. Sync Gradle and Run on an emulator or physical device.

---

## 🛠️ Tech Stack & Libraries
* **Android UI**: Jetpack Compose, Material 3, Accompanist
* **Media**: AndroidX Media3 (ExoPlayer, Session)
* **Networking**: Retrofit 2, OkHttp 3, Gson
* **Image Loading**: Coil Compose
* **Async**: Kotlin Coroutines & StateFlow
* **Backend**: Node.js, Express, Crypto (3DES)

---

## 👤 Author
Developed with ❤️ by [Ayush Gosai](https://github.com/ayushgosai).
