<#
.SYNOPSIS
    Runs the mobile test suite using the Maven wrapper.

.EXAMPLE
    .\scripts\run-tests.ps1 -Platform android -Environment dev -Suite src/test/resources/suites/testng-smoke.xml
#>
param(
    [ValidateSet("android", "ios")]
    [string]$Platform = "android",

    [ValidateSet("dev", "qa", "staging", "prod", "ci", "ios", "settings")]
    [string]$Environment = "dev",

    [string]$Suite = "src/test/resources/suites/testng-smoke.xml"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$mavenWrapper = Join-Path $projectRoot "mvnw.cmd"

if (-not (Test-Path -LiteralPath $mavenWrapper)) {
    throw "Maven wrapper not found at $mavenWrapper. Run 'mvn wrapper:wrapper' once."
}

Write-Host "Running mobile tests | platform=$Platform environment=$Environment suite=$Suite"

& $mavenWrapper -B test "-Denv=$Environment" "-Dplatform=$Platform" "-DsuiteXmlFile=$Suite"
exit $LASTEXITCODE
