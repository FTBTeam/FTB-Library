package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiSelectFluid;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author LatvianModder
 */
public class ConfigFluid extends ConfigValue<Fluid>
{
	public final boolean allowEmpty;

	public ConfigFluid(boolean empty)
	{
		allowEmpty = empty;
	}

	@Override
	public boolean isValid(Fluid value)
	{
		return allowEmpty || value != Fluids.EMPTY;
	}

	@Override
	public boolean isEmpty(Fluid value)
	{
		return value == Fluids.EMPTY;
	}

	@Override
	public String getStringForGUI(Fluid value)
	{
		return new FluidStack(value, FluidAttributes.BUCKET_VOLUME).getDisplayName().getFormattedText();
	}

	@Override
	public void onClicked(MouseButton button, Runnable callback)
	{
		new GuiSelectFluid(this, callback).openGui();
	}
}