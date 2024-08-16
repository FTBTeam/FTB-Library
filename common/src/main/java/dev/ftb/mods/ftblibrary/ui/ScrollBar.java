package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.Mth;

public class ScrollBar extends Widget {
    protected final Plane plane;
    private final int scrollBarSize;
    private double value = 0;
    private double scrollStep = 20;
    private double grab = -10000;
    private double minValue = 0;
    private double maxValue = 100;
    private boolean canAlwaysScroll = false;
    private boolean canAlwaysScrollPlane = true;
    public ScrollBar(Panel parent, Plane p, int ss) {
        super(parent);
        plane = p;
        scrollBarSize = Math.max(ss, 0);
    }

    public void setCanAlwaysScroll(boolean v) {
        canAlwaysScroll = v;
    }

    public void setCanAlwaysScrollPlane(boolean v) {
        canAlwaysScrollPlane = v;
    }

    public Plane getPlane() {
        return plane;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double min) {
        minValue = min;
        setValue(getValue());
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double max) {
        maxValue = max;
        setValue(getValue());
    }

    public int getScrollBarSize() {
        return scrollBarSize;
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (isMouseOver()) {
            grab = (plane.isVertical ?
                    (getMouseY() - (getY() + getMappedValue(height - getScrollBarSize()))) :
                    (getMouseX() - (getX() + getMappedValue(width - getScrollBarSize()))));
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double scroll) {
        if (scroll != 0 && canMouseScrollPlane() && canMouseScroll()) {
            setValue(getValue() - getScrollStep() * scroll);
            return true;
        }

        return false;
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (showValueOnMouseOver()) {
            var t = getTitle();
            list.string(t.getContents() == PlainTextContents.EMPTY ? (Double.toString(getValue())) : (t + ": " + getValue()));
        }

        if (Theme.renderDebugBoxes) {
            list.styledString("Size: " + getScrollBarSize(), ChatFormatting.DARK_GRAY);
            list.styledString("Max: " + getMaxValue(), ChatFormatting.DARK_GRAY);
            list.styledString("Value: " + getValue(), ChatFormatting.DARK_GRAY);
        }
    }

    public boolean showValueOnMouseOver() {
        return false;
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        var scrollBarSize = getScrollBarSize();

        if (scrollBarSize > 0) {
            var v = getValue();

            if (grab != -10000) {
                if (isMouseButtonDown(MouseButton.LEFT)) {
                    if (plane.isVertical) {
                        v = (getMouseY() - (y + grab)) * getMaxValue() / (double) (height - scrollBarSize);
                    } else {
                        v = (getMouseX() - (x + grab)) * getMaxValue() / (double) (width - scrollBarSize);
                    }
                } else {
                    grab = -10000;
                }
            }

            setValue(v);
        }

        drawBackground(graphics, theme, x, y, width, height);

        if (scrollBarSize > 0) {
            if (plane.isVertical) {
                drawScrollBar(graphics, theme, x, (int) (y + getMappedValue(height - scrollBarSize)), width, scrollBarSize);
            } else {
                drawScrollBar(graphics, theme, (int) (x + getMappedValue(width - scrollBarSize)), y, scrollBarSize, height);
            }
        }
    }

    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawScrollBarBackground(graphics, x, y, w, h, getWidgetType());
    }

    public void drawScrollBar(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawScrollBar(graphics, x, y, w, h, WidgetType.mouseOver(grab != -10000), plane.isVertical);
    }

    public void onMoved() {
    }

    public boolean canMouseScrollPlane() {
        return canAlwaysScrollPlane || isShiftKeyDown() != plane.isVertical;
    }

    public boolean canMouseScroll() {
        return canAlwaysScroll || isMouseOver();
    }

    public double getValue() {
        return value;
    }

    public void setValue(double v) {
        v = Mth.clamp(v, getMinValue(), getMaxValue());

        if (value != v) {
            value = v;
            onMoved();
        }
    }

    public double getMappedValue(double max) {
        return MathUtils.map(getMinValue(), getMaxValue(), 0, max, value);
    }

    public double getScrollStep() {
        return scrollStep;
    }

    public void setScrollStep(double s) {
        scrollStep = Math.max(0D, s);
    }

    public enum Plane {
        HORIZONTAL(false),
        VERTICAL(true);

        public final boolean isVertical;

        Plane(boolean v) {
            isVertical = v;
        }
    }
}
