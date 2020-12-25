package com.feed_the_beast.mods.ftbguilibrary;

import com.feed_the_beast.mods.ftbguilibrary.event.GuiInitEvent;
import com.feed_the_beast.mods.ftbguilibrary.event.RenderTickEvent;
import com.feed_the_beast.mods.ftbguilibrary.icon.AtlasSpriteIcon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconPresets;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconRenderer;
import com.feed_the_beast.mods.ftbguilibrary.sidebar.GuiButtonSidebarGroup;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import me.shedaniel.architectury.event.events.TextureStitchEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FTBGUILibrary
{
	public static final String MOD_ID = "ftbguilibrary";

	public FTBGUILibrary()
	{
		EnvExecutor.runInEnv(Env.CLIENT, () -> this::init);
	}

	public static boolean shouldRenderIcons = false;
	public static int showButtons = 1;

	public void init()
	{
		TextureStitchEvent.PRE.register(FTBGUILibrary::textureStitch);
		RenderTickEvent.PRE.register(FTBGUILibrary::renderTick);
		GuiInitEvent.POST.register(FTBGUILibrary::guiInit);
		ClientTickEvent.CLIENT_POST.register(FTBGUILibrary::clientTick);

		/* FIXME: workaround for selective resource listener */
		//((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(SidebarButtonManager.INSTANCE);

		CommandRegistrationEvent.EVENT.register(FTBGUILibraryCommands::registerCommands);
	}

	private static void textureStitch(TextureAtlas atlas, Consumer<ResourceLocation> addSprite)
	{
		if (!atlas.location().equals(TextureAtlas.LOCATION_BLOCKS))
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
					addSprite.accept(a.id);
					IconPresets.MAP.put(a.id.toString(), a);
				}
			}
		}
		catch (Exception ignored)
		{
		}
	}

	private static void renderTick()
	{
		if (shouldRenderIcons)
		{
			renderIcons();
		}
	}

	private static void renderIcons()
	{
		IconRenderer.render();
	}

	@SuppressWarnings("rawtypes")
	private static void guiInit(Screen gui, List<AbstractWidget> list, Consumer<AbstractWidget> add)
	{
		if (areButtonsVisible(gui))
		{
			add.accept(new GuiButtonSidebarGroup((AbstractContainerScreen) gui));
		}
	}

	private static void clientTick(Minecraft client)
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

	public static boolean areButtonsVisible(@Nullable Screen gui)
	{
		if (showButtons == 0 || showButtons == 2 && !(gui instanceof EffectRenderingInventoryScreen))
		{
			return false;
		}

		// FIXME: SidebarButtonManager
		return gui instanceof AbstractContainerScreen /*&& !SidebarButtonManager.INSTANCE.groups.isEmpty()*/;
	}

}