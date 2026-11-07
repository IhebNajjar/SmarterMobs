# Smarter Mobs: Hardcore AI & Combat Overhaul

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.28-orange.svg)](https://files.minecraftforge.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Release](https://img.shields.io/github/v/release/IhebNajjar/SmarterMobs?include_prereleases&color=blue)](https://github.com/IhebNajjar/SmarterMobs/releases/latest)

> A hardcore combat and mob AI overhaul for Minecraft Forge 1.20.1. No more towering 3 blocks up or casually dodging mobs — monsters now possess tactical instincts, teamwork, web traps, meat-shielding, and anti-cheese abilities.

---

## 📥 Download & Installation

1. Go to the **[Releases](https://github.com/IhebNajjar/SmarterMobs/releases)** tab and download the latest `smartermobsitscarderidea-1.2.0.jar`.
2. Make sure you have **Minecraft Forge 1.20.1** (version `47.0.0` or higher) installed.
3. Place the `.jar` file into your `.minecraft/mods` folder:
   * **Windows:** `%APPDATA%\.minecraft\mods`
   * **Linux:** `~/.minecraft/mods`
   * **macOS:** `~/Library/Application Support/minecraft/mods`
4. Launch Minecraft using your Forge profile and enjoy!

> **Multiplayer Server Support:** This mod is **server-side compatible**! When installed on a dedicated Forge server, joining players do not need any special configuration — all AI, attacks, and mechanics are handled by the server.

---

## ⚡ Features in v1.2.0

### 🌐 Universal Mob Upgrades (All Hostile Mobs)
* **Pack Aggro (24 Blocks):** When any hostile mob spots you, all nearby mobs of the same type within 24 blocks immediately join the hunt.
* **10-Second Memory Tracking:** Monsters remember and continue pathfinding to your last known position for 10 seconds after losing line of sight instead of giving up.

### 🏹 Skeletons (Tactical Kiting & Meat-Shields)
* **10-Block Retreat:** Retreats when you come within 10 blocks to maintain optimal bow range.
* **Meat-Shield Tactics:** Skeletons actively locate nearby tanky mobs (Zombies, Golems, etc.) and maneuver to keep that mob between themselves and you!
* **Fixed Trapped / Close-Up Combat:** Skeletons no longer run uselessly into walls when cornered or rushed! If trapped or within 3.5 blocks, they stand their ground, shoot point-blank arrows, and melee punch you!

### 🕷️ Spiders (Leap & Web Traps)
* **Pounce Attack:** Leaps through the air at you from 4 blocks away.
* **Web Throw (3s Cooldown):** If you stay within 3 blocks of a spider, it throws cobwebs right at your feet and inflicts severe Slowness and Poison!

### 🐷 Piglins & Piglin Brutes
* **Pack Retaliation (32 Blocks):** Striking any Piglin or Brute enrages all Piglins within 32 blocks.
* **Crossbow Piglins:** Kite backwards when you get close to maintain distance, or shoot point-blank if cornered.
* **Wall-Breaking Piglin Brutes:** Brutes dig through blocks like zombies to dismantle your walls or pillars.

### 🐗 Hoglins
* **Charging Speed Burst:** Gets a speed burst when you are 6 or more blocks away to charge you down.
* **Heavy Knockback:** Hits players with increased, brutal knockback.
* **Herd Aggro:** Calls all nearby Hoglins within 32 blocks to charge together.

### 🏹 Pillagers & 🪓 Vindicators
* **Crossbow Pillagers:** Kite backwards when approached and shoot point-blank if trapped.
* **Sprinting Vindicators:** Sprint with high movement speed and dig through walls and barricades.

### 🧙‍♂️ Evokers
* **Fangs Barrage:** Sends a straight line of Evoker Fangs directly at your position every 3.5 seconds.
* **Vex Summoning:** Spawns Vexes twice as frequently.
* **Tactical Escape:** Sprints away when you get within 8 blocks.

### 🌊 Drowned
* **Faster Swimming:** Gains Dolphin's Grace and Speed in water.
* **Whirlpool Drag:** Drags swimming players downwards into the depths.
* **Daylight Land Chasing:** Chases players onto dry land without burning or catching fire in daylight.
* **Trident Sniper:** Throws tridents from up to 30 blocks away.

### 🧙‍♀️ Witches
* **Rapid Self-Healing:** Instantly heals and regenerates twice as fast when injured.
* **Sniper Potions:** Throws debuff potions from up to 22 blocks away.
* **Tactical Escape:** Kites away with Speed when rushed and slows the player.

### 🔥 Blazes
* **Sideways Strafing:** Strafes left and right to evade player arrows.
* **Burst Fire:** Fires 3 small fireballs in rapid spread bursts.

### 🦇 Phantoms
* **Flock Dive:** Dives in synchronized groups of 3 or more with increased dive velocity.

### 🐉 The Ender Dragon
* **Paced Combat Cooldown:** Paced attack cooldown between breath fireball barrages and charge attacks so they never collide unfair.
* **Silent Ambush Swoop:** Detects when you look away and dive-bombs your blind spot with a sudden speed burst.

### 💥 Creepers, Zombies & Golems
* **Wall-Piercing Creepers:** Blast damages through shields and walls; commits to exploding if you circle it.
* **Tunneling Zombies:** Digs down or breaks pillar blocks when stuck.
* **Leaping Iron Golems & Medic Villagers:** Golems leap at players who harm villagers; villagers repair golems.

---

## 🛠️ Building from Source

```bash
git clone https://github.com/IhebNajjar/SmarterMobs.git
cd SmarterMobs
./gradlew build
```
The compiled jar will be created in `build/libs/`.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
