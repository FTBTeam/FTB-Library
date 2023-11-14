package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;


public class MenuScreenWrapper<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IScreenWrapper {
	private final BaseScreen wrappedGui;
	private boolean drawSlots = true;
	private final TooltipList tooltipList = new TooltipList();

	public MenuScreenWrapper(BaseScreen g, T c, Inventory playerInventory, Component title) {
		super(c, playerInventory, title);
		wrappedGui = g;
	}

	public MenuScreenWrapper<T> disableSlotDrawing() {
		drawSlots = false;
		return this;
	}

	@Override
	public void init() {
		super.init();
		wrappedGui.initGui();
		leftPos = wrappedGui.getX();
		topPos = wrappedGui.getY();
		imageWidth = wrappedGui.width;
		imageHeight = wrappedGui.height;
	}

	@Override
	public boolean isPauseScreen() {
		return wrappedGui.doesGuiPauseGame();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		wrappedGui.updateMouseOver((int) x, (int) y);

		if (button == MouseButton.BACK.id) {
			wrappedGui.onBack();
			return true;
		} else {
			wrappedGui.mousePressed(MouseButton.get(button));
			return super.mouseClicked(x, y, button);
		}
	}

	@Override
	public boolean mouseReleased(double x, double y, int button) {
		wrappedGui.updateMouseOver((int) x, (int) y);
		wrappedGui.mouseReleased(MouseButton.get(button));
		return super.mouseReleased(x, y, button);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double dirX, double dirY) {
		wrappedGui.mouseScrolled(dirY);
		return super.mouseScrolled(x, y, dirX, dirY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		var key = new Key(keyCode, scanCode, modifiers);

		if (wrappedGui.keyPressed(key)) {
			return true;
		} else {
			if (key.backspace()) {
				wrappedGui.onBack();
			} else if (wrappedGui.onClosedByKey(key)) {
				wrappedGui.closeGui(true);
			}

			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		var key = new Key(keyCode, scanCode, modifiers);
		wrappedGui.keyReleased(key);
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char keyChar, int modifiers) {
		if (wrappedGui.charTyped(keyChar, new KeyModifiers(modifiers))) {
			return true;
		}

		return super.charTyped(keyChar, keyChar);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float f, int mx, int my) {
		var theme = wrappedGui.getTheme();
		GuiHelper.setupDrawing();
		renderBackground(graphics, mx, my, f);
		GuiHelper.setupDrawing();
		wrappedGui.draw(graphics, theme, leftPos, topPos, imageWidth, imageHeight);

		if (drawSlots) {
			GuiHelper.setupDrawing();

			for (var slot : menu.slots) {
				theme.drawContainerSlot(graphics, leftPos + slot.x, topPos + slot.y, 16, 16);
			}
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.pose().pushPose();
		graphics.pose().translate(-leftPos, -topPos, 0);
		GuiHelper.setupDrawing();

		var theme = wrappedGui.getTheme();
		wrappedGui.drawForeground(graphics, theme, leftPos, topPos, imageWidth, imageHeight);

		wrappedGui.getContextMenu().orElse(wrappedGui).addMouseOverText(tooltipList);

		if (!tooltipList.shouldRender()) {
			wrappedGui.getIngredientUnderMouse().ifPresent(underMouse -> {
				if (underMouse.tooltip()) {
					var ingredient = underMouse.ingredient();
					if (ingredient instanceof ItemStack stack && !stack.isEmpty()) {
						graphics.pose().pushPose();
						graphics.pose().translate(0, 0, tooltipList.zOffsetItemTooltip);
						graphics.renderTooltip(theme.getFont(), (ItemStack) ingredient, mouseX, mouseY);
						graphics.pose().popPose();
					}
				}
			});
		} else {
			tooltipList.render(graphics, mouseX, Math.max(mouseY, 18), wrappedGui.getScreen().getGuiScaledWidth(), wrappedGui.getScreen().getGuiScaledHeight(), theme.getFont());
		}

		tooltipList.reset();

		if (wrappedGui.getContextMenu().isEmpty()) {
			renderTooltip(graphics, mouseX, mouseY);
		}

		graphics.pose().popPose();
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int x, int y, float partialTicks) {
		if (wrappedGui.drawDefaultBackground(graphics)) {
			super.renderBackground(graphics, x, y, partialTicks);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics, mouseX, mouseY, partialTicks);
		wrappedGui.updateGui(mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void containerTick() {
		super.containerTick();
		wrappedGui.tick();
	}

	@Override
	public BaseScreen getGui() {
		return wrappedGui;
	}

	@Override
	public void removed() {
		wrappedGui.onClosed();
		super.removed();
	}
}
