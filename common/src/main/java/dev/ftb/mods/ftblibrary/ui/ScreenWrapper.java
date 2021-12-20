package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.WrappedIngredient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * @author LatvianModder
 */
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
		wrappedGui.itemRenderer = itemRenderer;
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
		var key = new Key(keyCode, scanCode, modifiers);

		if (wrappedGui.keyPressed(key)) {
			return true;
		} else {
			if (key.backspace()) {
				wrappedGui.onBack();
			} else if (wrappedGui.onClosedByKey(key)) {
				wrappedGui.closeGui(false);
			} else if (Platform.isModLoaded("jei")) {
				var object = WrappedIngredient.unwrap(wrappedGui.getIngredientUnderMouse());

				if (object != null) {
					handleIngredientKey(key, object);
				}
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
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		wrappedGui.updateGui(mouseX, mouseY, partialTicks);
		renderBackground(matrixStack);
		GuiHelper.setupDrawing();
		var x = wrappedGui.getX();
		var y = wrappedGui.getY();
		var w = wrappedGui.width;
		var h = wrappedGui.height;
		var theme = wrappedGui.getTheme();
		wrappedGui.draw(matrixStack, theme, x, y, w, h);
		wrappedGui.drawForeground(matrixStack, theme, x, y, w, h);

		Objects.requireNonNullElse(wrappedGui.contextMenu, wrappedGui).addMouseOverText(tooltipList);

		if (!tooltipList.shouldRender()) {
			var object = wrappedGui.getIngredientUnderMouse();

			if (object instanceof WrappedIngredient && ((WrappedIngredient) object).tooltip) {
				var ingredient = WrappedIngredient.unwrap(object);

				if (ingredient instanceof ItemStack && !((ItemStack) ingredient).isEmpty()) {
					matrixStack.pushPose();
					matrixStack.translate(0, 0, tooltipList.zOffsetItemTooltip);
					renderTooltip(matrixStack, (ItemStack) ingredient, mouseX, mouseY);
					matrixStack.popPose();
				}
			}
		} else {
			tooltipList.render(matrixStack, mouseX, Math.max(mouseY, 18), wrappedGui.getScreen().getGuiScaledWidth(), wrappedGui.getScreen().getGuiScaledHeight(), theme.getFont());
		}

		tooltipList.reset();
	}

	@Override
	public void renderBackground(PoseStack matrixStack) {
		if (wrappedGui.drawDefaultBackground(matrixStack)) {
			super.renderBackground(matrixStack);
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
}