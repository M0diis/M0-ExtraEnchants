
<!-- Variables -->

[resourceId]: 88737

[ratingImage]: https://img.shields.io/badge/dynamic/json.svg?color=brightgreen&label=rating&query=%24.rating.average&suffix=%20%2F%205&url=https%3A%2F%2Fapi.spiget.org%2Fv2%2Fresources%2F88737
[buildImage]: https://github.com/M0diis/M0-ExtraEnchants/actions/workflows/gradle-publish.yml/badge.svg
[downloadsImage]: https://img.shields.io/badge/dynamic/json.svg?color=brightgreen&label=downloads%20%28spigotmc.org%29&query=%24.downloads&url=https%3A%2F%2Fapi.spiget.org%2Fv2%2Fresources%2F88737
[updatedImage]: https://badges.pufler.dev/updated/M0diis/M0-ExtraEnchants

<!-- End of variables block -->

![build][buildImage]
![downloads][downloadsImage] ![rating][ratingImage]

## M0-ExtraEnchants
Extra enchants to provide more game variety.

### Development
Building is quite simple.

To build M0-ExtraEnchants, you need JDK 21 or higher and Gradle installed on your system.

Clone the repository or download the source code from releases.  
Run `gradlew shadowjar` to build the jar.  
The jar will be found created in `/build/libs/` folder. 

**Building**
```
git clone https://github.com/M0diis/M0-ExtraEnchants.git
cd M0-ExtraEnchants
gradlew shadowjar
```

### Config-driven custom enchants

You can now create custom enchants without writing Java code.

Create files in `plugins/M0-ExtraEnchants/custom-enchants/*.yml` (example files are generated on first startup):

- `venom.yml`, `leeching.yml`, `freeze.yml`
- `soulrend.yml`, `thunderclap.yml`, `executioner.yml`, `hex.yml`, `bloodrush.yml`
- `momentum.yml`, `windstep.yml`, `foothold.yml`, `adrenaline.yml`, `warding.yml`
- `retaliate.yml`, `bulwark.yml`, `overcharge.yml`, `quarry.yml`, `prospect.yml`
- `overgrowth.yml`, `anglerluck.yml`, `battletrance.yml`, `skirmisher.yml`, `duskcloak.yml`

Supported out of the box:

- metadata (`id`, `display-name`, `description`, `rarity`, `max-level`, `enabled`, `show-in-list`, `category`, `icon`)
- trigger pipeline (`chance`, `cooldown`, `global-cooldown`, `delay`, `repeat`, `repeat-interval`, `conditions`, `chain-triggers`, `effects`)
- trigger bridge (`onAttack`, `onDamaged`, `onKill`, `onDeath`, `onBlockBreak`, `onMine`, `onInteract`, `onRightClick`, `onLeftClick`, `onConsume`, `onShoot`, `onProjectileHit`, `onFish`, `onChat`, `onMove`, `onJump`, `onFall`, `onSneak`, `onSprint`, `onEquip`, `onUnequip`)
- targets (`SELF`, `ATTACKER`, `VICTIM`, `PLAYERS`, `MONSTERS`, `ANIMALS`, `ALL_ENTITIES`, `NEARBY_ENTITIES`, `ENEMIES`)
- effects (`damage`, `true-damage`, `heal`, `lifesteal`, `potion`, `particle`, `sound`, `command`, `message`, `withdraw`, `deposit`, `stack_add`, `stack_clear`, `combo_reset`)
- advanced conditions (`region_allowed`, `region_name`, `balance_at_least`, `stack_at_least`, `combo_at_least`)
- formulas and placeholders (`level`, `random`, `attacker_health`, `victim_health`, `distance`, `balance`, `stack`, `combo`, `region_allowed`, `%player%`, `%attacker%`, `%victim%`, `%level%`)

Example config file:

```yaml
id: venom
display-name: "&2Venom"
description: "&7Poisons enemies on hit."
rarity: RARE
max-level: 3
enabled: true
weight: 8
category: OFFENSE
icon: SPIDER_EYE
applicable-items:
  - SWORD
  - AXE
conflicts-with:
  - withering

triggers:
  onAttack:
    chance: "20 + level * 10"
    cooldown: "5"
    global-cooldown: "0"
    target: VICTIM
    radius: "8"
    repeat: 1
    repeat-interval: 2
    delay: 0
    cancel-event: false
    conditions:
      all:
        - victim_not_poisoned
        - attacker_health > 5
        - region_allowed
    effects:
      - potion:
          type: POISON
          duration: "60 + level * 20"
          amplifier: "level - 1"
      - damage: "1 + level"
      - stack_add:
          amount: "1"
          duration: "200"
      - particle:
          type: SPELL_MOB
          count: 12
      - sound: ENTITY_SPIDER_HURT
      - command: "say %victim% was poisoned by %attacker%"
  onKill:
    chance: "100"
    target: SELF
    conditions:
      all:
        - combo_at_least: 2
    effects:
      - deposit: "5 + level * 2"
      - message: "&aVenom combo bonus! +$%level% stack=%stack% combo=%combo%"
      - combo_reset: true
```

You can apply config enchants with:

`/ee apply <enchantId> <level>`

And reload everything with:

`/ee reload`

### Dev-builds

All the development builds can be found on actions page.
Open the workflow and get the artifact from there.

https://github.com/M0diis/M0-ExtraEnchants/actions

#### Links

- [Spigot Page](https://www.spigotmc.org/resources/88737/)
- [Issues](https://github.com/M0diis/M0-ExtraEnchants/issues)
  - [Bug report](https://github.com/M0diis/M0-ExtraEnchants/issues)
  - [Feature request](https://github.com/M0diis/M0-ExtraEnchants/issues)
- [Pull requests](https://github.com/M0diis/M0-ExtraEnchants/pulls)

##### APIs
- [bStats](https://github.com/Bastian/bStats)

