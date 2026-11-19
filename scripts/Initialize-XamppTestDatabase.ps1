#requires -Version 5.1
<#
.SYNOPSIS
Creates the separate XAMPP acceptance database once, preserving existing data.
.DESCRIPTION
Start MySQL in the XAMPP Control Panel before running this script. The database
name is fixed to life_is_bananas_acceptance. If it already contains any tables,
the script leaves it unchanged. Passwords are passed only to the child process
through MYSQL_PWD; XAMPP_DB_PASSWORD can supply the password without a parameter.
#>
[CmdletBinding()]
param(
    [string]$XamppPath = 'C:\xampp',

    [ValidatePattern('^[A-Za-z0-9._:-]+$')]
    [string]$DbHost = '127.0.0.1',

    [ValidateRange(1, 65535)]
    [int]$DbPort = 3306,

    [ValidatePattern('^[A-Za-z0-9_.-]+$')]
    [string]$DbUser = 'root',

    [AllowEmptyString()]
    [string]$Password = $env:XAMPP_DB_PASSWORD
)

$ErrorActionPreference = 'Stop'
$testSchema = 'life_is_bananas_acceptance'
$mysqlPath = Join-Path $XamppPath 'mysql\bin\mysql.exe'
if (-not (Test-Path -LiteralPath $mysqlPath -PathType Leaf)) {
    throw "Cannot find the XAMPP MySQL client at '$mysqlPath'. Set -XamppPath to your XAMPP installation directory."
}
$mysqlPath = (Resolve-Path -LiteralPath $mysqlPath).ProviderPath
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Invoke-XamppSql {
    param([Parameter(Mandatory = $true)][string]$Sql)

    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $mysqlPath
    # These values are validated to contain no spaces, quotes, or shell syntax.
    # FileName handles installation paths containing spaces; no shell is used.
    $startInfo.Arguments = "--no-defaults --protocol=TCP --default-character-set=utf8mb4 --host=$DbHost --port=$DbPort --user=$DbUser --batch --skip-column-names"
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardInput = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.StandardOutputEncoding = $utf8
    $startInfo.StandardErrorEncoding = $utf8
    $startInfo.EnvironmentVariables['MYSQL_PWD'] = [string]$Password

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    try {
        [void]$process.Start()
        $outputTask = $process.StandardOutput.ReadToEndAsync()
        $errorTask = $process.StandardError.ReadToEndAsync()
        # Windows PowerShell 5.1 otherwise sends redirected text in a legacy
        # encoding. Write UTF-8 bytes directly so the canonical SQL is preserved.
        $sqlBytes = $utf8.GetBytes($Sql)
        $process.StandardInput.BaseStream.Write($sqlBytes, 0, $sqlBytes.Length)
        $process.StandardInput.Close()
        $process.WaitForExit()
        $output = $outputTask.GetAwaiter().GetResult()
        $errorOutput = $errorTask.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) {
            throw "XAMPP MySQL failed (exit $($process.ExitCode)): $($errorOutput.Trim())"
        }
        return $output.Trim()
    }
    finally {
        $process.Dispose()
    }
}

$tableCountText = Invoke-XamppSql "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$testSchema';"
$tableCount = 0
if (-not [int]::TryParse($tableCountText, [ref]$tableCount)) {
    throw "Could not read the table count for '$testSchema'. No schema was imported."
}
if ($tableCount -gt 0) {
    Write-Host "Database '$testSchema' already contains $tableCount tables. Existing data was preserved."
    return
}

$repositoryPath = Split-Path -Parent $PSScriptRoot
$schemaPath = Join-Path $repositoryPath 'Project-1-Rechttoe-Rechtaan\db\life_is_bananas.sql'
$canonicalSql = [System.IO.File]::ReadAllText($schemaPath, $utf8)
$useStatement = [regex]'(?im)^[ \t]*USE[ \t]+life_is_bananas[ \t]*;[ \t]*\r?$'
if ($useStatement.Matches($canonicalSql).Count -ne 1) {
    throw "Expected exactly one active 'USE life_is_bananas;' statement in '$schemaPath'. No schema was imported."
}
$testSql = $useStatement.Replace($canonicalSql, "USE $testSchema;")
$createSchema = "CREATE DATABASE IF NOT EXISTS $testSchema CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
[void](Invoke-XamppSql ($createSchema + "`n" + $testSql))
Write-Host "Initialized '$testSchema' on ${DbHost}:$DbPort from the canonical Project 1 schema."
