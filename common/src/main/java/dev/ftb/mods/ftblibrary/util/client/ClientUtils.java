package dev.ftb.mods.ftblibrary.util.client;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientChatEvent;
import dev.architectury.fluid.FluidStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.ui.CustomClickEvent;
import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URI;
import java.util.*;
import java.util.function.BooleanSupplier;

public class ClientUtils {
	public static final BooleanSupplier IS_CLIENT_OP = () -> Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasPermissions(1);
	public static final List<Runnable> RUN_LATER = new ArrayList<>();
	private static final MethodType EMPTY_METHOD_TYPE = MethodType.methodType(void.class);
	private static final HashMap<String, Optional<MethodHandle>> staticMethodCache = new HashMap<>();

	public static void execClientCommand(String command, boolean printChat) {
		if (!command.isEmpty() && Minecraft.getInstance().player != null) {
			EventResult res = ClientChatEvent.SEND.invoker().send(command, null);

			if (!res.interruptsFurtherEvaluation()) {
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
		if (gui instanceof IScreenWrapper) {
			var guiBase = ((IScreenWrapper) gui).getGui();

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
			case "http":
			case "https": {
				try {
					final var uri = new URI(scheme + ':' + path);
					if (Minecraft.getInstance().options.chatLinksPrompt().get()) {
						final var currentScreen = Minecraft.getInstance().screen;

						Minecraft.getInstance().setScreen(new ConfirmLinkScreen(result ->
						{
							if (result) {
								try {
									Util.getPlatform().openUri(uri);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
							Minecraft.getInstance().setScreen(currentScreen);
						}, scheme + ':' + path, false));
					} else {
						Util.getPlatform().openUri(uri);
					}

					return true;
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				return false;
			}
			case "file": {
				try {
					Util.getPlatform().openUri(new URI("file:" + path));
					return true;
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				return false;
			}
			case "command": {
				ClientUtils.execClientCommand(path, false);
				return true;
			}
			case "static_method": {
				var handle = staticMethodCache.get(path);

				if (handle == null) {
					handle = Optional.empty();
					var s = path.split(":", 2);

					try {
						Class<?> c = Class.forName(s[0]);
						var h = MethodHandles.publicLookup().findStatic(c, s[1], EMPTY_METHOD_TYPE);
						handle = Optional.ofNullable(h);
					} catch (Throwable ex) {
						ex.printStackTrace();
					}

					staticMethodCache.put(path, handle);
				}

				if (handle.isPresent()) {
					try {
						handle.get().invoke();
						return true;
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
				}

				return false;
			}
			case "custom":
				return ResourceLocation.read(path).result()
						.map(rl -> CustomClickEvent.EVENT.invoker().act(new CustomClickEvent(rl)).isPresent())
						.orElse(false);
			default:
				boolean res = ResourceLocation.read(scheme + ":" + path).result()
						.map(rl -> CustomClickEvent.EVENT.invoker().act(new CustomClickEvent(rl)).isPresent())
						.orElse(false);
				if (!res) {
					FTBLibrary.LOGGER.warn("invalid scheme/path resourcelocation for handleClick(): {}:{}", scheme, path);
				}
				return res;
		}
	}

	public static HolderLookup.Provider registryAccess() {
		return Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();
	}

	@ExpectPlatform
	public static ResourceLocation getStillTexture(FluidStack stack) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static int getFluidColor(FluidStack stack) {
		throw new AssertionError();
	}

}
