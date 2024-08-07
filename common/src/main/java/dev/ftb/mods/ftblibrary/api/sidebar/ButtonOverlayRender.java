package dev.ftb.mods.ftblibrary.api.sidebar;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Supplier;

public interface ButtonOverlayRender {

    /**
     * @param graphics The graphics object
     * @param font The font object
     * @param buttonSize The size of the button
     * Called when the button is rendering
     * graphics is aligned so that 0, 0 is the top left corner of the button
     */
    void render(GuiGraphics graphics, Font font, int buttonSize);

    static ButtonOverlayRender ofSimpleString(Supplier<String> customTextHandler) {
        return (graphics, font, buttonSize) -> {
            String text = customTextHandler.get();
            if (!text.isEmpty()) {
                var nw = font.width(text);
                Color4I.LIGHT_RED.draw(graphics, buttonSize - nw, -1, nw + 1, 9);
                graphics.drawString(font, text, buttonSize - nw + 1, 0, 0xFFFFFFFF);
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            }
        };
    }
}
