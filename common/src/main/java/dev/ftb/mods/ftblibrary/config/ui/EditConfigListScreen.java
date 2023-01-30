package dev.ftb.mods.ftblibrary.config.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class EditConfigListScreen<E, CV extends ConfigValue<E>> extends BaseScreen {
	private final ListConfig<E, CV> list;
	private final ConfigCallback callback;

	private final Component title;
	private final Panel configPanel;
	private final Button buttonAccept, buttonCancel;
	private final PanelScrollBar scroll;

	public EditConfigListScreen(ListConfig<E, CV> l, ConfigCallback cb) {
		list = l;
		callback = cb;

		title = Component.literal(list.getName()).withStyle(ChatFormatting.BOLD);

		configPanel = new Panel(this) {
			@Override
			public void addWidgets() {
				for (var i = 0; i < list.getValue().size(); i++) {
					add(new ButtonConfigValue<>(this, list, i));
				}

				if (list.getCanEdit()) {
					add(new ButtonAddValue(this));
				}
			}

			@Override
			public void alignWidgets() {
				for (var w : widgets) {
					w.setWidth(width - 16);
				}

				scroll.setMaxValue(align(WidgetLayout.VERTICAL));
			}
		};

		scroll = new PanelScrollBar(this, configPanel);
		buttonAccept = new SimpleButton(this, Component.translatable("gui.accept"), Icons.ACCEPT, (widget, button) -> doAccept());
		buttonCancel = new SimpleButton(this, Component.translatable("gui.cancel"), Icons.CANCEL, (widget, button) -> doCancel());
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

	private void doAccept() {
		callback.save(true);
	}

	private void doCancel() {
		callback.save(false);
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			buttonCancel.onClicked(MouseButton.LEFT);
			return true;
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

	public class ButtonAddValue extends Button {
		public ButtonAddValue(Panel panel) {
			super(panel);
			setHeight(12);
			setTitle(Component.literal("+ ").append(Component.translatable("gui.add")));
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			var mouseOver = getMouseY() >= 20 && isMouseOver();

			if (mouseOver) {
				Color4I.WHITE.withAlpha(33).draw(matrixStack, x, y, w, h);
			}

			theme.drawString(matrixStack, getTitle(), x + 4, y + 2, theme.getContentColor(getWidgetType()), Theme.SHADOW);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			CV listType = list.getType();
			listType.setValue(listType.getDefaultValue() == null ? null : listType.copy(listType.getDefaultValue()));
			listType.onClicked(button, accepted -> {
				if (accepted) {
					list.getValue().add(listType.getValue());
				}

				openGui();
			});
		}

		@Override
		public void addMouseOverText(TooltipList list) {
		}
	}

	public static class ButtonConfigValue<E, CV extends ConfigValue<E>> extends Button {
		public final ListConfig<E, CV> list;
		public final int index;

		public ButtonConfigValue(Panel panel, ListConfig<E, CV> list, int index) {
			super(panel);
			this.list = list;
			this.index = index;
			setHeight(12);
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			var mouseOver = getMouseY() >= 20 && isMouseOver();

			var textCol = list.getType().getColor(list.getValue().get(index)).mutable();
			textCol.setAlpha(255);

			if (mouseOver) {
				textCol.addBrightness(60);

				Color4I.WHITE.withAlpha(33).draw(matrixStack, x, y, w, h);

				if (getMouseX() >= x + w - 19) {
					Color4I.WHITE.withAlpha(33).draw(matrixStack, x + w - 19, y, 19, h);
				}
			}

			theme.drawString(matrixStack, getGui().getTheme().trimStringToWidth(list.getType().getStringForGUI(list.getValue().get(index)), width), x + 4, y + 2, textCol, 0);

			if (mouseOver) {
				theme.drawString(matrixStack, "[-]", x + w - 16, y + 2, Color4I.WHITE, 0);
			}

			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();

			if (getMouseX() >= getX() + width - 19) {
				if (list.getCanEdit()) {
					list.getValue().remove(index);
					parent.refreshWidgets();
				}
			} else {
				list.getType().setValue(list.getValue().get(index));
				list.getType().onClicked(button, accepted -> {
					if (accepted) {
						list.getValue().set(index, list.getType().getValue());
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
				list.getType().setValue(list.getValue().get(index));
				list.getType().addInfo(l);
			}
		}
	}
}
