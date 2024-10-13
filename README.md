# BlockSpawners

A simple Paper Plugin that allows players to create custom blocks that spawn items once placed, acting like an item spawmer in the block

---

## Commands

* **/blockspawners give (spawnerName) (PlayerName)** :

  * Gives the player the corresponding item from the spawner configuration.

* **/blockspawners reload**

  * Reloads the spawners' configuration.

**Both of these commands require the `blockspawners.admin` permission**

> [!IMPORTANT]
> You need a permissions plugin to do this.

---

## Configuration

The plugin reads the data from the yml files located at `plugins/BlockSpawners/spawners/<spawnerName>.yml`

You can create your own spawner following the example placeholder below:

First create a yml file inside the spawners folder ending with `.yml` and name it as you want.
Then, inside the yml file, you can add the following:

```
# This should mirror the name of the yml file
spawnerName:                    
  
  # The Material of the block, it should be a placable block
  # Please avoid using blocks that have gravity or errant behaviours,
  # such as cactus, grass, etc.
  Material: STONE
  
  # The display name of the item, it supports MiniMessage formatting
  DisplayName: "Stone Spawner"
                                
  # You can add as many lore lines as you want
  # Lore lines also support MiniMessage formatting              
  Lore:
    - " "
    - "This is a lore line"
    - " "
 
  # The enchantments that will be added to the item
  # Please write the correct name of the enchantment
  # and use "_" instead of spaces
  Enchantments:
    unbreaking: 10
    fire_aspect: 5
    
  
  # Set the custom flags for the item  
  Unbreakable: true         # Makes the item unbreakable or not
  Flags:
    HideEnchantments: true  # Hides the enchantments from the item 
    HideUnbreakable: true   # Hides the unbreakable flag from the item
  
  # The cooldown in ticks between each time the spawner generates an item
  # Every 20 ticks equals to 1 second so (200 ticks = 10 seconds)
  SpawnTicks: 200
  
  # The materials that will be spawned when the spawner is placed
  # In this field you can use items and not just blocks
  SpawningMaterials:
    - STONE
    - COBBLESTONE
    - GRANITE
    - DIORITE
    - ANDESITE
    - DIAMOND
    - EMERALD
```
---
