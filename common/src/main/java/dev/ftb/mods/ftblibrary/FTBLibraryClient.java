package dev.ftb.mods.ftblibrary;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTextureStitchEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ImageConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.AtlasSpriteIcon;
import dev.ftb.mods.ftblibrary.icon.IconPresets;
import dev.ftb.mods.ftblibrary.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonManager;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import dev.ftb.mods.ftblibrary.ui.CursorType;
import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class FTBLibraryClient extends FTBLibraryCommon {
	public static final List<IconRenderer<?>> ICON_RENDERERS = new ArrayList<>();
	/**
	 * Meaning of the different values: 0 = No, 1 = Yes, 2 = Only in inventory, 3 = Managed by integration
	 * (should this be an enum instead at this point?)
	 */
	public static int showButtons = 1;
	public CursorType lastCursorType = null;

	@Override
	public void init() {
		// when using REI >= 6, disable the regular sidebar buttons,
		// we'll be using REI's system favourites instead.
		if (Platform.isModLoaded("roughlyenoughitems") && !Platform.getMod("roughlyenoughitems").getVersion().startsWith("5.")) {
			showButtons = 3;
		}

		// Datagens hahayes
		if (Minecraft.getInstance() == null) {
			return;
		}

		ClientTextureStitchEvent.PRE.register(this::textureStitch);
		ClientGuiEvent.INIT_POST.register(this::guiInit);
		ClientGuiEvent.RENDER_PRE.register(this::renderTick);
		ClientTickEvent.CLIENT_POST.register(this::clientTick);

		ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, SidebarButtonManager.INSTANCE);
	}

	private void textureStitch(TextureAtlas atlas, Consumer<ResourceLocation> addSprite) {
		if (!atlas.location().equals(TextureAtlas.LOCATION_BLOCKS)) {
			return;
		}

		try {
			for (Field field : Icons.class.getDeclaredFields()) {
				field.setAccessible(true);
				Object o = field.get(null);

				if (o instanceof AtlasSpriteIcon) {
					AtlasSpriteIcon a = (AtlasSpriteIcon) o;
					addSprite.accept(a.id);
					IconPresets.MAP.put(a.id.toString(), a);
				}
			}
		} catch (Exception ignored) {
		}
	}

	private EventResult renderTick(Screen screen, PoseStack matrices, int mouseX, int mouseY, float delta) {
		if (!ICON_RENDERERS.isEmpty()) {
			for (IconRenderer<?> iconRenderer : ICON_RENDERERS) {
				iconRenderer.render();
			}
		}

		return EventResult.pass();
	}

	@SuppressWarnings("rawtypes")
	private void guiInit(Screen screen, ScreenAccess access) {
		if (areButtonsVisible(screen)) {
			SidebarGroupGuiButton group = new SidebarGroupGuiButton((AbstractContainerScreen) screen);
			access.addRenderableWidget(group);
		}
	}

	private void clientTick(Minecraft client) {
		CursorType t = client.screen instanceof IScreenWrapper ? ((IScreenWrapper) client.screen).getGui().getCursor() : null;

		if (lastCursorType != t) {
			lastCursorType = t;
			CursorType.set(t);
		}

		if (!ClientUtils.RUN_LATER.isEmpty()) {
			for (Runnable runnable : new ArrayList<>(ClientUtils.RUN_LATER)) {
				runnable.run();
			}

			ClientUtils.RUN_LATER.clear();
		}
	}

	public static boolean areButtonsVisible(@Nullable Screen gui) {
		if (showButtons == 0 || showButtons == 2 && !(gui instanceof EffectRenderingInventoryScreen) || showButtons == 3) {
			return false;
		}

		return gui instanceof AbstractContainerScreen && !SidebarButtonManager.INSTANCE.groups.isEmpty();
	}

	@Override
	public void testScreen() {
		ConfigGroup group = new ConfigGroup("test");
		group.add("image", new ImageConfig(), "", v -> {
		}, "");

		new EditConfigScreen(group).openGuiLater();
	}
}
