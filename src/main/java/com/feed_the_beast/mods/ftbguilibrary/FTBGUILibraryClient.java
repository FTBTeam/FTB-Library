package com.feed_the_beast.mods.ftbguilibrary;

import com.feed_the_beast.mods.ftbguilibrary.icon.AtlasSpriteIcon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconPresets;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconRenderer;
import com.feed_the_beast.mods.ftbguilibrary.sidebar.GuiButtonSidebarGroup;
import com.feed_the_beast.mods.ftbguilibrary.sidebar.SidebarButtonManager;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * @author LatvianModder
 */
public class FTBGUILibraryClient extends FTBGUILibraryCommon
{
	public static boolean shouldRenderIcons = false;
	public static int showButtons = 1;

	@Override
	public void init()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::textureStitch);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true, this::renderTick);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true, this::guiInit);
		((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(SidebarButtonManager.INSTANCE);
		MinecraftForge.EVENT_BUS.addListener(this::clientTick);
		new FTBGUILibraryTest().init();
	}

	private void textureStitch(TextureStitchEvent.Pre event)
	{
		if (!event.getMap().getId().equals(PlayerContainer.BLOCK_ATLAS_TEXTURE))
		{
			return;
		}

		try
		{
			for (Field field : GuiIcons.class.getDeclaredFields())
			{
				field.setAccessible(true);
				Object o = field.get(null);

				if (o instanceof AtlasSpriteIcon)
				{
					AtlasSpriteIcon a = (AtlasSpriteIcon) o;
					event.addSprite(a.id);
					IconPresets.MAP.put(a.id.toString(), a);
				}
			}
		}
		catch (Exception ex)
		{
		}
	}

	private void renderTick(TickEvent.RenderTickEvent event)
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

	private void guiInit(GuiScreenEvent.InitGuiEvent.Post event)
	{
		if (areButtonsVisible(event.getGui()))
		{
			event.addWidget(new GuiButtonSidebarGroup((ContainerScreen) event.getGui()));
		}
	}

	private void clientTick(TickEvent.ClientTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END)
		{
			if (!ClientUtils.RUN_LATER.isEmpty())
			{
				for (Runnable runnable : new ArrayList<>(ClientUtils.RUN_LATER))
				{
					runnable.run();
				}

				ClientUtils.RUN_LATER.clear();
			}
		}
	}

	public static boolean areButtonsVisible(@Nullable Screen gui)
	{
		if (showButtons == 0 || showButtons == 2 && !(gui instanceof DisplayEffectsScreen))
		{
			return false;
		}

		return gui instanceof ContainerScreen && !SidebarButtonManager.INSTANCE.groups.isEmpty();
	}
}