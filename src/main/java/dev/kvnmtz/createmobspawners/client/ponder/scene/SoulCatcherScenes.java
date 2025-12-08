package dev.kvnmtz.createmobspawners.client.ponder.scene;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.client.item.SoulCatcherItemClient;
import dev.kvnmtz.createmobspawners.common.item.SoulCatcherItem;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.common.util.ParticleUtils;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.joml.Quaternionf;

import java.util.concurrent.atomic.AtomicReference;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CreateMobSpawnersMod.MOD_ID, value = Dist.CLIENT)
public abstract class SoulCatcherScenes {

    private static final Quaternionf alignedCameraOrientation = new Quaternionf().rotateYXZ((float) Math.toRadians(35.0), (float) Math.toRadians(25.0), 0.0F);
    private static Quaternionf originalCameraOrientation;

    @SubscribeEvent
    protected static void preRenderScreen(ScreenEvent.Render.Pre event) {
        if (!(event.getScreen() instanceof PonderUI ponderUi)) {
            return;
        }
        if (!ponderUi.getActiveScene().getId().equals(CreateMobSpawnersMod.asResource("soul_catcher"))) {
            return;
        }

        var entityRendererDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        originalCameraOrientation = entityRendererDispatcher.cameraOrientation();

        /* Little hack to make the thrown splash potion face the camera in the ponder scene */
        entityRendererDispatcher.overrideCameraOrientation(alignedCameraOrientation);
    }

