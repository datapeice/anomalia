# Anomalia: Chunk Madness (Fabric 1.20.1)

Anomalia turns the Minecraft world into a dynamic landscape of unpredictable chunk anomaly zones.
Each chunk can contain unique anomalies that affect physics, block mechanics, entity behaviors, and survival conditions. Anomalies rotate dynamically every in-game day based on world seeds and chunk coordinates.

---

## The Bolt Item

The Bolt is a STALKER-inspired anomaly detector tool.

- Crafting Recipe: 1 Iron Nugget yields 4 Bolts (shapeless crafting).
- Usage: Right-click to throw a bolt forward.
- Anomaly Detection:
  - In gravity and lift zones: The bolt accelerates upward or floats.
  - In vortex and tornado zones: The bolt swirls in a spiral toward the chunk center.
  - In super trampoline zones: The bolt bounces high into the air.
  - In horizontal wind zones: The bolt gets pushed sideways.
  - In ice and fire zones: The bolt emits snowflake trails or flame sparks.
  - In electric and explosive zones: The bolt emits electric sparks.
  - On landing: The bolt scans the chunk and displays detected anomalies in chat. It can be picked back up.

---

## Configuration (config/anomalia.json)

The mod includes server-side configuration options:

```json
{
  "enabled": true,
  "chunkAnomalyChance": 0.40,
  "bossSpawnChance": 0.002,
  "minAnomaliesPerChunk": 1,
  "maxAnomaliesPerChunk": 3,
  "dailyRotation": true,
  "dailyRotationTicks": 24000,
  "allowBossDragonInOverworld": true,
  "allowBossWitherInOverworld": true,
  "allowBossWardenInOverworld": true
}
```

- enabled: Master toggle for all anomaly processing.
- chunkAnomalyChance: Probability that a chunk contains anomalies (default 0.40 for 40% anomaly chunks, 60% clean chunks).
- minAnomaliesPerChunk / maxAnomaliesPerChunk: Number of anomalies assigned per active chunk.
- dailyRotation: When true, anomaly distributions shuffle every in-game day.
- dailyRotationTicks: Duration of a day cycle (default 24000 ticks).

---

## Commands

- /anomalia check: Inspects the current chunk and prints active anomalies.
- /anomalia debug: Toggles real-time actionbar anomaly status overlay.

---

## Anomaly Catalog

### 1. Physics and Movement
- Skyward Lift: Applies levitation lift toward the sky.
- Heavy Gravity: Pulls airborne entities downward rapidly and suppresses jumping.
- Zero Gravity: Grants slow falling and low-gravity jumping.
- Creative Flight: Allows flight in survival mode while inside the chunk.
- Super Trampoline: Launches jumping players high into the air with slime bounce sounds.
- Zero Friction: Turns ground blocks beneath feet into frosted ice with high sliding speed.
- Ender Glitch: Every three jumps teleports the player to a random spot within the chunk.
- Vortex Tornado: Pulls players, mobs, and items into a swirling vortex at the chunk center.
- Speed Ramp: Accelerates running speed the longer the player sprints.
- Boomerang Projectiles: Reflected arrows turn 180 degrees back toward the shooter.
- Super Speed: Grants swift sprinting speed.
- Turtle Pace: Drastically reduces movement speed.
- High Jump: Enhances vertical jump height.
- Magnetic Pocket: Pulls dropped items within 16 blocks directly toward the player.
- Repulsion Aura: Pushes nearby mobs and projectiles away.
- Horizontal Wind: Continuous eastward wind pushes entities across the chunk.
- Blink Step: Jumping teleports the player 5 blocks in the look direction.
- Earthquake: Periodic seismic shocks launch entities upward.

### 2. Block and World Interaction
- Block Roulette: Placing a block replaces it with a random block.
- Living Escapists: Placing a block spawns a silverfish that runs away.
- Rocket Blocks: Placed blocks launch into the sky and detonate as fireworks.
- Proliferation: Breaking a block causes cobblestone blocks to emerge around it.
- Tele Placement: Blocks are placed overhead instead of the targeted surface.
- Sound Jumpscare: Mining triggers loud mob screams or creeper hisses.
- Chicken Fountain: Mining a block spawns a burst of chickens.
- Water Geyser: Mining a block erupts a water spring.
- Midas Glass: Walking turns the ground into glass blocks.
- Insta Mine: Allows instant breaking of blocks.
- Gravity Blocks: Placed blocks fall subject to gravity.
- Block Rejection: Non-crop blocks cannot be placed and burn upon placement.
- Anvil Rain: Breaking a block drops an anvil from above.
- Ore Transmutation: Breaking stone has a chance to transmute it into valuable ores.
- Instant Growth: Instantly grows neighboring crops and saplings to full maturity.
- Ice Age: Placed blocks become packed ice and nearby water freezes.
- Lava Sponge: Placed blocks turn into lava sources.
- Block Duplication: Triples item drops from mined blocks.
- Exploding Ores: Mining ores triggers an explosion.
- Bedrock Trap: Placed blocks turn into unbreakable bedrock.
- Sand Collapse: Breaking sand collapses neighboring blocks into falling sand.

### 3. Mob Chaos
- Boss Apocalypse: Small chance to spawn Withers, Wardens, or Ender Dragons upon mob spawning.
- Body Swap: Attacking a mob swaps positions between player and target.
- Killer Animals: Passive animals become hostile and attack players.
- Heavy Artillery: Skeleton arrows detonate on impact.
- Cell Division: Mobs duplicate into two copies upon taking damage.
- Disco Chaos: Mobs dance and jump while sheep shift through rainbow colors.
- Micro Fast: Mini mobs with increased speed and jump capabilities.
- Combustion Mobs: Mobs explode upon death.
- Lightning Striker: Striking a mob calls down a lightning bolt.
- Angry Bees Swarm: Aggressive bee swarms spawn around players.
- Creeper Chain: Exploding creepers spawn additional charged creepers.
- Flying Pigs: Pigs float upward into the atmosphere.
- Inviso Mobs: Mobs receive permanent invisibility.
- Slime Apocalypse: Continuous spawning of slimes throughout the chunk.
- Phantom Swarm: Phantoms spawn during the day.
- Armored Mobs: Mobs spawn equipped in full armor.

### 4. Survival and Combat
- Damage Inversion: Damage heals entities and reflects reciprocal damage to attackers.
- Stomach Flip: Inverts food effects; rotten flesh and poisonous items restore full hunger, while regular food drains hunger.
- Keep Moving: Standing still for more than 1.5 seconds ignites the player.
- Item Eruption: Picking up an item erupts copies in a fountain. Temporary volcanic copies dissolve upon exiting the chunk.
- Hot Hands: Holding an item for too long burns the player.
- Cursed Enchants: Held items periodically receive high-tier enchantments.
- Glass Cannon: Sets player health to half a heart but grants extreme attack strength.
- Potion Roulette: Applies random positive or negative status effects every few seconds.
- Vampirism: Attacks heal the player for a percentage of damage dealt.
- Thorns Aura: Returns heavy damage back to attackers.
- Random Drop on Hit: Taking damage causes the player to drop their held item.
- Fire Feet: Walking ignites the ground behind the player.
- Golden Touch: Defeated mobs drop gold ingots.
- Radio Noise: Emits eerie ambient cave sounds.

---

## Installation and Requirements

- Fabric Loader for Minecraft 1.20.1
- Fabric API (1.20.1)
- Java 17 or higher
