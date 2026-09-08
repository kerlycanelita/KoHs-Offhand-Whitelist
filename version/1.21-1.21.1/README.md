# Compatibility status - Minecraft 1.21 and 1.21.1

## General information
- Mod: KoHs Offhand Whitelist
- Unified directory: version/1.21-1.21.1
- Target support: 1.21, 1.21.1
- Local clients: KoHstest-1.21 and KoHstest-1.21.1
- Origin: merge of version/1.21 and version/1.21.1
- Created/synchronised: 2026-03-16

## Status per version
### 1.21
- Status: builds
- Build: builds (run 2026-03-14 21:14:30)
- Last review: 2026-03-16
- Expected artifact name: kohs-offhand-whitelist-1.21 - 1.21.1-1.0.2.jar

### 1.21.1
- Status: builds
- Build: builds (run 2026-03-14 21:15:15, runClient run 2026-03-16)
- Last review: 2026-03-16
- Expected artifact name: kohs-offhand-whitelist-1.21 - 1.21.1-1.0.2.jar

## Quick use (one directory, two versions)
- Build 1.21: `./gradlew.bat remapJar configureClientLaunch downloadAssets "-Ptarget_mc=1.21" --no-daemon`
- Build 1.21.1: `./gradlew.bat remapJar configureClientLaunch downloadAssets "-Ptarget_mc=1.21.1" --no-daemon`
- Run the 1.21 client: `./run-client-1.21.bat`
- Run the 1.21.1 client: `./run-client-1.21.1.bat`

## Notes
- `src` is single and shared by both versions.
- The clients are kept apart by run directory (`KoHstest-1.21` and
  `KoHstest-1.21.1`).
- Historical logs moved to `logs/1.21` and `logs/1.21.1`.

## Checklist
- [ ] Review gradle.properties
- [ ] Review the Minecraft version
- [ ] Review the Yarn mappings
- [ ] Review Fabric Loader
- [ ] Review Fabric API
- [ ] Confirm the final artifact name
- [ ] Run the build
- [ ] Fix import or mapping errors
- [ ] Validate fabric.mod.json
- [ ] Validate the mixins
- [ ] Document the final result
