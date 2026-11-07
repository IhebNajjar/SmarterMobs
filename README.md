# Smarter Mobs: Hardcore AI & Combat Overhaul

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.28-orange.svg)](https://files.minecraftforge.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Latest Release](https://img.shields.io/github/v/release/IhebNajjar/SmarterMobs?color=blue)](https://github.com/IhebNajjar/SmarterMobs/releases/latest)

> **Tired of dumb vanilla mobs?** No more towering up 3 dirt blocks, running in straight lines, or casually picking off monsters.
> **Smarter Mobs** completely rebuilds hostile mob intelligence: monsters build chasing momentum, skeletons use zombies as human meat-shields and bash you with their bows, creepers detonate through shields and walls, spiders trap your feet in cobwebs, and iron golems enforce village laws against thieves and murderers.

---

## 📥 How to Download & Install (Step-by-Step Guide)

> ⚠️ **CRITICAL FOR GITHUB USERS:**
> **DO NOT** click the green **"<> Code -> Download ZIP"** button at the top of the GitHub page! That button only downloads the uncompiled raw source code.
> To get the actual, working mod file, follow the simple steps below.

---

### Step 1: Download the Mod File (`.jar`)
1. Click here to go to the **[Latest Release Page](https://github.com/IhebNajjar/SmarterMobs/releases/latest)**.
2. Scroll down to the **"Assets"** section at the bottom of the release.
3. Click on **`smartermobsitscarderidea-1.3.2.jar`** to download it.
   * *Direct Download Link:* [Download v1.3.2 .jar](https://github.com/IhebNajjar/SmarterMobs/releases/download/v1.3.2/smartermobsitscarderidea-1.3.2.jar)

---

### Step 2: Install Minecraft Forge 1.20.1
If you haven't installed Forge yet:
1. Go to the official [Minecraft Forge 1.20.1 Download Page](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html).
2. Click **Installer** (Recommended version: `47.2.0` or `47.4.28`).
3. Run the downloaded `.jar` installer, choose **Install Client**, and click **OK**.

---

### Step 3: Put the Mod in your `mods` Folder

#### 🪟 Windows:
1. Press the **Windows Key + R** on your keyboard to open the *Run* dialog.
2. Type `%appdata%\.minecraft` and press **Enter**.
3. Inside your `.minecraft` directory, open the **`mods`** folder. *(If the `mods` folder doesn't exist, create a new folder named `mods`)*.
4. Move or copy the downloaded **`smartermobsitscarderidea-1.3.2.jar`** into this **`mods`** folder.

#### 🍏 macOS:
1. Open **Finder**.
2. Press **Command + Shift + G** and paste: `~/Library/Application Support/minecraft`
3. Open or create the **`mods`** folder.
4. Move the `.jar` file into the folder.

#### 🐧 Linux:
1. Navigate to `~/.minecraft/mods/`
2. Place the `.jar` file inside.

---

### Step 4: Using Third-Party Mod Launchers (Optional)
If you use launchers like **CurseForge**, **Prism Launcher**, **Modrinth App**, or **ATLauncher**:
1. Open your **Minecraft 1.20.1 (Forge)** instance.
2. Click on **"Mods"** or **"Open Mods Folder"**.
3. Drag and drop **`smartermobsitscarderidea-1.3.2.jar`** directly into the list or folder.

---

### Step 5: Launch the Game!
1. Open your Minecraft Launcher.
2. Select your **Forge 1.20.1** installation from the drop-down profile list next to the Play button.
3. Click **Play** and enjoy smarter, hardcore monsters!

---

## 🖥️ Multiplayer & Dedicated Server Installation

**Good news:** SmarterMobs is **100% Server-Side Compatible**!
* **Server Owners:** Drop `smartermobsitscarderidea-1.3.2.jar` into your Forge server's `mods/` directory and restart the server.
* **Joining Players:** Players connecting to your server **do NOT need to install the mod**! They can join using a standard vanilla client or any Forge client. All AI, knockback, tracking, and combat logic are executed on the server.

---

## ⚡ Complete Feature List (v1.3.2)

### 🏃 Chasing Momentum Acceleration (Slow Mobs)
* **Mobs:** Zombies, Husks, Zombie Villagers, Creepers, Piglin Brutes, Wither Skeletons, and Drowned on land.
* **Momentum Build-Up:** When pursuers spot you, they move at their standard slow speed at first. Once they continuously walk/pursue for **3 blocks** without being stopped or trapped, they build momentum and accelerate!
* **Sprint Tracking:** If you sprint away or get beyond 6 blocks, pursuers gain an extra speed burst (Speed II) so you cannot easily run away in straight lines.
* **Momentum Reset:** If a mob is hit with knockback, trapped, or blocked by a wall, its momentum resets to 0 and must be rebuilt.

### 🏹 Skeletons (Tactical Kiting, Meat-Shields & Low-Health Bow Bash)
* **10-Block Tactical Retreat:** Backs away using smart pathfinding that avoids obstacles and never runs blindly into walls.
* **Living Meat-Shields:** Actively navigates behind nearby tanky monsters (Zombies, Golems) to hide from your arrows and attacks!
* **Rapid-Fire Point-Blank:** When you rush within 3.5 blocks or corner a skeleton, it shoots rapid point-blank arrows (every 0.45s) and melee attacks!
* **Low-Health Emergency Bow Bash:** If a skeleton's health drops low (≤ 8 HP) and you get within 3 blocks, it violently strikes you with its bow, dealing blunt damage and **launching you 5 blocks backward**!

### 🛡️ Iron Golems & Village Law Enforcement
* **Crime Detection:** Golems immediately target and hunt players who attack or kill villagers.
* **Stealth Thievery:** You can loot village chests, barrels, and break beds/doors safely **ONLY IF** no villager or iron golem has line of sight to you. If caught stealing, golems will relentlessly hunt you down!
* **Leap Attacks:** Golems can leap towards targets to close distance quickly.
* **Medic Villagers:** Villagers will actively attempt to heal and repair damaged iron golems.

### 🐉 The Ender Dragon (Frequent Dives & Blind-Spot Ambush)
* **Frequent Swooping Dives:** Regularly dive-bombs the player (every ~2.7s), making the End fight intense and dynamic.
* **Blind-Spot Ambush:** Detects when you look away or turn your back and immediately dive-bombs your position.
* **Balanced Fireballs:** Reduced fireball spam; only shoots breath attacks from long range (> 28 blocks).

### 🕷️ Spiders (Leap & Web Traps)
* **Pounce Attack:** Leaps through the air at you from 4 blocks away.
* **Web Throw (3s Cooldown):** When within 3 blocks, places real cobwebs at your feet and inflicts severe Slowness and Poison!

### 💥 Creepers (Anti-Cheese & Shield Piercing)
* **Wall-Piercing Blast:** Explosions break shields on contact and deal lethal damage even through thin protective walls.
* **Anti-Circling Commitment:** Commits to detonating if the player circles it or walls it off within close range.

### 🐷 Piglins & Piglin Brutes
* **Pack Retaliation (32 Blocks):** Hitting any Piglin alerts all piglins in a 32-block radius.
* **Crossbow Kiting:** Crossbow Piglins back off to maintain shooting distance.
* **Wall-Breaking Brutes:** Brutes dig through barricades and blocks to reach you.

### 🐗 Hoglins
* **Speed Burst Charge:** Surges with speed when 6+ blocks away to charge you down.
* **Brutal Knockback:** Hits players with heavy, disorienting knockback.
* **Herd Charge:** Calls nearby Hoglins within 32 blocks to join the charge.

### 🏹 Pillagers & 🪓 Vindicators
* **Crossbow Pillagers:** Kite backwards when approached and shoot point-blank if trapped.
* **Sprinting Vindicators:** Sprint with high movement speed and dig through walls.

### 🧙‍♂️ Evokers
* **Fangs Line:** Sends a straight line of Evoker Fangs directly at your position every 3.5 seconds.
* **Frequent Vexes:** Summons Vexes twice as frequently.
* **Tactical Escape:** Sprints away when you get within 8 blocks.

### 🌊 Drowned
* **Fast Swimming:** Gains Dolphin's Grace and Speed in water.
* **Whirlpool Drag:** Drags swimming players downwards.
* **Daylight Land Chasing:** Chases players onto dry land without burning in sunlight.
* **Trident Sniper:** Throws tridents from up to 30 blocks away.

### 🧙‍♀️ Witches
* **Rapid Self-Healing:** Rapidly heals and regenerates when damaged.
* **Sniper Potions:** Throws debuff potions from up to 22 blocks away.
* **Ally Buffs:** Grants speed and strength to nearby monster allies.

### 🔥 Blazes
* **Sideways Strafing:** Strafes left and right to evade arrows.
* **Burst Fire:** Fires 3 fireballs in rapid spread bursts.

### 🦇 Phantoms
* **Flock Dive:** Dives in synchronized groups of 3 or more with increased velocity.

### 👁️ Endermen
* **Flanking Teleport:** When damaged by a player, teleports directly behind the player's back and inflicts temporary blindness.
* **End Dimension Balance:** Strictly excluded from pack aggro, preventing cascade swarming in The End.

### 🌐 Universal Monster Upgrades
* **Pack Aggro (24 Blocks):** When a hostile mob spots you, all nearby mobs of the same type within 24 blocks join the attack.
* **10-Second Memory Tracking:** Monsters remember and continue tracking your last known position for 10 seconds after losing sight.

---

## ❓ Frequently Asked Questions (FAQ)

<details>
<summary><b>Q: I downloaded a .zip file and there is no .jar inside! What happened?</b></summary>
You clicked the green "Code -> Download ZIP" button on GitHub. That downloads the source code! To get the playable mod, go to the <b><a href="https://github.com/IhebNajjar/SmarterMobs/releases/latest">Releases Page</a></b> and download the file ending in <code>.jar</code> under <b>Assets</b>.
</details>

<details>
<summary><b>Q: Does this mod work on Fabric or Quilt?</b></summary>
Currently, SmarterMobs is built specifically for <b>Minecraft Forge 1.20.1</b>. A Fabric version is planned for a future release.
</details>

<details>
<summary><b>Q: Do my friends need to install this mod to join my server?</b></summary>
<b>No!</b> This mod is completely server-side compatible. As long as the server has the mod in its <code>mods/</code> folder, anyone with a vanilla or modded 1.20.1 client can join seamlessly.
</details>

<details>
<summary><b>Q: Minecraft crashes on startup. What should I check?</b></summary>
1. Verify that your Forge version is for <b>Minecraft 1.20.1</b> (version 47.0.0 or higher).<br>
2. Ensure you do not have conflicting AI overhaul mods that override the same mob goals.<br>
3. Check that your Java version is Java 17 (standard for 1.20.1).
</details>

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
