package com.feed_the_beast.mods.ftbguilibrary.config.gui;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigFluid;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author LatvianModder
 */
public class GuiSelectFluid extends GuiButtonListBase
{
	private final ConfigFluid config;
	private final Runnable callback;

	public GuiSelectFluid(ConfigFluid c, Runnable cb)
	{
		setTitle(I18n.format("ftblib.select_fluid.gui"));
		setHasSearchBox(true);
		config = c;
		callback = cb;
	}

	@Override
	public void addButtons(Panel panel)
	{
		if (config.allowEmpty)
		{
			FluidStack fluidStack = new FluidStack(Fluids.EMPTY, FluidAttributes.BUCKET_VOLUME);

			panel.add(new SimpleTextButton(panel, fluidStack.getDisplayName().getFormattedText(), ItemIcon.getItemIcon(Items.BUCKET))
			{
				@Override
				public void onClicked(MouseButton button)
				{
					playClickSound();
					config.setCurrentValue(Fluids.EMPTY);
					callback.run();
				}

				@Override
				public Object getIngredientUnderMouse()
				{
					return new FluidStack(Fluids.EMPTY, FluidAttributes.BUCKET_VOLUME);
				}
			});
		}

		for (Fluid fluid : ForgeRegistries.FLUIDS)
		{
			if (fluid == Fluids.EMPTY)
			{
				continue;
			}

			FluidStack fluidStack = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
			FluidAttributes attributes = fluidStack.getFluid().getAttributes();

			panel.add(new SimpleTextButton(panel, fluidStack.getDisplayName().getFormattedText(), Icon.getIcon(attributes.getStill(fluidStack)).withTint(Color4I.rgb(attributes.getColor(fluidStack))))
			{
				@Override
				public void onClicked(MouseButton button)
				{
					playClickSound();
					config.setCurrentValue(fluid);
					callback.run();
				}

				@Override
				public Object getIngredientUnderMouse()
				{
					return new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
				}
			});
		}
	}
}