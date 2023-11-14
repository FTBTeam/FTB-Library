package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.platform.Window;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.LoadingScreen;
import dev.ftb.mods.ftblibrary.util.BooleanConsumer;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;


public abstract class BaseScreen extends Panel {
	public static class PositionedTextData {
		public final int posX, posY;
		public final int width, height;
		public final ClickEvent clickEvent;
		public final HoverEvent hoverEvent;
		public final String insertion;

		public PositionedTextData(int x, int y, int w, int h, Style s) {
			posX = x;
			posY = y;
			width = w;
			height = h;
			clickEvent = s.getClickEvent();
			hoverEvent = s.getHoverEvent();
			insertion = s.getInsertion();
		}
	}

	private final Screen prevScreen;
	private int mouseX, mouseY;
	private float partialTicks;
	private boolean refreshWidgets;
	private Window screen;
	private Panel contextMenu = null;
//	public ItemRenderer itemRenderer;
	private long lastClickTime = 0L;

	public BaseScreen() {
		super(null);
		setSize(176, 166);
		setOnlyRenderWidgetsInside(false);
		setOnlyInteractWithWidgetsInside(false);
		prevScreen = Minecraft.getInstance().screen;
	}

	@Override
	public final BaseScreen getGui() {
		return this;
	}

	@Override
	public void alignWidgets() {
	}

	public final void initGui() {
		if (parent instanceof BaseScreen) {
			screen = parent.getScreen();
		} else {
			screen = Minecraft.getInstance().getWindow();
		}

		if (onInit()) {
			super.refreshWidgets();
			alignWidgets();
			onPostInit();
		}
	}

	public Theme getTheme() {
		return Theme.DEFAULT;
	}

	@Override
	public int getX() {
		return (getScreen().getGuiScaledWidth() - width) / 2;
	}

	@Override
	public int getY() {
		return (getScreen().getGuiScaledHeight() - height) / 2;
	}

	@Override
	public void setScrollX(double scroll) {
	}

	@Override
	public void setScrollY(double scroll) {
	}

	@Override
	public double getScrollX() {
		return 0;
	}

	@Override
	public double getScrollY() {
		return 0;
	}

	public boolean onInit() {
		return true;
	}

	protected boolean setFullscreen() {
		return setSizeProportional(1f, 1f);
	}

	protected boolean setSizeProportional(float w, float h) {
		Validate.isTrue(w > 0f && w <= 1f && h > 0f && h <= 1f, "width and height must be > 0 and <= 1");

		if (screen == null) {
			return false;
		} else {
			setWidth((int) (screen.getGuiScaledWidth() * w));
			setHeight((int) (screen.getGuiScaledHeight() * h));
			return true;
		}
	}

	public void onPostInit() {
	}

	@Nullable
	public Screen getPrevScreen() {
		if (prevScreen instanceof ScreenWrapper && ((ScreenWrapper) prevScreen).getGui() instanceof LoadingScreen) {
			return ((ScreenWrapper) prevScreen).getGui().getPrevScreen();
		} else if (prevScreen instanceof ChatScreen) {
			return null;
		}

		return prevScreen;
	}

	@Override
	public final void closeGui(boolean openPrevScreen) {
		var mx = Minecraft.getInstance().mouseHandler.xpos();
		var my = Minecraft.getInstance().mouseHandler.ypos();

		var mc = Minecraft.getInstance();

		if (mc.player != null) {
			mc.player.closeContainer();

			if (mc.screen == null) {
				mc.setWindowActive(true);
			}
		}

		if (openPrevScreen && getPrevScreen() != null) {
			mc.setScreen(getPrevScreen());
			GLFW.glfwSetCursorPos(getScreen().getWindow(), mx, my);
		}

		onClosed();
	}

	public boolean onClosedByKey(Key key) {
		return key.escOrInventory();
	}

	public void onBack() {
		closeGui(true);
	}

	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public final void refreshWidgets() {
		refreshWidgets = true;
	}

	public final void updateGui(int mx, int my, float pt) {
		mouseX = mx;
		mouseY = my;
		partialTicks = pt;

		if (refreshWidgets) {
			super.refreshWidgets();
			refreshWidgets = false;
		}

		posX = getX();
		posY = getY();

		if (contextMenu != null) {
			if (contextMenu instanceof BaseScreen) {
				((BaseScreen) contextMenu).updateGui(mx, my, pt);
			} else {
				contextMenu.updateMouseOver(mouseX, mouseY);
			}
		}

		updateMouseOver(mouseX, mouseY);
	}

