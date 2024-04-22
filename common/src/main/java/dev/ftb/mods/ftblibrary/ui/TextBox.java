package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Predicate;

public class TextBox extends Widget implements IFocusableWidget {
	private boolean isFocused = false;
	public int charLimit = 2000;
	public Color4I textColor = Icon.empty();

	public String ghostText = "";
	private String text = "";
	private int displayPos;
	private int cursorPos;
	private int highlightPos;
	private boolean validText = true;
	private int maxLength = 1024;
	private Predicate<String> filter;

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

	public void setFilter(Predicate<String> filter) {
		this.filter = filter;
	}

	public final String getText() {
		return text;
	}

	public String getSelectedText() {
		return text.substring(Math.min(cursorPos, highlightPos), Math.max(cursorPos, highlightPos));
	}

	public void setText(String string, boolean triggerChange) {
		if (filter.test(string)) {
			if (string.length() > maxLength) {
				text = string.substring(0, maxLength);
			} else {
				text = string;
			}

			validText = isValid(text);

			moveCursorToEnd(false);
			setSelectionPos(cursorPos);
			if (triggerChange) {
				onTextChanged();
			}
		}
	}

	public final void setText(String s) {
		setText(s, true);
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

		onTextChanged();
	}

	public void moveCursorToStart(boolean extendSelection) {
		moveCursorTo(0, extendSelection);
	}

	public void moveCursorToEnd(boolean extendSelection) {
		moveCursorTo(text.length(), extendSelection);
	}

	public void setCursorPos(int pos) {
		cursorPos = Mth.clamp(pos, 0, text.length());
		scrollTo(cursorPos);
	}

	public void setSelectionPos(int i) {
		highlightPos = Mth.clamp(i, 0, text.length());
		scrollTo(highlightPos);
	}

	public int getCursorPos() {
		return cursorPos;
	}

	public void insertText(String string) {
		int selStart = Math.min(cursorPos, highlightPos);
		int selEnd = Math.max(cursorPos, highlightPos);
		int space = maxLength - text.length() - (selStart - selEnd);
		if (space > 0) {
			String filtered = SharedConstants.filterText(string);
			int nToInsert = filtered.length();
			if (space < nToInsert) {
				if (Character.isHighSurrogate(filtered.charAt(space - 1))) {
					--space;
				}

				filtered = filtered.substring(0, space);
				nToInsert = space;
			}

			String newText = (new StringBuilder(text)).replace(selStart, selEnd, filtered).toString();
			validText = isValid(newText);
			if (validText) {
				text = newText;
				setCursorPosition(selStart + nToInsert);
				setSelectionPos(cursorPos);
				onTextChanged();
			}
		}
	}

	private void scrollTo(int pos) {
		Font font = getGui().getTheme().getFont();
		if (font != null) {
			displayPos = Math.min(displayPos, text.length());
			String string = font.plainSubstrByWidth(text.substring(displayPos), width);
			int k = string.length() + displayPos;
			if (pos == displayPos) {
				displayPos -= font.plainSubstrByWidth(text, width, true).length();
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

		for(int m = 0; m < absCount; ++m) {
			if (!backwards) {
				int n = text.length();
				res = text.indexOf(' ', res);
				if (res == -1) {
					res = n;
				} else {
					while(res < n && text.charAt(res) == ' ') {
						++res;
					}
				}
			} else {
				while(res > 0 && text.charAt(res - 1) == ' ') {
					--res;
				}

				while(res > 0 && text.charAt(res - 1) != ' ') {
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
		if (Screen.hasControlDown()) {
			deleteWords(count);
		} else {
			deleteChars(count);
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
		if (isMouseOver()) {
			setFocused(true);

			if (button.isLeft()) {
				if (isFocused) {
					var i = getMouseX() - getX();
					var theme = getGui().getTheme();
					var s = theme.trimStringToWidth(text.substring(displayPos), width);
					if (isShiftKeyDown()) {
						setSelectionPos(theme.trimStringToWidth(s, i).length() + displayPos);
					} else {
						setCursorPos(theme.trimStringToWidth(s, i).length() + displayPos);
					}
				}
			} else if (button.isRight() && getText().length() > 0 && allowInput()) {
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
			switch (key.keyCode) {
				case GLFW.GLFW_KEY_ESCAPE -> {
					setFocused(false);
					return true;
				}
				case GLFW.GLFW_KEY_BACKSPACE -> {
					deleteText(-1);
					return true;
				}
				case GLFW.GLFW_KEY_HOME -> {
					moveCursorToStart(Screen.hasShiftDown());
					return true;
				}
				case GLFW.GLFW_KEY_LEFT -> {
					if (Screen.hasControlDown()) {
						moveCursorTo(getWordPosition(-1), Screen.hasShiftDown());
					} else {
						moveCursor(-1, Screen.hasShiftDown());
					}
					return true;
				}
				case GLFW.GLFW_KEY_RIGHT -> {
					if (Screen.hasControlDown()) {
						moveCursorTo(getWordPosition(1), Screen.hasShiftDown());
					} else {
						moveCursor(1, Screen.hasShiftDown());
					}
					return true;
				}
				case GLFW.GLFW_KEY_END -> {
					moveCursorToEnd(Screen.hasShiftDown());
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
	public boolean charTyped(char c, KeyModifiers modifiers) {
		if (isFocused()) {
			if (SharedConstants.isAllowedChatCharacter(c)) {
				insertText(Character.toString(c));
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
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		drawTextBox(graphics, theme, x, y, w, h);
		var drawGhostText = !isFocused() && text.isEmpty() && !ghostText.isEmpty();
		var textToDraw = getFormattedText();
		GuiHelper.pushScissor(getScreen(), x, y, w, h);

		var col = validText ? (textColor.isEmpty() ? theme.getContentColor(WidgetType.NORMAL) : textColor).withAlpha(drawGhostText ? 120 : 255) : Color4I.RED;
		var j = cursorPos - displayPos;
		var s = theme.trimStringToWidth(textToDraw.substring(displayPos), w);
		var textX = x + 4;
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
			cursorX = j > 0 ? textX + w : textX;
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
				col.draw(graphics, cursorX, textY - 1, 1, theme.getFontHeight() + 2);
			} else {
				col.draw(graphics, cursorX, textY + theme.getFontHeight() - 2, 5, 1);
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

			endX = Math.min(endX, x + w);
			startX = Math.min(startX, x + w);

			graphics.fill(RenderType.guiTextHighlight(), startX, startY, endX, endY, 0x80000080);
		}

		GuiHelper.popScissor(getScreen());
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}

	public void drawTextBox(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		theme.drawTextBox(graphics, x, y, w, h);
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
}
