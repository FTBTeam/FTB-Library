package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.LoadingScreen;
import dev.ftb.mods.ftblibrary.util.BooleanConsumer;
import dev.ftb.mods.ftblibrary.util.TooltipList;
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

import java.util.*;

public abstract class BaseScreen extends Panel {
    @Nullable private Screen prevScreen;
    private final Deque<ModalPanel> modalPanels;
    private int mouseX, mouseY;
    private float partialTicks;
    private boolean refreshWidgets;
    private long lastClickTime = 0L;
    private Widget focusedWidget = null;
    private boolean renderBlur = true;

    public BaseScreen(@Nullable Screen previousScreen) {
        super(null);
        setSize(176, 166);
        setOnlyRenderWidgetsInside(false);
        setOnlyInteractWithWidgetsInside(false);
        prevScreen = previousScreen;
        modalPanels = new ArrayDeque<>();
    }

    public BaseScreen() {
        this(Minecraft.getInstance().screen);
    }

    /**
     * Sets the previous screen that will be opened when this screen is closed.
     *
     * @param prevScreen the previous screen, or null to just close the GUI
     */
    public void setPreviousScreen(@Nullable Screen prevScreen) {
        this.prevScreen = prevScreen;
    }

    @Override
    public final BaseScreen getGui() {
        return this;
    }

    @Override
    public void alignWidgets() {
    }

    public final void initGui() {
        if (onInit()) {
            super.refreshWidgets();
            onPostInit();
        }
    }

    public Theme getTheme() {
        return ThemeManager.INSTANCE.getActiveTheme();
    }

    @Override
    public int getX() {
        return (getWindow().getGuiScaledWidth() - width) / 2;
    }

    @Override
    public int getY() {
        return (getWindow().getGuiScaledHeight() - height) / 2;
    }

    @Override
    public double getScrollX() {
        return 0;
    }

    @Override
    public void setScrollX(double scroll) {
    }

    @Override
    public double getScrollY() {
        return 0;
    }

    @Override
    public void setScrollY(double scroll) {
    }

    public boolean onInit() {
        return true;
    }

    /**
     * @return if the GUI should render a blur effect behind it
     */
    public boolean shouldRenderBlur() {
        return renderBlur;
    }

    /**
     * @param renderBlur sets if the GUI should render a blur effect behind it
     */
    public void setRenderBlur(boolean renderBlur) {
        this.renderBlur = renderBlur;
    }

    /**
     * Should the GUI automatically close when Escape (or the inventory key - E by default) is pressed? Override this
     * to return false if you need to implement custom close behaviour, e.g. a confirmation screen for unsaved changes.
     *
     * @return autoclose behaviour, true by default
     */
    public boolean shouldCloseOnEsc() {
        return true;
    }

    protected boolean setFullscreen() {
        return setSizeProportional(1f, 1f);
    }

    protected boolean setSizeProportional(float w, float h) {
        Validate.isTrue(w > 0f && w <= 1f && h > 0f && h <= 1f, "width and height must be > 0 and <= 1");

        setWidth((int) (getWindow().getGuiScaledWidth() * w));
        setHeight((int) (getWindow().getGuiScaledHeight() * h));
        return true;
    }

    public void onPostInit() {
    }

    /**
     * Push a modal panel onto the stack. It will render above any existing modal panels and take key/mouse input first.
     *
     * @param modalPanel the panel to push
     */
    public void pushModalPanel(ModalPanel modalPanel) {
        modalPanels.addFirst(modalPanel);
        modalPanel.refreshWidgets();

        // since the panel size will have changed, make sure it's still on-screen
        modalPanel.setX(Math.min(modalPanel.getX(), getWidth() - modalPanel.getWidth() - 10));
        modalPanel.setY(Math.min(modalPanel.getY(), getHeight() - modalPanel.getHeight() - 10));
    }

    /**
     * Close the top modal panel, by removing it from the stack.
     *
     * @return the panel that was just popped/closed, or null if no modal panels were present
     */
    public ModalPanel popModalPanel() {
        if (modalPanels.isEmpty()) {
            return null;
        }
        ModalPanel panel = modalPanels.removeFirst();
        panel.onClosed();
        focusedWidget = null;  // in case an editfield in the panel had focus
        return panel;
    }

    /**
     * Close the given panel, and all panels above it, from the stack
     *
     * @param panel the panel to pop/close
     */
    public void closeModalPanel(ModalPanel panel) {
        if (modalPanels.contains(panel)) {
            while (!modalPanels.isEmpty()) {
                if (popModalPanel() == panel) {
                    break;
                }
            }
        }
    }

    public boolean anyModalPanelOpen() {
        return !modalPanels.isEmpty();
    }

    @Override
    public void tick() {
        super.tick();

        modalPanels.forEach(Panel::tick);
    }

