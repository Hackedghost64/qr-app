# QR Studio — Jetpack Compose Android App

A modern, fast, and feature-rich Android app built with **Kotlin** and **Jetpack Compose** (Material 3) for converting links directly into customizable QR codes with high-resolution export.

---

## ✨ Features

- **Direct Link Input & Validation**:
  - Live link format normalization (automatically prepends `https://` if omitted).
  - Quick-clear button and one-tap **Paste from Clipboard** shortcut.
  - Quick-select sample chips (`github.com`, `google.com`, `youtube.com`, `wikipedia.org`).
- **Interactive QR Customizations**:
  - **Module Shapes**: Classic Squares, Modern Dots (Circles), or Squircles (Rounded Squares).
  - **Corner Finder Eyes**: Sharp Squares, Smooth/Rounded Corners, or Pure Concentric Circles.
  - **Color Themes**: Midnight Black, Ocean Blue, Emerald Forest, Electric Violet, Ruby Crimson, Amber Sunset, Slate Navy, and Indigo.
  - **Background Presets**: Pure White, Soft Cream, Light Cloud, Dark Obsidian, and AMOLED Black.
  - **Center Badge / Logo**: Embed custom vector icons (Globe/Web, Heart, Star, Share, Link) with high-redundancy Reed-Solomon Error Correction Level H (30%) so scanners never fail.
- **Export & Sharing**:
  - **Download Button**: Exports lossless PNG in **HD (1024x1024)** or **Ultra 4K (2048x2048)** directly to the device's public `Pictures/QR Studio` directory via modern MediaStore API.
  - **Share Sheet**: Share directly to WhatsApp, Telegram, Gmail, Drive, etc. via secure Android `FileProvider`.
  - **Copy Link**: Quick copy of the encoded link to system clipboard.
- **Edge-to-Edge Design**:
  - Fully supports modern Android Edge-to-Edge display with dynamic light & dark Material 3 theming.

---

## 🛠 Tech Stack

- **UI**: Jetpack Compose, Material 3, Compose BOM 2024.10.01
- **Language**: Kotlin 2.0.21
- **Engine**: ZXing Core 3.5.3 + custom Canvas module & eye vector renderer
- **Build**: Gradle 8.11.1, Android Gradle Plugin 8.6.1, compileSdk 35, minSdk 24

---

## 🚀 Building & Running

### Build Debug APK
```bash
./gradlew assembleDebug
```
The output APK is located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Run Unit Tests
```bash
./gradlew test
```

### Install to connected device via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
