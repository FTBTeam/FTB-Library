package com.feed_the_beast.mods.ftbguilibrary.widget;

import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.KeyModifiers;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

/**
 * @author LatvianModder
 */
public class GuiContainerWrapper extends ContainerScreen implements IGuiWrapper
{
	private final GuiBase wrappedGui;
	private boolean drawSlots = true;
	private final TooltipList tooltipList = new TooltipList();

	public GuiContainerWrapper(GuiBase g, Container c, PlayerInventory playerInventory, ITextComponent title)
	{
		super(c, playerInventory, title);
		wrappedGui = g;
	}

	public GuiContainerWrapper disableSlotDrawing()
	{
		drawSlots = false;
		return this;
	}

	@Override
	public void init()
	{
		super.init();
		wrappedGui.initGui();
		guiLeft = wrappedGui.getX();
		guiTop = wrappedGui.getY();
		xSize = wrappedGui.width;
		ySize = wrappedGui.height;
	}

	@Override
	public boolean isPauseScreen()
	{
		return wrappedGui.doesGuiPauseGame();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button)
	{
		wrappedGui.updateMouseOver((int) x, (int) y);

		if (button == MouseButton.BACK.id)
		{
			wrappedGui.onBack();
			return true;
		}
		else
		{
			wrappedGui.mousePressed(MouseButton.get(button));
			return super.mouseClicked(x, y, button);
		}
	}

	@Override
	public boolean mouseReleased(double x, double y, int button)
	{
		wrappedGui.updateMouseOver((int) x, (int) y);
		wrappedGui.mouseReleased(MouseButton.get(button));
		return super.mouseReleased(x, y, button);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scroll)
	{
		wrappedGui.mouseScrolled(scroll);
		return super.mouseScrolled(x, y, scroll);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		Key key = new Key(keyCode, scanCode, modifiers);

		if (wrappedGui.keyPressed(key))
		{
			return true;
		}
		else
		{
			if (key.backspace())
			{
				wrappedGui.onBack();
			}
			else if (wrappedGui.onClosedByKey(key))
			{
				wrappedGui.closeGui(false);
			}

			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers)
	{
		Key key = new Key(keyCode, scanCode, modifiers);
		wrappedGui.keyReleased(key);
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char keyChar, int modifiers)
	{
		if (wrappedGui.charTyped(keyChar, new KeyModifiers(modifiers)))
		{
			return true;
		}

		return super.charTyped(keyChar, keyChar);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float f, int mx, int my)
	{
		Theme theme = wrappedGui.getTheme();
		GuiHelper.setupDrawing();
		renderBackground(matrixStack);
		GuiHelper.setupDrawing();
		wrappedGui.draw(matrixStack, theme, guiLeft, guiTop, xSize, ySize);

		if (drawSlots)
		{
			GuiHelper.setupDrawing();

			for (Slot slot : container.inventorySlots)
			{
				theme.drawContainerSlot(matrixStack, guiLeft + slot.xPos, guiTop + slot.yPos, 16, 16);
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY)
	{
		matrixStack.push();
		matrixStack.translate(-guiLeft, -guiTop, 0);
		GuiHelper.setupDrawing();

		Theme theme = wrappedGui.getTheme();
		wrappedGui.drawForeground(matrixStack, theme, guiLeft, guiTop, xSize, ySize);

		if (wrappedGui.contextMenu != null)
		{
			wrappedGui.contextMenu.addMouseOverText(tooltipList);
		}
		else
		{
			wrappedGui.addMouseOverText(tooltipList);
		}

		if (!tooltipList.shouldRender())
		{
			Object object = wrappedGui.getIngredientUnderMouse();

			if (object instanceof WrappedIngredient && ((WrappedIngredient) object).tooltip)
			{
				Object ingredient = WrappedIngredient.unwrap(object);

				if (ingredient instanceof ItemStack && !((ItemStack) ingredient).isEmpty())
				{
					renderTooltip(matrixStack, (ItemStack) ingredient, mouseX, mouseY);
				}
			}
		}
		else
		{
			tooltipList.render(matrixStack, mouseX, Math.max(mouseY, 18), wrappedGui.getScreen().getScaledWidth(), wrappedGui.getScreen().getScaledHeight(), 0, theme.getFont());
		}

		tooltipList.reset();

		if (wrappedGui.contextMenu == null)
		{
			renderHoveredTooltip(matrixStack, mouseX, mouseY);
		}

		matrixStack.pop();
	}

	@Override
	public void renderBackground(MatrixStack matrixStack)
	{
		if (wrappedGui.drawDefaultBackground(matrixStack))
		{
			super.renderBackground(matrixStack);
		}
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		renderBackground(matrixStack);
		wrappedGui.updateGui(mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void tick()
	{
		super.tick();
		wrappedGui.tick();
	}

	@Override
	public GuiBase getGui()
	{
		return wrappedGui;
	}

	@Override
	public void onClose()
	{
		wrappedGui.onClosed();
		super.onClose();
	}
}