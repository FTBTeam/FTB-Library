package dev.ftb.mods.ftblibrary.ui.misc;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.mods.ftblibrary.ui.layout.Align;
import dev.ftb.mods.ftblibrary.ui.layout.Insets;
import dev.ftb.mods.ftblibrary.ui.layout.LayoutHints;
import net.minecraft.client.gui.GuiGraphics;

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
        // both buttons hug the right edge and are vertically centred; accept sits to the left of cancel
        LayoutHints rightCentred = LayoutHints.DEFAULT.withHAlign(Align.END).withVAlign(Align.CENTER);
        buttonCancel.setLayoutHints(rightCentred.withMargin(new Insets(0, 5, 0, 0)));
        buttonAccept.setLayoutHints(rightCentred.withMargin(new Insets(0, buttonCancel.width + 10, 0, 0)));
        align(WidgetLayout.stack().build());
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawPanelBackground(graphics, x, y, w, h);
        Color4I.GRAY.withAlpha(64).draw(graphics, x, y, w, 1);
    }
}
