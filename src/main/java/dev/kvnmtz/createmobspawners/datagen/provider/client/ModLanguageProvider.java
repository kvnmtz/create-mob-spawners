package dev.kvnmtz.createmobspawners.datagen.provider.client;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, CreateMobSpawnersMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Items
        add(ModItems.EMPTY_SOUL_CATCHER.get(), "Empty Soul Catcher");
        add(ModItems.EMPTY_SOUL_CATCHER.get().getDescriptionId() + ".tooltip.summary", "Utilizes the power of condensed souls to capture mobs. Right-clicking a mob that has a _weakness_ potion effect will _capture_ its soul and store it inside. It can later be _released_ by right-clicking the desired position.");

        add("item.create_mob_spawners.empty_soul_catcher.capturable_status.too_far", "Entity is too far away");
        add("item.create_mob_spawners.empty_soul_catcher.capturable_status.too_much_health", "Entity has too much health left, it needs to be weakened more");
        add("item.create_mob_spawners.empty_soul_catcher.capturable_status.dead", "Dead entities are not catchable");
        add("item.create_mob_spawners.empty_soul_catcher.capturable_status.no_weakness", "Entity is not weakened. Try applying a potion of weakness on it.");
        add("item.create_mob_spawners.empty_soul_catcher.capturable_status.player", "Players cannot be captured");
        add("item.create_mob_spawners.empty_soul_catcher.capturable_status.boss", "Bosses cannot be captured");
        add("item.create_mob_spawners.empty_soul_catcher.capturable_status.blacklisted", "This entity is not catchable");
        add("item.create_mob_spawners.empty_soul_catcher.capturable_status.incompatible", "This entity is not catchable");

        add(ModItems.SOUL_CATCHER.get(), "Soul Catcher");
        add(ModItems.SOUL_CATCHER.get().getDescriptionId() + ".tooltip.summary", "Contains a mob's soul. It can either be _released_ by right-clicking the desired position or placed inside a _Mechanical Spawner_.");
        
        add("item.create_mob_spawners.soul_catcher.no_space", "There is not enough room to release this entity here");

        // Blocks
        add(ModBlocks.MECHANICAL_SPAWNER.get(), "Mechanical Spawner");
        add(ModBlocks.MECHANICAL_SPAWNER.get().getDescriptionId() + ".tooltip.summary", "Utilizes the power of regenerative magic to spawn mobs. It needs _rotational force_, a supply of _Potion of Regeneration_ liquid and a _Soul Catcher_ placed inside to function.");
        add(ModBlocks.MECHANICAL_SPAWNER.get().getDescriptionId() + ".tooltip.stress", "Depends on mob type");

        // Ponder Index
        add("create_mob_spawners.ponder.tag.mob_spawning", "Mob Spawning");
        add("create_mob_spawners.ponder.tag.mob_spawning.description", "Components for catching and spawning mobs");

        // Ponder Scene - Soul Catcher
        add("create_mob_spawners.ponder.soul_catcher.header", "Soul Catcher");
        add("create_mob_spawners.ponder.soul_catcher.text_1", "See this innocent pig? Let's catch it.");
        add("create_mob_spawners.ponder.soul_catcher.text_2", "In order to catch a mob's soul, it needs to be weakened (e.g. with a Splash Potion of Weakness)");
        add("create_mob_spawners.ponder.soul_catcher.text_3", "Now, right-click it with an empty Soul Catcher to start catching its soul");
        add("create_mob_spawners.ponder.soul_catcher.text_4", "Gotcha! The pig was caught.");
        add("create_mob_spawners.ponder.soul_catcher.text_5", "If you want to release it again, just right-click the desired position with the Soul Catcher");

        // Ponder Scene - Mechanical Spawner
        add("create_mob_spawners.ponder.spawner.header", "Mechanical Spawner");
        add("create_mob_spawners.ponder.spawner.text_1", "Right-click the Spawner with a Soul Catcher to place it inside");
        add("create_mob_spawners.ponder.spawner.text_2", "The Spawner needs a supply of Potion of Regeneration to work");
        add("create_mob_spawners.ponder.spawner.text_3", "The Spawner also needs rotational force. The faster the rotation, the faster the spawning progress will finish.");
        add("create_mob_spawners.ponder.spawner.text_4", "Given some time, the Spawner will spawn the applied mob type on a nearby position");
        add("create_mob_spawners.ponder.spawner.text_5", "The Soul Catcher can be ejected by right-clicking the Spawner while sneaking");

        // GUI
        add("create_mob_spawners.gui.spawner.spawning_area", "Spawning Area");
        add("create_mob_spawners.gui.spawner.width", "Width");
        add("create_mob_spawners.gui.spawner.height", "Height");
        add("create_mob_spawners.gui.spawner.height_offset", "Height Offset from Center");

        // Creative tab
        add("itemGroup.create_mob_spawners.main", "Create: Mob Spawners");

        // Jade
        add("config.jade.plugin_create_mob_spawners.spawner_progress", "Mechanical Spawner Progress");
        add("create_mob_spawners.waila.spawner_progress", "Progress: %s");
        add("create_mob_spawners.waila.spawner_progress.delaying", "Delaying...");
        add("create_mob_spawners.waila.spawner_progress.delay_reason", "Reason: %s");
        add("create_mob_spawners.waila.spawner_delay_reason.unknown", "Unknown");
        add("create_mob_spawners.waila.spawner_delay_reason.invalid_entity", "Invalid entity contained");
        add("create_mob_spawners.waila.spawner_delay_reason.searching_position", "Searching for valid position...");
        add("create_mob_spawners.waila.spawner_delay_reason.entity_creation_error", "Could not create entity");
        add("create_mob_spawners.waila.spawner_delay_reason.too_many_entities", "Too many entities nearby");
        add("create_mob_spawners.waila.spawner_stalling_reason.no_soul", "No soul stored inside");
        add("create_mob_spawners.waila.spawner_stalling_reason.not_enough_fluid", "Not enough fluid");
        add("create_mob_spawners.waila.spawner_stalling_reason.no_rotational_force", "No rotational force");
        add("create_mob_spawners.waila.spawner_stalling_reason.rotation_speed_too_low", "Rotation speed too low");
        add("create_mob_spawners.waila.spawner_stalling_reason.recipe_cant_spawn_entity", "Entity cannot be spawned by contained fluid");
        add("create_mob_spawners.waila.spawner_title", "%s (%s)");

        // JEI
        add("create_mob_spawners.jei.spawning.title", "Mob Spawning");
        add("create_mob_spawners.jei.spawning.duration", "Spawning duration:");
        add("create_mob_spawners.jei.spawning.at", "at");
        add("create_mob_spawners.jei.spawning.rpm", "RPM");
        add("create_mob_spawners.jei.spawning.additional_spawn_attempts", "Additional spawn attempts: %d");
        add("create_mob_spawners.jei.spawning.question_mark", "Spawns mob type contained in the Soul Catcher");
        add("create_mob_spawners.jei.spawning.blacklist", "Does §lnot §rwork with:");
        add("create_mob_spawners.jei.spawning.whitelist", "Does §lonly §rwork with:");
    }
}