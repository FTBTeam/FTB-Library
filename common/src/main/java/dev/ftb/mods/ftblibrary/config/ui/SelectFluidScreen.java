package dev.ftb.mods.ftblibrary.config.ui;

import dev.architectury.fluid.FluidStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.FluidConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.ButtonListBaseScreen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/**
 * @author LatvianModder
 */
public class SelectFluidScreen extends ButtonListBaseScreen {
	private final FluidConfig config;
	private final ConfigCallback callback;

	public SelectFluidScreen(FluidConfig c, ConfigCallback cb) {
		setTitle(Component.translatable("ftblibrary.select_fluid.gui"));
		setHasSearchBox(true);
		config = c;
		callback = cb;
	}

	@Override
	public void addButtons(Panel panel) {
		if (config.allowEmpty) {
			var fluidStack = FluidStack.create(Fluids.EMPTY, FluidStack.bucketAmount());

			panel.add(new SimpleTextButton(panel, fluidStack.getName(), ItemIcon.getItemIcon(Items.BUCKET)) {
				@Override
				public void onClicked(MouseButton button) {
					playClickSound();
					config.setCurrentValue(Fluids.EMPTY);
					callback.save(true);
				}

				@Override
				public Object getIngredientUnderMouse() {
					return FluidStack.create(Fluids.EMPTY, FluidStack.bucketAmount());
				}
			});
		}

		for (var fluid : Registry.FLUID) {
			if (fluid == Fluids.EMPTY || !fluid.defaultFluidState().isSource()) {
				continue;
			}

			var fluidStack = FluidStack.create(fluid, FluidStack.bucketAmount());

			panel.add(new SimpleTextButton(panel, fluidStack.getName(), Icon.getIcon(getStillTexture(fluidStack)).withTint(Color4I.rgb(getColor(fluidStack)))) {
				@Override
				public void onClicked(MouseButton button) {
					playClickSound();
					config.setCurrentValue(fluid);
					callback.save(true);
				}

				@Override
				public Object getIngredientUnderMouse() {
					return FluidStack.create(fluid, FluidStack.bucketAmount());
				}
			});
		}
	}

	@ExpectPlatform
	private static ResourceLocation getStillTexture(FluidStack stack) {
		throw new AssertionError();
	}

	@ExpectPlatform
	private static int getColor(FluidStack stack) {
		throw new AssertionError();
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			callback.save(false);
			return false;
		}

		return false;
	}
}
