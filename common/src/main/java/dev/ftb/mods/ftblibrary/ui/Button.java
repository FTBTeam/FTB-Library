package dev.ftb.mods.ftblibrary.ui;

import com.google.common.base.Preconditions;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TextComponentUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class Button extends Widget {
    protected Component title;
    protected Icon icon;
    private boolean forceButtonSize;

    public Button(Panel panel, Component t, Icon i) {
        super(panel);
        setSize(16, 16);
        icon = i;
        title = t;
    }

    public Button(Panel panel) {
        this(panel, Component.empty(), Icon.empty());
    }

    @Override
    public Component getTitle() {
        return title;
    }

    public Button setTitle(Component s) {
        title = s;
        return this;
    }

    public Button setIcon(Icon i) {
        icon = i;
        return this;
    }

    public Button setForceButtonSize(boolean forceButtonSize) {
        this.forceButtonSize = forceButtonSize;
        return this;
    }

    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawButton(graphics, x, y, w, h, getWidgetType());
    }

    public void drawIcon(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        icon.draw(graphics, x, y, w, h);
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        GuiHelper.setupDrawing();
        drawBackground(graphics, theme, x, y, w, h);
        if (forceButtonSize) {
            var s = h >= 16 ? 16 : 8;
            drawIcon(graphics, theme, x + (w - s) / 2, y + (h - s) / 2, s, s);
        }else {
            drawIcon(graphics, theme, x, y, w, h);
        }
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (isMouseOver()) {
            if (getWidgetType() != WidgetType.DISABLED) {
                onClicked(button);
            }

            return true;
        }

        return false;
    }

    public abstract void onClicked(MouseButton button);

    @Override
    public Optional<PositionedIngredient> getIngredientUnderMouse() {
        return PositionedIngredient.of(icon.getIngredient(), this, true);
    }

    @Override
    public CursorType getCursor() {
        return CursorType.HAND;
    }

    public static Builder builder(Panel parent, Consumer<MouseButton> onClicked) {
        return new Builder(parent, onClicked);
    }

    public static class Builder {
        private final Panel parent;
        private final Consumer<MouseButton> onClicked;
        private Component label = Component.empty();
        private Icon icon = Icon.empty();
        private Icon disabledIcon = Icon.empty();
        private BooleanSupplier visiblityPredicate = () -> true;
        private BooleanSupplier enabledPredicate = () -> visiblityPredicate.getAsBoolean();
        private Consumer<TooltipList> extraTooltips = list -> {};
        private boolean drawBackground = true;
        private boolean centered = false;

        public Builder(Panel parent, Consumer<MouseButton> onClicked) {
            this.parent = parent;
            this.onClicked = onClicked;
        }

        public Builder withLabel(Component label) {
            this.label = label;
            return this;
        }

        public Builder withIcon(Icon icon) {
            this.icon = icon;
            return this;
        }

        public Builder withDisabledIcon(Icon disabledIcon) {
            this.disabledIcon = disabledIcon;
            return this;
        }

        public Builder visibleWhen(BooleanSupplier visbilityPredicate) {
            this.visiblityPredicate = visbilityPredicate;
            return this;
        }

        public Builder enabledWhen(BooleanSupplier enabledPredicate) {
            this.enabledPredicate = enabledPredicate;
            return this;
        }

        public Builder withCenteredTitle(boolean centred) {
            this.centered = centred;
            return this;
        }

        public Builder withExtraTooltipLines(Consumer<TooltipList> extraTooltips) {
            this.extraTooltips = extraTooltips;
            return this;
        }

        public Builder drawBackground(boolean drawBackground) {
            this.drawBackground = drawBackground;
            return this;
        }

        public Button build() {
            Preconditions.checkState(!icon.isEmpty() || !TextComponentUtils.isComponentEmpty(label),
                    "Either or both the icon and label must be non-empty");

            return new SimpleTextButton(parent, label, icon) {
                @Override
                public void onClicked(MouseButton button) {
                    if (enabledPredicate.getAsBoolean()) {
                        onClicked.accept(button);
                    }
                }

                @Override
                public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                    if (visiblityPredicate.getAsBoolean()) {
                        super.draw(graphics, theme, x, y, w, h);
                    }
                }

                @Override
                public void drawIcon(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                    if (!disabledIcon.isEmpty() && !enabledPredicate.getAsBoolean()) {
                        disabledIcon.draw(graphics, x, y, w, h);
                    } else {
                        super.drawIcon(graphics, theme, x, y, w, h);
                    }
                }

                @Override
                public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                    if (drawBackground) {
                        super.drawBackground(graphics, theme, x, y, w, h);
                    }
                }

                @Override
                public WidgetType getWidgetType() {
                    return enabledPredicate.getAsBoolean() ? super.getWidgetType() : WidgetType.DISABLED;
                }

                @Override
                public boolean renderTitleInCenter() {
                    return centered;
                }

                @Override
                public void addMouseOverText(TooltipList list) {
                    super.addMouseOverText(list);

                    extraTooltips.accept(list);
                }
            };
        }
    }
}
