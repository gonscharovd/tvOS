$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# Load the same version.properties contract as build.sh.
. (Join-Path $PSScriptRoot "tools/version.ps1")
$buildVersion = Get-TestOSBuildVersion -ConfigPath (Join-Path $PSScriptRoot "version.properties")
$versionCode = $buildVersion.VersionCode
$versionName = $buildVersion.VersionName

if (-not $env:JAVA_HOME) {
    $jdkCandidate = "C:\Users\asen4\AppData\Local\Programs\Microsoft\jdk-17.0.10.7-hotspot"
    if (Test-Path $jdkCandidate) {
        $env:JAVA_HOME = $jdkCandidate
    }
}
if ($env:JAVA_HOME) {
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
}

$SDK = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } elseif ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } else { "$env:LOCALAPPDATA\Android\Sdk" }
$BT = "$SDK\build-tools\34.0.0"
$JAR = "$SDK\platforms\android-34\android.jar"

if (Test-Path "build") { Get-ChildItem -Path "build" | Remove-Item -Recurse -Force }
New-Item -ItemType Directory -Force -Path "build\gen", "build\classes", "build\dex" | Out-Null
if ($env:ASSET_OPTIMIZE -eq "1") {
    $python = Get-Command python -ErrorAction SilentlyContinue
    if (-not $python) { throw "Python 3 is required when ASSET_OPTIMIZE=1" }
    & $python.Source tools/prepare_assets.py --source assets --dest build/assets --optimize --profile $(if ($env:ASSET_PROFILE) { $env:ASSET_PROFILE } else { "full" }) --quality 88 --max-width 1920 --max-height 1080
    if ($LASTEXITCODE -ne 0) { throw "Asset optimization failed" }
} else {
    Copy-Item -Path assets -Destination build -Recurse
}

& "$BT\aapt2.exe" compile --dir res -o build\res.zip
if ($LASTEXITCODE -ne 0) { throw "aapt2 compile failed" }

& "$BT\aapt2.exe" link -o build\unsigned.apk -I "$JAR" --manifest AndroidManifest.xml --rename-manifest-package "com.gonscharovd.tvoslauncher" `
    --java build\gen --min-sdk-version 24 --target-sdk-version 34 `
    --version-code $versionCode --version-name $versionName build\res.zip
if ($LASTEXITCODE -ne 0) { throw "aapt2 link failed" }

$javaFiles = Get-ChildItem -Recurse -Path src, build\gen -Filter "*.java" | Select-Object -ExpandProperty FullName
$javaFiles | Out-File -Encoding ascii build\sources.txt
& javac -encoding UTF-8 --release 11 -classpath "$JAR" -d build\classes "@build\sources.txt"
if ($LASTEXITCODE -ne 0) { throw "javac failed" }

& jar cf build\classes.jar -C build\classes .
if ($LASTEXITCODE -ne 0) { throw "jar failed" }

& "$BT\d8.bat" --release --min-api 24 --lib "$JAR" --output build\dex build\classes.jar
if ($LASTEXITCODE -ne 0) { throw "d8 failed" }

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::Open((Resolve-Path "build\unsigned.apk").Path, [System.IO.Compression.ZipArchiveMode]::Update)
try {
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, (Resolve-Path "build\dex\classes.dex").Path, "classes.dex", [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null

    # Package assets with POSIX forward slashes (Windows aapt2 -A writes backslashes which Android AssetManager cannot open)
    $assetsRoot = (Resolve-Path "build\assets").Path
    $noCompressExts = @(".jpg", ".jpeg", ".png", ".webp", ".gif", ".mp3", ".wav", ".ogg", ".mp4", ".webm")
    Get-ChildItem -Recurse -File -Path $assetsRoot | ForEach-Object {
        $rel = $_.FullName.Substring($assetsRoot.Length + 1).Replace('\', '/')
        $entryName = "assets/$rel"
        $existing = $archive.GetEntry($entryName)
        if ($existing) { $existing.Delete() }
        $ext = $_.Extension.ToLowerInvariant()
        $level = if ($noCompressExts -contains $ext) {
            [System.IO.Compression.CompressionLevel]::NoCompression
        } else {
            [System.IO.Compression.CompressionLevel]::Optimal
        }
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $_.FullName, $entryName, $level) | Out-Null
    }
} finally {
    $archive.Dispose()
}

& "$BT\zipalign.exe" -f 4 build\unsigned.apk build\aligned.apk
if ($LASTEXITCODE -ne 0) { throw "zipalign failed" }

if ($env:RELEASE_KEYSTORE) {
    if (-not $env:RELEASE_KEYSTORE_PASS) { throw "Set RELEASE_KEYSTORE_PASS when RELEASE_KEYSTORE is configured" }
    $releaseAlias = if ($env:RELEASE_KEY_ALIAS) { $env:RELEASE_KEY_ALIAS } else { "tvos-release" }
    $signArgs = @("sign", "--ks", $env:RELEASE_KEYSTORE, "--ks-pass", "env:RELEASE_KEYSTORE_PASS",
        "--ks-key-alias", $releaseAlias, "--out", "build\tvos-home.apk", "build\aligned.apk")
    & "$BT\apksigner.bat" @signArgs
    if ($LASTEXITCODE -ne 0) { throw "release apksigner failed" }
    Write-Host "Signed with the release key"
} else {
    if ($env:DEBUG_KEYSTORE) {
        $KS = $env:DEBUG_KEYSTORE
        $debugAlias = if ($env:DEBUG_KEY_ALIAS) { $env:DEBUG_KEY_ALIAS } elseif ($KS -like "*\.android\debug.keystore") { "androiddebugkey" } else { "debug" }
    } elseif (Test-Path "debug.keystore") {
        $KS = "debug.keystore"
        $debugAlias = if ($env:DEBUG_KEY_ALIAS) { $env:DEBUG_KEY_ALIAS } else { "debug" }
    } else {
        $androidDebugDir = Join-Path $env:USERPROFILE ".android"
        $androidDebugKey = Join-Path $androidDebugDir "debug.keystore"
        New-Item -ItemType Directory -Force -Path $androidDebugDir | Out-Null
        $KS = $androidDebugKey
        $debugAlias = if ($env:DEBUG_KEY_ALIAS) { $env:DEBUG_KEY_ALIAS } else { "androiddebugkey" }
        if (-not (Test-Path $KS)) {
            & keytool -genkeypair -keystore $KS -storepass android -keypass android -alias $debugAlias `
                -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=tvOS Launcher debug" | Out-Null
            if ($LASTEXITCODE -ne 0) { throw "debug keystore generation failed" }
        }
    }

    & "$BT\apksigner.bat" sign --ks $KS --ks-pass pass:android --key-pass pass:android --ks-key-alias $debugAlias `
        --out build\tvos-home.apk build\aligned.apk
    if ($LASTEXITCODE -ne 0) { throw "debug apksigner failed" }
    Write-Host "Signed with debug key: $KS (alias: $debugAlias)"
}

Write-Host "Built build\tvos-home.apk"
