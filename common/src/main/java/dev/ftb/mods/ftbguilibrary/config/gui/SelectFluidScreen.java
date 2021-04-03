package dev.ftb.mods.ftbguilibrary.config.gui;

import dev.ftb.mods.ftbguilibrary.config.ConfigCallback;
import dev.ftb.mods.ftbguilibrary.config.FluidConfig;
import dev.ftb.mods.ftbguilibrary.icon.Color4I;
import dev.ftb.mods.ftbguilibrary.icon.Icon;
import dev.ftb.mods.ftbguilibrary.icon.ItemIcon;
import dev.ftb.mods.ftbguilibrary.misc.ButtonListBaseScreen;
import dev.ftb.mods.ftbguilibrary.utils.Key;
import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import dev.ftb.mods.ftbguilibrary.widget.Panel;
import dev.ftb.mods.ftbguilibrary.widget.SimpleTextButton;
import me.shedaniel.architectury.annotations.ExpectPlatform;
import me.shedaniel.architectury.fluid.FluidStack;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * @author LatvianModder
 */
public class SelectFluidScreen extends ButtonListBaseScreen {
	private final FluidConfig config;
	private final ConfigCallback callback;

	public SelectFluidScreen(FluidConfig c, ConfigCallback cb) {
		setTitle(new TranslatableComponent("ftbguilibrary.select_fluid.gui"));
		setHasSearchBox(true);
		config = c;
		callback = cb;
	}

	@Override
	public void addButtons(Panel panel) {
		if (config.allowEmpty) {
			FluidStack fluidStack = FluidStack.create(Fluids.EMPTY, FluidStack.bucketAmount());

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

		for (Fluid fluid : Registry.FLUID) {
			if (fluid == Fluids.EMPTY || fluid.defaultFluidState().isSource()) {
				continue;
			}

			FluidStack fluidStack = FluidStack.create(fluid, FluidStack.bucketAmount());

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