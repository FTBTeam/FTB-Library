package dev.ftb.mods.ftblibrary;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.ftb.mods.ftblibrary.api.client.FTBLibraryClientApi;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.client.gui.CursorType;
import dev.ftb.mods.ftblibrary.client.gui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManagerClient;
import dev.ftb.mods.ftblibrary.icon.EntityIconLoader;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonManager;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.packs.PackType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

public class FTBLibraryClient {
    @Nullable
    public static CursorType lastCursorType = null;

    public static void onModConstruct() {
        ConfigManagerClient.registerEvents();

        ClientGuiEvent.INIT_POST.register(FTBLibraryClient::guiInit);
        ClientTickEvent.CLIENT_POST.register(FTBLibraryClient::clientTick);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(FTBLibraryClient::onPlayerLogout);

        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, SidebarButtonManager.INSTANCE, FTBLibrary.rl("sidebar"));
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, SelectImageResourceScreen.ResourceListener.INSTANCE, FTBLibrary.rl("image_select"));
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new EntityIconLoader(), FTBLibrary.rl("entity_icons"));
    }

    private static void guiInit(Screen screen, ScreenAccess access) {
        if (areButtonsVisible(screen)) {
            access.addRenderableWidget(new SidebarGroupGuiButton());
        }
    }

    private static void clientTick(Minecraft client) {
        var cursorType = client.screen instanceof IScreenWrapper w ? w.getGui().getCursor() : null;
        if (lastCursorType != cursorType) {
            lastCursorType = cursorType;
            CursorType.set(cursorType);
        }

        if (!ClientUtils.RUN_LATER.isEmpty()) {
            for (var runnable : new ArrayList<>(ClientUtils.RUN_LATER)) {
                runnable.run();
            }

            ClientUtils.RUN_LATER.clear();
        }
    }

    private static void onPlayerLogout(@Nullable LocalPlayer localPlayer) {
        KnownServerRegistries.client = null;
    }

    public static boolean areButtonsVisible(@Nullable Screen gui) {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) {
            return false;
        }

        if (!FTBLibraryClientConfig.SIDEBAR_ENABLED.get()) {
            return false;
        }

        return gui instanceof AbstractContainerScreen &&
                !SidebarButtonManager.INSTANCE.getButtons().isEmpty()
                && !FTBLibraryClientApi.get().isSidebarScreenBlacklisted(gui);
    }
}
