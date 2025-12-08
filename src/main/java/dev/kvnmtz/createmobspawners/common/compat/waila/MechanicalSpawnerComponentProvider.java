package dev.kvnmtz.createmobspawners.common.compat.waila;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.block.entity.MechanicalSpawnerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum MechanicalSpawnerComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var spawner = (MechanicalSpawnerBlockEntity) accessor.getBlockEntity();
        if (spawner.hasStoredEntity()) {
            var entityTag = spawner.getStoredEntity();
            var optEntityType = EntityType.by(entityTag);
            if (optEntityType.isPresent()) {
                var entityType = optEntityType.get();
                var displayName = entityType.getDescription().getString();

                var title = accessor.getBlock().getName();
                title = Component.translatable("create_mob_spawners.waila.spawner_title", title, displayName);
                tooltip.remove(JadeIds.CORE_OBJECT_NAME);
                tooltip.add(0, IThemeHelper.get().title(title), JadeIds.CORE_OBJECT_NAME);

                var serverData = accessor.getServerData();
                if (serverData.contains("StallingReason")) {
                    tooltip.add(IThemeHelper.get().danger(Component.translatable("create_mob_spawners.waila.spawner_stalling_reason." + serverData.getString("StallingReason"))));
                } else if (serverData.contains("Progress")) {
                    tooltip.add(
                            Component.translatable(
                                    "create_mob_spawners.waila.spawner_progress",
                                    IThemeHelper.get().info(String.format("%d%%", serverData.getInt("Progress")))
                            )
                    );
                } else if (serverData.contains("DelayReason")) {
                    tooltip.add(
                            Component.translatable(
                                    "create_mob_spawners.waila.spawner_progress",
                                    IThemeHelper.get().warning(Component.translatable("create_mob_spawners.waila" +
                                            ".spawner_progress.delaying"))
                            )
                    );
                    tooltip.add(
                            Component.translatable(
                                    "create_mob_spawners.waila.spawner_progress.delay_reason",
                                    IThemeHelper.get().info(Component.translatable(serverData.getString("DelayReason")))
                            )
                    );
                }

                return;
            }
        }

        tooltip.add(IThemeHelper.get().danger(Component.translatable("create_mob_spawners.waila.spawner_stalling_reason.no_soul")));
    }

    @Override
    public ResourceLocation getUid() {
        return CreateMobSpawnersMod.asResource("spawner_progress");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor accessor) {
        var spawner = (MechanicalSpawnerBlockEntity) accessor.getBlockEntity();

        var optStallingReasonKey = spawner.getStallingReasonTranslationKey();
        if (optStallingReasonKey.isPresent()) {
            compoundTag.putString("StallingReason", optStallingReasonKey.get());
        } else if (spawner.isDelayed()) {
            compoundTag.putString("DelayReason", spawner.getDelayReasonTranslationKey());
        } else {
            compoundTag.putInt("Progress", spawner.getSpawnProgressPercentage());
        }
    }
}
