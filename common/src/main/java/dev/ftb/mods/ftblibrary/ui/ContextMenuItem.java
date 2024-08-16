package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ContextMenuItem implements Comparable<ContextMenuItem> {
    public static final ContextMenuItem SEPARATOR = new ContextMenuItem(Component.empty(), Icon.empty(), b -> {
    }) {
        @Override
        public Widget createWidget(ContextMenu panel) {
            return new ContextMenu.CSeparator(panel);
        }
    };

    private final Component title;
    private final Icon icon;
    private final Consumer<Button> callback;

    private boolean enabled = true;
    private Component yesNoText = Component.literal("");
    private boolean closeMenu = true;

    public ContextMenuItem(Component title, Icon icon, @Nullable Consumer<Button> callback) {
        this.title = title;
        this.icon = icon;
        this.callback = callback;
    }

    public static ContextMenuItem title(Component title) {
        return new ContextMenuItem(title, Icon.empty(), null).setCloseMenu(false);
    }

    public static ContextMenuItem subMenu(Component title, Icon icon, List<ContextMenuItem> subItems) {
        return new ContextMenuItem(title, icon, button -> {
            ContextMenu subMenu = new ContextMenu(button.getParent(), subItems);
            button.getGui().openContextMenu(subMenu);
            int xPos = button.getPosX() + button.width;
            int yPos = button.getPosY();
            if (button.getX() + button.width + subMenu.width >= button.getScreen().getGuiScaledWidth()) {
                xPos = button.getPosX() - subMenu.width;
            }
            if (button.getY() + subMenu.height >= button.getScreen().getGuiScaledHeight()) {
                yPos -= subMenu.height - button.getScreen().getGuiScaledHeight();
            }
            subMenu.setPos(xPos, yPos);
        }).setCloseMenu(false);
    }

    public static ContextMenuItem separator() {
        return SEPARATOR;
    }

    public void addMouseOverText(TooltipList list) {
    }

    public void drawIcon(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        getIcon().draw(graphics, x, y, w, h);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ContextMenuItem setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Component getTitle() {
        return title;
    }

    public Icon getIcon() {
        return icon;
    }

    public Component getYesNoText() {
        return yesNoText;
    }

    public ContextMenuItem setYesNoText(Component s) {
        yesNoText = s;
        return this;
    }

    public ContextMenuItem setCloseMenu(boolean v) {
        closeMenu = v;
        return this;
    }

    public boolean isClickable() {
        return callback != null;
    }

    public Widget createWidget(ContextMenu panel) {
        return new ContextMenu.CButton(panel, this);
    }

    @Override
    public int compareTo(ContextMenuItem o) {
        return getTitle().getString().compareToIgnoreCase(o.getTitle().getString());
    }

    public void onClicked(Button button, Panel panel, MouseButton mouseButton) {
        if (closeMenu) {
            panel.getGui().closeContextMenu();
        }

        if (callback != null) {
            callback.accept(button);
        }
    }
}
