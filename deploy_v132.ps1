$buildJar = "C:\Users\ihebn\OneDrive\Documents\progects\SmarterMobs\build\libs\smartermobsitscarderidea-1.3.2.jar"
$mcModsDir = "$env:APPDATA\.minecraft\mods"
$downloadsDir = "C:\Users\ihebn\Downloads"

Write-Output "Checking jar: $buildJar"
if (-not (Test-Path $buildJar)) {
    Write-Error "Build jar not found!"
    exit 1
}

# 1. Clean up old smartermobs jars from .minecraft\mods and copy new one
if (Test-Path $mcModsDir) {
    Get-ChildItem -Path $mcModsDir -Filter "*smartermobs*" | Remove-Item -Force
    Copy-Item -Path $buildJar -Destination $mcModsDir -Force
    Write-Output "Installed to Minecraft mods folder: $mcModsDir"
}

# 2. Copy to Downloads folder
Copy-Item -Path $buildJar -Destination $downloadsDir -Force
Write-Output "Copied to Downloads: $downloadsDir"

# 3. Git commit & push
Set-Location "C:\Users\ihebn\OneDrive\Documents\progects\SmarterMobs"
git add .
git commit -m "v1.3.2: Re-introduce chase momentum acceleration for slow mobs (Zombies, Creepers, Brutes, Wither Skeletons, Land Drowned)"
git push origin main

# 4. GitHub Release
$notes = @"
### What's New in v1.3.2:
- **Chasing Momentum Acceleration**:
  - Slow pursuing mobs (**Zombies**, **Creepers**, **Piglin Brutes**, **Wither Skeletons**, and **Drowned on land**) start moving at their normal slow speed when they first spot you.
  - If they have been continuously walking / pursuing you for **3 blocks** without stopping or getting blocked, they build momentum and accelerate!
  - If the player is sprinting away or at a distance (> 6 blocks), pursuers gain extra speed burst (Speed II) so you cannot effortlessly outrun them.
  - If the mob is trapped, knocked back, or stopped (`moved < 0.02`), its momentum resets to 0, forcing it to build up speed from scratch again.
- **Includes all previous overhauls**:
  - Creepers commit to explosion if walled off or circled; explosions penetrate shields and walls.
  - Skeletons: obstacle-avoiding retreat when <= 10 blocks, hides behind meat-shield monsters, rapid-fire point-blank shots when <= 3.5 blocks, emergency 5-block bow bash at low health (<= 8 HP).
  - Iron Golem village crime & stealth theft detection.
  - Frequent swooping Ender Dragon dive attacks.
  - Spiders throw cobweb (3s cooldown) & leap.
  - Piglins, Pillagers, Evokers, Drowned, Witches, Blazes, and Phantoms enhancements.
"@

gh release create v1.3.2 "$buildJar" --title "SmarterMobs v1.3.2 - Chasing Momentum Acceleration" --notes "$notes"

# 5. Set clipboard to release url
Set-Clipboard -Value "https://github.com/IhebNajjar/SmarterMobs/releases/tag/v1.3.2"

Write-Output "Successfully deployed and released v1.3.2!"
