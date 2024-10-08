package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

import java.util.Optional;


public class TextField extends Widget {
    public Component component = Component.empty();  // TODO remove this in MC 1.21
    public int textFlags = 0;
    public int minWidth = 0;
    public int maxWidth = 5000;
    public int textSpacing = 10;
    public float scale = 1.0F;
    public Color4I textColor = Icon.empty();
    public boolean trim = false;
    private FormattedText[] formattedText = new FormattedText[0];
    private Component rawText = Component.empty();
    private boolean tooltip = false;

    public TextField(Panel panel) {
        super(panel);
    }

    public TextField addFlags(int flags) {
        textFlags |= flags;
        return this;
    }

    public TextField setMinWidth(int width) {
        minWidth = width;
        return this;
    }

    public TextField setMaxWidth(int width) {
        maxWidth = width;
        return this;
    }

    public TextField setColor(Color4I color) {
        textColor = color;
        return this;
    }

    public TextField setScale(float s) {
        scale = s;
        return this;
    }

    public TextField setSpacing(int s) {
        textSpacing = s;
        return this;
    }

    public TextField setTrim() {
        trim = true;
        return this;
    }

    public TextField showTooltipForLongText() {
        tooltip = true;
        return this;
    }

    public TextField setText(Component txt) {
        var theme = getGui().getTheme();

        rawText = txt;
        formattedText = theme.listFormattedStringToWidth(Component.literal("").append(txt), maxWidth).toArray(new FormattedText[0]);

        return resize(theme);
    }

    public TextField setText(String txt) {
        return setText(Component.literal(txt));
    }

    public TextField resize(Theme theme) {
        setWidth(0);

        for (var s : getDisplayedText()) {
            setWidth(Math.max(width, (int) ((float) theme.getStringWidth(s) * scale)));
        }

        setWidth(Mth.clamp(width, minWidth, maxWidth));
        setHeight((int) ((float) (Math.max(1, formattedText.length) * textSpacing - (textSpacing - theme.getFontHeight() + 1)) * scale));
        return this;
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (tooltip && formattedText.length > 1) {
            list.add(rawText);
        }
    }

    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
    }

    private FormattedText[] getDisplayedText() {
        return trim && formattedText.length > 0 ? new FormattedText[]{formattedText[0]} : formattedText;
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        drawBackground(graphics, theme, x, y, w, h);

        if (formattedText.length != 0) {
            var centered = Bits.getFlag(textFlags, Theme.CENTERED);
            var centeredV = Bits.getFlag(textFlags, Theme.CENTERED_V);
            var col = textColor;
            if (col.isEmpty()) {
                col = theme.getContentColor(WidgetType.mouseOver(Bits.getFlag(textFlags, Theme.MOUSE_OVER)));
            }

            var tx = x + (centered ? w / 2 : 0);
            var ty = y + (centeredV ? (h - theme.getFontHeight()) / 2 : 0);
            int i;
            if (scale == 1.0F) {
                for (i = 0; i < getDisplayedText().length; ++i) {
                    theme.drawString(graphics, formattedText[i], tx, ty + i * textSpacing, col, textFlags);
                }
            } else {
                graphics.pose().pushPose();
                graphics.pose().translate(tx, ty, 0.0D);
                graphics.pose().scale(scale, scale, 1.0F);

                for (i = 0; i < getDisplayedText().length; ++i) {
                    theme.drawString(graphics, formattedText[i], 0, i * textSpacing, col, textFlags);
                }

                graphics.pose().popPose();
            }
        }
    }

    public Optional<Style> getComponentStyleAt(Theme theme, int mouseX, int mouseY) {
        int line = (mouseY - getY()) / theme.getFontHeight();
        if (line >= 0 && line < getDisplayedText().length) {
            boolean centered = Bits.getFlag(textFlags, Theme.CENTERED);
            int textWidth = theme.getFont().width(formattedText[line]);
            int xStart = centered ? getX() + (width - textWidth) / 2 : getX();
            if (mouseX >= xStart && mouseX <= xStart + textWidth) {
                return Optional.ofNullable(theme.getFont().getSplitter().componentStyleAtWidth(formattedText[line], mouseX - xStart));
            }
        }
        return Optional.empty();
    }
}
