package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.core.mixin.common.MultilineTextFieldAccess;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class MultilineTextBox extends Widget {
    private final Font font;
    private boolean isFocused = false;
    private MultilineTextField textField;
    private Component placeHolder = Component.empty();
    private int frame;
    private Consumer<String> valueListener = str -> {};

    public MultilineTextBox(Panel panel) {
        super(panel);

        font = getGui().getTheme().getFont();
        createTextField("", 100);
    }

    @Override
    public void setWidth(int v) {
        super.setWidth(v);

        createTextField(textField.value(), width);
        recalculateHeight();
    }

    private void createTextField(String text, int width) {
        textField = new MultilineTextField(font, width);
        textField.setCursorListener(this::scrollToCursor);
        textField.setValue(text);
        textField.setValueListener(valueListener);
    }

    public void setValueListener(Consumer<String> valueListener) {
        this.valueListener = valueListener;
        textField.setValueListener(valueListener);
    }

    public final boolean isFocused() {
        return isFocused;
    }

    public final void setFocused(boolean v) {
        isFocused = v;
    }

    public String getText() {
        return textField.value();
    }

    public void setText(String text) {
        textField.setValue(text);
        recalculateHeight();
    }

    private void recalculateHeight() {
        height = innerPadding() * 2 + textField.getLineCount() * font.lineHeight;
    }

    public void seekCursor(Whence whence, int pos) {
        textField.seekCursor(whence, pos);
    }

    public void setPlaceHolder(Component placeHolder) {
        this.placeHolder = placeHolder;
    }

    public void setSelecting(boolean selecting) {
        textField.setSelecting(selecting);
    }

    public boolean hasSelection() {
        return textField.hasSelection();
    }

    public String getSelectedText() {
        return textField.getSelectedText();
    }

    public void insertText(String toInsert) {
        textField.insertText(toInsert);
        recalculateHeight();
    }

    public int cursorPos() {
        return textField.cursor();
    }

    public void selectCurrentLine() {
        MultilineTextField.StringView view = textField.getLineView(textField.getLineAtCursor());
        textField.setSelecting(false);
        textField.seekCursor(Whence.ABSOLUTE, view.beginIndex());
        textField.setSelecting(true);
        textField.seekCursor(Whence.ABSOLUTE, view.endIndex());
    }

    public StringExtents getLineView() {
        return StringExtents.of(textField.getLineView(textField.getLineAtCursor()));
    }

    public StringExtents getLineView(int line) {
        return StringExtents.of(textField.getLineView(line));
    }

    public StringExtents getSelected() {
        return StringExtents.of(textField.getSelected());
    }

    @Override
    public void tick() {
        ++frame;
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (super.mousePressed(button)) {
            return true;
        } else if (isMouseOver()) {
            if (button.isLeft()) {
                setFocused(true);

                textField.setSelecting(Screen.hasShiftDown());
                setCursorPos(getMouseX(), (int) (getMouseY() - parent.getScrollY()));
            }
            return true;
        } else {
            setFocused(false);
            return false;
        }
    }

    @Override
    public boolean mouseDoubleClicked(MouseButton button) {
        if (super.mouseDoubleClicked(button)) {
            return true;
        } else if (isMouseOver() && button.isLeft()) {
            // double-click to select word
            setCursorPos(getMouseX(), (int) (getMouseY() - parent.getScrollY()));
            MultilineTextField.StringView view = textField.getPreviousWord();
            textField.seekCursor(Whence.ABSOLUTE, view.beginIndex());
            ((MultilineTextFieldAccess) textField).setSelectCursor(view.endIndex());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(int button, double dragX, double dragY) {
        if (super.mouseDragged(button, dragX, dragY)) {
            return true;
        } else if (isMouseOver()) {
            textField.setSelecting(true);
            setCursorPos(getMouseX(), (int) (getMouseY() - parent.getScrollY()));
            textField.setSelecting(Screen.hasShiftDown());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(Key key) {
        boolean res = textField.keyPressed(key.keyCode);
        recalculateHeight();
        return res;
    }

    @Override
    public boolean charTyped(char c, KeyModifiers modifiers) {
        if (this.isFocused() && SharedConstants.isAllowedChatCharacter(c)) {
            this.textField.insertText(Character.toString(c));
            recalculateHeight();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        String s = getText();

        if (s.isEmpty() && !isFocused()) {
            theme.drawString(graphics, placeHolder, x + 4, y + 4, Theme.DARK);
            return;
        }

        int cursorPos = textField.cursor();
        boolean drawCursor = isFocused() && frame / 6 % 2 == 0;
        boolean cursorInRange = cursorPos < s.length();
        int xPos = 0;
        int k = 0;
        int yPos = getY() + innerPadding();

        for (MultilineTextField.StringView stringview : textField.iterateLines()) {
            boolean shouldDrawLine = withinContentArea(yPos, yPos + font.lineHeight);
            if (drawCursor && cursorInRange && cursorPos >= stringview.beginIndex() && cursorPos <= stringview.endIndex()) {
                if (shouldDrawLine) {
                    xPos = theme.drawString(graphics, s.substring(stringview.beginIndex(), cursorPos), getX() + innerPadding(), yPos, Color4I.rgb(0xE0E0E0), 0);
                    Color4I.rgb(0xA0A0A0).draw(graphics, xPos - 1, yPos, 1, font.lineHeight);
                    theme.drawString(graphics, s.substring(cursorPos, stringview.endIndex()), xPos, yPos, Color4I.rgb(0xE0E0E0), 0);
                }
            } else {
                if (shouldDrawLine) {
                    xPos = theme.drawString(graphics, s.substring(stringview.beginIndex(), stringview.endIndex()), getX() + innerPadding(), yPos, Color4I.rgb(0xE0E0E0), 0);
                }

                k = yPos;
            }

            yPos += font.lineHeight;
        }

        if (drawCursor && !cursorInRange && withinContentArea(k, k + 9)) {
            theme.drawString(graphics, "_", xPos, k, Color4I.rgb(0xA0A0A0), 0);
        }

        if (textField.hasSelection()) {
            MultilineTextField.StringView stringView = textField.getSelected();
            int xPos1 = getX() + innerPadding();
            yPos = getY() + innerPadding();

            for (MultilineTextField.StringView stringView1 : textField.iterateLines()) {
                if (stringView.beginIndex() <= stringView1.endIndex()) {
                    if (stringView1.beginIndex() > stringView.endIndex()) {
                        break;
                    }

                    if (withinContentArea(yPos, yPos + 9)) {
                        int xOff1 = font.width(s.substring(stringView1.beginIndex(), Math.max(stringView.beginIndex(), stringView1.beginIndex())));
                        int xOff2 = stringView.endIndex() > stringView1.endIndex() ?
                                width - innerPadding() :
                                font.width(s.substring(stringView1.beginIndex(), stringView.endIndex()));

                        renderHighlight(graphics, xPos1 + xOff1, yPos, xPos1 + xOff2, yPos + font.lineHeight);
                    }

                }
                yPos += font.lineHeight;
            }
        }
    }

    private void renderHighlight(GuiGraphics graphics, int x1, int y1, int x2, int y2) {
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);

        Color4I.rgb(0x0000FF).draw(graphics, x1, y1, x2 - x1, y2 - y1);
        RenderSystem.disableColorLogicOp();
    }

    private boolean withinContentArea(int y1, int y2) {
        return y1 - parent.getScrollY() >= getY() && y2 - parent.getScrollY() <= getY() + height;
    }

    private void setCursorPos(int mouseX, int mouseY) {
        double x = mouseX - (double) this.getX() - this.innerPadding();
        double y = mouseY - (double) this.getY() - this.innerPadding() + parent.getScrollY();
        seekCursorToPoint(x, y);
    }

    public void seekCursorToPoint(double x, double y) {
        // NOTE: not using MultiLineTextField#seekCursorToPoint() here. Clicking the right-hand side of a character
        // should put the cursor after the character for usability purposes, but the vanilla method doesn't do that :(

        int x1 = Mth.floor(x);
        int y1 = Mth.floor(y / 9.0);
        MultilineTextField.StringView stringView = textField.getLineView(Mth.clamp(y1, 0, textField.getLineCount() - 1));

        String lineSection = font.plainSubstrByWidth(textField.value().substring(stringView.beginIndex(), stringView.endIndex()), x1);
        int k = lineSection.length();
        textField.seekCursor(Whence.ABSOLUTE, stringView.beginIndex() + k);

        if (textField.cursor() < textField.value().length()) {
            // move the cursor right a character if we clicked the right-hand side of the character
            int w1 = font.width(lineSection);
            if (x1 <= font.width(textField.value().substring(stringView.beginIndex(), stringView.endIndex()))) {
                String c = String.valueOf(textField.value().charAt(textField.cursor()));
                int w2 = font.width(c) / 2;
                if (x1 - w1 >= w2) {
                    textField.seekCursor(Whence.RELATIVE, 1);
                }
            }
        }
    }

    private void scrollToCursor() {
        int lh = font.lineHeight;
        double d0 = parent.getScrollY();

        MultilineTextField.StringView stringView = textField.getLineView((int) (d0 / lh));
        if (textField.cursor() <= stringView.beginIndex()) {
            d0 = textField.getLineAtCursor() * lh;
        } else {
            MultilineTextField.StringView stringView1 = textField.getLineView((int) ((d0 + (double) parent.height) / lh) - 1);
            if (textField.cursor() > stringView1.endIndex()) {
                d0 = textField.getLineAtCursor() * lh - parent.height + lh + innerPadding() * 2;
            }
        }

        parent.setScrollY(d0);
    }

    private int innerPadding() {
        return 4;
    }

    public record StringExtents(int start, int end) {
        public static StringExtents of (MultilineTextField.StringView view) {
            return new StringExtents(view.beginIndex(), view.endIndex());
        }
    }
}
