# FilmSims - Film Simulator

FilmSims is a photo editing app for Android that applies film-like textures to your photos. It utilizes 3D LUT (Look-Up Table) technology to replicate the rich color profiles of various film stocks and camera brands.

## Key Features

* **Film Simulation:** Choose and apply your favorite color tones from multiple presets.
* **Advanced Adjustments:** * Granular effect intensity control (0-100%).
  * Addition and adjustment of film grain.
* **Intuitive UI:**
  * Easy-to-use control panel.
  * Two-finger pinch-to-zoom for detailed previewing.
  * Filter categorization by brand and genre.
* **High-Quality Export:** * High-speed processing powered by GPU acceleration.
  * Preservation of Exif data (shooting date, camera settings, etc.).
  * Export at original resolution without quality loss.

## Development Environment

* Android SDK 34
* Kotlin / Jetpack Compose
* OpenGL ES 3.0 (Image Processing Engine)

---

## ⚠️ Developer Notes (Fork Modifications)

### What's Changed
* Disabled some features for local debugging purposes.

### Build Instructions & Limitations
Please note that **this repository will NOT work fully if built out-of-the-box.** To respect and protect the original author's proprietary data and intellectual property, this repository does **NOT** include:
1. Proprietary filter assets (LUTs, encrypted textures, etc.).
2. The specific build configurations and keys required for decryption.
3. Firebase configuration files (`google-services.json`).

If you wish to build this project locally for development, you must provide your own configuration files and ensure all required asset files are placed within the `app/src/main/assets/` directory.