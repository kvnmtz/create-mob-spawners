package dev.kvnmtz.createmobspawners.common.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.common.recipe.registry.ModIngredients;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SoulIngredient implements ICustomIngredient {

    private final ResourceLocation entityId;

    public static final MapCodec<SoulIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("entity").forGetter(o -> o.entityId)
    ).apply(inst, SoulIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SoulIngredient> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    public SoulIngredient(ResourceLocation entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!stack.is(ModItems.SOUL_CATCHER)) {
            return false;
        }

        var data = stack.get(DataComponents.ENTITY_DATA);
        if (data == null) {
            return false;
        }

        var entityTag = data.copyTag();
        var entityId = ResourceLocation.parse(entityTag.getString("id"));
        return entityId.equals(this.entityId);
    }

    @Override
    public Stream<ItemStack> getItems() {
        var stack = ModItems.SOUL_CATCHER.asStack();

        var entityTag = new CompoundTag();
        entityTag.putString("id", this.entityId.toString());

        var data = CustomData.of(entityTag);
        stack.set(DataComponents.ENTITY_DATA, data);

        return Stream.of(stack);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return ModIngredients.SOUL.get();
    }
}
