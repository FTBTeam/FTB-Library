package dev.ftb.mods.ftblibrary.client.gui.widget;

import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.BooleanSupplier;

import static dev.ftb.mods.ftblibrary.util.TextComponentUtils.hotkeyTooltip;

public class LowerButtonPanel extends Panel {
    private final Button buttonAccept, buttonCancel;

    public LowerButtonPanel(Panel parent, Runnable onAccept, Runnable onCancel) {
        this(parent, onAccept, onCancel, () -> true);
    }

    public LowerButtonPanel(Panel parent, Runnable onAccept, Runnable onCancel, BooleanSupplier acceptEnabledWhen) {
        super(parent);

        buttonAccept = SimpleTextButton.acceptBuilder(this, mb -> onAccept.run(), hotkeyTooltip("⇧ + Enter"))
                .enabledWhen(acceptEnabledWhen)
                .build();
        buttonCancel = SimpleTextButton.cancel(this, mb -> onCancel.run(), hotkeyTooltip("ESC"));
    }

    @Override
    public void addWidgets() {
        add(buttonAccept);
        add(buttonCancel);
    }

    @Override
    public void alignWidgets() {
        buttonCancel.setPos(width - buttonCancel.width - 5, (height - buttonCancel.height) / 2);
        buttonAccept.setPos(buttonCancel.posX - buttonAccept.width - 5, buttonCancel.getPosY());
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawPanelBackground(graphics, x, y, w, h);
        IconHelper.renderIcon(Color4I.GRAY.withAlpha(64), graphics, x, y, w, 1);
    }
}
