<#
.SYNOPSIS
    Generates (or serves) the Allure report using the fully qualified plugin goal.

.EXAMPLE
    .\scripts\allure-report.ps1
    .\scripts\allure-report.ps1 -Serve
#>
param(
    [switch]$Serve
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$mavenWrapper = Join-Path $projectRoot "mvnw.cmd"

if (-not (Test-Path -LiteralPath $mavenWrapper)) {
    throw "Maven wrapper not found at $mavenWrapper"
}

$goal = if ($Serve) {
    "io.qameta.allure:allure-maven:2.15.0:serve"
} else {
    "io.qameta.allure:allure-maven:2.15.0:report"
}

& $mavenWrapper -B $goal
exit $LASTEXITCODE
