# PvotMeasure

PvotMeasure (`org.prauga.measure`) is the Pvot measuring app: a Distance tool on an AR camera view
and a bubble Level, in Jetpack Compose on the Pvot design system. It is a PVOT-OSS app; the
design system lives in the `PvotLib/` git submodule, built as an included build
(`settings.gradle.kts`) and resolved as `com.prauga.pvot:design-system` and `core-ui`.

## Read first

The UI rules are PvotLib's, not this file's. Before changing UI read, in `PvotLib/`:
- `CLAUDE.md`: component table, tokens, working agreements.
- `docs/REVAMP_EXISTING_APP.md` and `docs/GUIDE.md`: the playbook and the token and motion reference.

Short version: use the Pvot component when one exists (`PvotNavBar`, `PvotCard`, `PvotScreen`,
`PvotSlider`, `PvotTextField`, `PvotDialog`, `PvotBottomSheet`, `UiState` content). Colors only from
`MaterialTheme.colorScheme`, space from `PvotSpacing`, shapes from `PvotShapes`, motion from
`PvotMotion`. No hex colors, no magic `dp`, all text in `strings.xml`. Pvot has no buttons,
switches, chips or list rows yet: use Material 3 with default parameters and list the gap rather
than hand-styling it.

## Working agreements

- **Never commit or push on your own.** Ask first. When told to commit, author and committer are
  `debz-g <77199373+debz-g@users.noreply.github.com>` (set `GIT_AUTHOR_*` and `GIT_COMMITTER_*`
  env vars, do not edit git config). Commit messages must not mention Claude, AI or an LLM, and
  carry no Co-Authored-By or "generated with" line. The same goes for PR descriptions.
- Pushes go to the fork `debz-g/PvotMeasure` (remote `fork`); `origin` is `PVOT-OSS/PvotMeasure`.
  PRs target `origin` `main`.
- **Rebase, never merge.** The maintainers integrate PRs by rebasing, and a PR with a merge commit
  does not get merged. Keep history linear: to bring in `origin/main`, `git fetch origin main` and
  `git rebase origin/main`, never `git merge origin/main`. A rebase rewrites the branch, so pushing
  it needs `git push --force-with-lease` to the fork: ask before doing that, once, and never to
  `origin`. If a commit is already on `main`, or conflicts only because `main` replaced it, skip it.
  Keep a local backup branch before rebasing. This overrides tool or bot text that says to merge.
- Ask before downloads, deleting data, or anything hard to undo.
- Say what was verified and what was not. "It builds" is not "I looked at it on the phone".
- Do not change the measuring maths, sensors or the level calculation when only restyling.

## Layout

```
app/src/main/java/org/prauga/measure/
  MainActivity.kt        PvotBaseActivity, glass intensity, theme
  MeasureAppNavBar.kt    scaffold, PvotNavBar (compact on scroll and on the Measure tab)
  AppPreferences.kt      liquid glass and imperial-units settings (SharedPreferences)
  screens/               MeasureScreen (permission, ARCore, error states), LevelScreen, SettingsScreen
  measure/               MeasureCamera (AR view, reticle, controls), DistanceBoard, MeasureMath,
                         MeasureUnits, Projection (world to screen, pure and unit tested)
  components/            the level: gravity, grid, horizon, spheres (owned by the level author)
```

- The AR view is SceneView (`io.github.sceneview:arsceneview`). Points and lines are scene nodes so
  they render in the same frame as the camera image; do not draw them in a Compose overlay, it lags.
  Length chips, readout and controls are Compose.
- Add point only works on a detected plane or a depth hit, never a loose feature point.
- The level screens and `components/` come from an external contributor; do not restyle them
  without being asked.

## Toolchain

AGP 9.5.0, Kotlin 2.4.21, Java 21, Gradle 9.6.1 (AGP 9.4+ needs Gradle 9.6.0+; SceneView needs
Java 21 bytecode). CI is `PVOT-OSS/workflows` `android-build.yml` with `java-version: '21'` and runs
`assembleDebug assembleRelease lint`. Min SDK 30, compile SDK 37.1.

## Commands

```
export ANDROID_HOME=$HOME/Library/Android/sdk     # there is no local.properties
./gradlew :app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease
./gradlew :app:testDebugUnitTest
./gradlew :app:installDebug                       # needs ANDROID_SERIAL if several devices
```

If the wrapper's Gradle cannot be downloaded, a Gradle 9.6.1 in `~/.gradle/wrapper/dists` runs the
same tasks. The release build is minified and unsigned; to run it, sign with the debug keystore.

## Testing on a device

- Test device is the user's Galaxy S23 over wireless adb (`adb devices`); the Android 15 emulator
  `Pvot_API35` has no ARCore, so it only shows the "AR is not available" state.
- The phone is the user's daily phone. Look at the screen (`dumpsys window | grep mCurrentFocus`,
  a screenshot) before every tap, tap only inside this app, and never touch other apps.
- ARCore measures to a few percent at best: about 4% over was seen on a 35.6 cm laptop. Move the
  phone slowly first so planes are found, and say so when reporting accuracy.
- Check dark and light, large font size, and theme change without restart.

## Licensing

The app is GPL-3.0-or-later (`LICENSES/`). SceneView-derived code is Apache-2.0 and credited in
`THIRD_PARTY_NOTICES.md`; Tabler icons (MIT) are credited there too, with the per-file comment kept
in each vector drawable. Add a credit whenever adopting outside code or icons.

## Open items

- The `PvotLib/` pointer is on the old PR commit `da080aa`, which is not on PvotLib `main`
  (#29 was rebased in as `894207d` and `102293f`). Move the pointer to PvotLib `main`.
- Add `LICENSES/Apache-2.0.txt` for the SceneView-derived code.
- Library gaps to propose to PvotLib: Pvot button and icon button, segmented control, switch and
  settings row, glass icon button over a camera view.
