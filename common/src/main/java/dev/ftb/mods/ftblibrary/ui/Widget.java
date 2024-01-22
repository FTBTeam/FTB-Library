package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.platform.Window;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class Widget implements IScreenWrapper {
	protected final Panel parent;

	public int posX, posY, width, height;
	protected boolean isMouseOver;

	public Widget(Panel p) {
		parent = p;
	}

	public Panel getParent() {
		return parent;
	}

	@Override
	public BaseScreen getGui() {
		return parent.getGui();
	}

	public void setX(int v) {
		posX = v;
	}

	public void setY(int v) {
		posY = v;
	}

	public void setWidth(int v) {
		width = Math.max(v, 0);
	}

	public void setHeight(int v) {
		height = Math.max(v, 0);
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public final void setPos(int x, int y) {
		setX(x);
		setY(y);
	}

	public final void setSize(int w, int h) {
		setWidth(w);
		setHeight(h);
	}

	public final Widget setPosAndSize(int x, int y, int w, int h) {
		setX(x);
		setY(y);
		setWidth(w);
		setHeight(h);
		return this;
	}

	public int getX() {
		return parent.getX() + posX;
	}

	public int getY() {
		return parent.getY() + posY;
	}

	public boolean collidesWith(int x, int y, int w, int h) {
		var ay = getY();
		if (ay >= y + h || ay + height <= y) {
			return false;
		}

		var ax = getX();
		return ax < x + w && ax + width > x;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean shouldDraw() {
		return true;
	}

	public Component getTitle() {
		return Component.empty();
	}

	public WidgetType getWidgetType() {
		return WidgetType.mouseOver(isMouseOver());
	}

	public void addMouseOverText(TooltipList list) {
		var title = getTitle();

		if (title.getContents() != ComponentContents.EMPTY) {
			list.add(title);
		}
	}

	public final boolean isMouseOver() {
		return isMouseOver;
	}

	public boolean checkMouseOver(int mouseX, int mouseY) {
		if (parent == null) {
			return true;
		} else if (!parent.isMouseOver()) {
			return false;
		}

		var ax = getX();
		var ay = getY();
		return mouseX >= ax && mouseY >= ay && mouseX < ax + width && mouseY < ay + height;
	}

	public void updateMouseOver(int mouseX, int mouseY) {
		isMouseOver = checkMouseOver(mouseX, mouseY);
	}

	public boolean shouldAddMouseOverText() {
		return isEnabled() && isMouseOver();
	}

	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
	}

	public boolean mousePressed(MouseButton button) {
		return false;
	}

	public boolean mouseDoubleClicked(MouseButton button) {
		return false;
	}

	public void mouseReleased(MouseButton button) {
	}

	public boolean mouseScrolled(double scroll) {
		return false;
	}

	public boolean mouseDragged(int button, double dragX, double dragY) {
		return false;
	}

	public boolean keyPressed(Key key) {
		return false;
	}

	public void keyReleased(Key key) {
	}

	public boolean charTyped(char c, KeyModifiers modifiers) {
		return false;
	}

	public Window getScreen() {
		return parent.getScreen();
	}

	public int getMouseX() {
		return parent.getMouseX();
	}

	public int getMouseY() {
		return parent.getMouseY();
	}

	public float getPartialTicks() {
		return parent.getPartialTicks();
	}

	public boolean handleClick(String scheme, String path) {
		return parent.handleClick(scheme, path);
	}

	public final boolean handleClick(String click) {
		var index = click.indexOf(':');

		if (index == -1) {
			return handleClick("", click);
		}

		return handleClick(click.substring(0, index), click.substring(index + 1));
	}

	public void onClosed() {
	}

	public Optional<PositionedIngredient> getIngredientUnderMouse() {
		return Optional.empty();
	}

	public boolean isGhostIngredientTarget(Object ingredient) {
		return false;
	}

	public void acceptGhostIngredient(Object ingredient) {
	}

	public static boolean isMouseButtonDown(MouseButton button) {
		return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), button.id) == GLFW.GLFW_PRESS;
	}

	public static boolean isKeyDown(int key) {
		return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), key) == GLFW.GLFW_PRESS;
	}

	public static String getClipboardString() {
		return Minecraft.getInstance().keyboardHandler.getClipboard();
	}

	public static void setClipboardString(String string) {
		Minecraft.getInstance().keyboardHandler.setClipboard(string);
	}

	public static boolean isShiftKeyDown() {
		return Screen.hasShiftDown();
	}

	public static boolean isCtrlKeyDown() {
		return Screen.hasControlDown();
	}

	public void tick() {
	}

	public String toString() {
		var s = getClass().getSimpleName();

		if (s.isEmpty()) {
			s = getClass().getSuperclass().getSimpleName();
		}

		return s;
	}

	public void playClickSound() {
		GuiHelper.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1F);
	}

	@Nullable
	public CursorType getCursor() {
		return null;
	}
}
