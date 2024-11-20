package dev.ftb.mods.ftblibrary.ui.misc;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.ftb.mods.ftblibrary.util.TextComponentUtils.hotkeyTooltip;

public abstract class AbstractGroupedButtonListScreen<G, E> extends AbstractButtonListScreen {
    protected final List<GroupData<G, E>> groups;
    private final Set<G> collapsed = new HashSet<>();

    private final Button buttonCollapseAll, buttonExpandAll;
    private final Component title;

    public AbstractGroupedButtonListScreen(Component title) {
        showBottomPanel(false);
        showCloseButton(true);
        setHasSearchBox(true);
        this.title = title;
        this.groups = getGroups();

        groups.stream()
                .filter(GroupData::defaultedCollapsed)
                .map(GroupData::group)
                .forEach(collapsed::add);

        buttonExpandAll = new SimpleButton(topPanel, List.of(Component.translatable("gui.expand_all"), hotkeyTooltip("="), hotkeyTooltip("+")), Icons.UP,
                (widget, button) -> toggleAll(false));
        buttonCollapseAll = new SimpleButton(topPanel, List.of(Component.translatable("gui.collapse_all"), hotkeyTooltip("-")), Icons.DOWN,
                (widget, button) -> toggleAll(true));
    }

    protected abstract List<GroupData<G, E>> getGroups();

    protected GroupButton createGroupButton(Panel panel, GroupData<G, E> group) {
        return new GroupButton(panel, group.group(), group.groupName, group.values());
    }

    protected abstract RowPanel createRowPanel(Panel panel, E value);

    private void toggleAll(boolean collapsed) {
        boolean allOpen = this.groups.stream().noneMatch(g -> isCollapsed(g.group));
        // Don't try and re-render if everything is already open
        if (allOpen && !collapsed) {
            return;
        }
        this.groups.forEach(group -> setCollapsed(group.group(), collapsed));
        scrollBar.setValue(0);
        getGui().refreshWidgets();
    }

    @Override
    protected void doCancel() {

    }

    @Override
    protected void doAccept() {

    }

    @Override
    public boolean onInit() {
        setWidth(220);
        setHeight(getScreen().getGuiScaledHeight() * 4 / 5);
        return true;
    }

    @Override
    public boolean keyPressed(Key key) {
        if (super.keyPressed(key)) {
            return true;
        } else if (key.is(InputConstants.KEY_ADD) || key.is(InputConstants.KEY_EQUALS)) {
            toggleAll(false);
        } else if (key.is(InputConstants.KEY_MINUS) || key.is(GLFW.GLFW_KEY_KP_SUBTRACT)) {
            toggleAll(true);
        }
        return false;
    }

    @Override
    protected int getTopPanelHeight() {
        return 22;
    }

    @Override
    protected Panel createTopPanel() {
        return new CustomTopPanel();
    }

    @Override
    public void addButtons(Panel panel) {
        for (GroupData<G, E> group : groups) {
            GroupButton groupButton = createGroupButton(panel, group);
            panel.add(groupButton);
            if (!groupButton.isCollapsed()) {
                panel.addAll(groupButton.collectPanels());
            }
        }
    }


    public boolean isCollapsed(G group) {
        return collapsed.contains(group);
    }

    public void setCollapsed(G group, boolean collapsed) {
        if (collapsed) {
            this.collapsed.add(group);
        } else {
            this.collapsed.remove(group);
        }
    }

    public record GroupData<G, E>(G group, boolean defaultedCollapsed, Component groupName, List<E> values) {
    }

    protected class CustomTopPanel extends TopPanel {
        private final TextField titleLabel = new TextField(this);

        @Override
        public void addWidgets() {
            titleLabel.setText(title);
            titleLabel.addFlags(Theme.CENTERED_V);
            add(titleLabel);

            if (groups.size() > 1) {
                add(buttonExpandAll);
                add(buttonCollapseAll);
            }
        }

        @Override
        public void alignWidgets() {
            titleLabel.setPosAndSize(4, 0, titleLabel.width, height);
            if (groups.size() > 1) {
                buttonExpandAll.setPos(width - 18, 2);
                buttonCollapseAll.setPos(width - 38, 2);
            }
        }
    }

    protected class GroupButton extends Button {
        protected final G group;
        private final Component titleText;
        private final List<RowPanel> rowPanels;

        public GroupButton(Panel panel, G group, Component titleText, List<E> values) {
            super(panel);
            this.group = group;
            this.titleText = titleText;
            setCollapsed(isCollapsed());
            this.rowPanels = new ArrayList<>();
            for (E value : values) {
                rowPanels.add(createRowPanel(panel, value));
            }
        }

        public List<RowPanel> collectPanels() {
            return isCollapsed() ? List.of() : List.copyOf(rowPanels);
        }

        @Override
        public void onClicked(MouseButton button) {
            setCollapsed(!isCollapsed());
            parent.refreshWidgets();
            refreshWidgets();
            playClickSound();
        }

        public boolean isCollapsed() {
            return AbstractGroupedButtonListScreen.this.isCollapsed(group);
        }

        public void setCollapsed(boolean collapsed) {
            AbstractGroupedButtonListScreen.this.setCollapsed(group, collapsed);
            boolean isCollapsed = isCollapsed();
            setTitle(Component.literal(isCollapsed ? "▶ " : "▼ ").withStyle(isCollapsed ? ChatFormatting.RED : ChatFormatting.GREEN).append(titleText));
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawWidget(graphics, x, y, w, h, getWidgetType());
            theme.drawString(graphics, getTitle(), x + 3, y + 3);
            if (isMouseOver()) {
                Color4I.WHITE.withAlpha(33).draw(graphics, x, y, w, h);
            }
        }
    }

    protected abstract class RowPanel extends Panel {
        protected final E value;

        public RowPanel(Panel panel, E value) {
            super(panel);
            this.value = value;
            setHeight(18);
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.draw(graphics, theme, x, y, w, h);

            var mouseOver = getMouseY() >= 20 && isMouseOver();

            if (mouseOver) {
                Color4I.WHITE.withAlpha(33).draw(graphics, x, y, w, h);
            }
        }

        @Override
        public boolean mousePressed(MouseButton button) {
            if (isMouseOver() && button.isRight()) {
                return true;
            }
            return super.mousePressed(button);
        }
    }
}
