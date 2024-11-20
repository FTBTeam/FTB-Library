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
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class Widget implements IScreenWrapper, Comparable<Widget> {
    protected final Panel parent;

    public int posX, posY, width, height;
    protected boolean isMouseOver;
    private DrawLayer drawLayer = DrawLayer.FOREGROUND;

    public Widget(Panel p) {
        parent = p;
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

    public Panel getParent() {
        return parent;
    }

    @Override
    public BaseScreen getGui() {
        return parent.getGui();
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

    public void setWidth(int v) {
        width = Math.max(v, 0);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int v) {
        height = Math.max(v, 0);
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

    public void setX(int v) {
        posX = v;
    }

    public int getY() {
        return parent.getY() + posY;
    }

    public void setY(int v) {
        posY = v;
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

        if (title.getContents() != PlainTextContents.EMPTY) {
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

    public DrawLayer getDrawLayer() {
        return drawLayer;
    }

    public void setDrawLayer(DrawLayer drawLayer) {
        this.drawLayer = drawLayer;
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

    final boolean shouldRenderInLayer(DrawLayer layer, int x, int y, int w, int h) {
        return drawLayer == layer
                && shouldDraw()
                && (!parent.getOnlyRenderWidgetsInside() || collidesWith(x, y, w, h));
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

    /**
     * Get a widget comparison for draw ordering. To preserve API constraints, the widget types should be checked,
     * and if not the same, comparison should always return 0. Positive or negative returns should only apply to
     * widgets of the same or compatible type.
     *
     * @param widget the other widget to check
     * @return see {@link Comparable}
     */
    @Override
    public int compareTo(@NotNull Widget widget) {
        // default: don't care. override this in subclasses which do care about relative draw order
        return 0;
    }

    public enum DrawLayer {
        BACKGROUND,
        FOREGROUND
    }
}
