package dev.ftb.mods.ftblibrary.ui.misc;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import net.minecraft.client.gui.GuiGraphics;

import static dev.ftb.mods.ftblibrary.util.TextComponentUtils.hotkeyTooltip;

public class LowerButtonPanel extends Panel {
    private final Button buttonAccept, buttonCancel;

    public LowerButtonPanel(Panel parent, Runnable onAccept, Runnable onCancel) {
        super(parent);

        buttonAccept = SimpleTextButton.accept(this, mb -> onAccept.run(), hotkeyTooltip("⇧ + Enter"));
        buttonCancel = SimpleTextButton.cancel(this, mb -> onCancel.run(), hotkeyTooltip("ESC"));
    }

    @Override
    public void addWidgets() {
        add(buttonAccept);
        add(buttonCancel);
    }

    @Override
    public void alignWidgets() {
        buttonCancel.setPos(width - buttonCancel.width - 5, 2);
        buttonAccept.setPos(buttonCancel.posX - buttonAccept.width - 5, 2);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawPanelBackground(graphics, x, y, w, h);
        Color4I.GRAY.withAlpha(64).draw(graphics, x, y, w, 1);
    }
}
