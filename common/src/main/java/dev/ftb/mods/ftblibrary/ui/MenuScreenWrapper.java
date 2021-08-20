package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.WrappedIngredient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
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
	public boolean mouseScrolled(double x, double y, double scroll) {
		wrappedGui.mouseScrolled(scroll);
		return super.mouseScrolled(x, y, scroll);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		Key key = new Key(keyCode, scanCode, modifiers);

		if (wrappedGui.keyPressed(key)) {
			return true;
		} else {
			if (key.backspace()) {
				wrappedGui.onBack();
			} else if (wrappedGui.onClosedByKey(key)) {
				wrappedGui.closeGui(false);
			}

			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		Key key = new Key(keyCode, scanCode, modifiers);
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
	protected void renderBg(PoseStack matrixStack, float f, int mx, int my) {
		Theme theme = wrappedGui.getTheme();
		GuiHelper.setupDrawing();
		renderBackground(matrixStack);
		GuiHelper.setupDrawing();
		wrappedGui.draw(matrixStack, theme, leftPos, topPos, imageWidth, imageHeight);

		if (drawSlots) {
			GuiHelper.setupDrawing();

			for (Slot slot : menu.slots) {
				theme.drawContainerSlot(matrixStack, leftPos + slot.x, topPos + slot.y, 16, 16);
			}
		}
	}

	@Override
	protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
		matrixStack.pushPose();
		matrixStack.translate(-leftPos, -topPos, 0);
		GuiHelper.setupDrawing();

		Theme theme = wrappedGui.getTheme();
		wrappedGui.drawForeground(matrixStack, theme, leftPos, topPos, imageWidth, imageHeight);

		if (wrappedGui.contextMenu != null) {
			wrappedGui.contextMenu.addMouseOverText(tooltipList);
		} else {
			wrappedGui.addMouseOverText(tooltipList);
		}

		if (!tooltipList.shouldRender()) {
			Object object = wrappedGui.getIngredientUnderMouse();

			if (object instanceof WrappedIngredient && ((WrappedIngredient) object).tooltip) {
				Object ingredient = WrappedIngredient.unwrap(object);

				if (ingredient instanceof ItemStack && !((ItemStack) ingredient).isEmpty()) {
					matrixStack.pushPose();
					matrixStack.translate(0, 0, tooltipList.zOffsetItemTooltip);
					renderTooltip(matrixStack, (ItemStack) ingredient, mouseX, mouseY);
					matrixStack.popPose();
				}
			}
		} else {
			tooltipList.render(matrixStack, mouseX, Math.max(mouseY, 18), wrappedGui.getScreen().getGuiScaledWidth(), wrappedGui.getScreen().getGuiScaledHeight(), 0, theme.getFont());
		}

		tooltipList.reset();

		if (wrappedGui.contextMenu == null) {
			renderTooltip(matrixStack, mouseX, mouseY);
		}

		matrixStack.popPose();
	}

	@Override
	public void renderBackground(PoseStack matrixStack) {
		if (wrappedGui.drawDefaultBackground(matrixStack)) {
			super.renderBackground(matrixStack);
		}
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack);
		wrappedGui.updateGui(mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void tick() {
		super.tick();
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