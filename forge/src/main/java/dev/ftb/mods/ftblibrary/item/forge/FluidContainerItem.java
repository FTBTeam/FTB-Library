package dev.ftb.mods.ftblibrary.item.forge;

import dev.ftb.mods.ftblibrary.item.FTBLibraryItems;
import dev.ftb.mods.ftblibrary.item.FluidContainerBaseItem;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class FluidContainerItem extends FluidContainerBaseItem {
	public static FluidStack getFluidStack(ItemStack item) {
		return FluidUtil.getFluidContained(item).orElse(FluidStack.EMPTY);
	}

	public static String getFluidStackHash(ItemStack item, UidContext ctx) {
		FluidStack fs = getFluidStack(item);

		if (fs.isEmpty()) {
			return "";
		}

		return String.format("%08X:%08X", Objects.hashCode(fs.getFluid()), Objects.hashCode(fs.getTag()));
	}

	public static ItemStack of(FluidStack fluidStack) {
		ItemStack stack = new ItemStack(FTBLibraryItems.FLUID_CONTAINER.get());
		FluidUtil.getFluidHandler(stack).ifPresent(handler -> handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE));
		return stack;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new FluidHandlerItemStack.Consumable(stack, FluidAttributes.BUCKET_VOLUME);
	}

	@Override
	public ItemStack getDefaultInstance() {
		return of(new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME));
	}

	@Override
	public void fillItemCategory(CreativeModeTab tag, NonNullList<ItemStack> list) {
		if (CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY != null && allowdedIn(tag)) {
			for (Fluid fluid : ForgeRegistries.FLUIDS) {
				if (fluid != Fluids.EMPTY && fluid.isSource(fluid.defaultFluidState())) {
					list.add(of(new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME)));
				}
			}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		FluidStack fluidStack = getFluidStack(stack);

		if (!fluidStack.isEmpty()) {
			tooltip.add(new TextComponent("< ").append(new TranslatableComponent("ftblibrary.mb", fluidStack.getAmount(), fluidStack.getDisplayName())).append(" >").withStyle(ChatFormatting.GRAY));
		}

		tooltip.add(new TranslatableComponent("item.ftblibrary.fluid_container.use").withStyle(ChatFormatting.DARK_GRAY));
	}
}
