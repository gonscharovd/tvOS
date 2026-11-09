function Initialize-TestOSJava {
    [CmdletBinding()]
    param()

    $javaHome = $env:JAVA_HOME
    if (-not [string]::IsNullOrWhiteSpace($javaHome)) {
        $javaBin = Join-Path $javaHome "bin"
        $suffix = if ($env:OS -eq "Windows_NT") { ".exe" } else { "" }

        foreach ($tool in @("javac", "java", "keytool")) {
            $candidate = Join-Path $javaBin ($tool + $suffix)
            if (-not (Test-Path -LiteralPath $candidate -PathType Leaf)) {
                throw "JAVA_HOME is set but $candidate was not found. Install JDK 17+ or correct JAVA_HOME."
            }
        }

        $separator = [System.IO.Path]::PathSeparator
        $env:PATH = "$javaBin$separator$env:PATH"
        return [pscustomobject]@{
            Source = "JAVA_HOME"
            JavaHome = $javaHome
            Bin = $javaBin
        }
    }

    $resolved = @{}
    foreach ($tool in @("javac", "java", "keytool")) {
        $command = Get-Command $tool -CommandType Application -ErrorAction SilentlyContinue
        if (-not $command) {
            throw "JDK 17+ is required. Set JAVA_HOME or add javac, java and keytool to PATH."
        }
        $resolved[$tool] = $command.Source
    }

    return [pscustomobject]@{
        Source = "PATH"
        JavaHome = ""
        Bin = Split-Path -Parent $resolved["javac"]
    }
}
