package dev.ftb.mods.ftblibrary;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonManager;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import dev.ftb.mods.ftblibrary.ui.CursorType;
import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class FTBLibraryClient {

	public static CursorType lastCursorType = null;

	public static void init() {
		FTBLibraryClientConfig.load();

		// Datagens hahayes
		if (Minecraft.getInstance() == null) {
			return;
		}

		ClientGuiEvent.INIT_POST.register(FTBLibraryClient::guiInit);
		ClientTickEvent.CLIENT_POST.register(FTBLibraryClient::clientTick);

		ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, SidebarButtonManager.INSTANCE);
		ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, SelectImageResourceScreen.ResourceListener.INSTANCE);
	}

	private static void guiInit(Screen screen, ScreenAccess access) {
		if (areButtonsVisible(screen)) {
			access.addRenderableWidget(new SidebarGroupGuiButton());
		}
	}

	private static void clientTick(Minecraft client) {
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
		if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) {
			return false;
		}

		if(!FTBLibraryClientConfig.SIDEBAR_ENABLED.get()) {
			return false;
		}

		return gui instanceof AbstractContainerScreen && !SidebarButtonManager.INSTANCE.getButtons().isEmpty();
	}

	public static void editConfig(boolean isClientConfig) {
		// NOTE: only client config supported right now
		new EditConfigScreen(FTBLibraryClientConfig.getConfigGroup()).setAutoclose(true).openGui();
	}
}
