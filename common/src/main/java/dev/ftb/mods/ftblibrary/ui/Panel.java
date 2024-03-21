package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
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

	public void setOffset(boolean flag) {
		if (flag) {
			offsetX = (int) -scrollX;
			offsetY = (int) -scrollY;
		} else {
			offsetX = offsetY = 0;
		}
	}

	public boolean isOffset() {
		return offsetX != 0 || offsetY != 0;
	}

	public void setScrollX(double scroll) {
		scrollX = scroll;
	}

	public void setScrollY(double scroll) {
		scrollY = scroll;
	}

	public double getScrollX() {
		return scrollX;
	}

	public double getScrollY() {
		return scrollY;
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		var renderInside = getOnlyRenderWidgetsInside();

		drawBackground(graphics, theme, x, y, w, h);

		if (renderInside) {
			GuiHelper.pushScissor(getScreen(), x, y, w, h);
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
			GuiHelper.popScissor(getScreen());
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
	public boolean mouseScrolled(double scroll) {
		setOffset(true);

		for (var i = widgets.size() - 1; i >= 0; i--) {
			var widget = widgets.get(i);

			if (widget.isEnabled() && widget.mouseScrolled(scroll)) {
				setOffset(false);
				return true;
			}
		}

		var scrollPanel = scrollPanel(scroll);
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

	public void setScrollStep(double s) {
		scrollStep = s;
	}

	public double getScrollStep() {
		return scrollStep;
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