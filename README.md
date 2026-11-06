# Smarter Mobs: Hardcore AI & Combat Overhaul

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.28-orange.svg)](https://files.minecraftforge.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Release](https://img.shields.io/github/v/release/IhebNajjar/SmarterMobs?include_prereleases&color=blue)](https://github.com/IhebNajjar/SmarterMobs/releases/latest)

> A hardcore combat and mob AI overhaul for Minecraft Forge 1.20.1. No more towering 3 blocks up or casually dodging mobs — monsters now possess tactical instincts, hunting coordination, and anti-cheese abilities.

---

## 📥 Download & Installation

1. Go to the **[Releases](https://github.com/IhebNajjar/SmarterMobs/releases)** tab and download the latest `smartermobsitscarderidea-1.1.0.jar`.
2. Make sure you have **Minecraft Forge 1.20.1** (version `47.0.0` or higher) installed.
3. Place the `.jar` file into your `.minecraft/mods` folder:
   * **Windows:** `%APPDATA%\.minecraft\mods`
   * **Linux:** `~/.minecraft/mods`
   * **macOS:** `~/Library/Application Support/minecraft/mods`
4. Launch Minecraft using your Forge profile and enjoy!

> **Multiplayer Server Support:** This mod is **server-side compatible**! When installed on a dedicated Forge server, joining players do not need any special configuration — all AI and attacks are handled by the server.

---

## ⚡ Features

### 🐉 The Ender Dragon
* **No-Warning Ambush Swoop:** The dragon detects when your back is turned. If you look away, it will silently dive-bomb straight at you with an instant speed burst!
* **Targeted Fireball Barrage:** Spits a deadly 3-fireball spread directly at your position every 4 seconds.

### 💥 Smarter Creepers
* **Anti-Pillar & Shield Pierce:** Explosions deal blast damage right through shields and behind walls.
* **Circle & Wall Detection:** If you place a block to hide or try circling around it, the creeper commits and explodes anyway.
* **Sprint Burst:** Gains a sudden burst of speed when charging across open ground.

### 🏹 Kiting Skeletons
* **Tactical Retreat:** When you rush a skeleton with a melee weapon, it retreats backwards to keep you at bow range.
* **Sniper Vision:** Detects players from 40 blocks away and shoots faster with a 28-block firing range.

### 🕷️ Pouncing Spiders
* **Leap Attack:** If you are between 3.5 and 9 blocks away, the spider will leap through the air to pounce on you.
* **Web Venom:** Inflicts Slowness (web entanglement) and Poison when in close combat.

### ⛏️ Tunneling Zombies & Wither Skeletons
* **Anti-Towering / Anti-Digging:** Stuck zombies will actively break blocks beneath your pillar to knock you down, or dig through the floor if you hide underground!
* **Bloodhound Frenzy:** Moves faster when hunting wounded players ($\le$ 10 HP).
* **Horde Call:** Attacking a zombie alerts all nearby zombies within 35 blocks to swarm you.

### 👁️ Flanking Endermen
* **Blind-Spot Teleport:** When struck or shot, the Enderman instantly teleports directly behind your back and inflicts brief Blindness!

### 🧙‍♀️ Tactical Witches
* **Squad Buffs:** Casts Speed and Strength on nearby hostile monsters (Zombies, Creepers, Skeletons, Spiders) during fights.
* **Panic Retreat:** Hits you with Slowness and drinks Swiftness if you get too close.

### 🌊 Drowned
* **Fast Swimmers:** Moves with Dolphin's Grace in water.
* **Whirlpool Drag:** Drags swimming players underwater toward the depths.

### ⚔️ Golems, Villagers & Animals
* **Leaping Iron Golems:** Iron Golems leap through the air to smash players who harm passive mobs or villagers.
* **Medic Villagers:** Villagers will actively approach and repair damaged Iron Golems.
* **Skittish Animals:** Passive animals run away from players unless you are holding their favorite food.

---

## 🛠️ Building from Source

To compile the mod yourself:
```bash
git clone https://github.com/IhebNajjar/SmarterMobs.git
cd SmarterMobs
./gradlew build
```
The compiled jar will be created in `build/libs/`.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