    @Nullable
    public Screen getPrevScreen() {
        if (prevScreen instanceof ScreenWrapper sw && sw.getGui() instanceof LoadingScreen) {
            return sw.getGui().getPrevScreen();
        } else if (prevScreen instanceof ChatScreen) {
            return null;
        }

        return prevScreen;
    }

    /**
     * Close the GUI and optionally open the previous screen.
     *
     * @param openPrevScreen if true, will open the previous screen if the {@link #usePreviousScreenOnBack()} method returns true
     */
    @Override
    public final void closeGui(boolean openPrevScreen) {
        var mc = getMinecraft();

        var mx = mc.mouseHandler.xpos();
        var my = mc.mouseHandler.ypos();

        if (mc.player != null) {
            mc.player.closeContainer();

            if (mc.screen == null) {
                mc.setWindowActive(true);
            }
        }

        if (usePreviousScreenOnBack()) {
            if (openPrevScreen && getPrevScreen() != null) {
                mc.setScreen(getPrevScreen());
                GLFW.glfwSetCursorPos(getWindow().getWindow(), mx, my);
            }
        }

        modalPanels.clear();

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
            modalPanels.forEach(Panel::refreshWidgets);
            refreshWidgets = false;
        }

        posX = getX();
        posY = getY();

        updateMouseOver(mouseX, mouseY);
    }

    @Override
    public void updateMouseOver(int mouseX, int mouseY) {
        isMouseOver = checkMouseOver(mouseX, mouseY);
        setOffset(true);

        modalPanels.forEach(p -> p.updateMouseOver(mouseX, mouseY));
        widgets.forEach(w -> w.updateMouseOver(mouseX, mouseY));

        setOffset(false);
    }

    @Override
    public final void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        super.draw(graphics, theme, x, y, w, h);

        if (!modalPanels.isEmpty()) {
            // allow modal panels to draw outside scissor area if needed
            boolean r = getOnlyRenderWidgetsInside();
            boolean i = getOnlyInteractWithWidgetsInside();
            setOnlyRenderWidgetsInside(false);
            setOnlyInteractWithWidgetsInside(false);

            graphics.pose().pushPose();
            graphics.pose().translate(0f, 0f, 10f);
            Iterator<ModalPanel> iter = modalPanels.descendingIterator(); // stack is drawn from bottom to top
            while (iter.hasNext()) {
                ModalPanel p = iter.next();
                if (!iter.hasNext()) {
                    // dim the rest of the gui so the top modal panel is effectively highlighted
                    graphics.pose().translate(0.0, 0.0, -0.05);
                    Color4I.rgba(0xA0202020).draw(graphics, 0, 0, getWindow().getGuiScaledWidth(), getWindow().getGuiScaledHeight());
                    graphics.pose().translate(0.0, 0.0, 0.05);
                }
                graphics.pose().translate(0f, 0f, p.getExtraZlevel());
                p.draw(graphics, theme, p.getX(), p.getY(), p.getWidth(), p.getHeight());
                graphics.pose().translate(0, 0, 1);
            }
            graphics.pose().popPose();

            setOnlyRenderWidgetsInside(r);
            setOnlyRenderWidgetsInside(i);
        }
    }

    public Optional<ModalPanel> getContextMenu() {
        return modalPanels.stream().filter(p -> p instanceof PopupMenu).findFirst();
    }

    public void openPopupMenu(@Nullable PopupMenu popupMenu) {
        if (popupMenu == null) {
            modalPanels.removeIf(p -> p instanceof PopupMenu);
            return;
        }

        pushModalPanel(popupMenu.getModalPanel());

        // default positioning where the mouse was clicked. caller is free to reposition if needed
        var x = getX();
        var y = getY();
        int px = Math.min((getMouseX() - x), getWindow().getGuiScaledWidth() - popupMenu.getModalPanel().width - x) - 3;
        int py = Math.min((getMouseY() - y), getWindow().getGuiScaledHeight() - popupMenu.getModalPanel().height - y) - 3;
        popupMenu.getModalPanel().setPos(px, py);
    }

    public void openContextMenu(@Nullable ContextMenu newContextMenu) {
        openPopupMenu(newContextMenu);
    }

    public ContextMenu openContextMenu(@NotNull List<ContextMenuItem> menuItems) {
        var contextMenu = new ContextMenu(this, menuItems);
        openContextMenu(contextMenu);
        return contextMenu;
    }

    public void openDropdownMenu(@Nullable DropDownMenu dropDownMenu) {
        openPopupMenu(dropDownMenu);
    }

    public DropDownMenu openDropdownMenu(@NotNull List<ContextMenuItem> menuItems) {
        var contextMenu = new DropDownMenu(this, menuItems);
        openDropdownMenu(contextMenu);
        return contextMenu;
    }

    @Override
    public void closeContextMenu() {
        openContextMenu((ContextMenu) null);
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

    private Panel getDoubleClickTarget() {
        return modalPanels.isEmpty() ? this : modalPanels.peekFirst();
    }

    public int getMaxZLevel() {
        return modalPanels.stream().map(ModalPanel::getExtraZlevel).max(Integer::compare).orElse(0);
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (button == MouseButton.BACK) {
            closeGui(true);
            return true;
        }

        var now = System.currentTimeMillis();
        if (lastClickTime != 0L && (now - lastClickTime) <= 300L && getDoubleClickTarget().mouseDoubleClicked(button)) {
            lastClickTime = 0L;
            return true;
        }
        lastClickTime = now;

        if (modalPanels.isEmpty()) {
            return super.mousePressed(button);
        } else if (modalPanels.peekFirst().isMouseOver()) {
            return modalPanels.peekFirst().mousePressed(button);
        } else {
            // clicking outside any panel dismisses the top one
            popModalPanel();
            return false;
        }
    }

    @Override
    public boolean keyPressed(Key key) {
        if (focusedWidget != null && focusedWidget.keyPressed(key)) {
            return true;
        } else if (!modalPanels.isEmpty()) {
            if (key.esc()) {
                popModalPanel();
                return true;
            }
            //noinspection DataFlowIssue
            return modalPanels.peekFirst().keyPressed(key);  // we already checked it's not empty
        } else if (super.keyPressed(key)) {
            return true;
        } else if (InputConstants.isKeyDown(getWindow().getWindow(), GLFW.GLFW_KEY_F3) && key.is(GLFW.GLFW_KEY_B)) {
            Theme.renderDebugBoxes = !Theme.renderDebugBoxes;
            return true;
        }

        return false;
    }

    @Override
    public void keyReleased(Key key) {
        if (modalPanels.isEmpty()) {
            super.keyReleased(key);
        } else {
            modalPanels.peekFirst().keyReleased(key);
        }
    }

    @Override
    public boolean mouseDoubleClicked(MouseButton button) {
        return modalPanels.isEmpty() ? super.mouseDoubleClicked(button) : modalPanels.peekFirst().mouseDoubleClicked(button);
    }

    @Override
    public void mouseReleased(MouseButton button) {
        if (modalPanels.isEmpty()) {
            super.mouseReleased(button);
        } else {
            modalPanels.peekFirst().mouseReleased(button);
        }
    }

    @Override
    public boolean mouseScrolled(double scroll) {
        if (focusedWidget != null && focusedWidget.mouseScrolled(scroll)) {
            return true;
        }
        return modalPanels.isEmpty() ? super.mouseScrolled(scroll) : modalPanels.peekFirst().mouseScrolled(scroll);
    }

    @Override
    public boolean mouseDragged(int button, double dragX, double dragY) {
        return modalPanels.isEmpty() ? super.mouseDragged(button, dragX, dragY) : modalPanels.peekFirst().mouseDragged(button, dragX, dragY);
    }

    @Override
    public boolean charTyped(char c, KeyModifiers modifiers) {
        if (focusedWidget != null && focusedWidget.charTyped(c, modifiers)) {
            return true;
        }
        return modalPanels.isEmpty() ? super.charTyped(c, modifiers) : modalPanels.peekFirst().charTyped(c, modifiers);
    }

    @Override
    public boolean shouldAddMouseOverText() {
        return getContextMenu().isEmpty();
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (!modalPanels.isEmpty()) {
            modalPanels.peekFirst().addMouseOverText(list);
        } else {
            super.addMouseOverText(list);
        }
    }

    @Override
    public final void openGui() {
        openContextMenu((ContextMenu) null);
        getMinecraft().setScreen(new ScreenWrapper(this));
    }

    /**
     * This is poorly named, so let's move over to getWindow()
     *
     * @deprecated use {@link #getWindow()} instead
     */
    @Deprecated
    @Override
    public final Window getScreen() {
        return getMinecraft().getWindow();
    }

    @Override
    public Window getWindow() {
        return getMinecraft().getWindow();
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
        getMinecraft().setScreen(new ConfirmScreen(result -> {
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

    public void setFocusedWidget(Widget widget) {
        Validate.isTrue(widget instanceof IFocusableWidget);
        if (focusedWidget instanceof IFocusableWidget f && focusedWidget != widget) {
            f.setFocused(false);
        }
        focusedWidget = widget;
    }

    /**
     * Override this method to completely disable going back to the previous screen on various
     * back actions (e.g. pressing esc, calling back, etc).
     *
     * @return true to use the previous screen on back actions, false to just close the GUI
     */
    public boolean usePreviousScreenOnBack() {
        return true;
    }

    /**
     * Helper method to get the Minecraft Instance
     */
    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

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
}
