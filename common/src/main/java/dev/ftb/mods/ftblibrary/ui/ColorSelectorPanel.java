package dev.ftb.mods.ftblibrary.ui;

import com.google.common.primitives.Ints;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ColorConfig;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

import java.util.*;
import java.util.regex.Pattern;

import static dev.ftb.mods.ftblibrary.util.TextComponentUtils.hotkeyTooltip;

public class ColorSelectorPanel extends ModalPanel {
    private static final Icon WHEEL = Icon.getIcon(FTBLibrary.rl("textures/gui/rgbcolorwheel.png"));
    private static final MutableComponent ARGB = Component.literal("ARGB");
    private static final MutableComponent RGB = Component.literal("RGB");

    private final ColorConfig config;
    private final ConfigCallback callback;
    private final BrightnessButton bButton;
    private final HueSaturationButton hsButton;
    private final AlphaButton aButton;
    private final RGBTextBox rgbBox;
    private final Button acceptBtn, cancelBtn;
    private final PaletteSelectorButton presetBtn;
    private final List<PaletteButton> paletteButtons = new ArrayList<>();

    private final float[] hsb = new float[3];

    private boolean allowAlphaEdit = false;

    private static String curPalette = "chat";
    private static final Map<String,List<Integer>> PRESETS = new LinkedHashMap<>();
    static {
        setupPalettes();
    }

    public ColorSelectorPanel(Panel panel, ColorConfig config, ConfigCallback callback) {
        super(panel);

        this.config = config;
        this.callback = callback;

        setSize(224, 138);

        bButton = new BrightnessButton();
        hsButton = new HueSaturationButton();
        aButton = new AlphaButton();
        rgbBox = new RGBTextBox();
        presetBtn = new PaletteSelectorButton();
        acceptBtn = SimpleTextButton.accept(this, mb -> done(true),
                Component.translatable("gui.accept"), hotkeyTooltip("⇧ + Enter"));
        cancelBtn = SimpleTextButton.cancel(this, mb -> done(false),
                Component.translatable("gui.cancel"), hotkeyTooltip("ESC"));

        for (int i = 0; i < 16; i++) {
            paletteButtons.add(new PaletteButton());
        }

        updateHSB(config.getValue());
        selectPalette(curPalette);
    }

    public static ColorSelectorPanel popupAtMouse(BaseScreen gui, ColorConfig config, ConfigCallback callback) {
        ColorSelectorPanel selector = new ColorSelectorPanel(gui, config, callback);
        selector.setAllowAlphaEdit(config.isAllowAlphaEdit());
        int absX = Math.min(gui.getMouseX(), gui.getScreen().getGuiScaledWidth() - selector.width - 10);
        int absY = Math.min(gui.getMouseY(), gui.getScreen().getGuiScaledHeight() - selector.height - 10);
        selector.setPos(absX - selector.getParent().getX(), absY - selector.getParent().getY());

        gui.pushModalPanel(selector);
        return selector;
    }

    public void setAllowAlphaEdit(boolean allowAlphaEdit) {
        this.allowAlphaEdit = allowAlphaEdit;
    }

    @Override
    public void addWidgets() {
        addAll(List.of(bButton, hsButton, aButton, rgbBox, presetBtn, acceptBtn, cancelBtn));
        addAll(paletteButtons);
    }

    @Override
    public void alignWidgets() {
        bButton.setPosAndSize(5, 5, 16, 128);
        hsButton.setPosAndSize(26, 5, 96, 96);
        aButton.setPosAndSize(26, 106, 100, 27);
        rgbBox.setPosAndSize(159, 5, 60, 16);
        presetBtn.setPosAndSize(130, 26, 90, 16);
        acceptBtn.setPosAndSize(177, 113, 20, 20);
        cancelBtn.setPosAndSize(199, 113, 20, 20);

        for (int i = 0; i < paletteButtons.size(); i++) {
            int x = i % 4;
            int y = i / 4;
            paletteButtons.get(i).setPosAndSize(132 + x * 12, 45 + y * 12, 10, 10);
        }
    }

