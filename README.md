<p align="center">
  <img src="docs/title_image.webp" alt="Title"/>
  <br><br>
  <img src="https://cf.way2muchnoise.eu/versions/1175578(c70039).svg" alt="CurseForge"/>
  <br><br>
  <a href="https://www.curseforge.com/minecraft/mc-mods/create-mob-spawners"><img src="https://cf.way2muchnoise.eu/1175578.svg" alt="CurseForge"/></a>
  &nbsp
  <a href="https://modrinth.com/mod/create-mob-spawners"><img src="https://img.shields.io/modrinth/dt/create-mob-spawners?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"/></a>
</p>

# ⚙️ Create: Mob Spawners
An addon for the [Create](https://github.com/Creators-of-Create/Create) mod for Minecraft. It adds a tool that can catch mobs which can then be placed into a spawner. It aims to be "immersive" by making use of the base game magic components.

## 🆕 What does it add?
### Soul Catcher

![Soul Catcher](docs/soul_catcher.webp)

The Soul Catcher is a new item that can capture a mob's soul.
For this to work, the mob needs to have an active weakness effect which could be applied using a throwable potion of weakness.
Mobs that should not be capturable can be adjusted in the config. Bosses are not capturable by design.

The time it takes to catch a mob depends on its hitbox volume, so something small like a chicken will be caught pretty quickly while something big like a ghast can take some time.

While a mob is being captured, its AI will be disabled, so it will not be able to move or attack.

It can be crafted using this recipe:

![Soul Catcher Recipe](docs/recipe_soul_catcher.webp)

This is how it looks ingame:

![Soul Catcher Screencast](docs/soul_catcher_anim.webp)

Ponder scene:

![Soul Catcher Ponder](docs/soul_catcher_ponder.webp)

### Mechanical Spawner

![Mechanical Spawner](docs/mechanical_spawner.webp)

The Mechanical Spawner is a new kinetic block that can spawn mobs according to the Soul Catcher placed inside.
For this to work, it needs rotational force and a supply of liquid. Refer to recipes in JEI for more information about this.

The spawner will repeatedly spawn the contained mob type in the same range a vanilla spawner would. When 6 or more mobs of this type are already inside of this range, the spawner will stall.

It needs a minimum rotation speed of 128 RPM and its stress impact depends on the contained mob types max health, so something like an enderman will require a lot more SU than a chicken.

**(NEW!)** The spawning area can be configured by right-clicking the block with either an empty hand or a wrench.

The Mechanical Spawner can be crafted in a 5x5 Mechanical Crafter grid using this recipe:

![Mechanical Spawner Recipe](docs/recipe_mechanical_spawner.webp)

This is how it looks ingame:

![Mechanical Spawner Screencast](docs/mechanical_spawner_anim.webp)

Ponder scene:

![Mechanical Spawner Ponder](docs/mechanical_spawner_ponder.webp)

The exact stress impact is calculated using this graph (the cap at 80 SU / RPM is configurable):

<img alt="Stress Impact Graph" src="docs/stress_impact_graph.webp" width="400">

Some examples:

| Mob      | SU @ 128 RPM | SU @ 256 RPM |
|----------|--------------|--------------|
| Chicken  | 1,024        | 2,048        |
| Ghast    | 1,769        | 3,538        |
| Zombie   | 2,560        | 5,120        |
| Enderman | 5,120        | 10,240       |
| Ravager  | 10,240       | 20,480       |

Note that the Ravagers stress impact is capped in this example, as the default configuration will cap everything at 80 SU / RPM.

## 🤝 Integrations / Compatibilities

### JEI

Displays:
- Fluid and required amount
- Spawning duration at minimum and maximum RPM
- Additional spawn attempts
  - 1 spawned entity is guaranteed, additional attempts rely on RNG to find a fitting spot for the mob, so a spawner without any obstruction in its range will work most efficiently

<img alt="JEI" src="docs/jei.webp" width="687">

### Jade

Displays:
- Contained mob type
- Contained fluid
- Progression status
- Reason for stalling (if applicable)

<img alt="Jade" src="docs/jade.webp" width="600">

### Recipes

Required liquids and their effects are defined in standard minecraft recipes. They can easily be modified using something like *KubeJS*.

This is the recipe for spawning with standard Potion of Regeneration liquid:

```json
{
  "type": "create_mob_spawners:spawning",
  "input": {
    "amount": 200,
    "fluid": "create:potion",
    "nbt": {
      "Potion": "minecraft:regeneration",
      "Bottle": "REGULAR"
    }
  },
  "spawn_ticks_at_max_speed": 100,
  "additional_spawn_attempts": 0
}
```

- `input.nbt.Bottle` refers to the potion bottle type and can be either `REGULAR`, `SPLASH` or `LINGERING`
- everything else should be self-explanatory

This is an example of how a more advanced recipe could look like, just to show which fields can be used:

```json
{
  "type": "create_mob_spawners:spawning",
  "input": {
    "amount": 100,
    "fluid": "minecraft:lava"
  },
  "particle_color": "#fc7303",
  "spawn_ticks_at_max_speed": 100,
  "additional_spawn_attempts": 1,
  "spawnable_entity_whitelist": [
    "minecraft:magma_cube"
  ]
}
```

This recipe would use 100mb of lava to be able to spawn magma cubes (and only magma cubes) using the `spawnable_entity_whitelist` array.
It is also possible to use a blacklist instead in order to exclude selected mobs using `spawnable_entity_blacklist`.
The particle color also has been customized to an orange lava-like color using the `particle_color` field.
If a potion is used as fluid, the particle color will always reflect the effect color of this potion, if no custom color is specified.

The result would look like this:

![Custom Recipe Example](docs/custom_recipe_example.webp)

It is also possible to use Soul Catchers containing specific entities in recipes. An example could look like this:

```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    {
      "item": "minecraft:potato"
    },
    {
      "type": "create_mob_spawners:soul_catcher",
      "entity_type": "minecraft:pufferfish"
    }
  ],
  "result": {
    "item": "minecraft:poisonous_potato"
  }
}
```

Here, a potato and a Soul Catcher containing a pufferfish could be combined into a poisonous potato. After crafting, the Soul Catcher will turn into an Empty Soul Catcher, so only the entity contained inside will be "consumed".