package dev.ftb.mods.ftblibrary.client.gui.widget;

import dev.ftb.mods.ftblibrary.client.gui.*;
import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.layout.WidgetLayout;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.client.util.PositionedIngredient;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class Panel extends Widget {
    protected final List<Widget> widgets;

    private double scrollX = 0, scrollY = 0;
    private int offsetX = 0, offsetY = 0;
    private boolean onlyRenderWidgetsInside = true;
    private boolean onlyInteractWithWidgetsInside = true;
    private double scrollStep = 20;
    private int contentWidth = -1, contentHeight = -1;
    private int contentWidthExtra, contentHeightExtra;
    @Nullable
    private PanelScrollBar attachedScrollbar = null;

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

    public abstract void alignWidgets();

    public void clearWidgets() {
        widgets.clear();
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
    }

    public void addAll(Iterable<? extends Widget> list) {
        for (Widget w : list) {
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
    public void extract(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        var renderInside = getOnlyRenderWidgetsInside();

        extractBackground(graphics, theme, x, y, w, h);

        if (renderInside) {
            graphics.enableScissor(x, y, x + w, y + h);
        }

        doWithScrollOffset(() -> {
            var byLayer = widgets.stream()
                    .collect(Collectors.groupingBy(Widget::getDrawLayer, () -> new EnumMap<>(DrawLayer.class), Collectors.toList()));

            byLayer.getOrDefault(DrawLayer.BACKGROUND, List.of())
                    .forEach(widget -> extractWidget(graphics, theme, widget, x + offsetX, y + offsetY, w, h));

            extractOffsetBackground(graphics, theme, x + offsetX, y + offsetY, w, h);

            byLayer.getOrDefault(DrawLayer.FOREGROUND, List.of())
                    .forEach(widget -> extractWidget(graphics, theme, widget, x + offsetX, y + offsetY, w, h));
        });

        if (renderInside) {
            graphics.disableScissor();
        }
    }

    public void extractBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
    }

    public void extractOffsetBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
    }

    public void extractWidget(GuiGraphicsExtractor graphics, Theme theme, Widget widget, int x, int y, int w, int h) {
        var wx = widget.getX();
        var wy = widget.getY();
        var ww = widget.width;
        var wh = widget.height;

        widget.extract(graphics, theme, wx, wy, ww, wh);

        if (Theme.renderDebugBoxes) {
            var col = Color4I.rgb(Color4I.HSBtoRGB((widget.hashCode() & 255) / 255F, 1F, 1F));
            GuiHelper.drawHollowRect(graphics, wx, wy, ww, wh, col.withAlpha(150), false);
            IconHelper.renderIcon(col.withAlpha(30), graphics, wx + 1, wy + 1, ww - 2, wh - 2);
        }
    }

    public void doWithScrollOffset(Runnable runnable) {
        setOffset(true);
        try {
            runnable.run();
        } finally {
            setOffset(false);
        }
    }

    public <T> T getWithScrollOffset(Supplier<T> supplier) {
        setOffset(true);
        try {
            return supplier.get();
        } finally {
            setOffset(false);
        }
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (shouldAddMouseOverText() && (isMouseOver() || !getOnlyInteractWithWidgetsInside())) {
            doWithScrollOffset(() -> {
                for (var i = widgets.size() - 1; i >= 0; i--) {
                    var widget = widgets.get(i);

                    if (widget.shouldAddMouseOverText()) {
                        widget.addMouseOverText(list);

                        if (Theme.renderDebugBoxes) {
                            list.styledString(widget + "#" + (i + 1) + ": " + widget.width + "x" + widget.height, ChatFormatting.DARK_GRAY);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void updateMouseOver(int mouseX, int mouseY) {
        super.updateMouseOver(mouseX, mouseY);

        doWithScrollOffset(() -> {
            for (var widget : widgets) {
                widget.updateMouseOver(mouseX, mouseY);
            }
        });
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (getOnlyInteractWithWidgetsInside() && !isMouseOver()) {
            return false;
        }

        return getWithScrollOffset(() -> {
            for (var i = widgets.size() - 1; i >= 0; i--) {
                var widget = widgets.get(i);
                if (widget.isEnabled() && widget.shouldDraw() && widget.mousePressed(button)) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public boolean mouseDoubleClicked(MouseButton button) {
        if (getOnlyInteractWithWidgetsInside() && !isMouseOver()) {
            return false;
        }

        return getWithScrollOffset(() -> {
            for (var i = widgets.size() - 1; i >= 0; i--) {
                var widget = widgets.get(i);
                if (widget.isEnabled() && widget.mouseDoubleClicked(button)) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void mouseReleased(MouseButton button) {
        doWithScrollOffset(() -> {
            for (var i = widgets.size() - 1; i >= 0; i--) {
                var widget = widgets.get(i);
                if (widget.isEnabled()) {
                    widget.mouseReleased(button);
                }
            }
        });
    }

    @Override
    public boolean mouseScrolled(double scroll) {
        return getWithScrollOffset(() -> {
            for (var i = widgets.size() - 1; i >= 0; i--) {
                var widget = widgets.get(i);
                if (widget.isEnabled() && widget.mouseScrolled(scroll)) {
                    return true;
                }
            }

            return scrollPanel(scroll);
        });
    }

    @Override
    public boolean mouseDragged(int button, double dragX, double dragY) {
        return getWithScrollOffset(() -> {
            for (var i = widgets.size() - 1; i >= 0; i--) {
                var widget = widgets.get(i);
                if (widget.isEnabled() && widget.mouseDragged(button, dragX, dragY)) {
                    return true;
                }
            }

            return false;
        });

    }

    public boolean scrollPanel(double scroll) {
        if (attachedScrollbar != null || !isMouseOver()) {
            return false;
        }

        if (isDefaultScrollVertical() != isShiftKeyDown()) {
            return movePanelScroll(0, -getScrollStep() * scroll);
        } else {
            return movePanelScroll(-getScrollStep() * scroll, 0);
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

    public boolean isDefaultScrollVertical() {
        return true;
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

        return getWithScrollOffset(() -> {
            for (var i = widgets.size() - 1; i >= 0; i--) {
                var widget = widgets.get(i);
                if (widget.isEnabled() && widget.keyPressed(key)) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public boolean keyReleased(Key key) {
        return getWithScrollOffset(() -> {
            for (var i = widgets.size() - 1; i >= 0; i--) {
                var widget = widgets.get(i);
                if (widget.isEnabled() && widget.keyReleased(key)) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (super.charTyped(event)) {
            return true;
        }

        return getWithScrollOffset(() -> {
            for (var i = widgets.size() - 1; i >= 0; i--) {
                var widget = widgets.get(i);
                if (widget.isEnabled() && widget.charTyped(event)) {
                    return true;
                }
            }
            return false;
        });
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
        return getWithScrollOffset(() -> {
            for (var i = widgets.size() - 1; i >= 0; i--) {
                var widget = widgets.get(i);
                if (widget.isEnabled() && widget.isMouseOver()) {
                    var ingredient = widget.getIngredientUnderMouse();
                    if (ingredient.isPresent()) {
                        return ingredient;
                    }
                }
            }

            return Optional.empty();
        });
    }

    @Override
    public void tick() {
        doWithScrollOffset(() -> {
            for (var widget : widgets) {
                if (widget.isEnabled()) {
                    widget.tick();
                }
            }
        });
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
        return getWithScrollOffset(() -> {
            for (var i = widgets.size() - 1; i >= 0; i--) {
                var widget = widgets.get(i);
                if (widget.isEnabled() && widget.isMouseOver()) {
                    var cursor = widget.getCursor();
                    if (cursor != null) {
                        return cursor;
                    }
                }
            }
            return null;
        });
    }
}
