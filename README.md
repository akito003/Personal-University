# Personal University — Android (Kotlin/Compose)

A native Android client for the ALTER backend (Advisor · Librarian · Tutor ·
Editor · Roommate), built with Jetpack Compose, Navigation-Compose, and
Retrofit. Same design language as the web frontend: ink-slate background,
gold/brass accent, serif display type.

This is a real Android Studio project — open the `PersonalUniversity/` folder
directly in Android Studio (Koala or newer), let Gradle sync, and run it.
I can't compile or run Android builds in my own sandbox (no Android SDK /
emulator here), so double-check the first build in Studio and paste me any
errors if something doesn't sync — happy to fix.

## 1. Point it at your backend

The backend must be reachable from your phone/emulator over the network —
`localhost` inside the app means "the phone itself," not your PC.

Run the backend with:

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Then set `BASE_URL` in `app/build.gradle.kts`:

- **Android emulator**, backend running on your dev machine:
  `http://10.0.2.2:8000/` (already the default — 10.0.2.2 is the emulator's
  alias for your host machine's localhost)
- **Physical phone**, same Wi-Fi as your PC:
  `http://<your-pc-lan-ip>:8000/` — find your PC's LAN IP with `ipconfig`
  (Windows) or `ifconfig`/`ip addr` (Mac/Linux), e.g. `http://192.168.1.42:8000/`
- **Deployed backend** (Render, Railway, Fly.io, etc.): use that public URL
  and switch `usesCleartextTraffic` off in the manifest once it's HTTPS.

## 2. Open and run

1. Open `PersonalUniversity/` as a project in Android Studio.
2. Let Gradle sync (it'll pull Compose, Retrofit, Navigation, etc.).
3. Run on an emulator or a physical device (USB debugging enabled) — min SDK 26.

## Project layout

```
app/src/main/java/com/personaluniversity/app/
  MainActivity.kt              — Compose entry point
  ui/theme/Theme.kt            — colors, type, matches the web app's tokens
  ui/nav/NavGraph.kt           — bottom nav (A L T E R) + course/lesson routes
  ui/screens/
    RoleChatScreen.kt          — Advisor / Editor / Roommate (shared chat UI)
    LibrarianScreen.kt         — source curation
    TutorEntryScreen.kt        — course generation form + course list
    CourseDetailScreen.kt      — modules/lessons list
    LessonScreen.kt            — lesson content, quiz, diagnostic chat
    ChatPane.kt                — reusable message list + input, used everywhere
  data/model/Models.kt         — DTOs mirroring the backend's Pydantic schemas
  data/network/ApiService.kt   — Retrofit interface (same endpoints as the web app)
  data/network/RetrofitClient.kt
  data/repository/UniversityRepository.kt  — Result-wrapped API calls
  viewmodel/                   — one ViewModel per screen/role
```

## Notes

- Course generation can take 30–90 seconds — the OkHttp read timeout is set
  to 120s to accommodate that.
- The launcher icon is a plain vector adaptive icon (gold diamond on ink) so
  the project doesn't depend on binary PNG assets — swap it for a real one
  via Android Studio's Image Asset tool whenever you want.
- Display headings use the platform serif (`FontFamily.Serif`) as a stand-in
  for the web app's Fraunces. To get an exact match, add Fraunces as a
  downloadable font (Settings → Font → Downloadable Fonts in Android Studio,
  or bundle a `.ttf` under `res/font/`) and swap `DisplayFontFamily` in
  `Theme.kt`.
- Networking uses plain HTTP (`usesCleartextTraffic="true"`) since you'll
  likely be pointing at a local dev server. Turn that off once you deploy
  behind HTTPS.
