# Undead Fire Weakness
Increase the flame damage suffered by undead. Configurable.

The default configuration file (`config/undead_fire_weakness-common.toml`) is as follows:

```toml
#Whether to enable the mod
mod_enabled = true
#Entity types that apply damage modifier must be included in this list.
#Support entity type tags starting with "#" or write entity type directly
#Use "$all" to match all entity types
#Use "$undead" to match all undead
entity_type_list = ["$undead"]
#Damage types that be modified must be included in this list
#Support damage type tags starting with "#" or write damage directly
#Use "$all" to match all damage types
damage_type_list = ["#minecraft:is_fire"]
#Should the damage to modify must be caused by player.
must_by_player = false
#The value to be multiply to the damage.
#Works BEFORE addition.
#Range: 0.0 ~ 2.147483647E9
value_multiplier = 2.0
#The value to be added to the damage.
#Works AFTER multiplication.
#Range: 0.0 ~ 2.147483647E9
value_addition = 0.0
```

----------

I couldn't find any existing mods with the features I needed, and KubeJS wasn't quite up to the task either, so I spent a few hours making one myself.
