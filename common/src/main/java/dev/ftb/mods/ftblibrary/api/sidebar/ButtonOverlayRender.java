package dev.ftb.mods.ftblibrary.api.sidebar;

import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Supplier;

public interface ButtonOverlayRender {

    static ButtonOverlayRender ofSimpleString(Supplier<String> customTextHandler) {
        return (graphics, font, buttonSize) -> {
            String text = customTextHandler.get();
            if (!text.isEmpty()) {
                var nw = font.width(text);
                IconHelper.renderIcon(Color4I.LIGHT_RED, graphics, buttonSize - nw, -1, nw + 1, 9);
                graphics.text(font, text, buttonSize - nw + 1, 0, 0xFFFFFFFF);
            }
        };
    }

    /**
     * Called when the button is rendering
     * graphics is aligned so that 0, 0 is the top left corner of the button
     * @param graphics The graphics object
     * @param font The font object
     * @param buttonSize The size of the button
     */
    void render(GuiGraphicsExtractor graphics, Font font, int buttonSize);
}
