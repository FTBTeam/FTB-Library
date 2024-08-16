package dev.ftb.mods.ftblibrary.config.ui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractThreePanelScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.ftb.mods.ftblibrary.util.TextComponentUtils.hotkeyTooltip;

public class EditConfigScreen extends AbstractThreePanelScreen<EditConfigScreen.ConfigPanel> {
    private final ConfigGroup group;
    private final Component title;
    private final List<Widget> allConfigButtons; // both groups and entries
    private final Button buttonCollapseAll, buttonExpandAll;

    private int groupSize = 0;
    private boolean autoclose = false;
    private int widestKey = 0;
    private int widestValue = 0;
    private boolean changed = false;
    private boolean openPrevScreenOnClose = true;

    public EditConfigScreen(ConfigGroup configGroup) {
        super();

        group = configGroup;
        title = configGroup.getName().copy().withStyle(ChatFormatting.BOLD);
        allConfigButtons = new ArrayList<>();

        List<ConfigValue<?>> list = new ArrayList<>();
        collectAllConfigValues(group, list);

        if (!list.isEmpty()) {
            ConfigGroupButton group = null;

            for (var value : list) {
                if (group == null || group.group != value.getGroup()) {
                    allConfigButtons.add(new VerticalSpaceWidget(mainPanel, 4));
                    group = new ConfigGroupButton(mainPanel, value.getGroup());
                    allConfigButtons.add(group);
                    groupSize++;
                }

                ConfigEntryButton<?> btn = new ConfigEntryButton<>(mainPanel, group, value);
                allConfigButtons.add(btn);
            }

            if (groupSize == 1) {
                allConfigButtons.remove(group);
            }
        }

        buttonExpandAll = new SimpleButton(topPanel, List.of(Component.translatable("gui.expand_all"), hotkeyTooltip("="), hotkeyTooltip("+")), Icons.UP,
                (widget, button) -> toggleAll(false));
        buttonCollapseAll = new SimpleButton(topPanel, List.of(Component.translatable("gui.collapse_all"), hotkeyTooltip("-")), Icons.DOWN,
                (widget, button) -> toggleAll(true));
    }

    private void toggleAll(boolean collapsed) {
        for (var w : allConfigButtons) {
            if (w instanceof ConfigGroupButton cgb) {
                cgb.setCollapsed(collapsed);
            }
        }

        scrollBar.setValue(0);
        getGui().refreshWidgets();
    }

    private void collectAllConfigValues(ConfigGroup group, List<ConfigValue<?>> list) {
        list.addAll(group.getValues().stream()
                .sorted(Comparator.comparing(ConfigValue::getName))
                .toList()
        );

        for (var subgroup : group.getSubgroups()) {
            collectAllConfigValues(subgroup, list);
        }
    }

    @Override
    public boolean onInit() {
        widestKey = widestValue = 0;
        MutableInt widestGroup = new MutableInt(0);
        MutableInt cfgHeight = new MutableInt(0);

        allConfigButtons.forEach(w -> {
            if (w instanceof ConfigEntryButton<?> eb) {
                widestKey = Math.max(widestKey, getTheme().getFont().width(eb.keyText));
                widestValue = Math.max(widestValue, getTheme().getFont().width(eb.getValueStr()));
            } else if (w instanceof ConfigGroupButton gb) {
                widestGroup.setValue(Math.max(widestGroup.intValue(), getTheme().getStringWidth(gb.title)));
            }
            cfgHeight.add(w.height + 2);
        });

        setHeight(Mth.clamp(cfgHeight.intValue() + getTopPanelHeight() + BOTTOM_PANEL_H, 100, (int) (getScreen().getGuiScaledHeight() * .9f)));
        setWidth(Mth.clamp(Math.max(widestKey + widestValue, widestGroup.intValue()) + 50, 176, (int) (getScreen().getGuiScaledWidth() * .9f)));

        return true;
    }

    /**
     * Set auto-close behaviour when Accept or Cancel buttons are clicked
     * @param autoclose true to close the config screen, false if the config group's save-callback should handle it
     */
    public EditConfigScreen setAutoclose(boolean autoclose) {
        this.autoclose = autoclose;
        return this;
    }

    public EditConfigScreen setOpenPrevScreenOnClose(boolean openPrevScreenOnClose) {
        this.openPrevScreenOnClose = openPrevScreenOnClose;
        return this;
    }

    @Override
    protected int getTopPanelHeight() {
        return 20;
    }

    @Override
    protected Panel createTopPanel() {
        return new CustomTopPanel();
    }

    @Override
    protected ConfigPanel createMainPanel() {
        return new ConfigPanel();
    }

    @Override
    protected void doAccept() {
        group.save(true);
        if (autoclose) closeGui(openPrevScreenOnClose);
    }

    @Override
    protected void doCancel() {
        if (changed) {
            openYesNo(Component.translatable("ftblibrary.unsaved_changes"), Component.empty(), this::reallyCancel);
        } else {
            reallyCancel();
        }
    }

    private void reallyCancel() {
        group.save(false);
        if (autoclose) closeGui(openPrevScreenOnClose);
    }

