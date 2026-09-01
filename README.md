# Personal University × Kaizen Study — Android (Kotlin/Compose)

An intelligent, adaptive spaced-repetition learning system combining the **ALTER** council (Advisor · Librarian · Tutor · Editor · Roommate) with the **Kaizen Study** SM-2 memory retention engine, built with Jetpack Compose, Navigation-Compose, Retrofit, and automated GitHub Actions CI/CD.

## The Pedagogical Loop

1. **Orientation (Advisor + Librarian)**: Scopes custom goals or structured competitive exam syllabi (e.g. SSC CGL, RBI Grade B, Computer Science Systems) with PYQ frequency rankings.
2. **Deep Comprehension (Tutor + Roommate)**: Interactive lessons, code/concept breakdowns, and analogies for tough topics.
3. **Automated Harvesting**: Completing a lesson quiz automatically converts its questions into active **Kaizen Recall Units**.
4. **Daily Retention (SM-2 Spaced Repetition)**: Short, capped daily review queues update intervals and ease factors ($EF \ge 1.3$).
5. **Smart Remediation**: When stuck on a card during review, tap *"Stuck? Ask Roommate"* for an instant intuitive analogy.

---

## Project Structure

```
.github/workflows/
  android-ci.yml               — GitHub Actions CI: lint, unit tests, debug APK upload
gradle/wrapper/                — Gradle 8.7 wrapper (gradlew & gradlew.bat)
app/src/main/java/com/personaluniversity/app/
  MainActivity.kt              — Compose entry point
  ui/
    theme/Theme.kt             — Ink (#12161B), Gold (#C9A050), Parchment (#E9E4D8)
    nav/NavGraph.kt            — 4 Unified tabs: Review, Learn, Council, Progress
    screens/
      DailyReviewScreen.kt     — SM-2 flashcard queue + Roommate remediation
      SyllabusCatalogScreen.kt — Predefined exam syllabi + Tutor course generator
      CouncilScreen.kt         — ALTER Council (Advisor, Librarian, Editor, Roommate)
      ProgressScreen.kt        — Streak counters, 14-day heatmap, topic mastery
      LessonScreen.kt          — Lesson reading, quizzes, auto-harvesting to SRS
      ChatPane.kt              — Reusable message list and input pane
  data/
    model/Models.kt            — DTOs, RecallUnit, ExamCatalogEntry, DailyProgress
    spacedrepetition/
      Sm2Scheduler.kt          — Pure SuperMemo-2 algorithm implementation
    repository/
      SpacedRepetitionRepository.kt — In-memory & sync repository for recall units
      UniversityRepository.kt  — ALTER backend communication
app/src/test/java/.../
  Sm2SchedulerTest.kt          — Unit tests for SM-2 interval and ease-factor calculation
```

---

## Running & Testing

### 1. Build and Run via CLI
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run Android Lint
./gradlew lintDebug

# Assemble Debug APK (outputs to app/build/outputs/apk/debug/app-debug.apk)
./gradlew assembleDebug
```

### 2. GitHub Actions CI/CD
A GitHub Actions workflow is preconfigured in `.github/workflows/android-ci.yml`. Whenever you push to `main` or open a PR, GitHub Actions automatically:
* Sets up JDK 17 and caches Gradle dependencies.
* Runs the unit test suite (`Sm2SchedulerTest`).
* Runs Android Lint.
* Builds and uploads the signed debug APK as a downloadable artifact.

### 3. Point at your ALTER Backend
For full LLM course generation and council chats, run your backend:
```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```
Then verify `BASE_URL` in `app/build.gradle.kts` (`http://10.0.2.2:8000/` for emulators or your LAN IP for physical devices).

