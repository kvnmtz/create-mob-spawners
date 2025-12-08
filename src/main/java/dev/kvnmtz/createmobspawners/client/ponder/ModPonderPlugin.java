package dev.kvnmtz.createmobspawners.client.ponder;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.client.ponder.scene.SoulCatcherScenes;
import dev.kvnmtz.createmobspawners.client.ponder.scene.SpawnerScenes;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return CreateMobSpawnersMod.MOD_ID;
    }

    private static final ResourceLocation TAG_MOB_SPAWNING
            = CreateMobSpawnersMod.asResource("mob_spawning");

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(TAG_MOB_SPAWNING)
                .addToIndex()
                .item(ModItems.EMPTY_SOUL_CATCHER, true, false)
                .title("Mob Spawning")
                .description("Components for catching and spawning mobs")
                .register();

        helper.addToTag(TAG_MOB_SPAWNING)
                .add(ModItems.EMPTY_SOUL_CATCHER.getId())
                .add(ModBlocks.MECHANICAL_SPAWNER.getId());
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(ModBlocks.MECHANICAL_SPAWNER.getId())
                .addStoryBoard(CreateMobSpawnersMod.asResource("spawner"), SpawnerScenes::spawner);
        helper.forComponents(ModItems.SOUL_CATCHER.getId(), ModItems.EMPTY_SOUL_CATCHER.getId())
                .addStoryBoard(CreateMobSpawnersMod.asResource("soul_catcher"), SoulCatcherScenes::soulCatcher);
    }
}
