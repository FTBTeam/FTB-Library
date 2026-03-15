package dev.ftb.mods.ftblibrary.client;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.api.client.FTBLibraryClientApi;
import dev.ftb.mods.ftblibrary.api.event.client.RegisterCustomColorEvent;
import dev.ftb.mods.ftblibrary.client.gui.CursorType;
import dev.ftb.mods.ftblibrary.client.gui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManagerClient;
import dev.ftb.mods.ftblibrary.platform.event.EventPostingHandler;
import dev.ftb.mods.ftblibrary.sidebar.RegisteredSidebarButton;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonManager;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import dev.ftb.mods.ftblibrary.util.text.ExtendableTextColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FTBLibraryClient {
    public static final Identifier SIDEBAR_LISTENER = FTBLibrary.rl("sidebar");
    public static final Identifier IMAGE_SELECT_LISTENER = FTBLibrary.rl("image_select");
    public static final Identifier ENTITY_ICON_LISTENER = FTBLibrary.rl("entity_icons");

    public static final Identifier DAY_BUTTON = FTBLibrary.rl("toggle/day");
    public static final Identifier NIGHT_BUTTON = FTBLibrary.rl("toggle/night");

    @Nullable
    public static CursorType lastCursorType = null;

    public FTBLibraryClient() {
//        ClientGuiEvent.INIT_POST.register(FTBLibraryClient::guiInit);
//        ClientTickEvent.CLIENT_POST.register(FTBLibraryClient::clientTick);
//        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(FTBLibraryClient::onPlayerLogout);

//        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, SidebarButtonManager.INSTANCE, FTBLibrary.rl("sidebar"));
//        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, SelectImageResourceScreen.ResourceListener.INSTANCE, FTBLibrary.rl("image_select"));
//        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new EntityIconLoader(), FTBLibrary.rl("entity_icons"));
    }

    public void onClientStarted(Minecraft minecraft) {
        ConfigManagerClient.onClientStarted(minecraft);

        Map<String, TextColor> customColors = new HashMap<>();
        EventPostingHandler.INSTANCE.postEvent(new RegisterCustomColorEvent.Data(customColors));

        customColors.forEach(ExtendableTextColor::addCustomColor);
//        RegisterCustomColorEvent.EVENT.invoker().accept(new RegisterCustomColorEvent(customColors));
    }

    public void guiInit(Screen screen) {
        if (areButtonsVisible(screen)) {
            screen.addRenderableWidget(new SidebarGroupGuiButton());
        }
    }

    public void clientTick() {
        var client = Minecraft.getInstance();
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

    public void onPlayerLogout(@Nullable LocalPlayer localPlayer) {
        KnownServerRegistries.client = null;
    }

    public boolean areButtonsVisible(@Nullable Screen gui) {
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

    public void addVisibilityConditionToSidebarButton(RegisteredSidebarButton button) {
        var id = button.getId();
        if (id.equals(DAY_BUTTON) || id.equals(NIGHT_BUTTON)) {
            button.addVisibilityCondition(() -> {
                if (Minecraft.getInstance().level == null) {
                    return false;
                }

                var level = Minecraft.getInstance().level;
                var levelWorldKey = level.registryAccess().get(ResourceKey.create(Registries.WORLD_CLOCK, level.dimension().identifier()));
                return levelWorldKey.isPresent();
            });
        }
    }
}
