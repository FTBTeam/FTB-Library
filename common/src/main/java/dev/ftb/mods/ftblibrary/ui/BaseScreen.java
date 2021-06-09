package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.LoadingScreen;
import dev.ftb.mods.ftblibrary.util.BooleanConsumer;
import dev.ftb.mods.ftblibrary.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class BaseScreen extends Panel implements IOpenableScreen {
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

	private int mouseX, mouseY;
	private float partialTicks;
	private boolean refreshWidgets;
	private Window screen;
	private final Screen prevScreen;
	public Panel contextMenu = null;
	public ItemRenderer itemRenderer;

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
		setWidth(screen.getGuiScaledWidth());
		setHeight(screen.getGuiScaledHeight());
		return true;
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
		double mx = Minecraft.getInstance().mouseHandler.xpos();
		double my = Minecraft.getInstance().mouseHandler.ypos();

		Minecraft mc = Minecraft.getInstance();

		if (mc.player != null) {
			mc.player.closeContainer();

			if (mc.screen == null) {
				mc.setWindowActive(true);
			}
		}

		if (openPrevScreen) {
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
		CursorType.set(getCursor());
	}

	@Override
	public void updateMouseOver(int mouseX, int mouseY) {
		isMouseOver = checkMouseOver(mouseX, mouseY);
		setOffset(true);

		if (contextMenu != null) {
			contextMenu.updateMouseOver(mouseX, mouseY);
		} else {
			for (Widget widget : widgets) {
				widget.updateMouseOver(mouseX, mouseY);
			}
		}

		setOffset(false);
	}

	@Override
	public final void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		super.draw(matrixStack, theme, x, y, w, h);
	}

	@Override
	public void openContextMenu(@Nullable Panel panel) {
		int px = 0, py = 0;

		if (contextMenu != null) {
			px = contextMenu.posX;
			py = contextMenu.posY;
			contextMenu.onClosed();
			widgets.remove(contextMenu);
		}

		if (panel == null) {
			contextMenu = null;
			return;
		}

		int x = getX();
		int y = getY();

		if (contextMenu == null) {
			px = getMouseX() - x;
			py = getMouseY() - y;
		}

		contextMenu = panel;
		contextMenu.parent = this;
		widgets.add(contextMenu);
		contextMenu.refreshWidgets();
		px = Math.min(px, screen.getGuiScaledWidth() - contextMenu.width - x) - 3;
		py = Math.min(py, screen.getGuiScaledHeight() - contextMenu.height - y) - 3;
		contextMenu.setPos(px, py);

		if (contextMenu instanceof BaseScreen) {
			((BaseScreen) contextMenu).initGui();
		}
	}

	public ContextMenu openContextMenu(List<ContextMenuItem> menu) {
		ContextMenu contextMenu = new ContextMenu(this, menu);
		openContextMenu(contextMenu);
		return contextMenu;
	}

	@Override
	public void closeContextMenu() {
		openContextMenu((Panel) null);
		onInit();
	}

	@Override
	public void onClosed() {
		super.onClosed();
		closeContextMenu();
		CursorType.set(null);
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		theme.drawGui(matrixStack, x, y, w, h, WidgetType.NORMAL);
	}

	public boolean drawDefaultBackground(PoseStack matrixStack) {
		return true;
	}

	public void drawForeground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (button == MouseButton.BACK) {
			closeGui(true);
		}

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

	public Screen getWrapper() {
		return new ScreenWrapper(this);
	}

	@Override
	public final void openGui() {
		openContextMenu((Panel) null);
		Minecraft.getInstance().setScreen(getWrapper());
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
			boolean offset = widget.parent.isOffset();
			widget.parent.setOffset(false);
			boolean b = isMouseOver(widget.parent);
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