    @SubscribeEvent
    protected static void postRenderScreen(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof PonderUI ponderUi)) {
            return;
        }
        if (!ponderUi.getActiveScene().getId().equals(CreateMobSpawnersMod.asResource("soul_catcher"))) {
            return;
        }

        var entityRendererDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityRendererDispatcher.overrideCameraOrientation(originalCameraOrientation);
    }

    public static void soulCatcher(SceneBuilder builder, SceneBuildingUtil ignoredUtil) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title("soul_catcher", "Soul Catcher");
        scene.showBasePlate();

        scene.idle(5);

        var pigPosition = new Vec3(2.5, 1, 2.5);
        var pigCenter = pigPosition.add(0, 0.45f, 0);
        var pigReference = new AtomicReference<Pig>();
        var pigEntity = scene.world().createEntity(world -> {
            var pig = spawnPig(pigPosition, world);
            pigReference.set(pig);
            return pig;
        });

        scene.overlay().showText(60).placeNearTarget().pointAt(pigCenter).text("See this innocent pig? Let's catch it.");

        scene.idleSeconds(3);

        scene.overlay().showText(80).attachKeyFrame().placeNearTarget().pointAt(pigCenter).text("In order to catch a mob's soul, it needs to be weakened (e.g. with a Splash Potion of Healing)");

        scene.idleSeconds(4);

        var potionColor = new AtomicReference<>(0);
        var potionEntity = scene.world().createEntity(world -> {
            var potion = EntityType.POTION.create(world);
            if (potion == null) {
                return null;
            }

            var potionItem = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("splash_potion"));
            var potionItemStack = potionItem.getDefaultInstance();
            var potionContents = new PotionContents(Potions.WEAKNESS);
            potionItemStack.set(DataComponents.POTION_CONTENTS, potionContents);
            potion.setItem(potionItemStack);

            potionColor.set(potionContents.getColor());

            var potionPosition = pigPosition.add(0, 2, 0);
            potion.setPos(potion.xo = potionPosition.x, potion.yo = potionPosition.y, potion.zo = potionPosition.z);

            potion.shoot(0, 1.f, 0, 0.25f, 0f);

            return potion;
        });

        scene.idle(14);

        scene.world().modifyEntity(potionEntity, e -> {
            var potion = (ThrownPotion) e;

            var itemStack = potion.getItem();
            var potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
            if (potionContents == null) {
                return;
            }

            var color = potionContents.getColor();
            var random = potion.level().random;

            var splashOrigin = potion.position();

            for (int i = 0; i < 8; ++i) {
                potion.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)), splashOrigin.x, splashOrigin.y, splashOrigin.z, random.nextGaussian() * 0.15, random.nextDouble() * 0.2, random.nextGaussian() * 0.15);
            }

            var red = (float) (color >> 16 & 255) / 255.0F;
            var blue = (float) (color >> 8 & 255) / 255.0F;
            var green = (float) (color & 255) / 255.0F;

            for (int i = 0; i < 100; ++i) {
                var d13 = random.nextDouble() * (double) 1.5F;
                var d19 = random.nextDouble() * Math.PI * (double) 2.0F;
                var d25 = Math.cos(d19) * d13;
                var d30 = 0.01 + random.nextDouble() * (double) 0.5F;
                var d31 = Math.sin(d19) * d13;

                var particle = Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.EFFECT, splashOrigin.x + d25 * 0.1, splashOrigin.y + 0.3, splashOrigin.z + d31 * 0.1, d25, d30, d31);
                if (particle != null) {
                    var randomShift = 0.75F + random.nextFloat() * 0.25F;
                    particle.setColor(red * randomShift, blue * randomShift, green * randomShift);
                    particle.setPower((float) d13);
                }

                ((PonderLevel) potion.level()).addParticle(particle);
            }

            potion.discard();
        });

        scene.effects().emitParticles(Vec3.ZERO, (world, x, y, z) -> {
            var pig = pigReference.get();
            ParticleUtils.drawPotionEffectParticles(world, pig.getBoundingBox(), potionColor.get(), 1);
        }, 1, 107);

        scene.idleSeconds(3);

        scene.overlay().showText(40).attachKeyFrame().placeNearTarget().pointAt(pigCenter).text("Now, right-click it with an empty Soul Catcher to start catching its soul");

        scene.idleSeconds(2);

        scene.overlay().showControls(pigCenter.add(0, 0.5f, 0), Pointing.DOWN, 40).rightClick().withItem(ModItems.EMPTY_SOUL_CATCHER.get().getDefaultInstance());

        scene.idle(7);

        scene.world().modifyEntity(pigEntity, pig -> {
            SoulCatcherItemClient.addShrinkingEntity(pig);
            ParticleUtils.drawPotionEffectLikeParticles(ParticleTypes.WITCH, pig.level(), pig.getBoundingBox(), new Vec3(0.1, 0.1, 0.1), ParticleUtils.getParticleCountForEntity(pig));
        });

        scene.idle(SoulCatcherItem.getCatchingDurationInTicks(AABB.ofSize(Vec3.ZERO, 0.9f, 0.9f, 0.9f)));

        scene.world().modifyEntity(pigEntity, pig -> {
            var bb = pigReference.get().getBoundingBox();
            ParticleUtils.drawParticles(ParticleTypes.REVERSE_PORTAL, pig.level(), pigCenter, ParticleUtils.getParticleCountForEntity(pig), bb.getXsize() / 3, bb.getYsize() / 3, bb.getZsize() / 3, Vec3.ZERO);
            pig.discard();
        });

        scene.idleSeconds(1);

        scene.overlay().showText(40).independent(0).placeNearTarget().text("Gotcha! The pig was caught.");
        scene.idle(10);
        scene.overlay().showText(60).attachKeyFrame().independent(24).placeNearTarget().text("If you want to release it again, just right-click the desired position with the Soul Catcher");

        scene.idleSeconds(3);

        scene.overlay().showControls(pigPosition, Pointing.DOWN, 20).rightClick().withItem(ModItems.SOUL_CATCHER.get().getDefaultInstance());

        scene.idle(30);

        scene.world().createEntity(world -> {
            var pig = spawnPig(pigPosition, world);
            if (pig == null) {
                return null;
            }

            ParticleUtils.drawPotionEffectLikeParticles(ParticleTypes.WITCH, pig.level(), pig.getBoundingBox(), new Vec3(0.1, 0.1, 0.1), ParticleUtils.getParticleCountForEntity(pig));
            return pig;
        });

        scene.idleSeconds(2);
        scene.markAsFinished();
    }

    private static Pig spawnPig(Vec3 pigPosition, Level world) {
        var pig = EntityType.PIG.create(world);
        if (pig == null) {
            return null;
        }

        pig.setPos(pig.xo = pigPosition.x, pig.yo = pigPosition.y, pig.zo = pigPosition.z);
        pig.setYRot(pig.yRotO = 180);
        pig.setYHeadRot(pig.yHeadRotO = 180);

        return pig;
    }
}
