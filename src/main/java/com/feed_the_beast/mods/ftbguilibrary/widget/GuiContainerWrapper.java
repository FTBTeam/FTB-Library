package com.feed_the_beast.mods.ftbguilibrary.widget;

import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.KeyModifiers;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiContainerWrapper extends ContainerScreen implements IGuiWrapper
{
	private GuiBase wrappedGui;
	private boolean drawSlots = true;
	private List<String> tempTextList = new ArrayList<>();

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
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		if (wrappedGui.fixUnicode)
		{
			GuiHelper.setFixUnicode(true);
		}

		Theme theme = wrappedGui.getTheme();
		GuiHelper.setupDrawing();
		renderBackground();
		GuiHelper.setupDrawing();
		wrappedGui.draw(theme, guiLeft, guiTop, xSize, ySize);

		if (drawSlots)
		{
			GuiHelper.setupDrawing();

			for (Slot slot : container.inventorySlots)
			{
				theme.drawContainerSlot(guiLeft + slot.xPos, guiTop + slot.yPos, 16, 16);
			}
		}

		if (wrappedGui.fixUnicode)
		{
			GuiHelper.setFixUnicode(false);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		if (wrappedGui.fixUnicode)
		{
			GuiHelper.setFixUnicode(true);
		}

		RenderSystem.pushMatrix();
		RenderSystem.translatef(-guiLeft, -guiTop, 0F);
		GuiHelper.setupDrawing();

		Theme theme = wrappedGui.getTheme();
		wrappedGui.drawForeground(theme, guiLeft, guiTop, xSize, ySize);

		if (wrappedGui.contextMenu != null)
		{
			wrappedGui.contextMenu.addMouseOverText(tempTextList);
		}
		else
		{
			wrappedGui.addMouseOverText(tempTextList);
		}

		if (tempTextList.isEmpty())
		{
			Object object = wrappedGui.getIngredientUnderMouse();

			if (object instanceof WrappedIngredient && ((WrappedIngredient) object).tooltip)
			{
				Object ingredient = WrappedIngredient.unwrap(object);

				if (ingredient instanceof ItemStack && !((ItemStack) ingredient).isEmpty())
				{
					renderTooltip((ItemStack) ingredient, mouseX, mouseY);
				}
			}
		}
		else
		{
			GuiUtils.drawHoveringText(tempTextList, mouseX, Math.max(mouseY, 18), wrappedGui.getScreen().getScaledWidth(), wrappedGui.getScreen().getScaledHeight(), 0, theme.getFont());
		}

		tempTextList.clear();

		if (wrappedGui.contextMenu == null)
		{
			renderHoveredToolTip(mouseX, mouseY);
		}

		RenderSystem.popMatrix();

		if (wrappedGui.fixUnicode)
		{
			GuiHelper.setFixUnicode(false);
		}
	}

	@Override
	public void renderBackground()
	{
		if (wrappedGui.drawDefaultBackground())
		{
			super.renderBackground();
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		renderBackground();
		wrappedGui.updateGui(mouseX, mouseY, partialTicks);
		super.render(mouseX, mouseY, partialTicks);
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
}