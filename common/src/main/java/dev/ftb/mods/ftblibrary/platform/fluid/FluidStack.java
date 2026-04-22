package dev.ftb.mods.ftblibrary.platform.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.platform.Platform;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

/**
 * Note: Mostly taken directly from the source of NeoForge. Credit to them for the implementation of half of this class.
 *
 * TODO: Hover name + Fluid hover x-plat support (NeoForge supports this)
 * TODO: Hashcode + equals
 */
public class FluidStack implements DataComponentHolder, TypedInstance<Fluid> {
    public static FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

    public static final Codec<Holder<Fluid>> FLUID_HOLDER_CODEC = BuiltInRegistries.FLUID.holderByNameCodec()
            .validate(fluid -> fluid.value() == Fluids.EMPTY ?
                    DataResult.error(() -> "Fluid cannot be empty") :
                    DataResult.success(fluid)
            );
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_HOLDER_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FLUID);

    public static final MapCodec<FluidStack> MAP_CODEC = MapCodec.recursive(
            "FluidStack",
            c -> RecordCodecBuilder.mapCodec(
                    instance -> instance.group(
                                    FLUID_HOLDER_CODEC.fieldOf("fluid").forGetter(FluidStack::typeHolder),
                                    ExtraCodecs.POSITIVE_LONG.fieldOf("amount").forGetter(FluidStack::amount),
                                    DataComponentPatch.CODEC.optionalFieldOf(ItemInstance.FIELD_COMPONENTS, DataComponentPatch.EMPTY)
                                            .forGetter(stack -> stack.components.asPatch()))
                            .apply(instance, FluidStack::new)));

    public static final Codec<FluidStack> CODEC = MAP_CODEC.codec();
    public static final Codec<FluidStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
            .xmap(optional -> optional.orElse(FluidStack.EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC = StreamCodec.of(
            (buf, stack) -> {
                if (stack.isEmpty()) {
                    buf.writeVarLong(0L);
                } else {
                    buf.writeVarLong(stack.amount());
                    FLUID_HOLDER_STREAM_CODEC.encode(buf, stack.typeHolder());
                    DataComponentPatch.STREAM_CODEC.encode(buf, stack.components.asPatch());
                }
            },
            buf -> {
                long amount = buf.readVarLong();
                if (amount <= 0L) {
                    return EMPTY;
                } else {
                    Holder<Fluid> holder = FLUID_HOLDER_STREAM_CODEC.decode(buf);
                    DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
                    return new FluidStack(holder, amount, patch);
                }
            }
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf output, FluidStack value) {
            if (value.isEmpty()) {
                throw new EncoderException("Empty FluidStack not allowed");
            }

            FluidStack.OPTIONAL_STREAM_CODEC.encode(output, value);
        }

        @Override
        public FluidStack decode(RegistryFriendlyByteBuf input) {
            FluidStack stack = FluidStack.OPTIONAL_STREAM_CODEC.decode(input);
            if (stack.isEmpty()) {
                throw new DecoderException("Empty FluidStack not allowed");
            }

            return stack;
        }
    };

    private final Holder<Fluid> fluid;

    private long amount;
    private final PatchedDataComponentMap components;

    public FluidStack(Fluid fluid, long amount, DataComponentPatch patch) {
        this(fluid.builtInRegistryHolder(), amount, patch);
    }

    public FluidStack(Fluid fluid, long amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    public FluidStack(Fluid fluid, int amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    public FluidStack(Holder<Fluid> fluid, long amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    public FluidStack(Holder<Fluid> fluid, long amount, DataComponentPatch patch) {
        this(fluid, amount, PatchedDataComponentMap.fromPatch(fluid.components(), patch));
    }

    private FluidStack(Holder<Fluid> fluid, long amount, PatchedDataComponentMap components) {
        this.fluid = fluid;
        this.amount = amount;
        this.components = components;
    }

    public Component name() {
        Item bucket = this.fluid().getBucket();
        return bucket.getName(new ItemStack(bucket));
    }

    public long amount() {
        return this.amount;
    }

    /**
     * Helper: Returns the amount of fluid in the stack represented in buckets, rounding down.
     */
    public long amountAsBucket() {
        return this.amount / bucketFluidAmount();
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    /**
     * Helper: Sets the amount of fluid in the stack based on the given bucket count.
     */
    public void setAmountByBucketCount(long buckets) {
        this.amount = buckets * bucketFluidAmount();
    }

    public Fluid fluid() {
        return this.fluid.value();
    }

    public FluidStack copyWithAmount(long amount) {
        if (this.isEmpty()) {
            return empty();
        }

        var copy = copy();
        copy.setAmount(amount);
        return copy;
    }

    public boolean isEmpty() {
        return this == EMPTY || fluid.value().isSame(Fluids.EMPTY) || amount <= 0;
    }

    public FluidStack copy() {
        if (this.isEmpty()) {
            return empty();
        }

        return new FluidStack(this.fluid, this.amount, this.components.copy());
    }

    @Override
    public DataComponentMap getComponents() {
        return this.isEmpty() ? DataComponentMap.EMPTY : this.components;
    }

    public DataComponentMap getPrototype() {
        return !this.isEmpty() ? this.typeHolder().components() : DataComponentMap.EMPTY;
    }

    public DataComponentPatch getComponentsPatch() {
        return !this.isEmpty() ? this.components.asPatch() : DataComponentPatch.EMPTY;
    }

    public DataComponentMap immutableComponents() {
        return !this.isEmpty() ? this.components.toImmutableMap() : DataComponentMap.EMPTY;
    }

    public void applyComponents(DataComponentPatch patch) {
        this.components.applyPatch(patch);
    }

    public void applyComponents(DataComponentMap components) {
        this.components.setAll(components);
    }

    public static FluidStack empty() {
        return EMPTY;
    }

    public static long bucketFluidAmount() {
        return Platform.get().misc().bucketFluidAmount();
    }

    @Override
    public Holder<Fluid> typeHolder() {
        return this.fluid;
    }

    public void grow(long amount) {
        setAmount(this.amount() + amount);
    }

    public void shrink(long amount) {
        // Restrict to 0 instead of allowing negative amounts
        setAmount(Math.max(0, this.amount() - amount));
    }
}
