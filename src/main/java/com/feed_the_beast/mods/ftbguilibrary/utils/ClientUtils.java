package com.feed_the_beast.mods.ftbguilibrary.utils;

import com.feed_the_beast.mods.ftbguilibrary.widget.CustomClickEvent;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.IGuiWrapper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL13;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class ClientUtils
{
	public static final BooleanSupplier IS_CLIENT_OP = () -> Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasPermissionLevel(1);
	public static final List<Runnable> RUN_LATER = new ArrayList<>();
	private static final MethodType EMPTY_METHOD_TYPE = MethodType.methodType(void.class);
	private static final HashMap<String, Optional<MethodHandle>> staticMethodCache = new HashMap<>();

	private static float lastBrightnessX, lastBrightnessY;
	private static Boolean hasJavaFX = null;

	public static void pushBrightness(float u, float t)
	{
		lastBrightnessX = GlStateManager.lastBrightnessX;
		lastBrightnessY = GlStateManager.lastBrightnessY;
		RenderSystem.glMultiTexCoord2f(GL13.GL_TEXTURE1, u, t);
	}

	public static void pushMaxBrightness()
	{
		pushBrightness(240F, 240F);
	}

	public static void popBrightness()
	{
		RenderSystem.glMultiTexCoord2f(GL13.GL_TEXTURE1, lastBrightnessX, lastBrightnessY);
	}

	public static void execClientCommand(String command, boolean printChat)
	{
		command = net.minecraftforge.event.ForgeEventFactory.onClientSendMessage(command);

		if (command.isEmpty())
		{
			return;
		}

		if (printChat)
		{
			Minecraft.getInstance().ingameGUI.getChatGUI().addToSentMessages(command);
		}

		Minecraft.getInstance().player.sendChatMessage(command);
	}

	public static void runLater(final Runnable runnable)
	{
		RUN_LATER.add(runnable);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T getGuiAs(Screen gui, Class<T> clazz)
	{
		if (gui instanceof IGuiWrapper)
		{
			GuiBase guiBase = ((IGuiWrapper) gui).getGui();

			if (clazz.isAssignableFrom(guiBase.getClass()))
			{
				return (T) guiBase;
			}
		}

		return clazz.isAssignableFrom(gui.getClass()) ? (T) Minecraft.getInstance().currentScreen : null;
	}

	@Nullable
	public static <T> T getCurrentGuiAs(Class<T> clazz)
	{
		return Minecraft.getInstance().currentScreen == null ? null : getGuiAs(Minecraft.getInstance().currentScreen, clazz);
	}

	public static boolean hasJavaFX()
	{
		if (hasJavaFX == null)
		{
			try
			{
				Class.forName("javafx.scene.image.Image");
				hasJavaFX = true;
			}
			catch (Exception ex)
			{
				hasJavaFX = false;
			}
		}

		return hasJavaFX;
	}

	public static boolean handleClick(String scheme, String path)
	{
		switch (scheme)
		{
			case "http":
			case "https":
			{
				try
				{
					final URI uri = new URI(scheme + ':' + path);
					if (Minecraft.getInstance().gameSettings.chatLinksPrompt)
					{
						final Screen currentScreen = Minecraft.getInstance().currentScreen;

						Minecraft.getInstance().displayGuiScreen(new ConfirmOpenLinkScreen(result ->
						{
							if (result)
							{
								try
								{
									Util.getOSType().openURI(uri);
								}
								catch (Exception ex)
								{
									ex.printStackTrace();
								}
							}
							Minecraft.getInstance().displayGuiScreen(currentScreen);
						}, scheme + ':' + path, false));
					}
					else
					{
						Util.getOSType().openURI(uri);
					}

					return true;
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}

				return false;
			}
			case "file":
			{
				try
				{
					Util.getOSType().openURI(new URI("file:" + path));
					return true;
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}

				return false;
			}
			case "command":
			{
				ClientUtils.execClientCommand(path, false);
				return true;
			}
			case "static_method":
			{
				Optional<MethodHandle> handle = staticMethodCache.get(path);

				if (handle == null)
				{
					handle = Optional.empty();
					String[] s = path.split(":", 2);

					try
					{
						Class c = Class.forName(s[0]);
						MethodHandle h = MethodHandles.publicLookup().findStatic(c, s[1], EMPTY_METHOD_TYPE);
						handle = Optional.ofNullable(h);
					}
					catch (Throwable ex)
					{
						ex.printStackTrace();
					}

					staticMethodCache.put(path, handle);
				}

				if (handle.isPresent())
				{
					try
					{
						handle.get().invoke();
						return true;
					}
					catch (Throwable ex)
					{
						ex.printStackTrace();
					}
				}

				return false;
			}
			case "custom":
				return MinecraftForge.EVENT_BUS.post(new CustomClickEvent(new ResourceLocation(path)));
			default:
				return MinecraftForge.EVENT_BUS.post(new CustomClickEvent(new ResourceLocation(scheme, path)));
		}
	}
}