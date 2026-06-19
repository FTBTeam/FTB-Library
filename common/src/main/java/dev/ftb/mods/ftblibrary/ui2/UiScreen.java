package dev.ftb.mods.ftblibrary.ui2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

public class UiScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger(UiScreen.class);

    private final Layout layout;
    private final Deque<Layout> nestedLayouts = new ArrayDeque<>();

    // TODO: Private is likely not ideal here as this will almost definitely get more details.
    private UiScreen(String layout) {
        super(Component.empty());

        this.layout = LayoutLoader.INSTANCE.findTemplate(layout);
        if (this.layout == null) {
            // TODO: throw error screen instead of crashing the game
            throw new IllegalArgumentException("Layout not found: " + layout);
        }
    }

    public static void openScreen(String layout) {
        // What thread are we on? If it's not the main thread, defer.
        if (Minecraft.getInstance().isSameThread()) {
            _openScreen(layout);
        } else {
            Minecraft.getInstance().execute(() -> _openScreen(layout));
        }
    }

    private static void _openScreen(String layout) {
        try {
            UiScreen screen = new UiScreen(layout);
            Minecraft.getInstance().setScreen(screen);
        } catch (Exception e) {
            LOGGER.error("Failed to open UI screen for layout: {}", layout, e);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);

        guiGraphics.drawString(Minecraft.getInstance().font, "Layout: " + layout.name(), 10, 10, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
