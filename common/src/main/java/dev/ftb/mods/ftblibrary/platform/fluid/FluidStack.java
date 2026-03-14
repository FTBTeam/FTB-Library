package dev.ftb.mods.ftblibrary.platform.fluid;

import dev.ftb.mods.ftblibrary.platform.Platform;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.function.Predicate;

public class FluidStack implements DataComponentHolder {
    private final Holder<Fluid> fluid;

    private long amount;
    private final PatchedDataComponentMap components;

    public static FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

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

    public boolean is(Predicate<Holder<Fluid>> test) {
        return test.test(this.fluid);
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

    public void applyComponents(DataComponentMap components) {
    }

    public static FluidStack empty() {
        return EMPTY;
    }

    public static long bucketFluidAmount() {
        return Platform.get().misc().bucketFluidAmount();
    }
}
