package dev.ftb.mods.ftbguilibrary.config.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbguilibrary.config.ConfigCallback;
import dev.ftb.mods.ftbguilibrary.config.ConfigValue;
import dev.ftb.mods.ftbguilibrary.config.ListConfig;
import dev.ftb.mods.ftbguilibrary.icon.Color4I;
import dev.ftb.mods.ftbguilibrary.icon.MutableColor4I;
import dev.ftb.mods.ftbguilibrary.utils.Key;
import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import dev.ftb.mods.ftbguilibrary.utils.TooltipList;
import dev.ftb.mods.ftbguilibrary.widget.BaseScreen;
import dev.ftb.mods.ftbguilibrary.widget.Button;
import dev.ftb.mods.ftbguilibrary.widget.GuiIcons;
import dev.ftb.mods.ftbguilibrary.widget.Panel;
import dev.ftb.mods.ftbguilibrary.widget.PanelScrollBar;
import dev.ftb.mods.ftbguilibrary.widget.SimpleButton;
import dev.ftb.mods.ftbguilibrary.widget.Theme;
import dev.ftb.mods.ftbguilibrary.widget.Widget;
import dev.ftb.mods.ftbguilibrary.widget.WidgetLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class EditConfigListScreen<E, CV extends ConfigValue<E>> extends BaseScreen {
	public static class ButtonConfigValue<E, CV extends ConfigValue<E>> extends Button {
		public final ListConfig<E, CV> list;
		public final int index;

		public ButtonConfigValue(Panel panel, ListConfig<E, CV> l, int i) {
			super(panel);
			list = l;
			index = i;
			setHeight(12);
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			boolean mouseOver = getMouseY() >= 20 && isMouseOver();

			MutableColor4I textCol = list.type.getColor(list.value.get(index)).mutable();
			textCol.setAlpha(255);

			if (mouseOver) {
				textCol.addBrightness(60);

				Color4I.WHITE.withAlpha(33).draw(matrixStack, x, y, w, h);

				if (getMouseX() >= x + w - 19) {
					Color4I.WHITE.withAlpha(33).draw(matrixStack, x + w - 19, y, 19, h);
				}
			}

			theme.drawString(matrixStack, getGui().getTheme().trimStringToWidth(list.type.getStringForGUI(list.value.get(index)), width), x + 4, y + 2, textCol, 0);

			if (mouseOver) {
				theme.drawString(matrixStack, "[-]", x + w - 16, y + 2, Color4I.WHITE, 0);
			}

			RenderSystem.color4f(1F, 1F, 1F, 1F);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();

			if (getMouseX() >= getX() + width - 19) {
				if (list.getCanEdit()) {
					list.value.remove(index);
					parent.refreshWidgets();
				}
			} else {
				list.type.value = list.value.get(index);
				list.type.onClicked(button, accepted -> {
					if (accepted) {
						list.value.set(index, list.type.value);
					}

					openGui();
				});
			}
		}

		@Override
		public void addMouseOverText(TooltipList l) {
			if (getMouseX() >= getX() + width - 19) {
				l.translate("selectServer.delete");
			} else {
				list.type.value = list.value.get(index);
				list.type.addInfo(l);
			}
		}
	}

	public class ButtonAddValue extends Button {
		public ButtonAddValue(Panel panel) {
			super(panel);
			setHeight(12);
			setTitle(new TextComponent("+ ").append(new TranslatableComponent("gui.add")));
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			boolean mouseOver = getMouseY() >= 20 && isMouseOver();

			if (mouseOver) {
				Color4I.WHITE.withAlpha(33).draw(matrixStack, x, y, w, h);
			}

			theme.drawString(matrixStack, getTitle(), x + 4, y + 2, theme.getContentColor(getWidgetType()), Theme.SHADOW);
			RenderSystem.color4f(1F, 1F, 1F, 1F);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			list.type.value = list.type.defaultValue == null ? null : list.type.copy(list.type.defaultValue);
			list.type.onClicked(button, accepted -> {
				if (accepted) {
					list.value.add(list.type.value);
				}

				openGui();
			});
		}

		@Override
		public void addMouseOverText(TooltipList list) {
		}
	}

	private final ListConfig<E, CV> list;
	private final ConfigCallback callback;

	private final Component title;
	private final Panel configPanel;
	private final Button buttonAccept, buttonCancel;
	private final PanelScrollBar scroll;

	public EditConfigListScreen(ListConfig<E, CV> l, ConfigCallback cb) {
		list = l;
		callback = cb;

		title = new TextComponent(list.getName()).withStyle(ChatFormatting.BOLD);

		configPanel = new Panel(this) {
			@Override
			public void addWidgets() {
				for (int i = 0; i < list.value.size(); i++) {
					add(new ButtonConfigValue<>(this, list, i));
				}

				if (list.getCanEdit()) {
					add(new ButtonAddValue(this));
				}
			}

			@Override
			public void alignWidgets() {
				for (Widget w : widgets) {
					w.setWidth(width - 16);
				}

				scroll.setMaxValue(align(WidgetLayout.VERTICAL));
			}
		};

		scroll = new PanelScrollBar(this, configPanel);
		buttonAccept = new SimpleButton(this, new TranslatableComponent("gui.accept"), GuiIcons.ACCEPT, (widget, button) -> callback.save(true));
		buttonCancel = new SimpleButton(this, new TranslatableComponent("gui.cancel"), GuiIcons.CANCEL, (widget, button) -> callback.save(false));
	}

	@Override
	public boolean onInit() {
		return setFullscreen();
	}

	@Override
	public void addWidgets() {
		add(buttonAccept);
		add(buttonCancel);
		add(configPanel);
		add(scroll);
	}

	@Override
	public void alignWidgets() {
		configPanel.setPosAndSize(0, 20, width, height - 20);
		configPanel.alignWidgets();
		scroll.setPosAndSize(width - 16, 20, 16, height - 20);

		buttonAccept.setPos(width - 18, 2);
		buttonCancel.setPos(width - 38, 2);
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			buttonCancel.onClicked(MouseButton.LEFT);
		}

		return false;
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		EditConfigScreen.COLOR_BACKGROUND.draw(matrixStack, 0, 0, w, 20);
		theme.drawString(matrixStack, getTitle(), 6, 6, Theme.SHADOW);
	}

	@Override
	public Component getTitle() {
		return title;
	}

	@Override
	public Theme getTheme() {
		return EditConfigScreen.THEME;
	}
}