	@Override
	public void updateMouseOver(int mouseX, int mouseY) {
		isMouseOver = checkMouseOver(mouseX, mouseY);
		setOffset(true);

		if (contextMenu != null) {
			contextMenu.updateMouseOver(mouseX, mouseY);
		} else {
			for (var widget : widgets) {
				widget.updateMouseOver(mouseX, mouseY);
			}
		}

		setOffset(false);
	}

	@Override
	public final void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		super.draw(graphics, theme, x, y, w, h);
	}

	public Optional<Panel> getContextMenu() {
		return Optional.ofNullable(contextMenu);
	}

	public void openContextMenu(@Nullable ContextMenu newContextMenu) {
		int px = 0, py = 0;

		if (contextMenu != null) {
			px = contextMenu.posX;
			py = contextMenu.posY;
			contextMenu.onClosed();
			widgets.remove(contextMenu);
		}

		if (newContextMenu == null) {
			contextMenu = null;
			return;
		}

		var x = getX();
		var y = getY();

		if (contextMenu == null) {
			px = getMouseX() - x;
			py = getMouseY() - y;
		}

		contextMenu = newContextMenu;
		widgets.add(contextMenu);
		contextMenu.refreshWidgets();
		px = Math.min(px, screen.getGuiScaledWidth() - contextMenu.width - x) - 3;
		py = Math.min(py, screen.getGuiScaledHeight() - contextMenu.height - y) - 3;
		contextMenu.setPos(px, py);

		if (contextMenu instanceof BaseScreen b) {
			b.initGui();
		}
	}

	public ContextMenu openContextMenu(@NotNull List<ContextMenuItem> menu) {
		var contextMenu = new ContextMenu(this, menu);
		openContextMenu(contextMenu);
		return contextMenu;
	}

	@Override
	public void closeContextMenu() {
		openContextMenu((ContextMenu) null);
//		onInit();
	}

	@Override
	public void onClosed() {
		super.onClosed();
		closeContextMenu();
		CursorType.set(null);
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		theme.drawGui(graphics, x, y, w, h, WidgetType.NORMAL);
	}

	public boolean drawDefaultBackground(GuiGraphics graphics) {
		return true;
	}

	public void drawForeground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (button == MouseButton.BACK) {
			closeGui(true);
			return true;
		}

		var now = System.currentTimeMillis();
		if (lastClickTime != 0L && (now - lastClickTime) <= 300L && mouseDoubleClicked(button)) {
			lastClickTime = 0L;
			return true;
		}
		lastClickTime = now;

		return super.mousePressed(button);
	}

	@Override
	public boolean keyPressed(Key key) {
		if (super.keyPressed(key)) {
			return true;
		} else if (GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F3) == GLFW.GLFW_PRESS && key.is(GLFW.GLFW_KEY_B)) {
			Theme.renderDebugBoxes = !Theme.renderDebugBoxes;
			return true;
		}

		return false;
	}

	@Override
	public boolean shouldAddMouseOverText() {
		return contextMenu == null;
	}

	@Override
	public final void openGui() {
		openContextMenu((ContextMenu) null);
		Minecraft.getInstance().setScreen(new ScreenWrapper(this));
	}

	@Override
	public final Window getScreen() {
		if (screen == null) {
			return parent.getScreen();
		}

		return screen;
	}

	@Override
	public final int getMouseX() {
		return mouseX;
	}

	@Override
	public final int getMouseY() {
		return mouseY;
	}

	@Override
	public final float getPartialTicks() {
		return partialTicks;
	}

	public boolean isMouseOver(int x, int y, int w, int h) {
		return getMouseX() >= x && getMouseY() >= y && getMouseX() < x + w && getMouseY() < y + h;
	}

	public boolean isMouseOver(Widget widget) {
		if (widget == this) {
			return isMouseOver(getX(), getY(), width, height);
		} else if (isMouseOver(widget.getX(), widget.getY(), widget.width, widget.height)) {
			var offset = widget.parent.isOffset();
			widget.parent.setOffset(false);
			var b = isMouseOver(widget.parent);
			widget.parent.setOffset(offset);
			return b;
		}

		return false;
	}

	@Override
	public boolean handleClick(String scheme, String path) {
		return ClientUtils.handleClick(scheme, path);
	}

	public void openYesNoFull(Component title, Component desc, BooleanConsumer callback) {
		Minecraft.getInstance().setScreen(new ConfirmScreen(result ->
		{
			openGui();
			callback.accept(result);
			refreshWidgets();
		}, title, desc));
	}

	public final void openYesNo(Component title, Component desc, Runnable callback) {
		openYesNoFull(title, desc, result -> {
			if (result) {
				callback.run();
			}
		});
	}
}
