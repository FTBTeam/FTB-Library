package dev.ftb.mods.ftblibrary.ui;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ScreenWrapper extends Screen implements IScreenWrapper {
	private final BaseScreen wrappedGui;
	private final TooltipList tooltipList = new TooltipList();

	public ScreenWrapper(BaseScreen g) {
		super(g.getTitle());
		wrappedGui = g;
	}

	@Override
	public void init() {
		super.init();
//		wrappedGui.itemRenderer = itemRenderer;
		wrappedGui.initGui();
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
			return wrappedGui.mousePressed(MouseButton.get(button)) || super.mouseClicked(x, y, button);
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
		return wrappedGui.mouseScrolled(dirY) || super.mouseScrolled(x, y, dirX, dirY);
	}

	@Override
	public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
		return wrappedGui.mouseDragged(button, dragX, dragY) || super.mouseDragged(x, y, button, dragX, dragY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		var key = new Key(keyCode, scanCode, modifiers);

		if (wrappedGui.keyPressed(key)) {
			return true;
		} else {
			if (key.backspace()) {
				wrappedGui.onBack();
				return true;
			} else if (wrappedGui.onClosedByKey(key)) {
				if (shouldCloseOnEsc()) {
					wrappedGui.closeGui(true);
				}
				return true;
			} else if (Platform.isModLoaded("jei")) {
				wrappedGui.getIngredientUnderMouse().ifPresent(underMouse -> handleIngredientKey(key, underMouse.ingredient()));
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

	private void handleIngredientKey(Key key, Object object) {
		//FIXME: FTBLibJEIIntegration.handleIngredientKey(key, object);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		wrappedGui.updateGui(mouseX, mouseY, partialTicks);
		renderBackground(graphics, mouseX, mouseY, partialTicks);
		GuiHelper.setupDrawing();
		var x = wrappedGui.getX();
		var y = wrappedGui.getY();
		var w = wrappedGui.width;
		var h = wrappedGui.height;
		var theme = wrappedGui.getTheme();
		wrappedGui.draw(graphics, theme, x, y, w, h);
		wrappedGui.drawForeground(graphics, theme, x, y, w, h);

		wrappedGui.addMouseOverText(tooltipList);

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
			graphics.pose().translate(0, 0, 600);
			graphics.setColor(1f, 1f, 1f, 0.8f);
			graphics.renderTooltip(theme.getFont(), tooltipList.getLines(), Optional.empty(), mouseX, Math.max(mouseY, 18));
			graphics.setColor(1f, 1f, 1f, 1f);
			graphics.pose().translate(0, 0, -600);
		}

		tooltipList.reset();
	}

	@Override
	public void renderBackground(GuiGraphics matrixStack, int x, int y, float partialTicks) {
		if (wrappedGui.drawDefaultBackground(matrixStack)) {
			super.renderBackground(matrixStack, x, y, partialTicks);
		}
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

	@Override
	public boolean shouldCloseOnEsc() {
		return getGui().shouldCloseOnEsc();
	}
}
