# Creates a practice Git repository with a real merge conflict, for the Codebook lesson
# "Merge Conflicts Without Panic". Usage from PowerShell in the exercises folder:
#   powershell -ExecutionPolicy Bypass -File .\git-practice\make-conflict.ps1 [folder-name]
param([string]$Folder = "git-conflict-practice")
$ErrorActionPreference = "Stop"

if (Test-Path $Folder) {
    Write-Error "Folder '$Folder' already exists. Delete it or pass a different folder name."
    exit 1
}

New-Item -ItemType Directory $Folder | Out-Null
Push-Location $Folder
try {
    git init -q -b main
    git config user.name "Codebook Practice"
    git config user.email "practice@example.invalid"

    $file = "TurretConstants.java"
    $base = @'
public class TurretConstants {
    public static final double turretOffsetChange = 0.05; // radians per operator bumper press
    public static final double flywheelTolerance = 1000;  // RPM below setpoint that still counts as ready
}
'@
    Set-Content -Path $file -Value $base -Encoding ascii
    git add $file
    git commit -q -m "Add turret constants"

    git switch -q -c operator-trim
    (Get-Content $file) -replace 'turretOffsetChange = 0\.05', 'turretOffsetChange = 0.02' | Set-Content $file -Encoding ascii
    git commit -q -am "Make operator trim finer (0.02 rad per press)"

    git switch -q main
    (Get-Content $file) -replace 'turretOffsetChange = 0\.05', 'turretOffsetChange = 0.10' | Set-Content $file -Encoding ascii
    git commit -q -am "Make operator trim coarser for fast corrections (0.10 rad per press)"
}
finally {
    Pop-Location
}

Write-Host "Practice repository ready in '$Folder'."
Write-Host "Next:"
Write-Host "  cd $Folder"
Write-Host "  git merge operator-trim"
