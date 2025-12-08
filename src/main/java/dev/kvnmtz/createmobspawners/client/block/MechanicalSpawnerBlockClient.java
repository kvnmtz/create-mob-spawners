package dev.kvnmtz.createmobspawners.client.block;

import dev.kvnmtz.createmobspawners.client.gui.screen.SpawnerScreen;
import dev.kvnmtz.createmobspawners.common.block.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.common.config.ModServerConfig;
import net.createmod.catnip.gui.ScreenOpener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class MechanicalSpawnerBlockClient {

    public static void openConfigGuiIfAllowed(MechanicalSpawnerBlockEntity be) {
        if (!ModServerConfig.CONFIG.mechanicalSpawnerConfigurationAllowed.get()) {
            return;
        }

        ScreenOpener.open(new SpawnerScreen(be));
    }
}
