function Get-TestOSBuildVersion {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$ConfigPath,
        [string]$VersionCodeOverride = $env:VERSION_CODE,
        [string]$VersionNameOverride = $env:VERSION_NAME
    )

    $config = @{}
    foreach ($line in Get-Content -LiteralPath $ConfigPath) {
        if ($line -match '^\s*([^#=\s]+)\s*=\s*(.*?)\s*$') {
            $config[$matches[1]] = $matches[2]
        }
    }

    if (-not $config.ContainsKey("DEFAULT_VERSION_CODE") -or
        -not $config.ContainsKey("DEFAULT_VERSION_NAME")) {
        throw "version.properties must define DEFAULT_VERSION_CODE and DEFAULT_VERSION_NAME"
    }

    return [pscustomobject]@{
        VersionCode = if ([string]::IsNullOrWhiteSpace($VersionCodeOverride)) {
            $config["DEFAULT_VERSION_CODE"]
        } else {
            $VersionCodeOverride
        }
        VersionName = if ([string]::IsNullOrWhiteSpace($VersionNameOverride)) {
            $config["DEFAULT_VERSION_NAME"]
        } else {
            $VersionNameOverride
        }
    }
}
