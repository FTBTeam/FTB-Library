package dev.ftb.mods.ftblibrary.client.gui.widget;

import dev.ftb.mods.ftblibrary.client.gui.CursorType;
import dev.ftb.mods.ftblibrary.client.gui.WidgetType;
import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

public class TextBox extends Widget implements IFocusableWidget {
    /** Horizontal space between the edge of the box and its text, on each side */
    protected static final int PADDING = 4;

    public int charLimit = 2000;
    public Color4I textColor = Icon.empty();
    public String ghostText = "";
    private boolean isFocused = false;
    private String text = "";
    private int displayPos;
    private int cursorPos;
    private int highlightPos;
    private boolean validText = true;
    private int maxLength = 1024;
    private Predicate<String> filter;
    private Component label;
    private Color4I labelColor = Color4I.WHITE;
    private boolean strictValidity = false;
    private @Nullable CustomStyle style = null;

    public TextBox(Panel panel) {
        super(panel);

        this.filter = Objects::nonNull;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void setWidth(int v) {
        super.setWidth(v);

        // force a recalc of displayPos, since it's dependent on the widget width
        scrollTo(getCursorPos());
    }

    @Override
    public final boolean isFocused() {
        return isFocused;
    }

    @Override
    public final void setFocused(boolean focused) {
        if (isFocused != focused) {
            isFocused = focused;
            if (focused) {
                getGui().setFocusedWidget(this);
            }
        }
    }

    public void setStrictValidity(boolean strictValidity) {
        this.strictValidity = strictValidity;
    }

    public void setFilter(Predicate<String> filter) {
        this.filter = filter;
    }

    /**
     * Draw this box with a self-contained style rather than the active theme's text box background.
     *
     * @param style the style, or null to use the theme's (the default)
     */
    public void setStyle(@Nullable CustomStyle style) {
        this.style = style;
    }

    public @Nullable CustomStyle getStyle() {
        return style;
    }

    /** @return the width available for text, i.e. the box width less the padding on both sides */
    protected int getTextWidth() {
        return Math.max(0, width - PADDING * 2);
    }

    public void setLabel(Component label) {
        this.label = label;
    }

    public void setLabelColor(Color4I color) {
        this.labelColor = color;
    }

    public final String getText() {
        return text;
    }

    public final void setText(String s) {
        setText(s, true);
    }

    public String getSelectedText() {
        return text.substring(Math.min(cursorPos, highlightPos), Math.max(cursorPos, highlightPos));
    }

    /**
     * Set the text without notifying {@link #onTextChanged()}. Use this when updating the box from code, e.g. to
     * reflect a value which was changed elsewhere, so that listeners only see edits made by the user.
     */
    public final void setTextQuietly(String s) {
        setText(s, false);
    }

    public void setText(String string, boolean triggerChange) {
        if (filter.test(string)) {
            if (string.length() > maxLength) {
                text = string.substring(0, maxLength);
            } else {
                text = string;
            }

            validText = isValid(text);

            setCursorPosition(text.length());
            setSelectionPos(cursorPos);
            if (triggerChange) {
                onTextChanged();
            }
        }
    }

    public void moveCursor(int pos, boolean extendSelection) {
        moveCursorTo(getCursorPos(pos), extendSelection);
    }

    private int getCursorPos(int pos) {
        return Util.offsetByCodepoints(text, cursorPos, pos);
    }

    public void setCursorPosition(int pos) {
        cursorPos = Mth.clamp(pos, 0, text.length());
        scrollTo(cursorPos);
    }

    public void moveCursorTo(int pos, boolean extendSelection) {
        setCursorPosition(pos);
        if (!extendSelection) {
            setSelectionPos(cursorPos);
        }
    }

    public void moveCursorToStart(boolean extendSelection) {
        moveCursorTo(0, extendSelection);
    }

    public void moveCursorToEnd(boolean extendSelection) {
        moveCursorTo(text.length(), extendSelection);
    }

    public void setSelectionPos(int i) {
        highlightPos = Mth.clamp(i, 0, text.length());
        scrollTo(highlightPos);
    }

    public int getCursorPos() {
        return cursorPos;
    }

    public void setCursorPos(int pos) {
        cursorPos = Mth.clamp(pos, 0, text.length());
        scrollTo(cursorPos);
    }

    public void insertText(String string) {
        int selStart = Math.min(cursorPos, highlightPos);
        int selEnd = Math.max(cursorPos, highlightPos);
        int space = maxLength - text.length() - (selStart - selEnd);
        if (space > 0) {
            String filtered = StringUtil.filterText(string);
            int nToInsert = filtered.length();
            if (space < nToInsert) {
                if (Character.isHighSurrogate(filtered.charAt(space - 1))) {
                    --space;
                }

                filtered = filtered.substring(0, space);
                nToInsert = space;
            }

            String newText = (new StringBuilder(text)).replace(selStart, selEnd, filtered).toString();
            boolean prevValid = validText;
            validText = isValid(newText);
            if (!text.equals(newText) && (validText || !strictValidity)) {
                text = newText;
                setCursorPosition(selStart + nToInsert);
                setSelectionPos(cursorPos);
                onTextChanged();
            } else {
                validText = prevValid;
            }
        }
    }

    private void scrollTo(int pos) {
        Font font = getGui().getTheme().getFont();
        if (font != null) {
            displayPos = Math.min(displayPos, text.length());
            String string = font.plainSubstrByWidth(text.substring(displayPos), getTextWidth());
            int k = string.length() + displayPos;
            if (pos == displayPos) {
                displayPos -= font.plainSubstrByWidth(text, getTextWidth(), true).length();
            }

            if (pos > k) {
                displayPos += pos - k;
            } else if (pos <= displayPos) {
                displayPos -= displayPos - pos;
            }

            displayPos = Mth.clamp(displayPos, 0, text.length());
        }
    }

    public int getWordPosition(int count) {
        return getWordPosition(count, getCursorPos());
    }

    private int getWordPosition(int count, int fromPos) {
        int res = fromPos;
        boolean backwards = count < 0;
        int absCount = Math.abs(count);

        for (int m = 0; m < absCount; ++m) {
            if (!backwards) {
                int n = text.length();
                res = text.indexOf(' ', res);
                if (res == -1) {
                    res = n;
                } else {
                    while (res < n && text.charAt(res) == ' ') {
                        ++res;
                    }
                }
            } else {
                while (res > 0 && text.charAt(res - 1) == ' ') {
                    --res;
                }

                while (res > 0 && text.charAt(res - 1) != ' ') {
                    --res;
                }
            }
        }

        return res;
    }

    public boolean allowInput() {
        return true;
    }

    private void deleteText(int count) {
        String prevText = text;
        if (isCtrlKeyDown()) {
            deleteWords(count);
        } else {
            deleteChars(count);
        }
        if (!prevText.equals(text)) {
            validText = isValid(text);
            onTextChanged();
        }
    }

    public void deleteWords(int count) {
        if (!text.isEmpty()) {
            if (highlightPos != cursorPos) {
                insertText("");
            } else {
                deleteCharsToPos(getWordPosition(count));
            }
        }
    }

    public void deleteChars(int count) {
        deleteCharsToPos(getCursorPos(count));
    }

    public void deleteCharsToPos(int pos) {
        if (!text.isEmpty()) {
            if (highlightPos != cursorPos) {
                insertText("");
            } else {
                int from = Math.min(pos, cursorPos);
                int to = Math.max(pos, cursorPos);
                if (from != to) {
                    String newText = new StringBuilder(text).delete(from, to).toString();
                    if (filter.test(newText)) {
                        text = newText;
                        moveCursorTo(from, false);
                    }
                }
            }
        }
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (allowInput() && isMouseOver()) {
            setFocused(true);

            if (button.isLeft()) {
                if (isFocused) {
                    var i = getMouseX() - getX() - PADDING;
                    var theme = getGui().getTheme();
                    var s = theme.trimStringToWidth(text.substring(displayPos), getTextWidth());
                    if (isShiftKeyDown()) {
                        setSelectionPos(theme.trimStringToWidth(s, i).length() + displayPos);
                    } else {
                        setCursorPos(theme.trimStringToWidth(s, i).length() + displayPos);
                        setSelectionPos(getCursorPos());
                    }
                }
            } else if (button.isRight() && !getText().isEmpty() && allowInput()) {
                setText("");
            }

            return true;
        } else {
            setFocused(false);
        }

        return false;
    }

    @Override
    public boolean keyPressed(Key key) {
        if (!allowInput()) {
            return false;
        }
        if (!isFocused()) {
            return false;
        } else if (key.selectAll()) {
            setCursorPos(text.length());
            setSelectionPos(0);
            return true;
        } else if (key.copy()) {
            setClipboardString(getSelectedText());
            return true;
        } else if (key.paste()) {
            insertText(getClipboardString());
            return true;
        } else if (key.cut()) {
            setClipboardString(getSelectedText());
            insertText("");
            return true;
        } else {
            switch (key.event().key()) {
                case GLFW.GLFW_KEY_ESCAPE -> {
                    setFocused(false);
                    return true;
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    deleteText(-1);
                    return true;
                }
                case GLFW.GLFW_KEY_HOME -> {
                    moveCursorToStart(isShiftKeyDown());
                    return true;
                }
                case GLFW.GLFW_KEY_LEFT -> {
                    if (isCtrlKeyDown()) {
                        moveCursorTo(getWordPosition(-1), isShiftKeyDown());
                    } else {
                        moveCursor(-1, isShiftKeyDown());
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    if (isCtrlKeyDown()) {
                        moveCursorTo(getWordPosition(1), isShiftKeyDown());
                    } else {
                        moveCursor(1, isShiftKeyDown());
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_END -> {
                    moveCursorToEnd(isShiftKeyDown());
                    return true;
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    deleteText(1);
                    return true;
                }
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                    if (validText) {
                        setFocused(false);
                        onEnterPressed();
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_TAB -> {
                    if (validText) {
                        setFocused(false);
                        onTabPressed();
                    }
                    return true;
                }
            }
        }

        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (allowInput() && isFocused()) {
            if (event.isAllowedChatCharacter()) {
                insertText(event.codepointAsString());
            }

            return true;
        }

        return false;
    }

    public void onTextChanged() {
    }

    public void onTabPressed() {
    }

    public void onEnterPressed() {
    }

    public String getFormattedText() {
        return (!isFocused() && text.isEmpty() && !ghostText.isEmpty()) ? (ChatFormatting.ITALIC + ghostText) : text;
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        drawTextBox(graphics, theme, x, y, w, h);
        var drawGhostText = !isFocused() && text.isEmpty() && !ghostText.isEmpty();
        var textToDraw = getFormattedText();

        var col = validText ? (textColor.isEmpty() ? theme.getContentColor(WidgetType.NORMAL) : textColor).withAlpha(drawGhostText ? 120 : 255) : Color4I.RED;
        var j = cursorPos - displayPos;
        var s = theme.trimStringToWidth(textToDraw.substring(displayPos), getTextWidth());
        var textX = x + PADDING;
        var textY = y + (h - 8) / 2;
        var textX1 = textX;

        // render text up to cursor pos
        if (!s.isEmpty()) {
            var s1 = j > 0 && j <= s.length() ? s.substring(0, j) : s;
            textX1 = theme.drawString(graphics, Component.literal(s1), textX, textY, col, 0);
        }

        // calculate cursor draw pos
        var drawCursor = cursorPos < textToDraw.length() || textToDraw.length() >= charLimit;
        var cursorX = textX1;
        if (j <= 0 || j > s.length()) {
            cursorX = j > 0 ? textX + getTextWidth() : textX;
        } else if (drawCursor) {
            cursorX = textX1 - 1;
        }

        // render text after cursor pos
        if (j > 0 && j < s.length()) {
            theme.drawString(graphics, Component.literal(s.substring(j)), textX1, textY, col, 0);
        }

        // render the cursor
        if (j >= 0 && j <= s.length() && isFocused() && System.currentTimeMillis() % 1000L > 500L) {
            if (drawCursor) {
                IconHelper.renderIcon(col, graphics, cursorX, textY - 1, 1, theme.getFontHeight() + 2);
            } else {
                IconHelper.renderIcon(col, graphics, cursorX, textY + theme.getFontHeight() - 2, 5, 1);
            }
        }

        // highlight the selection if needed
        int k = Mth.clamp(highlightPos - displayPos, 0, s.length());
        if (k != j) {
            var xMax = textX + theme.getStringWidth(Component.literal(s.substring(0, k)));

            int startX = Math.min(cursorX, xMax - 1);
            int endX = Math.max(cursorX, xMax - 1);
            int startY = textY - 1;
            int endY = textY + theme.getFontHeight();

            endX = Math.min(endX, x + w - PADDING);
            startX = Math.min(startX, x + w - PADDING);

            graphics.textHighlight(startX, startY, endX, endY, true);
            graphics.fill(RenderPipelines.GUI_TEXT_HIGHLIGHT, startX, startY, endX, endY, 0x80000080);
        }
    }

    public void drawTextBox(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        if (style != null) {
            IconHelper.renderIcon(isFocused() ? style.focusedBorder() : isMouseOver() ? style.hoverBorder() : style.border(), graphics, x, y, w, h);
            IconHelper.renderIcon(style.fill(), graphics, x + 1, y + 1, w - 2, h - 2);
        } else {
            theme.drawTextBox(graphics, x, y, w, h);
        }

        if (label != null) {
            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(x + 1, y - 5);
            pose.scale(0.5F, 0.5F);
            theme.drawString(graphics, label, 0, 0, labelColor, Theme.SHADOW);
            pose.popMatrix();
        }
    }

    public boolean isValid(String txt) {
        return filter.test(txt);
    }

    public final boolean isTextValid() {
        return validText;
    }

    @Override
    public CursorType getCursor() {
        return CursorType.IBEAM;
    }

    /// Optional custom style for text box edge and background drawing where the default theme isn't suitable.
    ///
    /// @param border        border colour when the box is neither focused nor hovered
    /// @param hoverBorder   border colour when the mouse is over the box
    /// @param focusedBorder border colour when the box has keyboard focus
    /// @param fill          background colour inside the border
    public record CustomStyle(Color4I border, Color4I hoverBorder, Color4I focusedBorder, Color4I fill) {
        // neutral light-on-dark style
        public static final CustomStyle DARK = new CustomStyle(
                Color4I.rgb(0x6A6F73), Color4I.rgb(0x9AA2AA), Color4I.rgb(0xC8D0D8), Color4I.rgb(0x0B0C0D));
    }

}
