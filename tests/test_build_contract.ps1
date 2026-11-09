$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "../tools/version.ps1")
$configPath = Join-Path $PSScriptRoot "../version.properties"

$defaults = Get-TestOSBuildVersion -ConfigPath $configPath -VersionCodeOverride "" -VersionNameOverride ""
if ($defaults.VersionCode -ne "89" -or $defaults.VersionName -ne "2.1.74") {
    throw "Unexpected shared defaults: $($defaults.VersionCode) / $($defaults.VersionName)"
}

$overrides = Get-TestOSBuildVersion -ConfigPath $configPath -VersionCodeOverride "123" -VersionNameOverride "2.1.123-test"
if ($overrides.VersionCode -ne "123" -or $overrides.VersionName -ne "2.1.123-test") {
    throw "Environment overrides were not preserved"
}


# Verify JAVA_HOME precedence, PATH fallback, and invalid JAVA_HOME diagnostics.
. (Join-Path $PSScriptRoot "../tools/java.ps1")
$originalJavaHome = $env:JAVA_HOME
$originalPath = $env:PATH
$tempJdk = Join-Path ([System.IO.Path]::GetTempPath()) ("testos-jdk-" + [guid]::NewGuid().ToString("N"))
try {
    $fakeBin = Join-Path $tempJdk "bin"
    New-Item -ItemType Directory -Force -Path $fakeBin | Out-Null
    $suffix = if ($env:OS -eq "Windows_NT") { ".exe" } else { "" }
    foreach ($tool in @("javac", "java", "keytool")) {
        New-Item -ItemType File -Path (Join-Path $fakeBin ($tool + $suffix)) | Out-Null
    }

    $env:JAVA_HOME = $tempJdk
    $env:PATH = $originalPath
    $fromJavaHome = Initialize-TestOSJava
    if ($fromJavaHome.Source -ne "JAVA_HOME" -or $fromJavaHome.Bin -ne $fakeBin) {
        throw "JAVA_HOME was not preferred"
    }

    $env:JAVA_HOME = ""
    $env:PATH = $originalPath
    $fromPath = Initialize-TestOSJava
    if ($fromPath.Source -ne "PATH") {
        throw "PATH fallback was not selected when JAVA_HOME was empty"
    }

    $env:JAVA_HOME = Join-Path $tempJdk "missing"
    $failedAsExpected = $false
    try {
        Initialize-TestOSJava | Out-Null
    } catch {
        $failedAsExpected = $_.Exception.Message -like "*JAVA_HOME is set but*"
    }
    if (-not $failedAsExpected) {
        throw "Invalid JAVA_HOME did not produce an actionable error"
    }
} finally {
    $env:JAVA_HOME = $originalJavaHome
    $env:PATH = $originalPath
    if (Test-Path -LiteralPath $tempJdk) {
        Remove-Item -LiteralPath $tempJdk -Recurse -Force
    }
}

Write-Host "PowerShell build contract: versions, JAVA_HOME and PATH resolution passed."
