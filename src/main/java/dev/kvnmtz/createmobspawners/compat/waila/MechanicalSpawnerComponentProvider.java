package dev.kvnmtz.createmobspawners.compat.waila;

import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.blocks.entity.MechanicalSpawnerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.view.*;

import java.util.List;

public enum MechanicalSpawnerComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>, IServerExtensionProvider<MechanicalSpawnerBlockEntity, CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView> {
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
                if (serverData.contains("StallingReason")) {
                    tooltip.add(Component.translatable("create_mob_spawners.waila.spawner_stalling_reason." + serverData.getString("StallingReason")).withStyle(ChatFormatting.RED));
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

        tooltip.add(Component.translatable("create_mob_spawners.waila.spawner_stalling_reason.no_soul").withStyle(ChatFormatting.RED));
    }

    @Override
    public ResourceLocation getUid() {
        return CreateMobSpawners.asResource("spawner_progress");
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        var spawner = (MechanicalSpawnerBlockEntity) accessor.getBlockEntity();
        
        var optStallingReasonKey = spawner.getStallingReasonTranslationKey();
        if (optStallingReasonKey.isPresent()) {
            data.putString("StallingReason", optStallingReasonKey.get());
        } else if (spawner.isDelayed()) {
            data.putString("DelayReason", spawner.getDelayReasonTranslationKey());
        } else {
            data.putInt("Progress", spawner.getSpawnProgressPercentage());
        }
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
        return ClientViewGroup.map(groups, FluidView::readDefault, null);
    }

    @Override
    public @NotNull List<ViewGroup<CompoundTag>> getGroups(ServerPlayer player, ServerLevel world, MechanicalSpawnerBlockEntity blockEntity, boolean showDetails) {
        var fluidStack = blockEntity.getFluidStack();
        var tank = new ViewGroup<>(List.of(FluidView.writeDefault(JadeFluidObject.of(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getTag()), blockEntity.getTank().getPrimaryHandler().getCapacity())));
        return List.of(tank);
    }
}