    @Override
    public boolean keyPressed(Key key) {
        if (key.esc()) {
            done(false);
            return true;
        } else if (key.enter() && BaseScreen.isShiftKeyDown()) {
            done(true);
            return true;
        } else {
            return super.keyPressed(key);
        }
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawContextMenuBackground(graphics, x - 1, y - 1, w + 2, h + 2);

        Color4I.GRAY.withAlpha(40).draw(graphics, x + 130, y + 43, 50, 50);
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        super.draw(graphics, theme, x, y, w, h);

        theme.drawString(graphics, allowAlphaEdit ? ARGB : RGB, x + 157 - theme.getStringWidth(ARGB), y + 9);
    }

    private void done(boolean accept) {
        if (accept) {
            List<Integer> l = PRESETS.computeIfAbsent("recent", k -> new ArrayList<>());
            if (!l.contains(config.getValue().rgba())) {
                l.add(config.getValue().rgba());
                if (l.size() > 16) {
                    l.remove(0);
                }
                FTBLibraryClientConfig.RECENT.set(Ints.toArray(l));
                FTBLibraryClientConfig.save();
            }
        }
        callback.save(accept);
        getGui().popModalPanel();
    }

    private void setColor(Color4I newColor) {
        if (config.setCurrentValue(newColor)) {
            rgbBox.setTextFromColor(config.getValue());
        }
    }

    private void updateHSB(Color4I newColor) {
        Color4I.RGBtoHSB(newColor.redi(), newColor.greeni(), newColor.bluei(), hsb);
    }

    private void selectPalette(String paletteName) {
        if (PRESETS.containsKey(paletteName)) {
            curPalette = paletteName;
            presetBtn.setTitle(getPaletteName(ColorSelectorPanel.curPalette).append(" ▼"));
            var cols = PRESETS.get(paletteName);
            paletteButtons.forEach(b -> b.setIcon(Color4I.empty()));
            for (int i = 0; i < paletteButtons.size() && i < cols.size(); i++) {
                paletteButtons.get(i).setIcon(Color4I.rgba(cols.get(i)));
            }
        }
    }

    private static MutableComponent getPaletteName(String palette) {
        return Component.translatable("ftblibrary.palette." + palette);
    }

    private class BrightnessButton extends SimpleButton {
        public BrightnessButton() {
            super(ColorSelectorPanel.this, Component.empty(), Color4I.empty(), (b,m) -> {});
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            Color4I bg = Color4I.rgb(Color4I.HSBtoRGB(hsb[0], hsb[1], 1f));
            GuiHelper.drawGradientRect(graphics, x, y, w, h, bg, Color4I.BLACK);
            GuiHelper.drawHollowRect(graphics, x, y, w, h, Color4I.BLACK, false);
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.draw(graphics, theme, x, y, w, h);
            int yVal = (int) (y + h * (1f - hsb[2]));
            Color4I.BLACK.draw(graphics, x - 2, yVal, width + 4, 3);
            Color4I.GRAY.draw(graphics, x - 1, yVal + 1, width + 2, 1);
        }

        @Override
        public void onClicked(MouseButton button) {
            adjustToMouseY();
        }

        @Override
        public boolean mouseDragged(int button, double dragX, double dragY) {
            if (isMouseOver) {
                adjustToMouseY();
                return true;
            }
            return false;
        }

        private void adjustToMouseY() {
            int yVal = getHeight() - 1 - (getMouseY() - getY());
            float newB = Mth.clamp(yVal / ((float) height - 1), 0f, 1f);

            hsb[2] = newB;
            setColor(Color4I.rgb(Color4I.HSBtoRGB(hsb[0], hsb[1], hsb[2])).withAlpha(config.getValue().alphai()));
        }
    }

