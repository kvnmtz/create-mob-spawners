package dev.kvnmtz.createmobspawners.ponder.scenes;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.utility.Pointing;
import dev.kvnmtz.createmobspawners.block.custom.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.capabilities.entitystorage.StoredEntityData;
import dev.kvnmtz.createmobspawners.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.utils.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.atomic.AtomicReference;

public abstract class SpawnerScenes {
    public static void spawner(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("spawner", "Mechanical Spawner");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        scene.idle(5);

        // Show spawner
        var spawnerPos = new Vec3i(2, 1, 2);
        scene.world.showSection(util.select.position(new BlockPos(spawnerPos)), Direction.DOWN);

        scene.idleSeconds(1);

        var spawnerFaceUp = util.vector.blockSurface(new BlockPos(spawnerPos), Direction.UP);
        var spawnerCenter = new Vec3(2.5, 1.5, 2.5);
        scene.overlay.showControls((new InputWindowElement(spawnerFaceUp, Pointing.DOWN)).rightClick().withItem(ModItems.SOUL_CATCHER.get().getDefaultInstance()), 40);
        scene.idle(7);
        scene.world.modifyBlockEntity(new BlockPos(spawnerPos), MechanicalSpawnerBlockEntity.class, blockEntity -> {
            var level = blockEntity.getLevel();
            if (level == null) return;
            var pig = EntityType.PIG.create(level);
            if (pig == null) return;
            blockEntity.setStoredEntityData(StoredEntityData.of(pig));
        });
        scene.idle(10);
        scene.overlay.showText(40).attachKeyFrame().placeNearTarget().pointAt(spawnerCenter).text("Right-click the Spawner with a Soul Catcher to place it inside");

        scene.idleSeconds(3);

        scene.rotateCameraY(90);

        scene.idleSeconds(2);

        // Show tank & pump
        scene.world.showSection(util.select.fromTo(4, 1, 2, 4, 2, 2), Direction.DOWN);
        scene.idle(10);
        scene.world.showSection(util.select.position(3, 1, 2), Direction.DOWN);

        scene.idleSeconds(2);

        scene.rotateCameraY(-90);

        scene.idleSeconds(1);

        var upperTankCenter = new Vec3(4.5, 2.5, 2.5);
        scene.overlay.showText(40).attachKeyFrame().placeNearTarget().pointAt(upperTankCenter).text("The Spawner needs a supply of Potion of Regeneration to work");

        scene.idleSeconds(2);

        // Show shafts to pump
        scene.world.showSection(util.select.position(5, 0, 0), Direction.WEST);
        scene.world.setKineticSpeed(util.select.position(5, 0, 0), -32.f);
        scene.idle(10);
        scene.world.showSection(util.select.fromTo(3, 1, 1, 5, 1, 1), Direction.WEST);
        scene.world.setKineticSpeed(util.select.position(3, 1, 2), -64.f);
        scene.world.setKineticSpeed(util.select.fromTo(3, 1, 1, 5, 1, 1), 64.f);

        scene.idleSeconds(1);

        scene.overlay.showText(80).attachKeyFrame().placeNearTarget().pointAt(spawnerFaceUp).text("The Spawner also needs rotational force. The faster the rotation, the faster the spawning progress will finish.");

        scene.idleSeconds(4);

        // Show shafts to spawner
        scene.world.showSection(util.select.position(1, 5, 3), Direction.DOWN);
        scene.world.setKineticSpeed(util.select.position(1, 5, 3), -64.f);
        scene.idle(10);
        scene.world.showSection(util.select.fromTo(2, 2, 2, 2, 5, 2), Direction.DOWN);
        scene.world.setKineticSpeed(util.select.fromTo(2, 1, 2, 2, 5, 2), 128.f);

        scene.idleSeconds(3);

        // Spawn pig and play particles
        var pigBoundingBox = new AtomicReference<>(AABB.ofSize(Vec3.ZERO, 0, 0, 0));
        scene.world.createEntity(w -> {
            var pig = EntityType.PIG.create(w);
            if (pig == null) return null;

            pig.setPosRaw(pig.xo = 1, pig.yo = 1, pig.zo = 1);
            pig.setYRot(pig.yRotO = 180);
            pig.setYHeadRot(pig.yHeadRotO = 180);

            pigBoundingBox.set(pig.getBoundingBox());

            return pig;
        });
        scene.effects.emitParticles(Vec3.ZERO, (w, unused1, unused2, unused3) -> {
            var bb = pigBoundingBox.get();
            ParticleUtils.drawPotionEffectLikeParticles(ParticleTypes.WITCH, w, bb, new Vec3(1, 1, 1), new Vec3(0.1, 0.1, 0.1), ParticleUtils.getParticleCountForBoundingBox(bb), 0.75f);
        }, 1, 1);

        var pigPosition = new Vec3(1, 1.5, 1);
        scene.overlay.showText(60).attachKeyFrame().placeNearTarget().pointAt(pigPosition).text("Given some time, the Spawner will spawn the applied mob type on a nearby position");
        scene.idleSeconds(3);

        scene.overlay.showControls((new InputWindowElement(spawnerFaceUp, Pointing.DOWN)).rightClick().whileSneaking(), 40);
        scene.idle(7);
        scene.world.modifyBlockEntity(new BlockPos(spawnerPos), MechanicalSpawnerBlockEntity.class, MechanicalSpawnerBlockEntity::ejectSoulCatcher);
        scene.idle(10);
        scene.overlay.showText(40).attachKeyFrame().placeNearTarget().pointAt(spawnerCenter).text("The Soul Catcher can be ejected by right-clicking the Spawner while sneaking");

        scene.idleSeconds(2);

        scene.markAsFinished();
    }
}
