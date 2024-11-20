package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SimpleButton extends Button {
    private Callback consumer;
    private final List<Component> tooltip;

    public SimpleButton(Panel panel, Component text, Icon icon, Callback c) {
        super(panel, text, icon);
        consumer = c;
        tooltip = List.of();
    }

    public SimpleButton(Panel panel, List<Component> text, Icon icon, Callback c) {
        super(panel, text.isEmpty() ? Component.empty() : text.get(0), icon);
        consumer = c;
        tooltip = text;
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (tooltip.isEmpty()) {
            super.addMouseOverText(list);
        } else {
            tooltip.forEach(list::add);
        }
    }

    public void setConsumer(Callback consumer) {
        this.consumer = consumer;
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
    }

    @Override
    public void onClicked(MouseButton button) {
        playClickSound();
        if (consumer != null) {
            consumer.onClicked(this, button);
        }
    }

    public interface Callback {
        void onClicked(SimpleButton widget, MouseButton button);
    }
}
