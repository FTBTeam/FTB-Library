package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.layout.LayoutHints;
import dev.ftb.mods.ftblibrary.ui.layout.Size;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Panel extends Widget {
    protected final List<Widget> widgets;

    private double scrollX = 0, scrollY = 0;
    private int offsetX = 0, offsetY = 0;
    private boolean onlyRenderWidgetsInside = true;
    private boolean onlyInteractWithWidgetsInside = true;
    private double scrollStep = 20;
    private int contentWidth = -1, contentHeight = -1;
    private int contentWidthExtra, contentHeightExtra;
    private PanelScrollBar attachedScrollbar = null;
    private static final int MAX_LAYOUT_PASSES = 8;

    private boolean layoutDirty = false;
    private boolean inLayout = false;
    private boolean fitWidth = false;
    private boolean fitHeight = false;

    public Panel(Panel panel) {
        super(panel);
        widgets = new ArrayList<>();
    }

    public boolean getOnlyRenderWidgetsInside() {
        return onlyRenderWidgetsInside;
    }

    public void setOnlyRenderWidgetsInside(boolean value) {
        onlyRenderWidgetsInside = value;
    }

    public boolean getOnlyInteractWithWidgetsInside() {
        return onlyInteractWithWidgetsInside;
    }

    public void setOnlyInteractWithWidgetsInside(boolean value) {
        onlyInteractWithWidgetsInside = value;
    }

    public List<Widget> getWidgets() {
        return widgets;
    }

    public abstract void addWidgets();

    /**
     * Position and size the child widgets. By default this applies {@link #getLayout()}, if there is one, and does
     * nothing otherwise. Existing panels override this and call {@link #align(WidgetLayout)} themselves; that keeps
     * working unchanged.
     */
    public void alignWidgets() {
        applyManagedLayout();
    }

    /**
     * Opt this panel in to managed layout by returning a layout here. The panel then lays its widgets out by itself:
     * when {@link #refreshWidgets()} runs, and again, before the panel is next drawn, after its width or height
     * changes (so widgets with {@link dev.ftb.mods.ftblibrary.ui.layout.Flex#RESIZE} follow their parent). Child panels that are resized by the layout
     * are re-laid out in turn, from the top down, and panels that {@link #setFitContent(boolean, boolean) fit their
     * content} are settled first, from the bottom up, so a parent always sees their current size.
     * <p>
     * Return {@code null} (the default) to keep full manual control. This may be called at any time, including during
     * construction, so it should not depend on fields that might not be initialised yet.
     */
    @Nullable
    protected WidgetLayout getLayout() {
        return null;
    }

    /**
     * Make this panel's own size follow its content on the chosen axes. Off by default.
     * <p>
     * A fitting panel measures its {@link #getLayout() layout} and sets its width and/or height to what the content
     * wants (within its own {@link LayoutHints} min and max size), then lays the widgets out. Widgets marked
     * {@link dev.ftb.mods.ftblibrary.ui.layout.Flex#RESIZE} on a fitted axis keep their current size instead of
     * stretching, since there is no fixed room to stretch into. On an axis that is not fitted, the panel's current size
     * is the room the content is laid out in, so for example a flow layout that fits only its height wraps to the
     * panel's width.
     * <p>
     * The panel refits when its content or the size of a widget in it changes, and its parent is told, so a parent
     * layout always sees the up to date size. A parent layout will not stretch a fitting panel along a fitted axis.
     * <p>
     * Fitting needs a layout that can {@link WidgetLayout#measure(Panel, int, int) measure}; for any other layout,
     * and for panels without a layout, the flags have no effect.
     *
     * @param width  fit the width to the content
     * @param height fit the height to the content
     */
    public void setFitContent(boolean width, boolean height) {
        fitWidth = width;
        fitHeight = height;
        invalidateLayout();
    }

    public boolean isFitContentWidth() {
        return fitWidth;
    }

    public boolean isFitContentHeight() {
        return fitHeight;
    }

    @Override
    public boolean isFittingContent(boolean horizontal) {
        return horizontal ? fitWidth : fitHeight;
    }

    /**
     * Mark this panel's layout, and that of every panel above it, as out of date. The managed layout is redone by
     * {@link #validateLayout()}, which runs before the panel is drawn. Adding or removing widgets, changing the size or
     * layout hints of a widget in the panel, and resizing the panel all do this for you. Has no visible effect on panels
     * without a {@link #getLayout() layout}.
     */
    public void invalidateLayout() {
        layoutDirty = true;
        for (Panel p = parent; p != null && !p.layoutDirty; p = p.parent) {
            p.layoutDirty = true;
        }
    }

    /**
     * Redo the managed layout now if something made it stale. Does nothing for panels without a
     * {@link #getLayout() layout}, or while a layout pass is already running.
     */
    public void validateLayout() {
        if (!layoutDirty || inLayout) {
            return;
        }
        layoutDirty = false;
        applyManagedLayout();
    }

    private void applyManagedLayout() {
        WidgetLayout layout = getLayout();
        if (layout == null || inLayout) {
            return;
        }

        inLayout = true;
        try {
            // Child panels whose content changed settle their own size first, so what is measured below is current
            validateChildLayouts();

            // Fitting a size and laying out can each change what the other needs, e.g. a child that is stretched to
            // a new width may then want a different height. Repeat until the child panels stop changing size.
            for (int pass = 0; pass < MAX_LAYOUT_PASSES; pass++) {
                int availableWidth = fitWidth || width <= 0 ? Size.UNBOUNDED : width;
                int availableHeight = fitHeight || height <= 0 ? Size.UNBOUNDED : height;

                if (fitWidth || fitHeight) {
                    fitToContent(layout, availableWidth, availableHeight);
                }
                // From here on, anything that marks this layout dirty is a widget that changed size while being laid
                // out (a text field that rewraps to its new width, say), which the layout has to be redone for.
                layoutDirty = false;
                align(layout, availableWidth, availableHeight);

                if (!validateChildLayouts() && !layoutDirty) {
                    break;
                }
            }
        } finally {
            inLayout = false;
            layoutDirty = false;
        }
    }

    private void fitToContent(WidgetLayout layout, int availableWidth, int availableHeight) {
        Size wanted = layout.measure(this, availableWidth, availableHeight);
        if (wanted == null) {
            return;
        }

        LayoutHints hints = getLayoutHints();
        if (fitWidth) {
            setWidth(Math.clamp(wanted.width(), hints.minWidth(), hints.maxWidth()));
        }
        if (fitHeight) {
            setHeight(Math.clamp(wanted.height(), hints.minHeight(), hints.maxHeight()));
        }
    }

    /**
     * Let every child panel that needs it redo its layout.
     *
     * @return true if any of them changed size while doing so
     */
    private boolean validateChildLayouts() {
        boolean changed = false;
        for (Widget widget : widgets) {
            if (widget instanceof Panel child) {
                int w = child.width;
                int h = child.height;
                child.validateLayout();
                changed |= child.width != w || child.height != h;
            }
        }
        return changed;
    }

    @Override
    public void setWidth(int v) {
        int old = width;
        super.setWidth(v);
        if (width != old) {
            layoutDirty = true;
        }
    }

    @Override
    public void setHeight(int v) {
        int old = height;
        super.setHeight(v);
        if (height != old) {
            layoutDirty = true;
        }
    }

    public void clearWidgets() {
        widgets.clear();
        invalidateLayout();
    }

    public void refreshWidgets() {
        contentWidth = contentHeight = -1;

        clearWidgets();
        try {
            addWidgets();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        widgets.sort(null);

        for (var widget : widgets) {
            if (widget instanceof Panel p) {
                p.refreshWidgets();
            }
        }

        alignWidgets();
    }

    public void add(Widget widget) {
        if (widget.parent != this) {
            throw new MismatchingParentPanelException(this, widget);
        }
        if (widget instanceof PanelScrollBar psb) {
            attachedScrollbar = psb;
        }

        widgets.add(widget);
        contentWidth = contentHeight = -1;
        invalidateLayout();
    }

    public void addAll(Iterable<? extends Widget> list) {
        for (Widget w : list) {
            add(w);
        }
    }

    public void addAll(Widget... widgets) {
        for (Widget w : widgets) {
            add(w);
        }
    }

    public final int align(WidgetLayout layout) {
        contentWidth = contentHeight = -1;
        int res = layout.align(this);
        contentHeightExtra = layout.getLayoutPadding().vertical();
        contentWidthExtra = layout.getLayoutPadding().horizontal();
        return res;
    }

    /**
     * Like {@link #align(WidgetLayout)}, but lays the widgets out within the given room rather than this panel's own
     * size; see {@link WidgetLayout#align(Panel, int, int)}.
     *
     * @param availableWidth  room available horizontally, or {@link dev.ftb.mods.ftblibrary.ui.layout.Size#UNBOUNDED}
     * @param availableHeight room available vertically, or {@link dev.ftb.mods.ftblibrary.ui.layout.Size#UNBOUNDED}
     */
    public final int align(WidgetLayout layout, int availableWidth, int availableHeight) {
        contentWidth = contentHeight = -1;
        int res = layout.align(this, availableWidth, availableHeight);
        contentHeightExtra = layout.getLayoutPadding().vertical();
        contentWidthExtra = layout.getLayoutPadding().horizontal();
        return res;
    }

    @Override
    public int getX() {
        return super.getX() + offsetX;
    }

    @Override
    public int getY() {
        return super.getY() + offsetY;
    }

    public int getContentWidth() {
        if (contentWidth == -1) {
            var minX = Integer.MAX_VALUE;
            var maxX = Integer.MIN_VALUE;

            for (var widget : widgets) {
                if (widget.posX < minX) {
                    minX = widget.posX;
                }

                if (widget.posX + widget.width > maxX) {
                    maxX = widget.posX + widget.width;
                }
            }

            contentWidth = maxX - minX + contentWidthExtra;
        }

        return contentWidth;
    }

    public int getContentHeight() {
        if (contentHeight == -1) {
            var minY = Integer.MAX_VALUE;
            var maxY = Integer.MIN_VALUE;

            for (var widget : widgets) {
                if (widget.posY < minY) {
                    minY = widget.posY;
                }

                if (widget.posY + widget.height > maxY) {
                    maxY = widget.posY + widget.height;
                }
            }

            contentHeight = maxY - minY + contentHeightExtra;
        }

        return contentHeight;
    }

    public boolean isOffset() {
        return offsetX != 0 || offsetY != 0;
    }

    public void setOffset(boolean flag) {
        if (flag) {
            offsetX = (int) -scrollX;
            offsetY = (int) -scrollY;
        } else {
            offsetX = offsetY = 0;
        }
    }

    public double getScrollX() {
        return scrollX;
    }

    public void setScrollX(double scroll) {
        scrollX = scroll;
    }

    public double getScrollY() {
        return scrollY;
    }

    public void setScrollY(double scroll) {
        scrollY = scroll;
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        validateLayout();

        var renderInside = getOnlyRenderWidgetsInside();

        drawBackground(graphics, theme, x, y, w, h);

        if (renderInside) {
            GuiHelper.pushScissor(getWindow(), x, y, w, h);
        }

        setOffset(true);

        widgets.stream()
                .filter(widget -> widget.shouldRenderInLayer(DrawLayer.BACKGROUND, x, y, w, h))
                .forEach(widget -> drawWidget(graphics, theme, widget, x + offsetX, y + offsetY, w, h));

        drawOffsetBackground(graphics, theme, x + offsetX, y + offsetY, w, h);

        widgets.stream()
                .filter(widget -> widget.shouldRenderInLayer(DrawLayer.FOREGROUND, x, y, w, h))
                .forEach(widget -> drawWidget(graphics, theme, widget, x + offsetX, y + offsetY, w, h));

        setOffset(false);

        if (renderInside) {
            GuiHelper.popScissor(getWindow());
        }
    }

    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
    }

    public void drawOffsetBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
    }

    public void drawWidget(GuiGraphics graphics, Theme theme, Widget widget, int x, int y, int w, int h) {
        var wx = widget.getX();
        var wy = widget.getY();
        var ww = widget.width;
        var wh = widget.height;

        widget.draw(graphics, theme, wx, wy, ww, wh);

        if (Theme.renderDebugBoxes) {
            var col = Color4I.rgb(Color4I.HSBtoRGB((widget.hashCode() & 255) / 255F, 1F, 1F));
            GuiHelper.drawHollowRect(graphics, wx, wy, ww, wh, col.withAlpha(150), false);
            col.withAlpha(30).draw(graphics, wx + 1, wy + 1, ww - 2, wh - 2);
        }
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (!shouldAddMouseOverText() || getOnlyInteractWithWidgetsInside() && !isMouseOver()) {
            return;
        }

        setOffset(true);

        for (var i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);

            if (widget.shouldAddMouseOverText()) {
                widget.addMouseOverText(list);

                if (Theme.renderDebugBoxes) {
                    list.styledString(widget + "#" + (i + 1) + ": " + widget.width + "x" + widget.height, ChatFormatting.DARK_GRAY);
                }
            }
        }

        setOffset(false);
    }

    @Override
    public void updateMouseOver(int mouseX, int mouseY) {
        super.updateMouseOver(mouseX, mouseY);
        setOffset(true);

        for (var widget : widgets) {
            widget.updateMouseOver(mouseX, mouseY);
        }

        setOffset(false);
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (getOnlyInteractWithWidgetsInside() && !isMouseOver()) {
            return false;
        }

        setOffset(true);

        for (var i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);

            if (widget.isEnabled() && widget.shouldDraw() && widget.mousePressed(button)) {
                setOffset(false);
                return true;
            }
        }

        setOffset(false);
        return false;
    }

    @Override
    public boolean mouseDoubleClicked(MouseButton button) {
        if (getOnlyInteractWithWidgetsInside() && !isMouseOver()) {
            return false;
        }

        setOffset(true);

        for (var i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);

            if (widget.isEnabled() && widget.mouseDoubleClicked(button)) {
                setOffset(false);
                return true;
            }
        }

        setOffset(false);
        return false;
    }

    @Override
    public void mouseReleased(MouseButton button) {
        setOffset(true);

        for (var i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);

            if (widget.isEnabled()) {
                widget.mouseReleased(button);
            }
        }

        setOffset(false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        setOffset(true);

        for (var i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);

            if (widget.isEnabled() && widget.mouseScrolled(mouseX, mouseY, xDelta, yDelta)) {
                setOffset(false);
                return true;
            }
        }

        var scrollPanel = scrollPanel(xDelta, yDelta);
        setOffset(false);
        return scrollPanel;
    }

    @Override
    public boolean mouseDragged(int button, double dragX, double dragY) {
        setOffset(true);

        for (var i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);

            if (widget.isEnabled() && widget.mouseDragged(button, dragX, dragY)) {
                setOffset(false);
                return true;
            }
        }

        setOffset(false);
        return false;
    }

    public boolean scrollPanel(double xDelta, double yDelta) {
        if (attachedScrollbar != null || !isMouseOver()) {
            return false;
        }

        // No scroll direction was given?
        var directionlessDelta = yDelta != 0 ? yDelta : xDelta;
        if (directionlessDelta == 0) {
            return false;
        }

        // If the user is pressing shift, we'll always attempt to scroll horizontally, otherwise we'll just blindly apply both directions
        if (isShiftKeyDown()) {
            var scrollAmount = -getScrollStep() * directionlessDelta;
            return movePanelScroll(scrollAmount, 0);
        } else {
            return movePanelScroll(-getScrollStep() * xDelta, -getScrollStep() * yDelta);
        }
    }

    public boolean movePanelScroll(double dx, double dy) {
        if (dx == 0 && dy == 0) {
            return false;
        }

        var sx = getScrollX();
        var sy = getScrollY();

        if (dx != 0) {
            var w = getContentWidth();

            if (w > width) {
                setScrollX(Mth.clamp(sx + dx, 0, w - width));
            }
        }

        if (dy != 0) {
            var h = getContentHeight();

            if (h > height) {
                setScrollY(Mth.clamp(sy + dy, 0, h - height));
            }
        }

        return getScrollX() != sx || getScrollY() != sy;
    }

    public double getScrollStep() {
        return scrollStep;
    }

    public void setScrollStep(double s) {
        scrollStep = s;
    }

    @Override
    public boolean keyPressed(Key key) {
        if (super.keyPressed(key)) {
            return true;
        }

        setOffset(true);

        for (var i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);

            if (widget.isEnabled() && widget.keyPressed(key)) {
                setOffset(false);
                return true;
            }
        }

        setOffset(false);
        return false;
    }

    @Override
    public void keyReleased(Key key) {
        setOffset(true);

        for (var i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);

            if (widget.isEnabled()) {
                widget.keyReleased(key);
            }
        }

        setOffset(false);
    }

    @Override
    public boolean charTyped(char c, KeyModifiers modifiers) {
        if (super.charTyped(c, modifiers)) {
            return true;
        }

        setOffset(true);

        for (var i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);

            if (widget.isEnabled() && widget.charTyped(c, modifiers)) {
                setOffset(false);
                return true;
            }
        }

        setOffset(false);
        return false;
    }

    @Override
    public void onClosed() {
        for (var widget : widgets) {
            widget.onClosed();
        }
    }

    @Nullable
    public Widget getWidget(int index) {
        return index < 0 || index >= widgets.size() ? null : widgets.get(index);
    }

    @Override
    public Optional<PositionedIngredient> getIngredientUnderMouse() {
        setOffset(true);

        Optional<PositionedIngredient> result = Optional.empty();

        for (var i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);
            if (widget.isEnabled() && widget.isMouseOver()) {
                var ingredient = widget.getIngredientUnderMouse();
                if (ingredient.isPresent()) {
                    result = ingredient;
                    break;
                }
            }
        }

        setOffset(false);

        return result;
    }

    @Override
    public void tick() {
        setOffset(true);

        for (var widget : widgets) {
            if (widget.isEnabled()) {
                widget.tick();
            }
        }

        setOffset(false);
    }

    public boolean isMouseOverAnyWidget() {
        for (var widget : widgets) {
            if (widget.isMouseOver()) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Nullable
    public CursorType getCursor() {
        setOffset(true);

        for (var i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);

            if (widget.isEnabled() && widget.isMouseOver()) {
                var cursor = widget.getCursor();

                if (cursor != null) {
                    setOffset(false);
                    return cursor;
                }
            }
        }

        setOffset(false);
        return null;
    }
}
