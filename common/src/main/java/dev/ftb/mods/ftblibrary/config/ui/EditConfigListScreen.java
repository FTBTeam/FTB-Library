package dev.ftb.mods.ftblibrary.config.ui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractThreePanelScreen;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.ftb.mods.ftblibrary.util.TextComponentUtils.hotkeyTooltip;

public class EditConfigListScreen<E, CV extends ConfigValue<E>> extends AbstractThreePanelScreen<EditConfigListScreen<E,CV>.ConfigPanel> {
	private final ListConfig<E, CV> listConfig;
	private final ConfigCallback callback;
	private final List<E> localValues;
	private final int widestElement;

	private final Component title;
	private final ButtonAddValue addButton;

	boolean changed = false;

	public EditConfigListScreen(ListConfig<E, CV> listConfig, ConfigCallback callback) {
		super();

		this.listConfig = listConfig;
		this.callback = callback;

		localValues = new ArrayList<>(listConfig.getValue());

		title = Component.literal(listConfig.getName()).withStyle(ChatFormatting.BOLD);

		addButton = new ButtonAddValue(topPanel);

		widestElement = Math.max(getTheme().getStringWidth(title) + 25, listConfig.getValue().stream()
				.map(item -> getTheme().getStringWidth(listConfig.getType().getStringForGUI(item)))
				.max(Integer::compareTo)
				.orElse(176));
	}

	@Override
	public boolean onInit() {
		int maxH = (int) (getScreen().getGuiScaledHeight() * .8f);
		int maxW = (int) (getScreen().getGuiScaledWidth() * .9f);

		setHeight(Mth.clamp(localValues.size() * 12 + getTopPanelHeight() + bottomPanel.height, 176, maxH));
		setWidth(Mth.clamp(widestElement + 20, 176, maxW));
		return true;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	protected void doAccept() {
		if (changed) {
			listConfig.getValue().clear();
			listConfig.getValue().addAll(localValues);
		}
		callback.save(changed);
	}

	@Override
	protected int getTopPanelHeight() {
		return 20;
	}

	@Override
	protected Panel createTopPanel() {
		return new CustomTopPanel();
	}

	@Override
	protected ConfigPanel createMainPanel() {
		return new ConfigPanel();
	}

	@Override
	protected void doCancel() {
		if (changed) {
			openYesNo(Component.translatable("ftblibrary.unsaved_changes"), Component.empty(), this::reallyCancel);
		} else {
			reallyCancel();
		}
	}

	private void reallyCancel() {
		callback.save(false);
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			doCancel();
			return true;
		}

		return false;
	}

	@Override
	public boolean keyPressed(Key key) {
		if ((key.is(InputConstants.KEY_RETURN) || key.is(InputConstants.KEY_NUMPADENTER)) && key.modifiers.shift()) {
			doAccept();
			return true;
		} else if (key.is(InputConstants.KEY_INSERT)) {
			addButton.onClicked(MouseButton.LEFT);
			return true;
		} else if (key.is(InputConstants.KEY_DELETE)) {
			return mainPanel.getHoveredDeletable().map(d -> {
				d.deleteItem();
				return true;
			}).orElse(super.keyPressed(key));
		}
		return super.keyPressed(key);
	}

	@Override
	public Component getTitle() {
		return title;
	}

	public class ButtonAddValue extends SimpleButton implements EditStringConfigOverlay.PosProvider {
		public ButtonAddValue(Panel panel) {
			super(panel, Component.translatable("gui.add"), Icons.ADD, (btn, mb) -> {});
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			list.translate("gui.add");
			list.styledString("[Ins]", ChatFormatting.GRAY);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			CV listType = listConfig.getType();
			listType.setValue(listType.getDefaultValue() == null ? null : listType.copy(listType.getDefaultValue()));
			listType.onClicked(this, button, accepted -> {
				if (accepted) {
					localValues.add(listType.getValue());
					changed = true;
				}

				openGui();
			});
		}

