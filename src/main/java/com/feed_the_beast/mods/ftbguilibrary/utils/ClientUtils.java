package com.feed_the_beast.mods.ftbguilibrary.utils;

import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.IGuiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ClientUtils
{
	public static final BooleanSupplier IS_CLIENT_OP = () -> Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasPermissionLevel(1);
	public static final List<Runnable> RUN_LATER = new ArrayList<>();

	private static float lastBrightnessX, lastBrightnessY;
	private static Boolean hasJavaFX = null;

	public static DimensionType getDim()
	{
		return Minecraft.getInstance().world != null ? Minecraft.getInstance().world.getDimension().getType() : DimensionType.OVERWORLD;
	}

	public static void pushBrightness(int u, int t)
	{
		//lastBrightnessX = OpenGlHelper.lastBrightnessX;
		//lastBrightnessY = OpenGlHelper.lastBrightnessY;
		//FIXME: OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, u, t);
	}

	public static void pushMaxBrightness()
	{
		pushBrightness(240, 240);
	}

	public static void popBrightness()
	{
		//FIXME: OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
	}

	public static void execClientCommand(String command, boolean printChat)
	{
		if (printChat)
		{
			Minecraft.getInstance().ingameGUI.getChatGUI().addToSentMessages(command);
		}

		//FIXME: if (ClientCommandHandler.instance.executeCommand(Minecraft.getInstance().player, command) == 0)
		{
			Minecraft.getInstance().player.sendChatMessage(command);
		}
	}

	public static void execClientCommand(String command)
	{
		execClientCommand(command, false);
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
}