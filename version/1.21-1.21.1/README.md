# Estado de compatibilidad - Minecraft 1.21 y 1.21.1

## Informacion general
- Mod: KoHs Offhand Whitelist
- Carpeta unificada: version/1.21-1.21.1
- Soporte objetivo: 1.21, 1.21.1
- Clientes locales: KoHstest-1.21 y KoHstest-1.21.1
- Origen: fusion de version/1.21 y version/1.21.1
- Fecha de creacion/sincronizacion: 2026-03-16

## Estado por version
### 1.21
- Estado: Compila
- Compilacion: Compila (build ejecutado 2026-03-14 21:14:30)
- Ultima revision: 2026-03-16
- Nombre esperado del artefacto: kohs-offhand-whitelist-1.21 - 1.21.1-1.0.2.jar

### 1.21.1
- Estado: Compila
- Compilacion: Compila (build ejecutado 2026-03-14 21:15:15 y runClient ejecutado 2026-03-16)
- Ultima revision: 2026-03-16
- Nombre esperado del artefacto: kohs-offhand-whitelist-1.21 - 1.21.1-1.0.2.jar

## Uso rapido (misma carpeta, dos versiones)
- Compilar 1.21: `./gradlew.bat remapJar configureClientLaunch downloadAssets "-Ptarget_mc=1.21" --no-daemon`
- Compilar 1.21.1: `./gradlew.bat remapJar configureClientLaunch downloadAssets "-Ptarget_mc=1.21.1" --no-daemon`
- Ejecutar cliente 1.21: `./run-client-1.21.bat`
- Ejecutar cliente 1.21.1: `./run-client-1.21.1.bat`

## Observaciones
- `src` es unico y compartido para ambas versiones.
- Los clientes quedan separados por carpeta de ejecucion (`KoHstest-1.21` y `KoHstest-1.21.1`).
- Logs historicos movidos a `logs/1.21` y `logs/1.21.1`.

## Checklist
- [ ] Revisar gradle.properties
- [ ] Revisar version de Minecraft
- [ ] Revisar Yarn mappings
- [ ] Revisar Fabric Loader
- [ ] Revisar Fabric API
- [ ] Confirmar nombre final del artefacto
- [ ] Ejecutar compilacion
- [ ] Corregir errores de imports o mappings
- [ ] Validar fabric.mod.json
- [ ] Validar mixins
- [ ] Documentar resultado final