		@Override
		public Offset getOverlayOffset() {
			return new Offset(-getGui().width / 2, 20);
		}
	}

	@FunctionalInterface
	public interface Deletable {
		void deleteItem();
	}

	public class ButtonConfigValue extends Button implements Deletable, EditStringConfigOverlay.PosProvider {
		private static final Component DEL_BUTTON_TXT =
				Component.literal("[").withStyle(ChatFormatting.RED)
						.append(Component.literal("X").withStyle(ChatFormatting.GOLD))
						.append(Component.literal("]").withStyle(ChatFormatting.RED));

		public final int index;

		public ButtonConfigValue(int index) {
			super(mainPanel);
			this.index = index;
			setHeight(12);
		}

		@Override
		public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			var mouseOver = getMouseY() >= 20 && isMouseOver();

			var textCol = listConfig.getType().getColor(localValues.get(index)).mutable();
			textCol.setAlpha(255);

			if (mouseOver) {
				textCol.addBrightness(60);
				Color4I.WHITE.withAlpha(33).draw(graphics, x, y, w, h);
				if (getMouseX() >= x + w - 19) {
					Color4I.WHITE.withAlpha(33).draw(graphics, x + w - 19, y, 19, h);
				}
			}

			theme.drawString(graphics, getGui().getTheme().trimStringToWidth(listConfig.getType().getStringForGUI(localValues.get(index)), width), x + 4, y + 2, textCol, 0);

			if (mouseOver) {
				theme.drawString(graphics, DEL_BUTTON_TXT, x + w - 16, y + 2, Color4I.WHITE, 0);
			}

			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();

			if (getMouseX() >= getX() + width - 19) {
				if (listConfig.getCanEdit()) {
					deleteItem();
				}
			} else {
				listConfig.getType().setValue(localValues.get(index));
				listConfig.getType().onClicked(this, button, accepted -> {
					if (accepted) {
						localValues.set(index, listConfig.getType().getValue());
						changed = true;
					}

					openGui();
				});
			}
		}

		@Override
		public void addMouseOverText(TooltipList l) {
			if (getMouseX() >= getX() + width - 19) {
				l.translate("selectServer.delete");
				l.add(hotkeyTooltip("Del"));
			} else {
				listConfig.getType().setValue(localValues.get(index));
				listConfig.getType().addInfo(l);
			}
		}

		@Override
		public void deleteItem() {
			localValues.remove(index);
			changed = true;
			parent.refreshWidgets();
		}

		@Override
		public Offset getOverlayOffset() {
			return new Offset(0, 0);
		}
	}

	public class ConfigPanel extends Panel {
		public ConfigPanel() {
			super(EditConfigListScreen.this);
		}

		@Override
		public void addWidgets() {
			for (var i = 0; i < localValues.size(); i++) {
				add(new ButtonConfigValue(i));
			}
		}

		@Override
		public void alignWidgets() {
			for (var w : widgets) {
				w.setX(2);
				w.setWidth(width - 4);
			}
			align(WidgetLayout.VERTICAL);
		}

		public Optional<Deletable> getHoveredDeletable() {
			return getWidgets().stream().filter(w -> w.isMouseOver() && w instanceof Deletable).map(w -> (Deletable) w).findFirst();
		}
	}

	private class CustomTopPanel extends TopPanel {
		private final TextField titleLabel = new TextField(this).setText(getTitle());

		@Override
		public void addWidgets() {
			titleLabel.addFlags(Theme.CENTERED_V);
			add(titleLabel);

			if (listConfig.getCanEdit()) {
				add(addButton);
			}
		}

		@Override
		public void alignWidgets() {
			titleLabel.setPosAndSize(4, 0, titleLabel.width, height);

			addButton.setPosAndSize(width - 18, 1, 16, 16);
		}

		@Override
		public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			super.draw(graphics, theme, x, y, w, h);

			theme.drawString(graphics, getGui().getTitle(), x + 6, y + 6, Theme.SHADOW);
		}
	}
}
