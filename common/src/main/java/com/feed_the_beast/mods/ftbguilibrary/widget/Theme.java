package com.feed_the_beast.mods.ftbguilibrary.widget;

import com.feed_the_beast.mods.ftbguilibrary.FTBGUILibrary;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ImageIcon;
import com.feed_the_beast.mods.ftbguilibrary.icon.PartIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.Bits;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class Theme {
    public static final Theme DEFAULT = new Theme();
    public static boolean renderDebugBoxes = false;

    public static final int DARK = 1;
    public static final int SHADOW = 2;
    public static final int CENTERED = 4;
    public static final int UNICODE = 8;
    public static final int MOUSE_OVER = 16;
    public static final int CENTERED_V = 32;

    private static final Color4I CONTENT_COLOR_MOUSE_OVER = Color4I.rgb(16777120);
    private static final Color4I CONTENT_COLOR_DISABLED = Color4I.rgb(10526880);
    private static final Color4I CONTENT_COLOR_DARK = Color4I.rgb(4210752);

    public static final ImageIcon BACKGROUND_SQUARES = (ImageIcon) Icon.getIcon(FTBGUILibrary.MOD_ID + ":textures/gui/background_squares.png");
    private static final ImageIcon TEXTURE_BEACON = (ImageIcon) Icon.getIcon("textures/gui/container/beacon.png");
    private static final ImageIcon TEXTURE_WIDGETS = (ImageIcon) Icon.getIcon("textures/gui/widgets.png");
    private static final ImageIcon TEXTURE_RECIPE_BOOK = (ImageIcon) Icon.getIcon("textures/gui/recipe_book.png");
    private static final ImageIcon TEXTURE_ENCHANTING_TABLE = (ImageIcon) Icon.getIcon("textures/gui/container/enchanting_table.png");

    private static final Icon GUI = new PartIcon(TEXTURE_RECIPE_BOOK, 82, 208, 32, 32, 8);
    private static final Icon GUI_MOUSE_OVER = GUI.withTint(Color4I.rgb(0xAFB6DA));

    private static final Icon BUTTON = new PartIcon(TEXTURE_WIDGETS, 0, 66, 200, 20, 4);
    private static final Icon BUTTON_MOUSE_OVER = new PartIcon(TEXTURE_WIDGETS, 0, 86, 200, 20, 4);
    private static final Icon BUTTON_DISABLED = new PartIcon(TEXTURE_WIDGETS, 0, 46, 200, 20, 4);

    private static final Icon WIDGET = new PartIcon(TEXTURE_BEACON, 0, 219, 22, 22, 6);
    private static final Icon WIDGET_MOUSE_OVER = new PartIcon(TEXTURE_BEACON, 66, 219, 22, 22, 6);
    private static final Icon WIDGET_DISABLED = new PartIcon(TEXTURE_BEACON, 44, 219, 22, 22, 6);

    private static final Icon SLOT = new PartIcon(TEXTURE_BEACON, 35, 136, 18, 18, 3);
    private static final Icon SLOT_MOUSE_OVER = SLOT.combineWith(Color4I.WHITE.withAlpha(33));

    private static final Icon SCROLL_BAR_BG = SLOT;
    private static final Icon SCROLL_BAR_BG_DISABLED = SCROLL_BAR_BG.withTint(Color4I.BLACK.withAlpha(100));

    private static final Icon TEXT_BOX = new PartIcon(TEXTURE_ENCHANTING_TABLE, 0, 185, 108, 19, 6);

    private static final Icon TAB_H_UNSELECTED = TEXTURE_RECIPE_BOOK.withUV(150, 2, 35, 26, 256, 256);
    private static final Icon TAB_H_SELECTED = TEXTURE_RECIPE_BOOK.withUV(188, 2, 35, 26, 256, 256);

    private final BooleanStack fontUnicode = new BooleanArrayList();

    public Color4I getContentColor(WidgetType type) {
        return type == WidgetType.MOUSE_OVER ? CONTENT_COLOR_MOUSE_OVER : type == WidgetType.DISABLED ? CONTENT_COLOR_DISABLED : Color4I.WHITE;
    }

    public Color4I getInvertedContentColor() {
        return CONTENT_COLOR_DARK;
    }

    public void drawGui(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type) {
        (type == WidgetType.MOUSE_OVER ? GUI_MOUSE_OVER : GUI).draw(matrixStack, x, y, w, h);
    }

    public void drawWidget(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type) {
        (type == WidgetType.MOUSE_OVER ? WIDGET_MOUSE_OVER : type == WidgetType.DISABLED ? WIDGET_DISABLED : WIDGET).draw(matrixStack, x, y, w, h);
    }

    public void drawSlot(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type) {
        (type == WidgetType.MOUSE_OVER ? SLOT_MOUSE_OVER : SLOT).draw(matrixStack, x, y, w, h);
    }

    public void drawContainerSlot(PoseStack matrixStack, int x, int y, int w, int h) {
        SLOT.draw(matrixStack, x - 1, y - 1, w + 2, h + 2);
    }

    public void drawButton(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type) {
        (type == WidgetType.MOUSE_OVER ? BUTTON_MOUSE_OVER : type == WidgetType.DISABLED ? BUTTON_DISABLED : BUTTON).draw(matrixStack, x, y, w, h);
    }

    public void drawScrollBarBackground(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type) {
        (type == WidgetType.DISABLED ? SCROLL_BAR_BG_DISABLED : SCROLL_BAR_BG).draw(matrixStack, x, y, w, h);
    }

    public void drawScrollBar(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type, boolean vertical) {
        (type == WidgetType.MOUSE_OVER ? WIDGET_MOUSE_OVER : WIDGET).draw(matrixStack, x + 1, y + 1, w - 2, h - 2);
    }

    public void drawTextBox(PoseStack matrixStack, int x, int y, int w, int h) {
        TEXT_BOX.draw(matrixStack, x, y, w, h);
    }

    public void drawCheckboxBackground(PoseStack matrixStack, int x, int y, int w, int h, boolean radioButton) {
        drawSlot(matrixStack, x, y, w, h, WidgetType.NORMAL);
    }

    public void drawCheckbox(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type, boolean selected, boolean radioButton) {
        if (selected) {
            drawWidget(matrixStack, x, y, w, h, type);
        }
    }

    public void drawPanelBackground(PoseStack matrixStack, int x, int y, int w, int h) {
        drawContainerSlot(matrixStack, x, y, w, h);
    }

    public void drawHorizontalTab(PoseStack matrixStack, int x, int y, int w, int h, boolean selected) {
        (selected ? TAB_H_SELECTED : TAB_H_UNSELECTED).draw(matrixStack, x, y, w, h);
    }

    public void drawContextMenuBackground(PoseStack matrixStack, int x, int y, int w, int h) {
        drawGui(matrixStack, x, y, w, h, WidgetType.NORMAL);
        Color4I.BLACK.withAlpha(90).draw(matrixStack, x, y, w, h);
    }

    public Font getFont() {
        return Minecraft.getInstance().font;
    }

    public final int getStringWidth(FormattedText text) {
        return text == TextComponent.EMPTY ? 0 : getFont().width(text);
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
        if (width <= 0 || text == TextComponent.EMPTY) {
            return Collections.emptyList();
        }

        return getFont().getSplitter().splitLines(text, width, Style.EMPTY);
    }

    public final int drawString(PoseStack matrixStack, @Nullable Object text, float x, float y, Color4I color, int flags) {
        if (text == null || text == FormattedCharSequence.EMPTY || text == TextComponent.EMPTY || (text instanceof String && ((String) text).isEmpty()) || color.isEmpty()) {
            return 0;
        }

        if (text instanceof FormattedCharSequence) {
            if (Bits.getFlag(flags, CENTERED)) {
                x -= getStringWidth((FormattedCharSequence) text) / 2F;
            }

            int i;

            if (Bits.getFlag(flags, SHADOW)) {
                i = getFont().drawShadow(matrixStack, (FormattedCharSequence) text, x, y, color.rgba());
            } else {
                i = getFont().draw(matrixStack, (FormattedCharSequence) text, x, y, color.rgba());
            }

            RenderSystem.color4f(1F, 1F, 1F, 1F);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableBlend();
            return i;
        } else if (text instanceof Component) {
            if (Bits.getFlag(flags, CENTERED)) {
                x -= getStringWidth((Component) text) / 2F;
            }

            int i;

            if (Bits.getFlag(flags, SHADOW)) {
                i = getFont().drawShadow(matrixStack, (Component) text, x, y, color.rgba());
            } else {
                i = getFont().draw(matrixStack, (Component) text, x, y, color.rgba());
            }

            RenderSystem.color4f(1F, 1F, 1F, 1F);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableBlend();
            return i;
        } else if (text instanceof FormattedText) {
            return drawString(matrixStack, Language.getInstance().getVisualOrder((FormattedText) text), x, y, color, flags);
        } else {
            String s = String.valueOf(text);

            if (Bits.getFlag(flags, CENTERED)) {
                x -= getStringWidth(s) / 2F;
            }

            int i;

            if (Bits.getFlag(flags, SHADOW)) {
                i = getFont().drawShadow(matrixStack, s, x, y, color.rgba());
            } else {
                i = getFont().draw(matrixStack, s, x, y, color.rgba());
            }

            RenderSystem.color4f(1F, 1F, 1F, 1F);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableBlend();
            return i;
        }
    }

    public final int drawString(PoseStack matrixStack, @Nullable Object text, int x, int y, int flags) {
        return drawString(matrixStack, text, x, y, getContentColor(WidgetType.mouseOver(Bits.getFlag(flags, MOUSE_OVER))), flags);
    }

    public final int drawString(PoseStack matrixStack, @Nullable Object text, int x, int y) {
        return drawString(matrixStack, text, x, y, getContentColor(WidgetType.NORMAL), 0);
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