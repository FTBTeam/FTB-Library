package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.icon.PartIcon;
import dev.ftb.mods.ftblibrary.math.Bits;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;


public class Theme {
    public static final Theme DEFAULT = new Theme();
    public static final int DARK = 1;
    public static final int SHADOW = 2;
    public static final int CENTERED = 4;
    public static final int UNICODE = 8;
    public static final int MOUSE_OVER = 16;
    public static final int CENTERED_V = 32;
    public static final ImageIcon BACKGROUND_SQUARES = (ImageIcon) Icon.getIcon(FTBLibrary.MOD_ID + ":textures/gui/background_squares.png");
    private static final Color4I CONTENT_COLOR_MOUSE_OVER = Color4I.rgb(0xFFFFA0);
    private static final Color4I CONTENT_COLOR_DISABLED = Color4I.rgb(0xA0A0A0);
    private static final Color4I CONTENT_COLOR_DARK = Color4I.rgb(0x404040);
    private static final ImageIcon TEXTURE_BEACON = (ImageIcon) Icon.getIcon("textures/gui/container/beacon.png");
    private static final ImageIcon TEXTURE_RECIPE_BOOK = (ImageIcon) Icon.getIcon("textures/gui/recipe_book.png");
    private static final Icon GUI = new PartIcon("ftblibrary:textures/gui/background.png",
            0, 0, 16, 16, 4, 16, 16);
    private static final Icon GUI_MOUSE_OVER = GUI.withTint(Color4I.rgb(0xAFB6DA));
    private static final Icon BUTTON = PartIcon.wholeTexture("textures/gui/sprites/widget/button.png",
            200, 20, 4);
    private static final Icon BUTTON_MOUSE_OVER = PartIcon.wholeTexture("textures/gui/sprites/widget/button_highlighted.png",
            200, 20, 4);
    private static final Icon BUTTON_DISABLED = PartIcon.wholeTexture("textures/gui/sprites/widget/button_disabled.png",
            200, 20, 4);
    private static final Icon WIDGET = PartIcon.wholeTexture("textures/gui/sprites/container/beacon/button.png",
            22, 22, 4);
    private static final Icon WIDGET_MOUSE_OVER = PartIcon.wholeTexture("textures/gui/sprites/container/beacon/button_highlighted.png",
            22, 22, 4);
    private static final Icon WIDGET_DISABLED = PartIcon.wholeTexture("textures/gui/sprites/container/beacon/button_disabled.png",
            22, 22, 4);
    private static final Icon SCROLLER = PartIcon.wholeTexture("textures/gui/sprites/widget/slider_handle.png",
            8, 20, 2);
    private static final Icon SCROLLER_MOUSE_OVER = PartIcon.wholeTexture("textures/gui/sprites/widget/slider_handle_highlighted.png",
            8, 20, 2);
    private static final Icon SLOT = new PartIcon(TEXTURE_BEACON, 35, 136, 18, 18, 3);
    private static final Icon SLOT_MOUSE_OVER = SLOT.combineWith(Color4I.WHITE.withAlpha(33));
    private static final Icon SCROLL_BAR_BG = SLOT;
    private static final Icon SCROLL_BAR_BG_DISABLED = SCROLL_BAR_BG.withTint(Color4I.BLACK.withAlpha(100));
    private static final Icon TEXT_BOX = PartIcon.wholeTexture("textures/gui/sprites/container/enchanting_table/enchantment_slot_disabled.png",
            108, 19, 4);
    private static final Icon TAB_H_UNSELECTED = TEXTURE_RECIPE_BOOK.withUV(150, 2, 35, 26, 256, 256);
    private static final Icon TAB_H_SELECTED = TEXTURE_RECIPE_BOOK.withUV(188, 2, 35, 26, 256, 256);
    public static boolean renderDebugBoxes = false;
    private final BooleanStack fontUnicode = new BooleanArrayList();

    public Color4I getContentColor(WidgetType type) {
        return type == WidgetType.MOUSE_OVER ? CONTENT_COLOR_MOUSE_OVER : type == WidgetType.DISABLED ? CONTENT_COLOR_DISABLED : Color4I.WHITE;
    }

    public Color4I getInvertedContentColor() {
        return CONTENT_COLOR_DARK;
    }

    public void drawGui(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
        (type == WidgetType.MOUSE_OVER ? GUI_MOUSE_OVER : GUI).draw(graphics, x - 3, y - 3, w + 6, h + 6);
    }

    public void drawWidget(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
        (type == WidgetType.MOUSE_OVER ? WIDGET_MOUSE_OVER : type == WidgetType.DISABLED ? WIDGET_DISABLED : WIDGET).draw(graphics, x, y, w, h);
    }

    public void drawSlot(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
        (type == WidgetType.MOUSE_OVER ? SLOT_MOUSE_OVER : SLOT).draw(graphics, x, y, w, h);
    }

    public void drawContainerSlot(GuiGraphics graphics, int x, int y, int w, int h) {
        SLOT.draw(graphics, x - 1, y - 1, w + 2, h + 2);
    }

    public void drawButton(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
        (type == WidgetType.MOUSE_OVER ? BUTTON_MOUSE_OVER : type == WidgetType.DISABLED ? BUTTON_DISABLED : BUTTON).draw(graphics, x, y, w, h);
    }

    public void drawScrollBarBackground(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
        (type == WidgetType.DISABLED ? SCROLL_BAR_BG_DISABLED : SCROLL_BAR_BG).draw(graphics, x, y, w, h);
    }

