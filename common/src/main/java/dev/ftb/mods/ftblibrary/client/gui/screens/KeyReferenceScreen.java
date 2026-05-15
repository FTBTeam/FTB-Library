package dev.ftb.mods.ftblibrary.client.gui.screens;

import dev.ftb.mods.ftblibrary.client.gui.WidgetType;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.layout.WidgetLayout;
import dev.ftb.mods.ftblibrary.client.gui.theme.NordTheme;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.*;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class KeyReferenceScreen extends BaseScreen {
    private static final int SCROLLBAR_WIDTH = 16;
    private static final int GUTTER_SIZE = 2;
    private final Panel textPanel;
    private final PanelScrollBar scrollBar;
    private final SimpleTextButton closeButton;
    private final List<Pair<Component, Component>> lines;
    private int widestLeft = 0;
    private int widestOverall = 0;

    public KeyReferenceScreen(String... translationKeys) {
        lines = buildText(translationKeys);
        for (var line : lines) {
            widestLeft = Math.max(widestLeft, getGui().getTheme().getStringWidth(line.getLeft()));
            widestOverall = Math.max(widestOverall, widestLeft + getGui().getTheme().getStringWidth(line.getRight()));
        }

        textPanel = new TextPanel(this);

        closeButton = new SimpleTextButton(this, Component.translatable("gui.close"), Icons.CANCEL) {
            @Override
            public void onClicked(MouseButton button) {
                onBack();
            }
        };
        scrollBar = new PanelScrollBar(this, textPanel);
    }

    private List<Pair<Component, Component>> buildText(String... translationKeys) {
        List<Pair<Component, Component>> res = new ArrayList<>();
        for (String translationKey : translationKeys) {
            for (String line : I18n.get(translationKey).split("\\n")) {
                String[] parts = line.split(";", 2);
                switch (parts.length) {
                    case 0 -> res.add(Pair.of(Component.empty(), Component.empty()));
                    case 1 ->
                            res.add(Pair.of(Component.literal(parts[0]).withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE), Component.empty()));
                    default ->
                            res.add(substituteKeyMapping(parts[0], parts[1]));
                }
            }
            res.add(Pair.of(Component.empty(), Component.empty()));
        }
        return res;
    }

    private static Pair<Component,Component> substituteKeyMapping(String part0, String part1) {
        int s = part0.indexOf('{');
        int e = part0.indexOf('}');
        if (s >= 0 && e >= 0 && e > s + 1) {
            String key = part0.substring(s + 1, e);
            var keyMapping = KeyMapping.get(key);
            if (keyMapping != null) {
                var c0 = Component.literal(part0.substring(0, s))
                        .append(PlatformClient.get().keymap().getKeyMappingDisplayName(keyMapping))
                        .append(part0.substring(e + 1));
                Component c1 = part1.isEmpty() ? Component.translatable(keyMapping.getName()) : Component.literal(part1);
                return Pair.of(c0, c1.copy().withStyle(ChatFormatting.GRAY));
            }
        }
        return Pair.of(Component.literal(part0), Component.literal(part1).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean onInit() {
        boolean ok = setSizeProportional(0.75f, 0.8f);

        setWidth(Math.min(getWidth(), widestOverall + GUTTER_SIZE * 3 + 10));
        setHeight(Math.min(getHeight(), (getGui().getTheme().getFontHeight() + 2) * lines.size() + 10));

        return ok;
    }

    @Override
    public Theme getTheme() {
        return NordTheme.THEME;
    }

    @Override
    public void addWidgets() {
        add(textPanel);
        add(scrollBar);
        add(closeButton);
    }

    @Override
    public void alignWidgets() {
        int textPanelWidth = getGui().width - GUTTER_SIZE * 3 - SCROLLBAR_WIDTH;

        textPanel.setPosAndSize(GUTTER_SIZE, GUTTER_SIZE, textPanelWidth, getGui().height - GUTTER_SIZE * 2);
        textPanel.alignWidgets();

        scrollBar.setPosAndSize(getGui().width - GUTTER_SIZE - SCROLLBAR_WIDTH, textPanel.getPosY(), SCROLLBAR_WIDTH, textPanel.getHeight());

        closeButton.setPosAndSize(width + 2, 0, 20, 20);
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ftblibrary.gui.key_reference");
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawGui(graphics, x, y, w, h, WidgetType.NORMAL);

        int w1 = theme.getStringWidth(getTitle());
        theme.drawString(graphics, getTitle(), x + (w - w1) / 2, y - theme.getFontHeight() - 2, Color4I.rgb(0x00FFFF), Theme.SHADOW);
    }

    protected void drawTextBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawPanelBackground(graphics, x, y, w, h);
    }

    private class TwoColumnList extends Widget {
        private final List<Pair<Component, Component>> data;
        private final List<Pair<Component, FormattedCharSequence>> reflowed = new ArrayList<>();

        public TwoColumnList(Panel p, List<Pair<Component, Component>> data) {
            super(p);

            this.data = data;
        }

        @Override
        public void setWidth(int v) {
            super.setWidth(v);

            reflowText();
        }

        private void reflowText() {
            Theme theme = getGui().getTheme();
            int h = 0;
            int maxWidth = getParent().getWidth() - GUTTER_SIZE * 2;
            reflowed.clear();
            for (var entry : data) {
                if (entry.getRight().getString().isEmpty()) {
                    // header line
                    reflowed.add(Pair.of(entry.getLeft(), FormattedCharSequence.EMPTY));
                    h += theme.getFontHeight() + 3;
                } else {
                    var l = theme.getFont().split(entry.getRight(), maxWidth - 10 - widestLeft);
                    if (!l.isEmpty()) {
                        reflowed.add(Pair.of(entry.getLeft(), l.getFirst()));
                        for (int i = 1; i < l.size(); i++) {
                            reflowed.add(Pair.of(Component.empty(), l.get(i)));
                        }
                        h += (theme.getFontHeight() + 1) * l.size();
                    }
                }
            }

            height = h;
        }

        @Override
        public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
            int yPos = y;

            for (var entry : reflowed) {
                boolean header = entry.getRight() == FormattedCharSequence.EMPTY;
                int leftWidth = theme.getStringWidth(entry.getLeft());
                int xOff = header ? (width - leftWidth) / 2 : widestLeft - leftWidth - 2;
                theme.drawString(graphics, entry.getLeft(), x + xOff, yPos);
                if (!header) {
                    theme.drawString(graphics, entry.getRight(), x + widestLeft + 10, yPos);
                }
                yPos += theme.getFontHeight() + (header ? 3 : 1);
            }
        }
    }

    private class TextPanel extends Panel {
        private final TwoColumnList textWidget;

        public TextPanel(Panel panel) {
            super(panel);

            textWidget = new TwoColumnList(this, lines);
        }

        @Override
        public void addWidgets() {
            add(textWidget);
        }

        @Override
        public void alignWidgets() {
            align(WidgetLayout.VERTICAL);

            textWidget.setPos(4, 2);
            textWidget.setWidth(width);
        }

        @Override
        public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
            drawTextBackground(graphics, theme, x, y, w, h);
        }
    }
}
