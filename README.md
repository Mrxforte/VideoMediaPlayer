# Video Media Player · All Format

A modern, fully functional Android video & audio player built with:

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **Media3 ExoPlayer** for reliable local playback
- **MVVM + Clean Architecture**
- **Hilt**, **Room**, **DataStore**, **Navigation Compose**
- **MediaStore** scanning + Storage Access Framework
- Light / Dark / System themes
- Gestures (double-tap seek, brightness, volume indicators)
- Picture-in-Picture support
- Playback speed, aspect ratio, sleep timer
- Folders, search, sort, grid/list views
- Resume from last position

## Requirements

- Android Studio Ladybug (2024.2+) or newer
- JDK 17
- minSdk 26 / targetSdk 35

## Open & Run

1. Unzip the project.
2. Open the folder in Android Studio.
3. Let Gradle sync (first sync downloads dependencies).
4. Run on a device or emulator (API 26+).

Grant **Photos & videos** / **Music & audio** (or storage) permission when prompted.

## Project Structure

```
app/src/main/java/com/videomediaplayer/app/
├── data/           # MediaStore, Room, DataStore, Repository
├── domain/         # Models
├── di/             # Hilt modules
├── playback/       # ExoPlayer manager + MediaSessionService
└── presentation/
    ├── components/
    ├── navigation/
    ├── screens/    # Home, Player, Settings, Folders
    ├── theme/
    └── viewmodel/
```

## Features Implemented

| Feature                    | Status |
|---------------------------|--------|
| MediaStore video/audio scan | ✅ |
| Grid & List library views | ✅ |
| Folders browser           | ✅ |
| Search & Sort             | ✅ |
| Real ExoPlayer playback   | ✅ |
| Double-tap ±10 s          | ✅ |
| Brightness / volume gestures | ✅ |
| Playback speed 0.25×–2×   | ✅ |
| Aspect ratio modes        | ✅ |
| Sleep timer               | ✅ |
| Screen lock               | ✅ |
| Picture-in-Picture        | ✅ |
| Resume position (Room)    | ✅ |
| Light / Dark / System     | ✅ |
| Settings (DataStore)      | ✅ |
| Open document picker      | ✅ |
| Background MediaSession   | ✅ |

## Notes

- Codec support depends on the device’s hardware/software decoders and Media3.
- No proprietary logos or assets are included.
- This is an independently created recreation focused on local media playback UX.

## License

Independent open-source style project for educational / personal use.
