package com.feed_the_beast.mods.ftbguilibrary;

import com.feed_the_beast.mods.ftbguilibrary.icon.AtlasSpriteIcon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconPresets;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconRenderer;
import com.feed_the_beast.mods.ftbguilibrary.sidebar.GuiButtonSidebarGroup;
import com.feed_the_beast.mods.ftbguilibrary.sidebar.SidebarButtonManager;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.TextureStitchEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import me.shedaniel.architectury.hooks.ScreenHooks;
import me.shedaniel.architectury.registry.ReloadListeners;
import me.shedaniel.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;
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
		EnvExecutor.runInEnv(EnvType.CLIENT, () -> this::init);
	}

	public static boolean shouldRenderIcons = false;
	public static int showButtons = 1;

	public void init()
	{
		TextureStitchEvent.PRE.register(FTBGUILibrary::textureStitch);
		GuiEvent.INIT_POST.register(FTBGUILibrary::guiInit);
		GuiEvent.RENDER_PRE.register((screen, matrices, mouseX, mouseY, delta) -> {
			FTBGUILibrary.renderTick();
			return InteractionResult.PASS;
		});
		ClientTickEvent.CLIENT_POST.register(FTBGUILibrary::clientTick);

		ReloadListeners.registerReloadListener(PackType.CLIENT_RESOURCES, SidebarButtonManager.INSTANCE);

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
	private static void guiInit(Screen screen, List<AbstractWidget> abstractWidgets, List<GuiEventListener> guiEventListeners)
	{
		if (areButtonsVisible(screen))
		{
			GuiButtonSidebarGroup group = new GuiButtonSidebarGroup((AbstractContainerScreen) screen);
			ScreenHooks.addButton(screen, group);
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

		return gui instanceof AbstractContainerScreen && !SidebarButtonManager.INSTANCE.groups.isEmpty();
	}

}