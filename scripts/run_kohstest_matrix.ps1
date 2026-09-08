param(
    [switch]$PrepareOnly,
    [string[]]$Versions = @('1.21','1.21.1','1.21.2','1.21.3','1.21.4','1.21.5','1.21.6','1.21.7','1.21.8','1.21.9','1.21.10','1.21.11')
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot

$unifiedVersionDirs = @{
    '1.21' = 'version/1.21-1.21.1'
    '1.21.1' = 'version/1.21-1.21.1'
    '1.21.2' = 'version/1.21.2-1.21.5'
    '1.21.3' = 'version/1.21.2-1.21.5'
    '1.21.4' = 'version/1.21.2-1.21.5'
    '1.21.5' = 'version/1.21.2-1.21.5'
    '1.21.6' = 'version/1.21.6-1.21.11'
    '1.21.7' = 'version/1.21.6-1.21.11'
    '1.21.8' = 'version/1.21.6-1.21.11'
    '1.21.9' = 'version/1.21.6-1.21.11'
    '1.21.10' = 'version/1.21.6-1.21.11'
    '1.21.11' = 'version/1.21.6-1.21.11'
}

foreach($v in $Versions){
    $dir = if($unifiedVersionDirs.ContainsKey($v)){
        Join-Path $repo $unifiedVersionDirs[$v]
    } else {
        Join-Path $repo "version/$v"
    }
    if(-not (Test-Path $dir)){
        Write-Warning ("No existe carpeta para version {0}: {1}" -f $v, $dir)
        continue
    }

    $targetPropertyArgs = @()
    if($unifiedVersionDirs.ContainsKey($v)){
        $targetPropertyArgs += "-Ptarget_mc=$v"
    }

    Write-Host "\n=== Compilando y preparando KoHstest $v ==="
    Push-Location $dir
    try {
        & .\gradlew.bat remapJar configureClientLaunch downloadAssets @targetPropertyArgs --no-daemon
        if($LASTEXITCODE -ne 0){
            Write-Warning ("Fallo compilacion/preparacion para {0} (exit code {1})" -f $v, $LASTEXITCODE)
            continue
        }

        if(-not $PrepareOnly){
            Write-Host "Iniciando instancia KoHstest-$v..."
            & .\gradlew.bat runClient @targetPropertyArgs --no-daemon
            if($LASTEXITCODE -ne 0){
                Write-Warning ("Fallo runClient para {0} (exit code {1})" -f $v, $LASTEXITCODE)
            }
        }
    }
    finally {
        Pop-Location
    }
}
