<#
.SYNOPSIS
    Starts a local Appium server (Appium 2+).

.EXAMPLE
    .\scripts\start-appium.ps1 -Address 127.0.0.1 -Port 4723
#>
param(
    [string]$Address = "127.0.0.1",
    [int]$Port = 4723,
    [string]$LogLevel = "info"
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command appium -ErrorAction SilentlyContinue)) {
    throw "Appium is not installed. Run: npm install -g appium; appium driver install uiautomator2; appium driver install xcuitest"
}

Write-Host "Starting Appium on $Address`:$Port"
appium --address $Address --port $Port --log-level $LogLevel
