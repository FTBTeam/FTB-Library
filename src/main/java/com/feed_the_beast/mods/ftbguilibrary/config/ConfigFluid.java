package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiSelectFluid;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.text.ITextComponent;
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
		defaultValue = Fluids.EMPTY;
		value = Fluids.EMPTY;
	}

	@Override
	public ITextComponent getStringForGUI(Fluid v)
	{
		return new FluidStack(v, FluidAttributes.BUCKET_VOLUME).getDisplayName();
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback)
	{
		new GuiSelectFluid(this, callback).openGui();
	}
}