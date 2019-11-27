package com.feed_the_beast.mods.ftbguilibrary;

import com.feed_the_beast.mods.ftbguilibrary.icon.AtlasSpriteIcon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconPresets;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconRenderer;
import com.feed_the_beast.mods.ftbguilibrary.sidebar.GuiButtonSidebarGroup;
import com.feed_the_beast.mods.ftbguilibrary.sidebar.SidebarButtonManager;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * @author LatvianModder
 */
public class FTBGUILibraryClient extends FTBGUILibraryCommon
{
	public static boolean shouldRenderIcons = false;

	@Override
	public void init()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::textureStitch);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true, this::renderTick);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true, this::guiInit);
		((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(SidebarButtonManager.INSTANCE);
	}

	private void textureStitch(TextureStitchEvent.Pre event)
	{
		try
		{
			for (Field field : GuiIcons.class.getDeclaredFields())
			{
				field.setAccessible(true);
				Object o = field.get(null);

				if (o instanceof AtlasSpriteIcon)
				{
					AtlasSpriteIcon a = (AtlasSpriteIcon) o;
					event.addSprite(new ResourceLocation(a.name));
					IconPresets.MAP.put(a.name, a);
				}
			}
		}
		catch (Exception ex)
		{
		}
	}

	public void renderTick(TickEvent.RenderTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START)
		{
			if (shouldRenderIcons)
			{
				renderIcons();
			}
		}
	}

	private void renderIcons()
	{
		IconRenderer.render();
	}

	public void guiInit(GuiScreenEvent.InitGuiEvent.Post event)
	{
		//sidebarButtonScale = 0D;

		if (areButtonsVisible(event.getGui()))
		{
			event.addWidget(new GuiButtonSidebarGroup((DisplayEffectsScreen) event.getGui()));
		}
	}

	public static boolean areButtonsVisible(@Nullable Screen gui)
	{
		return /*FIXME: FTBLibClientConfig.action_buttons != EnumSidebarButtonPlacement.DISABLED && */gui instanceof DisplayEffectsScreen && !SidebarButtonManager.INSTANCE.groups.isEmpty();
	}
}