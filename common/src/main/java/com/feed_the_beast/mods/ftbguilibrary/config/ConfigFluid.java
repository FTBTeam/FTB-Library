package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiSelectFluid;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import me.shedaniel.architectury.fluid.FluidStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

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
	public Component getStringForGUI(Fluid v)
	{
		return FluidStack.create(v, FluidStack.bucketAmount()).getName();
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback)
	{
		new GuiSelectFluid(this, callback).openGui();
	}
}