    private class HueSaturationButton extends SimpleButton {
        public HueSaturationButton() {
            super(ColorSelectorPanel.this, Component.empty(), Color4I.empty(), (b,m) -> {});
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            WHEEL.draw(graphics, x, y, w, h);
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.draw(graphics, theme, x, y, w, h);

            int xc = getWidth() / 2;
            int yc = getHeight() / 2;

            double rad = Math.PI / 2 - (hsb[0] * Math.PI * 2);
            int dx = (int) (xc + xc * hsb[1] * Math.cos(rad));
            int dy = (int) (yc - yc * hsb[1] * Math.sin(rad));

            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            Color4I.BLACK.draw(graphics, dx - 1, dy - 5, 3, 11);
            Color4I.BLACK.draw(graphics, dx - 5, dy - 1, 11, 3);
            Color4I.GRAY.draw(graphics, dx, dy - 4, 1, 9);
            Color4I.GRAY.draw(graphics, dx - 4, dy, 9, 1);
            graphics.pose().popPose();
        }

        @Override
        public void onClicked(MouseButton button) {
            adjustToMouseXY();
        }

        private boolean adjustToMouseXY() {
            int xc = getWidth() / 2;
            int yc = getHeight() / 2;

            int xVal = getMouseX() - getX();
            int yVal = getHeight() - 1 - (getMouseY() - getY());
            int dx = xVal - xc;
            int dy = yVal - yc;
            int dSq = dx * dx + dy * dy;

            if (dSq < xc * xc) {
                double a = flippedAtan2(dy, dx); // north at 0, going clockwise
                hsb[0] = (float) (a / (Math.PI * 2)); // hue = angle (0 is north, moving clockwise)
                hsb[1] = (float) Math.sqrt((float)dSq / (xc * xc));  // saturation = dist from center

                setColor(Color4I.rgb(Color4I.HSBtoRGB(hsb[0], hsb[1], hsb[2])).withAlpha(config.getValue().alphai()));
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(int button, double dragX, double dragY) {
            return adjustToMouseXY();
        }

        private static double flippedAtan2(double y, double x) {
            double angle = Math.atan2(y, x);
            double flippedAngle = Math.PI / 2 - angle;
            //  additionally put the angle into [0; 2*Pi) range from its [-pi; +pi] range
            return (flippedAngle >= 0) ? flippedAngle : flippedAngle + 2 * Math.PI;
        }
    }

    private class AlphaButton extends SimpleButton {
        public AlphaButton() {
            super(ColorSelectorPanel.this, Component.empty(), Color4I.empty(), (b,m) -> {});
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            GuiHelper.drawHollowRect(graphics, x, y, w, h, Color4I.BLACK, false);

            if (allowAlphaEdit) {
                if (config.getValue().alphai() < 255) {
                    GuiHelper.pushScissor(getScreen(), x, y, w, h);
                    for (int i = 0; i < w; i += 10) {
                        for (int j = 0; j < h; j += 10) {
                            Color4I c = (i + j) / 10 % 2 == 0 ? Color4I.WHITE : Color4I.GRAY;
                            c.draw(graphics, x + i, y + j, 10, 10);
                        }
                    }
                    GuiHelper.popScissor(getScreen());
                }
                config.getValue().draw(graphics, x, y, w, h);

                int xVal = x + (w - 1) * config.getValue().alphai() / 255;
                Color4I.BLACK.draw(graphics, xVal, y - 2, 3, height + 4);
                Color4I.GRAY.draw(graphics, xVal + 1, y - 1, 1, height + 2);
            } else {
                config.getValue().draw(graphics, x, y, w, h);
            }
        }

        @Override
        public void onClicked(MouseButton button) {
            if (allowAlphaEdit) {
                adjustToMouseX();
            }
        }

        @Override
        public boolean mouseDragged(int button, double dragX, double dragY) {
            if (allowAlphaEdit && isMouseOver()) {
                adjustToMouseX();
                return true;
            }
            return false;
        }

        private void adjustToMouseX() {
            int xVal = getWidth() - 1 - (getMouseX() - getX());
            int newA = 255 - Mth.clamp(xVal * 255 / getWidth(), 0, 255);

            setColor(Color4I.rgb(Color4I.HSBtoRGB(hsb[0], hsb[1], hsb[2])).withAlpha(newA));
        }
    }

    private class RGBTextBox extends TextBox {
        private static final Pattern HEX = Pattern.compile("^[0-9a-fA-F]{1,8}$");

        public RGBTextBox() {
            super(ColorSelectorPanel.this);

            setTextFromColor(config.getValue());
            setFilter(s -> {
                if (s.isEmpty()) return true;
                if (s.startsWith("#")) s = s.substring(1);
                return s.isEmpty() || HEX.matcher(s).matches();
            });
        }

        private void setTextFromColor(Color4I color) {
            if (allowAlphaEdit) {
                setText(String.format("#%08x", color.rgba()));
            } else {
                setText(String.format("#%06x", color.rgb()));
            }
        }

        @Override
        public void onEnterPressed() {
            String s = getText();
            if (s.startsWith("#")) s = s.substring(1);

            if (s.length() == 6 || !allowAlphaEdit) {
                s = "FF" + s;
            }

            try {
                int col = Integer.parseUnsignedInt(s, 16);
                setColor(allowAlphaEdit ? Color4I.rgba(col) : Color4I.rgb(col));
                updateHSB(config.getValue());
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private class PaletteSelectorButton extends SimpleTextButton {
        public PaletteSelectorButton() {
            super(ColorSelectorPanel.this, getPaletteName(ColorSelectorPanel.curPalette).append(" ▼"), Color4I.empty());
        }

        @Override
        public void onClicked(MouseButton button) {
            List<ContextMenuItem> items = new ArrayList<>();
            PRESETS.forEach((k, v) ->
                    items.add(new ContextMenuItem(getPaletteName(k), Color4I.empty(), b -> selectPalette(k)))
            );
            getGui().openContextMenu(items);
        }
    }

    private class PaletteButton extends SimpleButton {
        public PaletteButton() {
            super(ColorSelectorPanel.this, Component.empty(), Color4I.empty(),
                    (b, mb) -> ((PaletteButton) b).applyColor());
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            if (icon instanceof Color4I col && !col.isEmpty()) {
                col.draw(graphics, x, y, w, h);
                Color4I shade = col.addBrightness(-0.15f);
                shade.draw(graphics, x, y + h - 1, w, 1);
                shade.draw(graphics, x + w - 1, y, 1, h);
            }

        }

        private void applyColor() {
            if (icon instanceof Color4I col && !col.isEmpty()) {
                setColor(col);
                updateHSB(col);
            }
        }
    }

    private static void setupPalettes() {
        PRESETS.put("chat", Util.make(new ArrayList<>(), l -> {
            Arrays.stream(ChatFormatting.values()).filter(ChatFormatting::isColor).map(ChatFormatting::getColor).forEach(e -> l.add(e | 0xFF000000));
        }));

        PRESETS.put("dye", Util.make(new ArrayList<>(), l -> {
            Arrays.stream(DyeColor.values()).map(DyeColor::getTextColor).forEach(e -> l.add(e | 0xFF000000));
        }));

        PRESETS.put("nord", List.of(
                0xFF2E3440, 0xFF3B4252, 0xFF434C5E, 0xFF4C566A,
                0xFFD8DEE9, 0xFFE5E9F0, 0xFFECEFF4, 0xFF8FBCBB,
                0xFF88C0D0, 0xFF81A1C1, 0xFF5E81AC, 0xFFBF616A,
                0xFFD08770, 0xFFEBCB8B, 0xFFA3BE8C, 0xFFB48EAD
        ));

        PRESETS.put("reds", List.of(
                0xFF560d0d, 0xFF5c1010, 0xFF6f0000, 0xFF940000, 0xFFc30101, 0xFFFF0000
        ));

        PRESETS.put("greens", List.of(
                0xFF1E5631, 0xFF4C9A2A, 0xFF76BA1B, 0xFF68BB59, 0xFFA4DE02, 0xFFACDF87
        ));

        PRESETS.put("blues", List.of(
                0xFF0000FF, 0xFF0044FF, 0xFF0066FF, 0xFF3388FF, 0xFF55AAFF, 0xFF77CCFF
        ));

        PRESETS.put("recent", Util.make(new ArrayList<>(), l -> {
            Arrays.stream(FTBLibraryClientConfig.RECENT.get()).forEach(l::add);
        }));
    }
}
