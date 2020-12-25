package com.feed_the_beast.mods.ftbguilibrary.icon;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * @author LatvianModder
 */
public abstract class IconWithParent extends Icon
{
	public final Icon parent;

	public IconWithParent(Icon i)
	{
		parent = i;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void bindTexture()
	{
		parent.bindTexture();
	}
}