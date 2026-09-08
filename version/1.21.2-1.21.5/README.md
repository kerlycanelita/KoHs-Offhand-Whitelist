# Compatibility status - Minecraft 1.21.2 to 1.21.5

## General information
- Mod: KoHs Offhand Whitelist
- Unified directory: version/1.21.2-1.21.5
- Target support: 1.21.2, 1.21.3, 1.21.4, 1.21.5
- Local clients: KoHstest-1.21.2, KoHstest-1.21.3, KoHstest-1.21.4, KoHstest-1.21.5
- Origin: merge of the individual 1.21.2 to 1.21.5 directories
- Created/synchronised: 2026-03-16

## Status per version
- 1.21.2: builds, client run (logs in logs/1.21.2)
- 1.21.3: builds, client run (logs in logs/1.21.3)
- 1.21.4: builds, client run (logs in logs/1.21.4)
- 1.21.5: builds, client run (logs in logs/1.21.5)

## Quick use
- Build 1.21.2: `./gradlew.bat remapJar configureClientLaunch downloadAssets "-Ptarget_mc=1.21.2" --no-daemon`
- Build 1.21.3: `./gradlew.bat remapJar configureClientLaunch downloadAssets "-Ptarget_mc=1.21.3" --no-daemon`
- Build 1.21.4: `./gradlew.bat remapJar configureClientLaunch downloadAssets "-Ptarget_mc=1.21.4" --no-daemon`
- Build 1.21.5: `./gradlew.bat remapJar configureClientLaunch downloadAssets "-Ptarget_mc=1.21.5" --no-daemon`

## Expected unified artifact
- `kohs-offhand-whitelist-1.21.2 - 1.21.5-1.0.2.jar`
