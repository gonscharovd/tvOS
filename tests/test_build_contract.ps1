$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "..\\tools/version.ps1")
$configPath = Join-Path $PSScriptRoot "../version.properties"

$defaults = Get-TestOSBuildVersion -ConfigPath $configPath -VersionCodeOverride "" -VersionNameOverride ""
if ($defaults.VersionCode -ne "89" -or $defaults.VersionName -ne "2.1.74") {
    throw "Unexpected shared defaults: $($defaults.VersionCode) / $($defaults.VersionName)"
}

$overrides = Get-TestOSBuildVersion -ConfigPath $configPath -VersionCodeOverride "123" -VersionNameOverride "2.1.123-test"
if ($overrides.VersionCode -ne "123" -or $overrides.VersionName -ne "2.1.123-test") {
    throw "Environment overrides were not preserved"
}

Write-Host "PowerShell build version contract: defaults and environment overrides passed."
