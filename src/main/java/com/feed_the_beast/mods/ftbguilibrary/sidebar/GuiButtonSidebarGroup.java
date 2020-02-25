package com.feed_the_beast.mods.ftbguilibrary.sidebar;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiButtonSidebarGroup extends AbstractButton
{
	public static Rectangle lastDrawnArea = new Rectangle();

	private final ContainerScreen gui;
	public final List<GuiButtonSidebar> buttons;
	private GuiButtonSidebar mouseOver;

	public GuiButtonSidebarGroup(ContainerScreen g)
	{
		super(0, 0, 0, 0, "");
		gui = g;
		buttons = new ArrayList<>();
	}

	@Override
	public void render(int mx, int my, float partialTicks)
	{
		buttons.clear();
		mouseOver = null;
		int rx, ry = 0;
		boolean addedAny;

		for (SidebarButtonGroup group : SidebarButtonManager.INSTANCE.groups)
		{
			rx = 0;
			addedAny = false;

			for (SidebarButton button : group.getButtons())
			{
				if (button.isActuallyVisible())
				{
					buttons.add(new GuiButtonSidebar(rx, ry, button));
					rx++;
					addedAny = true;
				}
			}

			if (addedAny)
			{
				ry++;
			}
		}

		for (GuiButtonSidebar button : buttons)
		{
			button.x = 1 + button.buttonX * 17;
			button.y = 1 + button.buttonY * 17;
		}

		x = Integer.MAX_VALUE;
		y = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;

		for (GuiButtonSidebar b : buttons)
		{
			if (b.x >= 0 && b.y >= 0)
			{
				x = Math.min(x, b.x);
				y = Math.min(y, b.y);
				maxX = Math.max(maxX, b.x + 16);
				maxY = Math.max(maxY, b.y + 16);
			}

			if (mx >= b.x && my >= b.y && mx < b.x + 16 && my < b.y + 16)
			{
				mouseOver = b;
			}
		}

		x -= 2;
		y -= 2;
		maxX += 2;
		maxY += 2;

		width = maxX - x;
		height = maxY - y;
		//zLevel = 0F;

		MatrixStack matrixStack = new MatrixStack();
		matrixStack.translate(0, 0, 500);

		RenderSystem.translatef(0, 0, 500);

		FontRenderer font = gui.getMinecraft().fontRenderer;

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1F, 1F, 1F, 1F);

		for (GuiButtonSidebar b : buttons)
		{
			b.button.getIcon().draw(b.x, b.y, 16, 16);

			if (b == mouseOver)
			{
				Color4I.WHITE.withAlpha(33).draw(b.x, b.y, 16, 16);
			}

			if (b.button.getCustomTextHandler() != null)
			{
				String text = b.button.getCustomTextHandler().get();

				if (!text.isEmpty())
				{
					int nw = font.getStringWidth(text);
					int width = 16;
					Color4I.LIGHT_RED.draw(b.x + width - nw, b.y - 1, nw + 1, 9);
					font.drawString(text, b.x + width - nw + 1, b.y, 0xFFFFFFFF);
					RenderSystem.color4f(1F, 1F, 1F, 1F);
				}
			}
		}

		if (mouseOver != null)
		{
			int mx1 = mx + 10;
			int my1 = Math.max(3, my - 9);

			List<String> list = new ArrayList<>();
			list.add(I18n.format(mouseOver.button.getLangKey()));

			if (mouseOver.button.getTooltipHandler() != null)
			{
				mouseOver.button.getTooltipHandler().accept(list);
			}

			int tw = 0;

			for (String s : list)
			{
				tw = Math.max(tw, font.getStringWidth(s));
			}

			matrixStack.translate(0, 0, 500);

			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			Color4I.DARK_GRAY.draw(mx1 - 3, my1 - 2, tw + 6, 2 + list.size() * 10);

			for (int i = 0; i < list.size(); i++)
			{
				font.drawString(list.get(i), mx1, my1 + i * 10, 0xFFFFFFFF);
			}

			RenderSystem.color4f(1F, 1F, 1F, 1F);
		}

		RenderSystem.color4f(1F, 1F, 1F, 1F);
		//zLevel = 0F;

		lastDrawnArea = new Rectangle(x, y, width, height);
	}

	@Override
	public void onPress()
	{
		if (mouseOver != null)
		{
			mouseOver.button.onClicked(Screen.hasShiftDown());
		}
	}
}