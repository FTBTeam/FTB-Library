package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class TextBox extends Widget implements IFocusableWidget {
	private boolean isFocused = false;
	public int charLimit = 2000;
	public Color4I textColor = Icon.empty();

	public String ghostText = "";
	private String text = "";
	private int lineScrollOffset;
	private int cursorPosition;
	private int selectionEnd;
	private boolean validText;

	public TextBox(Panel panel) {
		super(panel);
		setText("", false);
	}

	@Override
	public final boolean isFocused() {
		return isFocused;
	}

	@Override
	public final void setFocused(boolean focused) {
		if (isFocused != focused) {
			isFocused = focused;
			validText = isValid(text);
			if (focused) {
				getGui().setFocusedWidget(this);
			}
		}
	}

//	@Override
//	public void onClosed() {
//	}

	public final String getText() {
		return text;
	}

	public String getSelectedText() {
		return text.substring(Math.min(cursorPosition, selectionEnd), Math.max(cursorPosition, selectionEnd));
	}

	public final void setText(String s, boolean triggerChange) {
		text = s;

		if (text.isEmpty()) {
			lineScrollOffset = 0;
			cursorPosition = 0;
			selectionEnd = 0;
		}

		cursorPosition = Math.min(cursorPosition, s.length());

		validText = isValid(s);

		if (validText && triggerChange) {
			onTextChanged();
		}
	}

	public final void setText(String s) {
		setText(s, true);
	}

	public void setCursorPosition(int pos) {
		cursorPosition = pos;
		var i = text.length();
		cursorPosition = Mth.clamp(cursorPosition, 0, i);
		setSelectionPos(cursorPosition);
	}

	public int getCursorPosition() {
		return cursorPosition;
	}

	public void moveCursorBy(int num) {
		setCursorPosition(selectionEnd + num);
//		int from = num > 0 ?
//				Math.max(cursorPosition, selectionEnd) :
//				Math.min(cursorPosition, selectionEnd);
//
//		setCursorPosition(from + num);
	}

	public void writeText(String textToWrite) {
		if (!textToWrite.isEmpty() && !allowInput()) {
			return;
		}

		var s = "";
		var s1 = SharedConstants.filterText(textToWrite);
		var i = Math.min(cursorPosition, selectionEnd);
		var j = Math.max(cursorPosition, selectionEnd);
		var k = charLimit - text.length() - (i - j);

		if (!text.isEmpty()) {
			s = s + text.substring(0, i);
		}

		int l;

		if (k < s1.length()) {
			s = s + s1.substring(0, k);
			l = k;
		} else {
			s = s + s1;
			l = s1.length();
		}

		if (!text.isEmpty() && j < text.length()) {
			s = s + text.substring(j);
		}

		setText(s);
		moveCursorBy(i - selectionEnd + l);
	}

	public void setSelectionPos(int position) {
		var i = text.length();

		if (position > i) {
			position = i;
		}

		if (position < 0) {
			position = 0;
		}

		selectionEnd = position;

		if (lineScrollOffset > i) {
			lineScrollOffset = i;
		}

		var j = width - 10;
		var theme = getGui().getTheme();
		var s = theme.trimStringToWidth(text.substring(lineScrollOffset), j);
		var k = s.length() + lineScrollOffset;

		if (position == lineScrollOffset) {
			lineScrollOffset -= theme.trimStringToWidthReverse(text, j).length();
		}

		if (position > k) {
			lineScrollOffset += position - k;
		} else if (position <= lineScrollOffset) {
			lineScrollOffset -= lineScrollOffset - position;
		}

		lineScrollOffset = Mth.clamp(lineScrollOffset, 0, i);
	}

	public int getNthWordFromCursor(int numWords) {
		return getNthWordFromPos(numWords, cursorPosition);
	}

	public int getNthWordFromPos(int n, int pos) {
		return getNthWordFromPosWS(n, pos, true);
	}

	public int getNthWordFromPosWS(int n, int pos, boolean skipWs) {
		var i = pos;
		var flag = n < 0;
		var j = Math.abs(n);

		for (var k = 0; k < j; ++k) {
			if (!flag) {
				var l = text.length();
				i = text.indexOf(32, i);

				if (i == -1) {
					i = l;
				} else {
					while (skipWs && i < l && text.charAt(i) == 32) {
						++i;
					}
				}
			} else {
				while (skipWs && i > 0 && text.charAt(i - 1) == 32) {
					--i;
				}

				while (i > 0 && text.charAt(i - 1) != 32) {
					--i;
				}
			}
		}

		return i;
	}

	public boolean allowInput() {
		return true;
	}

	public void deleteWords(int num) {
		if (!text.isEmpty() && allowInput()) {
			if (selectionEnd != cursorPosition) {
				writeText("");
			} else {
				deleteFromCursor(getNthWordFromCursor(num) - cursorPosition);
			}
		}
	}

	public void deleteFromCursor(int num) {
		if (text.isEmpty() || !allowInput()) {
			return;
		}

		if (selectionEnd != cursorPosition) {
			writeText("");
		} else {
			var flag = num < 0;
			var i = flag ? cursorPosition + num : cursorPosition;
			var j = flag ? cursorPosition : cursorPosition + num;
			var s = "";

			if (i >= 0) {
				s = text.substring(0, i);
			}

			if (j < text.length()) {
				s = s + text.substring(j);
			}

			setText(s);

			if (flag) {
				moveCursorBy(num);
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
					var s = theme.trimStringToWidth(text.substring(lineScrollOffset), width);
					if (isShiftKeyDown()) {
						setSelectionPos(theme.trimStringToWidth(s, i).length() + lineScrollOffset);
					} else {
						setCursorPosition(theme.trimStringToWidth(s, i).length() + lineScrollOffset);
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
			setCursorPosition(text.length());
			setSelectionPos(0);
			return true;
		} else if (key.copy()) {
			setClipboardString(getSelectedText());
			return true;
		} else if (key.paste()) {
			writeText(getClipboardString());
			return true;
		} else if (key.cut()) {
			setClipboardString(getSelectedText());
			writeText("");
			return true;
		} else {
			switch (key.keyCode) {
				case GLFW.GLFW_KEY_ESCAPE -> {
					setFocused(false);
					return true;
				}
				case GLFW.GLFW_KEY_BACKSPACE -> {
					if (Screen.hasControlDown()) {
						deleteWords(-1);
					} else {
						deleteFromCursor(-1);
					}
					return true;
				}
				case GLFW.GLFW_KEY_HOME -> {
					if (Screen.hasShiftDown()) {
						setSelectionPos(0);
					} else {
						setCursorPosition(0);
					}
					return true;
				}
				case GLFW.GLFW_KEY_LEFT -> {
					if (Screen.hasShiftDown()) {
						if (Screen.hasControlDown()) {
							setSelectionPos(getNthWordFromPos(-1, selectionEnd));
						} else {
							setSelectionPos(selectionEnd - 1);
						}
					} else if (Screen.hasControlDown()) {
						setCursorPosition(getNthWordFromCursor(-1));
					} else {
						moveCursorBy(-1);
					}
					return true;
				}
				case GLFW.GLFW_KEY_RIGHT -> {
					if (Screen.hasShiftDown()) {
						if (Screen.hasControlDown()) {
							setSelectionPos(getNthWordFromPos(1, selectionEnd));
						} else {
							setSelectionPos(selectionEnd + 1);
						}
					} else if (Screen.hasControlDown()) {
						setCursorPosition(getNthWordFromCursor(1));
					} else {
						moveCursorBy(1);
					}
					return true;
				}
				case GLFW.GLFW_KEY_END -> {
					if (Screen.hasShiftDown()) {
						setSelectionPos(text.length());
					} else {
						setCursorPosition(text.length());
					}
					return true;
				}
				case GLFW.GLFW_KEY_DELETE -> {
					if (Screen.hasControlDown()) {
						deleteWords(1);
					} else {
						deleteFromCursor(1);
					}
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
				writeText(Character.toString(c));
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
		var j = cursorPosition - lineScrollOffset;
		var k = selectionEnd - lineScrollOffset;
		var s = theme.trimStringToWidth(textToDraw.substring(lineScrollOffset), w);
		var textX = x + 4;
		var textY = y + (h - 8) / 2;
		var textX1 = textX;

		if (k > s.length()) {
			k = s.length();
		}

		if (!s.isEmpty()) {
			var s1 = j > 0 && j <= s.length() ? s.substring(0, j) : s;
			textX1 = theme.drawString(graphics, Component.literal(s1), textX, textY, col, 0);
		}

		var drawCursor = cursorPosition < textToDraw.length() || textToDraw.length() >= charLimit;
		var cursorX = textX1;

		if (j <= 0 || j > s.length()) {
			cursorX = j > 0 ? textX + w : textX;
		} else if (drawCursor) {
			cursorX = textX1 - 1;
			//--textX1;
		}

		if (j > 0 && j < s.length()) {
			theme.drawString(graphics, Component.literal(s.substring(j)), textX1, textY, col, 0);
		}

		if (j >= 0 && j <= s.length() && isFocused() && System.currentTimeMillis() % 1000L > 500L) {
			if (drawCursor) {
				col.draw(graphics, cursorX, textY - 1, 1, theme.getFontHeight() + 2);
			} else {
				col.draw(graphics, cursorX, textY + theme.getFontHeight() - 2, 5, 1);
			}
		}

		if (k != j) {
			var xMax = textX + theme.getStringWidth(Component.literal(s.substring(0, k)));

			int startX = Math.min(cursorX, xMax - 1);
			int endX = Math.max(cursorX, xMax - 1);
			int startY = textY - 1;
			int endY = textY + theme.getFontHeight();

//			int startX = cursorX;
//            int startY = textY - 1;
//            int endX = xMax - 1;
//            int endY = textY + 1 + theme.getFontHeight();

//            if (startX < endX) {
//				var i = startX;
//				startX = endX;
//				endX = i;
//			}
//
//			if (startY < endY) {
//				var j12 = startY;
//				startY = endY;
//				endY = j12;
//			}

			endX = Math.min(endX, x + w);
			startX = Math.min(startX, x + w);

//			if (endX > x + w) {
//				endX = x + w;
//			}
//
//			if (startX > x + w) {
//				startX = x + w;
//			}

			graphics.fill(RenderType.guiTextHighlight(), startX, startY, endX, endY, 0x80000080);

			// (please help)
//			var tesselator = Tesselator.getInstance();
//			var bufferBuilder = tesselator.getBuilder();
//			RenderSystem.setShader(GameRenderer::getPositionShader);
//			RenderSystem.setShaderColor(0, 0, 1, 1);
//			RenderSystem.enableColorLogicOp();
//			RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
//			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
//			bufferBuilder.vertex(startX, endY, 0).endVertex();
//			bufferBuilder.vertex(endX, endY, 0).endVertex();
//			bufferBuilder.vertex(endX, startY, 0).endVertex();
//			bufferBuilder.vertex(startX, startY, 0).endVertex();
//			tesselator.end();
//			RenderSystem.disableColorLogicOp();
		}

		GuiHelper.popScissor(getScreen());
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}

	public void drawTextBox(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		theme.drawTextBox(graphics, x, y, w, h);
	}

	public boolean isValid(String txt) {
		return true;
	}

	public final boolean isTextValid() {
		return validText;
	}

	@Override
	public CursorType getCursor() {
		return CursorType.IBEAM;
	}
}
