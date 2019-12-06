package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiSelectFluid;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.fluid.Fluid;
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
	public String getStringForGUI(Fluid v)
	{
		return new FluidStack(v, FluidAttributes.BUCKET_VOLUME).getDisplayName().getFormattedText();
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback)
	{
		new GuiSelectFluid(this, callback).openGui();
	}
}