    @Override
    public boolean onClosedByKey(Key key) {
        if (super.onClosedByKey(key)) {
            doCancel();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(Key key) {
        if (super.keyPressed(key)) {
            return true;
        } else if ((key.is(InputConstants.KEY_RETURN) || key.is(InputConstants.KEY_NUMPADENTER)) && key.modifiers.shift()) {
            doAccept();
            return true;
        } else if (key.is(InputConstants.KEY_ADD) || key.is(InputConstants.KEY_EQUALS)) {
            buttonExpandAll.onClicked(MouseButton.LEFT);
        } else if (key.is(InputConstants.KEY_MINUS) || key.is(GLFW.GLFW_KEY_KP_SUBTRACT)) {
            buttonCollapseAll.onClicked(MouseButton.LEFT);
        }
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    public static class ConfigGroupButton extends Button {
        private final ConfigGroup group;
        private final MutableComponent title, info;
        private boolean collapsed = false;

        public ConfigGroupButton(Panel panel, ConfigGroup g) {
            super(panel);
            setHeight(14);
            group = g;

            if (group.getParent() != null) {
                List<ConfigGroup> groups = new ArrayList<>();
                while (g.getParent() != null) {
                    groups.add(g);
                    g = g.getParent();
                }
                title = groups.stream()
                        .map(grp -> Component.translatable(grp.getNameKey()).withStyle(ChatFormatting.YELLOW))
                        .reduce((g1, g2) -> g2.append(Component.literal(" → ").withStyle(ChatFormatting.GOLD)).append(g1))
                        .orElse(Component.empty());
            } else {
                title = Component.translatable("stat.generalButton").withStyle(ChatFormatting.YELLOW);
            }

            var infoKey = group.getPath() + ".info";
            info = I18n.exists(infoKey) ? Component.translatable(infoKey) : null;
            setCollapsed(collapsed);
        }

        public void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
            setTitle(Component.literal(this.collapsed ? "▶ " : "▼ ").withStyle(this.collapsed ? ChatFormatting.RED : ChatFormatting.GREEN).append(title));
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawWidget(graphics, x, y, w, h, getWidgetType());
            theme.drawString(graphics, getTitle(), x + 3, y + 3);
            if (isMouseOver()) {
                Color4I.WHITE.withAlpha(33).draw(graphics, x, y, w, h);
            }
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            if (info != null) {
                list.add(info);
            }
        }

        @Override
        public void onClicked(MouseButton button) {
            setCollapsed(!collapsed);
            getGui().refreshWidgets();
        }
    }

    private class ConfigEntryButton<T> extends Button implements EditStringConfigOverlay.PosProvider {
        private final ConfigGroupButton groupButton;
        private final ConfigValue<T> configValue;
        private final Component keyText;

        public ConfigEntryButton(Panel panel, ConfigGroupButton groupButton, ConfigValue<T> configValue) {
            super(panel);
            setHeight(getTheme().getFontHeight() + 2);
            this.groupButton = groupButton;
            this.configValue = configValue;

            keyText = this.configValue.getCanEdit() ?
                    Component.literal(this.configValue.getName()) :
                    Component.literal(this.configValue.getName()).withStyle(ChatFormatting.GRAY);
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawString(graphics, keyText, x + 5, y + 2, Bits.setFlag(0, Theme.SHADOW, isMouseOver()));

            Component valueText = configValue.getStringForGUI(configValue.getValue());

            int maxLen = width - (scrollBar.shouldDraw() ? scrollBar.width : 0) - widestKey;
            if (theme.getStringWidth(valueText) > maxLen) {
                valueText = Component.literal(theme.trimStringToWidth(valueText, maxLen).getString().trim() + "...");
            }

            var textCol = configValue.getColor().mutable();
            textCol.setAlpha(255);

            if (isMouseOver()) {
                textCol.addBrightness(60);
                Color4I.WHITE.withAlpha(33).draw(graphics, x, y, w, h);
            }

            Color4I.GRAY.withAlpha(33).draw(graphics, x + widestKey + 10, y, 1, height);

            theme.drawString(graphics, valueText, x + widestKey + 15, y + 2, textCol, 0);
        }

        @Override
        public void onClicked(MouseButton button) {
            if (getMouseY() >= 20) {
                playClickSound();
                configValue.onClicked(this, button, accepted -> {
                    if (accepted) changed = true;
                    run();
                });
            }
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            if (getMouseY() > 18) {
                list.add(keyText.copy().withStyle(ChatFormatting.UNDERLINE));
                var tooltip = configValue.getTooltip();

                if (!tooltip.isEmpty()) {
                    for (var s : tooltip.split("\n")) {
                        list.styledString(s, Style.EMPTY.withItalic(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.GRAY)));
                    }
                }

                list.blankLine();
                configValue.addInfo(list);
            }
        }

        Component getValueStr() {
            return configValue.getStringForGUI(configValue.getValue());
        }

        @Override
        public Offset getOverlayOffset() {
            return new Offset(widestKey + 12, -2);
        }
    }

    public class ConfigPanel extends Panel {
        public ConfigPanel() {
            super(EditConfigScreen.this);
        }

        @Override
        public void addWidgets() {
            for (var w : allConfigButtons) {
                if (!(w instanceof ConfigEntryButton<?> cgb) || !cgb.groupButton.collapsed) {
                    add(w);
                }
            }
        }

        @Override
        public void alignWidgets() {
            allConfigButtons.forEach(btn -> {
                btn.setX(1);
                btn.setWidth(width - 2);
            });

            align(WidgetLayout.VERTICAL);
        }
    }

    protected class CustomTopPanel extends TopPanel {
        private final TextField titleLabel = new TextField(this);

        @Override
        public void addWidgets() {
            titleLabel.setText(getGui().getTitle());
            titleLabel.addFlags(Theme.CENTERED_V);
            add(titleLabel);

            if (groupSize > 1) {
                add(buttonExpandAll);
                add(buttonCollapseAll);
            }
        }

        @Override
        public void alignWidgets() {
            titleLabel.setPosAndSize(4, 0, titleLabel.width, height);
            if (groupSize > 1) {
                buttonExpandAll.setPos(width - 18, 2);
                buttonCollapseAll.setPos(width - 38, 2);
            }
        }
    }
}