    public void drawScrollBar(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type, boolean vertical) {
        (type == WidgetType.MOUSE_OVER ? SCROLLER_MOUSE_OVER : SCROLLER).draw(graphics, x + 1, y + 1, w - 2, h - 2);
    }

    public void drawTextBox(GuiGraphics graphics, int x, int y, int w, int h) {
        TEXT_BOX.draw(graphics, x, y, w, h);
    }

    public void drawCheckboxBackground(GuiGraphics graphics, int x, int y, int w, int h, boolean radioButton) {
        drawSlot(graphics, x, y, w, h, WidgetType.NORMAL);
    }

    public void drawCheckbox(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type, boolean selected, boolean radioButton) {
        if (selected) {
            drawWidget(graphics, x, y, w, h, type);
        }
    }

    public void drawPanelBackground(GuiGraphics graphics, int x, int y, int w, int h) {
        Color4I.rgb(0x8B8B8B).draw(graphics, x, y, w, h);
    }

    public void drawHorizontalTab(GuiGraphics graphics, int x, int y, int w, int h, boolean selected) {
        (selected ? TAB_H_SELECTED : TAB_H_UNSELECTED).draw(graphics, x, y, w, h);
    }

    public void drawContextMenuBackground(GuiGraphics graphics, int x, int y, int w, int h) {
        drawGui(graphics, x, y, w, h, WidgetType.NORMAL);
        Color4I.BLACK.withAlpha(90).draw(graphics, x, y, w, h);
    }

    public Font getFont() {
        return Minecraft.getInstance().font;
    }

    public final int getStringWidth(FormattedText text) {
        return text == Component.EMPTY ? 0 : getFont().width(text);
    }

    public final int getStringWidth(FormattedCharSequence text) {
        return text == FormattedCharSequence.EMPTY ? 0 : getFont().width(text);
    }

    public final int getStringWidth(String text) {
        return text.isEmpty() ? 0 : getFont().width(text);
    }

    public final int getFontHeight() {
        return getFont().lineHeight;
    }

    public final String trimStringToWidth(String text, int width) {
        return text.isEmpty() || width <= 0 ? "" : getFont().plainSubstrByWidth(text, width, false);
    }

    public final FormattedText trimStringToWidth(FormattedText text, int width) {
        return getFont().substrByWidth(text, width);
    }

    public final String trimStringToWidthReverse(String text, int width) {
        return text.isEmpty() || width <= 0 ? "" : getFont().plainSubstrByWidth(text, width, true);
    }

    public final List<FormattedText> listFormattedStringToWidth(FormattedText text, int width) {
        if (width <= 0 || text == Component.EMPTY) {
            return Collections.emptyList();
        }

        return getFont().getSplitter().splitLines(text, width, Style.EMPTY);
    }

    public final int drawString(GuiGraphics graphics, @Nullable Object text, int x, int y, Color4I color, int flags) {
        if (text == null || text == FormattedCharSequence.EMPTY || text == Component.EMPTY || (text instanceof String s && s.isEmpty()) || color.isEmpty()) {
            return x;
        }

        switch (text) {
            case FormattedCharSequence fcs -> {
                if (Bits.getFlag(flags, CENTERED)) {
                    x -= getStringWidth(fcs) / 2;
                }
                int i = graphics.drawString(getFont(), (FormattedCharSequence) text, x, y, color.rgba(), Bits.getFlag(flags, SHADOW));
                GuiHelper.setupDrawing();
                return i;
            }
            case Component comp -> {
                if (Bits.getFlag(flags, CENTERED)) {
                    x -= getStringWidth(comp) / 2;
                }
                int i = graphics.drawString(getFont(), comp, x, y, color.rgba(), Bits.getFlag(flags, SHADOW));
                GuiHelper.setupDrawing();
                return i;
            }
            case FormattedText formattedText -> {
                return drawString(graphics, Language.getInstance().getVisualOrder(formattedText), x, y, color, flags);
            }
            default -> {
                var s = String.valueOf(text);
                if (Bits.getFlag(flags, CENTERED)) {
                    x -= getStringWidth(s) / 2;
                }
                int i = graphics.drawString(getFont(), s, x, y, color.rgba(), Bits.getFlag(flags, SHADOW));
                GuiHelper.setupDrawing();
                return i;
            }
        }
    }

    public final int drawString(GuiGraphics graphics, @Nullable Object text, int x, int y, int flags) {
        return drawString(graphics, text, x, y, getContentColor(WidgetType.mouseOver(Bits.getFlag(flags, MOUSE_OVER))), flags);
    }

    public final int drawString(GuiGraphics graphics, @Nullable Object text, int x, int y) {
        return drawString(graphics, text, x, y, getContentColor(WidgetType.NORMAL), 0);
    }

	/*
	public List<GuiBase.PositionedTextData> createDataFrom(ITextComponent component, int width)
	{
		if (width <= 0 || component.getString().isEmpty())
		{
			return Collections.emptyList();
		}

		List<GuiBase.PositionedTextData> list = new ArrayList<>();

		int line = 0;
		int currentWidth = 0;

		for (IFormattableTextComponent t : component.deepCopy())
		{
			String text = t.getUnformattedComponentText();
			int textWidth = getStringWidth(text);

			while (textWidth > 0)
			{
				int w = textWidth;
				if (w > width - currentWidth)
				{
					w = width - currentWidth;
				}

				list.add(new GuiBase.PositionedTextData(currentWidth, line * 10, w, 10, t.getStyle()));

				currentWidth += w;
				textWidth -= w;

				if (currentWidth >= width)
				{
					currentWidth = 0;
					line++;
				}
			}
		}

		return list;
	}
	 */
}
