package dev.kvnmtz.createmobspawners;

import com.mojang.logging.LogUtils;
import com.simibubi.create.compat.jei.ConversionRecipe;
import com.simibubi.create.compat.jei.category.MysteriousItemConversionCategory;
import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import dev.kvnmtz.createmobspawners.blocks.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.blocks.MechanicalSpawnerBlock;
import dev.kvnmtz.createmobspawners.blocks.entity.renderer.MechanicalSpawnerBlockEntityRenderer;
import dev.kvnmtz.createmobspawners.blocks.entity.registry.ModBlockEntities;
import dev.kvnmtz.createmobspawners.items.ModCreativeModeTabs;
import dev.kvnmtz.createmobspawners.items.registry.ModItems;
import dev.kvnmtz.createmobspawners.items.SoulCatcherItem;
import dev.kvnmtz.createmobspawners.network.PacketHandler;
import dev.kvnmtz.createmobspawners.ponder.PonderIndex;
import dev.kvnmtz.createmobspawners.ponder.scenes.SoulCatcherScenes;
import dev.kvnmtz.createmobspawners.recipe.ModRecipes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import java.util.function.Function;

@Mod(CreateMobSpawners.MOD_ID)
public class CreateMobSpawners
{
    public static final String MOD_ID = "create_mob_spawners";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateMobSpawners()
    {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModRecipes.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(SoulCatcherItem.class);
        MinecraftForge.EVENT_BUS.register(MechanicalSpawnerBlock.class);

        if (FMLEnvironment.dist.isClient()) {
            MinecraftForge.EVENT_BUS.register(SoulCatcherScenes.class);
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            PacketHandler.register();

            BlockStressDefaults.DEFAULT_IMPACTS.put(asResource("mechanical_spawner"), 4.0);

            Function<Item, TooltipModifier> tooltipModifierFactory = item -> new ItemDescription.Modifier(item, TooltipHelper.Palette.PURPLE)
                    .andThen(TooltipModifier.mapNull(KineticStats.create(item)));
            TooltipModifier.REGISTRY.registerDeferred(ModBlocks.SPAWNER.get().asItem(), tooltipModifierFactory);
            TooltipModifier.REGISTRY.registerDeferred(ModItems.EMPTY_SOUL_CATCHER.get(), tooltipModifierFactory);
            TooltipModifier.REGISTRY.registerDeferred(ModItems.SOUL_CATCHER.get(), tooltipModifierFactory);

            try {
                Class.forName("mezz.jei.api.JeiPlugin");
                MysteriousItemConversionCategory.RECIPES.add(ConversionRecipe.create(ModItems.EMPTY_SOUL_CATCHER.get().getDefaultInstance(), ModItems.SOUL_CATCHER.get().getDefaultInstance()));
            } catch (ClassNotFoundException ignored) {
            }
        });
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            BlockEntityRenderers.register(ModBlockEntities.SPAWNER_BE.get(), MechanicalSpawnerBlockEntityRenderer::new);
            PonderIndex.register();
        }
    }
}
