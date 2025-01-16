package dev.kvnmtz.createmobspawners.ponder.scenes;

import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import com.simibubi.create.foundation.utility.Pointing;
import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.items.SoulCatcherItem;
import dev.kvnmtz.createmobspawners.items.registry.ModItems;
import dev.kvnmtz.createmobspawners.utils.ParticleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;

import java.util.concurrent.atomic.AtomicReference;

public abstract class SoulCatcherScenes {
    private static final Quaternionf alignedCameraOrientation = new Quaternionf().rotateYXZ((float) Math.toRadians(35.0), (float) Math.toRadians(25.0), 0.f);
    private static Quaternionf originalCameraOrientation;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    protected static void preRenderScreen(ScreenEvent.Render.Pre event) {
        if (!(event.getScreen() instanceof PonderUI ponderUi)) return;
        if (!ponderUi.getActiveScene().getId().equals(CreateMobSpawners.asResource("soul_catcher"))) return;

        var entityRendererDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        originalCameraOrientation = entityRendererDispatcher.cameraOrientation();

        /* Little hack to make the thrown splash potion face the camera in the ponder scene */
        entityRendererDispatcher.overrideCameraOrientation(alignedCameraOrientation);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    protected static void postRenderScreen(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof PonderUI ponderUi)) return;
        if (!ponderUi.getActiveScene().getId().equals(CreateMobSpawners.asResource("soul_catcher"))) return;

        var entityRendererDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityRendererDispatcher.overrideCameraOrientation(originalCameraOrientation);
    }

    public static void soulCatcher(SceneBuilder scene, SceneBuildingUtil ignoredUtil) {
        scene.title("soul_catcher", "Soul Catcher");
        scene.showBasePlate();

        scene.idle(5);

        var pigPosition = new Vec3(2.5, 1, 2.5);
        var pigCenter = pigPosition.add(0, 0.45f, 0);
        var pigReference = new AtomicReference<Pig>();
        var pigEntity = scene.world.createEntity(w -> {
            var pig = EntityType.PIG.create(w);
            if (pig == null) return null;

            pig.setPosRaw(pig.xo = pigPosition.x, pig.yo = pigPosition.y, pig.zo = pigPosition.z);
            pig.setYRot(pig.yRotO = 180);
            pig.setYHeadRot(pig.yHeadRotO = 180);

            pigReference.set(pig);

            return pig;
        });

        scene.overlay.showText(60).placeNearTarget().pointAt(pigCenter).text("See this innocent pig? Let's catch it.");

        scene.idleSeconds(3);

        scene.overlay.showText(80).attachKeyFrame().placeNearTarget().pointAt(pigCenter).text("In order to catch a mob's soul, it needs to be weakened (e.g. with a Splash Potion of Healing)");

        scene.idleSeconds(4);

        var potionColor = new AtomicReference<>(0);
        var potionEntity = scene.world.createEntity(w -> {
            var potion = EntityType.POTION.create(w);
            if (potion == null) return null;

            var potionItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:splash_potion"));
            if (potionItem == null) return null;

            var potionItemStack = potionItem.getDefaultInstance();
            PotionUtils.setPotion(potionItemStack, Potions.WEAKNESS);
            potion.setItem(potionItemStack);

            potionColor.set(PotionUtils.getColor(potionItemStack));

            var potionPosition = pigPosition.add(0, 2, 0);
            potion.setPosRaw(potion.xo = potionPosition.x, potion.yo = potionPosition.y, potion.zo = potionPosition.z);

            potion.shoot(0, 1.f, 0, 0.25f, 0f);

            return potion;
        });

        scene.idle(14);

        scene.world.modifyEntity(potionEntity, e -> {
            var potion = (ThrownPotion) e;

            var itemStack = potion.getItem();

            var color = PotionUtils.getColor(itemStack);
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

                ((PonderWorld) potion.level()).addParticle(particle);
            }

            potion.discard();
        });

        scene.effects.emitParticles(Vec3.ZERO, (level, unused1, unused2, unused3) -> {
            var pig = pigReference.get();
            ParticleUtils.drawPotionEffectParticles(level, pig.getBoundingBox(), pig.position(), potionColor.get(), 1);
        }, 1, 107);

        scene.idleSeconds(3);

        scene.overlay.showText(40).attachKeyFrame().placeNearTarget().pointAt(pigCenter).text("Now, right-click it with an empty Soul Catcher to start catching its soul");

        scene.idleSeconds(2);

        scene.overlay.showControls((new InputWindowElement(pigCenter.add(0, 0.5f, 0), Pointing.DOWN)).rightClick().withItem(ModItems.EMPTY_SOUL_CATCHER.get().getDefaultInstance()), 40);

        scene.idle(7);

        scene.world.modifyEntity(pigEntity, pig -> {
            SoulCatcherItem.addShrinkingEntity(pig);
            ParticleUtils.drawPotionEffectLikeParticles(ParticleTypes.WITCH, pig.level(), pig.getBoundingBox(), pig.position(), new Vec3(0.1, 0.1, 0.1), ParticleUtils.getParticleCountForEntity(pig), 0.75f);
        });

        scene.idle(SoulCatcherItem.getCatchingDurationInTicks(AABB.ofSize(Vec3.ZERO, 0.9f, 0.9f, 0.9f)));

        scene.world.modifyEntity(pigEntity, pig -> {
            var bb = pigReference.get().getBoundingBox();
            ParticleUtils.drawParticles(ParticleTypes.REVERSE_PORTAL, pig.level(), pigCenter, ParticleUtils.getParticleCountForEntity(pig), bb.getXsize() / 3, bb.getYsize() / 3, bb.getZsize() / 3, Vec3.ZERO);
            pig.discard();
        });

        scene.idleSeconds(1);

        scene.overlay.showText(40).independent(0).placeNearTarget().text("Gotcha! The pig was caught.");
        scene.idle(10);
        scene.overlay.showText(60).attachKeyFrame().independent(24).placeNearTarget().text("If you want to release it again, just right-click the desired position with the Soul Catcher");

        scene.idleSeconds(3);

        scene.overlay.showControls((new InputWindowElement(pigPosition, Pointing.DOWN)).rightClick().withItem(ModItems.SOUL_CATCHER.get().getDefaultInstance()), 20);

        scene.idle(30);

        scene.world.createEntity(w -> {
            var pig = EntityType.PIG.create(w);
            if (pig == null) return null;

            pig.setPosRaw(pig.xo = pigPosition.x, pig.yo = pigPosition.y, pig.zo = pigPosition.z);
            pig.setYRot(pig.yRotO = 180);
            pig.setYHeadRot(pig.yHeadRotO = 180);

            ParticleUtils.drawPotionEffectLikeParticles(ParticleTypes.WITCH, pig.level(), pig.getBoundingBox(), pig.position(), new Vec3(0.1, 0.1, 0.1), ParticleUtils.getParticleCountForEntity(pig), 0.75f);

            return pig;
        });

        scene.idleSeconds(2);
        scene.markAsFinished();
    }
}
