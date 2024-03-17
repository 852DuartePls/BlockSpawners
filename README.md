```
  .--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--.
 / .. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \
 \ \/\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ \/ /
  \/ /`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'\/ /
  / /\                                                                                                                    / /\
 / /\ \  ██████╗ ██╗      ██████╗  ██████╗██╗  ██╗███████╗██████╗  █████╗ ██╗    ██╗███╗   ██╗███████╗██████╗ ███████╗   / /\ \
 \ \/ /  ██╔══██╗██║     ██╔═══██╗██╔════╝██║ ██╔╝██╔════╝██╔══██╗██╔══██╗██║    ██║████╗  ██║██╔════╝██╔══██╗██╔════╝   \ \/ /
  \/ /   ██████╔╝██║     ██║   ██║██║     █████╔╝ ███████╗██████╔╝███████║██║ █╗ ██║██╔██╗ ██║█████╗  ██████╔╝███████╗    \/ /
  / /\   ██╔══██╗██║     ██║   ██║██║     ██╔═██╗ ╚════██║██╔═══╝ ██╔══██║██║███╗██║██║╚██╗██║██╔══╝  ██╔══██╗╚════██║    / /\
 / /\ \  ██████╔╝███████╗╚██████╔╝╚██████╗██║  ██╗███████║██║     ██║  ██║╚███╔███╔╝██║ ╚████║███████╗██║  ██║███████║   / /\ \
 \ \/ /  ╚═════╝ ╚══════╝ ╚═════╝  ╚═════╝╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝  ╚═╝ ╚══╝╚══╝ ╚═╝  ╚═══╝╚══════╝╚═╝  ╚═╝╚══════╝   \ \/ /
  \/ /                                                                                              by: DaveDuart         \/ /
  / /\.--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--..--./ /\
 / /\ \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \.. \/\ \
 \ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `'\ `' /
  `--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'`--'
```
#    How to use this?
```
     Well, it's very simple. The plugin comes with a bunch of premade BlockSpawners stored in the blocks folder
     that can be modified as desired and/or you can create your own custom ones very easily.

    Format:#    You need to create a .yml file and store it inside the blocks folder inside /plugins/BlockSpawners/blocks.
    That file should be in all lowercase, but you can name it whatever you want. For example:
      dsadajdkwadjaw.yml. Yes, it's a silly name, but in the end, this part only matters when using the
    "/blockspawners give <spawnername>" command, looking something like this ...
    "/blockspawners give dsadajdkwadjaw". Okay, now onto the contents of the files.
    Inside the .yml files, it should look like this:
    ╭──────────────────╮
    │DisplayName: ""   │
    │Material:         │
    │SpawnTicks:       │
    │lore:             │
    │ - ""             │
    │SpawningMaterials:│
    │ -                │
    │ -                │
    ╰──────────────────╯
    And your custom BlockSpawner is complete. But let's break down each item first:
    DisplayName: "" <<< This needs to be between quotation marks, and it accepts color format from vanilla Minecraft.
    For example, you can name it &c&k!!!&4&lThe Most Amazing Spawner&c&k!!!

   Material: <<< This is the material that will represent your custom spawner, it
   needs to be in all caps and must correspond to the list of materials cited here
  (https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html). You really just need to look at that page
  once and then it's already common sense.
  SpawnTicks: << Very straightforward, it's just the number of ticks that have to pass to trigger the spawner.
  It's 200 by default, and that's 10 IRL seconds. If you want to make something custom, just remember to multiply
  your seconds by 20. For example, if you want 10 minutes between each item being dropped, it's 12000 ticks.
  lore: <<< You can do it like this lore: hello or use multiline.
    - like
    - this
    - btw, remember to do 2 spaces for each line for it to work properly.
  But if you want to use color codes, remember to put your text between quotation marks "&dhi" because
  otherwise, the YAML file will think it's another thing, and it will not display correctly.
  And now the
  SpawningMaterials: <<< Again, work with the materials cited here (https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html).
    - BLOCK
    - BLOCK2
    - BLOCK3
  You can just put one block if you want, but remember to use the line and leave 2 spaces.
  And that's pretty much it.
  Thank you for downloading my plugin! :D
```
