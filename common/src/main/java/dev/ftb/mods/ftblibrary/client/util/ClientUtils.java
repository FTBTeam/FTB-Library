package dev.ftb.mods.ftblibrary.client.util;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.api.event.client.AllowChatCommandEvent;
import dev.ftb.mods.ftblibrary.api.event.client.CustomClickEvent;
import dev.ftb.mods.ftblibrary.client.gui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.platform.fluid.FluidStack;
import net.minecraft.IdentifierException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class ClientUtils {
    public static final BooleanSupplier IS_CLIENT_OP = () ->
            Minecraft.getInstance().player != null && Minecraft.getInstance().player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);

    public static final List<Runnable> RUN_LATER = new ArrayList<>();

    public static void execClientCommand(String command, boolean printChat) {
        if (!command.isEmpty() && Minecraft.getInstance().player != null) {
            if (AllowChatCommandEvent.TYPE.post(new AllowChatCommandEvent.Data(command))) {
                if (printChat) {
                    Minecraft.getInstance().gui.getChat().addRecentChat(command);
                }
                Minecraft.getInstance().player.connection.sendCommand(command.replace("/", ""));
            }
        }
    }

    public static void runLater(final Runnable runnable) {
        RUN_LATER.add(runnable);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getGuiAs(Screen gui, Class<T> clazz) {
        if (gui instanceof IScreenWrapper wrapper) {
            var guiBase = wrapper.getGui();
            if (clazz.isAssignableFrom(guiBase.getClass())) {
                return (T) guiBase;
            }
        }

        return clazz.isAssignableFrom(gui.getClass()) ? (T) Minecraft.getInstance().screen : null;
    }

    @Nullable
    public static <T> T getCurrentGuiAs(Class<T> clazz) {
        return Minecraft.getInstance().screen == null ? null : getGuiAs(Minecraft.getInstance().screen, clazz);
    }

    public static boolean handleClick(String scheme, String path) {
        switch (scheme) {
            case "http", "https" -> {
                String uriStr = scheme + ':' + path;
                try {
                    final var uri = new URI(uriStr);
                    if (Minecraft.getInstance().options.chatLinksPrompt().get()) {
                        final var currentScreen = Minecraft.getInstance().screen;
                        Minecraft.getInstance().setScreen(new ConfirmLinkScreen(accepted -> {
                            if (accepted) {
                                Util.getPlatform().openUri(uri);
                            }
                            Minecraft.getInstance().setScreen(currentScreen);
                        }, uriStr, false));
                    } else {
                        Util.getPlatform().openUri(uri);
                    }

                    return true;
                } catch (Exception ex) {
                    logHandleClickFailure(scheme, uriStr, ex);
                    FTBLibrary.LOGGER.warn("handleClick: unexpected exception handling http/https action {}: {}", uriStr, ex.getMessage());
                    return false;
                }
            }
            case "file" -> {
                try {
                    Util.getPlatform().openUri(new URI("file:" + path));
                    return true;
                } catch (Exception ex) {
                    logHandleClickFailure(scheme, path, ex);
                    return false;
                }
            }
            case "command" -> {
                execClientCommand(path, false);
                return true;
            }
            case "custom" -> {
                return trySendCustomClickEvent(path);
            }
            default -> {
                return trySendCustomClickEvent(scheme + ":" + path);
            }
        }
    }

    private static boolean trySendCustomClickEvent(String name) {
        try {
            return CustomClickEvent.TYPE.post(new CustomClickEvent.Data(Identifier.parse(name)));
        } catch (IdentifierException ex) {
            logHandleClickFailure("custom", name, ex);
            return false;
        }
    }

    private static void logHandleClickFailure(String scheme, String path, Throwable ex) {
        FTBLibrary.LOGGER.warn("handleClick: unexpected exception handling action {} / {}: {}", scheme, path, ex.getMessage());
    }

    public static HolderLookup.Provider registryAccess() {
        return Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();
    }

    public static TextureAtlasSprite getStillTexture(Fluid fluid) {
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState())
                .stillMaterial()
                .sprite();
    }

    public static TextureAtlasSprite getStillTexture(FluidStack stack) {
        return getStillTexture(stack.fluid());
    }

    public static int getFluidColor(FluidStack stack) {
        return getFluidColor(stack, null, null);
    }

    public static int getFluidColor(FluidStack stack, @Nullable Level level, @Nullable BlockPos pos) {
        return getFluidColor(stack.fluid(), level, pos);
    }

    public static int getFluidColor(Fluid fluid, @Nullable Level level, @Nullable BlockPos pos) {
        FluidState state = fluid.defaultFluidState();
        FluidModel fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state);
        if (fluidModel.tintSource() == null) {
            return 0xFFFFFFFF;
        }

        if (fluid.isSame(Fluids.WATER)) {
            if (level != null && pos != null) {
                return level.getBiome(pos).value().getWaterColor();
            }

            return 0x3F76E4; // default water color, used when not in world or biome is missing
        }

        return fluidModel.tintSource().color(state.createLegacyBlock());
    }

    public static Level getClientLevel() {
        return Objects.requireNonNull(Minecraft.getInstance().level);
    }

    public static Player getClientPlayer() {
        return Objects.requireNonNull(Minecraft.getInstance().player);
    }

    public static Optional<Player> getOptionalClientPlayer() {
        return Optional.ofNullable(Minecraft.getInstance().player);
    }

    public static String getCurrentLanguageCode() {
        return Minecraft.getInstance().options.languageCode;
    }
}
