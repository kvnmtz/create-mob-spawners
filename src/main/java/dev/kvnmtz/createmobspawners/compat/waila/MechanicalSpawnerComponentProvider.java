package dev.kvnmtz.createmobspawners.compat.waila;

import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.blocks.entity.MechanicalSpawnerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum MechanicalSpawnerComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var spawner = (MechanicalSpawnerBlockEntity) accessor.getBlockEntity();
        var entityData = spawner.getStoredEntityData();
        if (!entityData.isEmpty()) {
            var optEntityDisplayName = entityData.getEntityDisplayName();
            if (optEntityDisplayName.isPresent()) {
                var title = accessor.getBlock().getName();
                title = Component.translatable("create_mob_spawners.waila.spawner_title", title, optEntityDisplayName.get());
                tooltip.remove(Identifiers.CORE_OBJECT_NAME);
                tooltip.add(0, IThemeHelper.get().title(title), Identifiers.CORE_OBJECT_NAME);

                var serverData = accessor.getServerData();
                if (serverData.contains("NoProgressReason")) {
                    tooltip.add(Component.translatable("create_mob_spawners.waila.spawner_no_progress_reason." + serverData.getString("NoProgressReason")).withStyle(ChatFormatting.RED));
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
                                    IThemeHelper.get().warning(Component.translatable("create_mob_spawners.waila.spawner_progress.delaying"))
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

        tooltip.add(Component.translatable("create_mob_spawners.waila.spawner_no_progress_reason.no_soul").withStyle(ChatFormatting.RED));
    }

    @Override
    public ResourceLocation getUid() {
        return CreateMobSpawners.asResource("spawner_progress");
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        var spawner = (MechanicalSpawnerBlockEntity) accessor.getBlockEntity();
        var optReasonForNotProgressingKey = spawner.getReasonForNotProgressingTranslationKey();
        if (optReasonForNotProgressingKey.isPresent()) {
            data.putString("NoProgressReason", optReasonForNotProgressingKey.get());
        } else if (spawner.isDelayed()) {
            data.putString("DelayReason", spawner.getDelayReasonTranslationKey());
        } else {
            data.putInt("Progress", spawner.getSpawnProgressPercentage());
        }
    }
}
