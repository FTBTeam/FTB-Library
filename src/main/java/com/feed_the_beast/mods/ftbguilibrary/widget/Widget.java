package com.feed_the_beast.mods.ftbguilibrary.widget;

import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.KeyModifiers;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

public class Widget implements IGuiWrapper
{
	public Panel parent;
	public int posX, posY, width, height;
	protected boolean isMouseOver;

	public Widget(Panel p)
	{
		parent = p;
	}

	@Override
	public GuiBase getGui()
	{
		return parent.getGui();
	}

	public void setX(int v)
	{
		posX = v;
	}

	public void setY(int v)
	{
		posY = v;
	}

	public void setWidth(int v)
	{
		width = Math.max(v, 0);
	}

	public void setHeight(int v)
	{
		height = Math.max(v, 0);
	}

	public final void setPos(int x, int y)
	{
		setX(x);
		setY(y);
	}

	public final void setSize(int w, int h)
	{
		setWidth(w);
		setHeight(h);
	}

	public final Widget setPosAndSize(int x, int y, int w, int h)
	{
		setX(x);
		setY(y);
		setWidth(w);
		setHeight(h);
		return this;
	}

	public int getX()
	{
		return parent.getX() + posX;
	}

	public int getY()
	{
		return parent.getY() + posY;
	}

	public boolean collidesWith(int x, int y, int w, int h)
	{
		int ay = getY();
		if (ay >= y + h || ay + height <= y)
		{
			return false;
		}

		int ax = getX();
		return ax < x + w && ax + width > x;
	}

	public boolean isEnabled()
	{
		return true;
	}

	public boolean shouldDraw()
	{
		return true;
	}

	public ITextComponent getTitle()
	{
		return StringTextComponent.EMPTY;
	}

	public WidgetType getWidgetType()
	{
		return WidgetType.mouseOver(isMouseOver());
	}

	public void addMouseOverText(TooltipList list)
	{
		ITextComponent title = getTitle();

		if (title != StringTextComponent.EMPTY)
		{
			list.add(title);
		}
	}

	public final boolean isMouseOver()
	{
		return isMouseOver;
	}

	public boolean checkMouseOver(int mouseX, int mouseY)
	{
		if (parent == null)
		{
			return true;
		}
		else if (!parent.isMouseOver())
		{
			return false;
		}

		int ax = getX();
		int ay = getY();
		return mouseX >= ax && mouseY >= ay && mouseX < ax + width && mouseY < ay + height;
	}

	public void updateMouseOver(int mouseX, int mouseY)
	{
		isMouseOver = checkMouseOver(mouseX, mouseY);
	}

	public boolean shouldAddMouseOverText()
	{
		return isEnabled() && isMouseOver();
	}

	public void draw(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
	}

	public boolean mousePressed(MouseButton button)
	{
		return false;
	}

	public void mouseReleased(MouseButton button)
	{
	}

	public boolean mouseScrolled(double scroll)
	{
		return false;
	}

	public boolean keyPressed(Key key)
	{
		return false;
	}

	public void keyReleased(Key key)
	{
	}

	public boolean charTyped(char c, KeyModifiers modifiers)
	{
		return false;
	}

	public MainWindow getScreen()
	{
		return parent.getScreen();
	}

	public int getMouseX()
	{
		return parent.getMouseX();
	}

	public int getMouseY()
	{
		return parent.getMouseY();
	}

	public float getPartialTicks()
	{
		return parent.getPartialTicks();
	}

	public boolean handleClick(String scheme, String path)
	{
		return parent.handleClick(scheme, path);
	}

	public final boolean handleClick(String click)
	{
		int index = click.indexOf(':');

		if (index == -1)
		{
			return handleClick("", click);
		}

		return handleClick(click.substring(0, index), click.substring(index + 1));
	}

	public void onClosed()
	{
	}

	@Nullable
	public Object getIngredientUnderMouse()
	{
		return null;
	}

	public boolean isGhostIngredientTarget(Object ingredient)
	{
		return false;
	}

	public void acceptGhostIngredient(Object ingredient)
	{
	}

	public static boolean isMouseButtonDown(MouseButton button)
	{
		return GLFW.glfwGetMouseButton(Minecraft.getInstance().getMainWindow().getHandle(), button.id) == GLFW.GLFW_PRESS;
	}

	public static boolean isKeyDown(int key)
	{
		return GLFW.glfwGetKey(Minecraft.getInstance().getMainWindow().getHandle(), key) == GLFW.GLFW_PRESS;
	}

	public static String getClipboardString()
	{
		return Minecraft.getInstance().keyboardListener.getClipboardString();
	}

	public static void setClipboardString(String string)
	{
		Minecraft.getInstance().keyboardListener.setClipboardString(string);
	}

	public static boolean isShiftKeyDown()
	{
		return Screen.hasShiftDown();
	}

	public static boolean isCtrlKeyDown()
	{
		return Screen.hasControlDown();
	}

	public void tick()
	{
	}

	public String toString()
	{
		String s = getClass().getSimpleName();

		if (s.isEmpty())
		{
			s = getClass().getSuperclass().getSimpleName();
		}

		return s;
	}

	public void playClickSound()
	{
		GuiHelper.playSound(SoundEvents.UI_BUTTON_CLICK, 1F);
	}
}