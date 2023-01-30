package dev.ftb.mods.ftblibrary;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ImageConfig;
import dev.ftb.mods.ftblibrary.config.IntConfig;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonManager;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import dev.ftb.mods.ftblibrary.ui.CursorType;
import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.ui.misc.SelectImageScreen;
import dev.ftb.mods.ftblibrary.util.ClientUtils;
import me.shedaniel.rei.api.client.config.ConfigObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class FTBLibraryClient extends FTBLibraryCommon {
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
		if (Platform.isModLoaded("roughlyenoughitems")) {
			showButtons = 3;
		}

		// Datagens hahayes
		if (Minecraft.getInstance() == null) {
			return;
		}

//		ClientTextureStitchEvent.PRE.register(this::textureStitch);
		ClientGuiEvent.INIT_POST.register(this::guiInit);
		ClientTickEvent.CLIENT_POST.register(this::clientTick);

		ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, SidebarButtonManager.INSTANCE);
		ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, SelectImageScreen.ResourceListener.INSTANCE);
	}

//	private void textureStitch(TextureAtlas atlas, Consumer<ResourceLocation> addSprite) {
//		if (!atlas.location().equals(InventoryMenu.BLOCK_ATLAS)) {
//			return;
//		}
//
//		try {
//			for (var field : Icons.class.getDeclaredFields()) {
//				field.setAccessible(true);
//				var o = field.get(null);
//
//				if (o instanceof AtlasSpriteIcon a) {
//					addSprite.accept(a.id);
//					IconPresets.MAP.put(a.id.toString(), a);
//				}
//			}
//		} catch (Exception ignored) {
//		}
//	}

	private void guiInit(Screen screen, ScreenAccess access) {
		if (areButtonsVisible(screen)) {
			access.addRenderableWidget(new SidebarGroupGuiButton());
		}
	}

	private void clientTick(Minecraft client) {
		var t = client.screen instanceof IScreenWrapper ? ((IScreenWrapper) client.screen).getGui().getCursor() : null;

		if (lastCursorType != t) {
			lastCursorType = t;
			CursorType.set(t);
		}

		if (!ClientUtils.RUN_LATER.isEmpty()) {
			for (var runnable : new ArrayList<>(ClientUtils.RUN_LATER)) {
				runnable.run();
			}

			ClientUtils.RUN_LATER.clear();
		}
	}

	public static boolean areButtonsVisible(@Nullable Screen gui) {
		if (showButtons == 0 || showButtons == 2 && !(gui instanceof EffectRenderingInventoryScreen)) {
			return false;
		}

		if(showButtons == 3 && Platform.isModLoaded("roughlyenoughitems")) {
			if(ConfigObject.getInstance().isFavoritesEnabled()) {
				return false;
			}
		}

		return gui instanceof AbstractContainerScreen && !SidebarButtonManager.INSTANCE.getGroups().isEmpty();
	}

	@Override
	public void testScreen() {
		var group = new ConfigGroup("test");
		group.add("image", new ImageConfig(), "", v -> { }, "");

		group.addItemStack("item", ItemStack.EMPTY, v -> { }, ItemStack.EMPTY, false, true);

		ConfigGroup grp1 = group.getOrCreateSubgroup("group1");
		grp1.addInt("integer", 1, v -> {}, 0, 0, 10);
		grp1.addBool("bool", true, v -> {}, false);

		ConfigGroup grp2 = grp1.getOrCreateSubgroup("subgroup1");
		grp2.addEnum("enum", Direction.UP, v -> {}, NameMap.of(Direction.UP, Direction.values()).create());
		List<Integer> integers = new ArrayList<>(List.of(1, 2, 3, 4));
		grp2.addList("list", integers, new IntConfig(0, 10), 1);

		new EditConfigScreen(group).openGuiLater();
	}
}
