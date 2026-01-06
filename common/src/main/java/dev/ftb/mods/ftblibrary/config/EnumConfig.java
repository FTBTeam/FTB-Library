package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class EnumConfig<E> extends ConfigWithVariants<E> {
    public final NameMap<E> nameMap;

    public EnumConfig(NameMap<E> nm) {
        nameMap = nm;
        defaultValue = nameMap.defaultValue;
        value = nameMap.defaultValue;
    }

    @Override
    public Component getStringForGUI(E v) {
        return nameMap.getDisplayName(v);
    }

    @Override
    public Color4I getColor(E v) {
        var col = nameMap.getColor(v);
        return col.isEmpty() ? Tristate.DEFAULT.color : col;
    }

    @Override
    public void addInfo(TooltipList list) {
        super.addInfo(list);

        if (nameMap.size() > 0) {
            list.blankLine();

            for (int i = 0; i < nameMap.size(); i++) {
                if (i >= 10) {
                    // prevent big enums producing giant unwieldy tooltips
                    list.add(Component.literal("... " + (nameMap.size() - i) + " more ...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                    break;
                }
                var v = nameMap.get(i);
                var e = isEqual(v, value);
                var c = Component.literal(e ? "+ " : "- ");
                c.withStyle(e ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY);
                c.append(nameMap.getDisplayName(v));
                list.add(c);
            }
        }
    }

    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        if (nameMap.values.size() > 16 || BaseScreen.isCtrlKeyDown()) {
            var screen = new EnumSelectScreen(clickedWidget.getParent());
            screen.setHasSearchBox(true);
            screen.showBottomPanel(false);
            screen.showCloseButton(true);
            screen.openGui();
        } else {
            super.onClicked(clickedWidget, button, callback);
        }
    }

    @Override
    public E getIteration(E currentValue, boolean next) {
        return next ? nameMap.getNext(currentValue) : nameMap.getPrevious(currentValue);
    }

    @Override
    public Icon<?> getIcon(@Nullable E v) {
        if (v != null) {
            var icon = nameMap.getIcon(v);

            if (!icon.isEmpty()) {
                return icon;
            }
        }

        return super.getIcon(v);
    }

    private class EnumSelectScreen extends AbstractButtonListScreen {
        private final Panel parent;
        private int maxWidth = 176;

        public EnumSelectScreen(Panel parent) {
            this.parent = parent;
            for (var v : nameMap) {
                maxWidth = Math.max(maxWidth, getTheme().getStringWidth(nameMap.getDisplayName(v)));
            }
        }

        @Override
        public void addButtons(Panel panel) {
            for (var v : nameMap) {
                panel.add(new SimpleTextButton(panel, nameMap.getDisplayName(v), nameMap.getIcon(v)) {
                    @Override
                    public void onClicked(MouseButton button) {
                        playClickSound();
                        setCurrentValue(v);
                        doAccept();
                    }
                });
            }
        }

        @Override
        public boolean onInit() {
            setSize(
                    Mth.clamp(maxWidth + 35, 176, getWindow().getGuiScaledWidth() * 3 / 4),
                    Mth.clamp(nameMap.size() * 20 + 50, 166, getWindow().getGuiScaledHeight() * 4 / 5)
            );
            return super.onInit();
        }

        @Override
        protected void doCancel() {
            parent.run();
        }

        @Override
        protected void doAccept() {
            parent.run();
        }
    }
}
