package com.feed_the_beast.mods.ftbguilibrary.icon;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
	@OnlyIn(Dist.CLIENT)
	public void bindTexture()
	{
		parent.bindTexture();
	}
}