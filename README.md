# FlowVid

A local, offline, TikTok/Reels-style video feed for videos already on your Android device. Built with Kotlin, Jetpack Compose, and Media3/ExoPlayer.

**Nothing is ever uploaded.** There is no `INTERNET` permission anywhere in this app. Every feature — discovery, playback, favorites, history, search, settings — runs entirely on-device against `MediaStore`, Room, and DataStore.

## Opening the project

1. Install **Android Studio** (a recent stable release — anything that ships AGP 9.x works).
2. `File > Open`, select the `FlowVid` folder.
3. Let Gradle sync. First sync will download the Android SDK platform/build-tools it needs if you don't already have them.
4. Run on a device or emulator running **Android 8.0 (API 26) or newer**.

**Gradle wrapper note:** this project ships `gradle-wrapper.properties` (pinned to Gradle 9.6, the minimum AGP 9.4.0 requires) but not the wrapper's binary `gradle-wrapper.jar`, since that file can't be produced without network access to Gradle's distribution servers. Android Studio will offer to regenerate it automatically on first open ("Gradle wrapper is missing" prompt). If you'd rather do it yourself from a terminal that already has Gradle installed: `gradle wrapper --gradle-version 9.6`. The GitHub Actions workflows below do this step automatically, so you don't need Android Studio at all just to get a build.

## Building via GitHub Actions (no Android Studio needed)

Two workflows live in `.github/workflows/`:

- **`android-build.yml`** — runs on every push/PR to `main`, and can also be triggered manually. Compiles the app and uploads a debug APK as a build artifact (Actions tab → the run → Artifacts section → `flowvid-debug-apk`). This is your "does it still compile" check.
- **`android-release.yml`** — runs when you push a tag like `v1.0.0`, or manually via Actions → Android Release → Run workflow. Builds a release APK and attaches it to a GitHub Release.

To get started:
1. Push this repo to GitHub.
2. Go to the **Actions** tab and enable workflows if prompted.
3. Push a commit (or use "Run workflow") — the debug build kicks off automatically. Download the APK from the finished run's Artifacts section, then install it on your phone (enable "install unknown apps" for whatever app you use to open it).
4. For a proper release build, tag a commit: `git tag v1.0.0 && git push origin v1.0.0`.

**Signing (optional, only needed for a Play Store–ready build):** by default `android-release.yml` signs the release APK with the debug key, so it installs fine for testing but isn't suitable for distribution. To sign it for real, add these secrets under repo Settings → Secrets and variables → Actions:

| Secret | What it is |
|---|---|
| `KEYSTORE_BASE64` | Your `.jks`/`.keystore` file, base64-encoded (`base64 -i my.keystore \| pbcopy` on macOS, `base64 -w0 my.keystore` on Linux) |
| `KEYSTORE_PASSWORD` | The keystore's password |
| `KEY_ALIAS` | The key alias inside the keystore |
| `KEY_PASSWORD` | The key's password |

If you don't have a keystore yet, generate one with `keytool -genkey -v -keystore my.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias`. Once all four secrets are set, the next tagged run will produce a properly signed APK automatically — no code changes needed.

**Dependency versions:** every library in `gradle/libs.versions.toml` was checked against its current stable release at the time this project was generated (September 2026) — AGP 9.4.0, Kotlin 2.4.0, Compose BOM 2026.08.00, Media3 1.11.0, Room 2.8.4, Coil 3.6.2, Navigation Compose 2.9.8. AGP 9.4.0 requires Gradle 9.6+ to run at all, which is why the wrapper is pinned there. The Android ecosystem moves fast; if Android Studio flags a newer stable version for any of these when you open the project, it's safe to accept the upgrade.

This project was written by hand and has **not** been compiled — the environment it was generated in has no Android SDK, emulator, or network access to run Gradle. Everything was checked for API correctness against current documentation as carefully as possible, but treat the first build in Android Studio as the real check, and expect that you may need to fix a small issue or two (an import, a version bump) the way you would with any hand-reviewed diff.

## Architecture

```
data/         MediaStore access, Room (favorites/history), DataStore (settings)
domain/       Plain models + repository interfaces — no Android framework types leak past here
player/       FlowVidPlayerPool — a bounded pool of ExoPlayer instances
ui/           One package per screen, plus theme/ and navigation/
```

- **No DI framework.** A small hand-written `AppContainer` (in `FlowVidApplication.kt`) builds each repository once as a lazy singleton. This was a deliberate choice over Hilt/Koin: one less moving part, and easier for you to read top-to-bottom.
- **Player pool, not one player per video.** `FlowVidPlayerPool` keeps exactly 3 `ExoPlayer` instances alive at any time (previous/current/next), reassigning them as you swipe. This is what keeps memory and CPU bounded whether your library has 10 videos or 10,000.
- **MediaStore only.** Video discovery uses `ContentResolver` queries against `MediaStore.Video.Media`, reacting live to a `ContentObserver`. No raw filesystem paths, no `MANAGE_EXTERNAL_STORAGE`.

## Where the spec's edge cases landed

A few places where the original spec bumps into real Android platform constraints, and the call made:

- **Landscape rotation (Settings → "Allow landscape videos to rotate").** When off (default), the Activity is portrait-locked and landscape videos are letterboxed — never cropped or stretched. When on, the Activity follows the sensor (`SCREEN_ORIENTATION_FULL_USER`), so turning the phone sideways lets a landscape video use the full screen. The Activity is declared with `android:configChanges` for orientation so this never triggers a destructive recreate.
- **Android 14+ partial photo/video access.** If the user grants "select videos" instead of full access, FlowVid works normally with whatever subset MediaStore returns — there's just no dedicated "manage selection" shortcut wired into the UI yet (the strings for it exist in `strings.xml` for anyone who wants to add it).
- **Loop vs. auto-advance.** Both are real, independent settings. If auto-advance is off and loop is also turned off, a video simply pauses on its last frame at the end instead of restarting or advancing — a third state the spec's wording implied but didn't name.
- **Gradle wrapper jar.** See above — a binary file this environment can't produce without network access.
