package com.feed_the_beast.mods.ftbguilibrary.widget;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Stack;

/**
 * @author LatvianModder
 */
public class GuiHelper
{
	private static class Scissor
	{
		private final int x, y, w, h;

		private Scissor(int _x, int _y, int _w, int _h)
		{
			x = _x;
			y = _y;
			w = Math.max(0, _w);
			h = Math.max(0, _h);
		}

		public Scissor crop(int sx, int sy, int sw, int sh)
		{
			int x0 = Math.max(x, sx);
			int y0 = Math.max(y, sy);
			int x1 = Math.min(x + w, sx + sw);
			int y1 = Math.min(y + h, sy + sh);
			return new Scissor(x0, y0, x1 - x0, y1 - y0);
		}

		public void scissor(MainWindow screen)
		{
			double scale = screen.getGuiScaleFactor();
			int sx = (int) (x * scale);
			int sy = (int) ((screen.getScaledHeight() - (y + h)) * scale);
			int sw = (int) (w * scale);
			int sh = (int) (h * scale);
			GL11.glScissor(sx, sy, sw, sh);
		}
	}

	private static final Stack<Scissor> SCISSOR = new Stack<>();

	public static final GuiBase BLANK_GUI = new GuiBase()
	{
		@Override
		public void addWidgets()
		{
		}

		@Override
		public void alignWidgets()
		{
		}
	};

	public static void setupDrawing()
	{
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		RenderSystem.disableLighting();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
	}

	public static void playSound(SoundEvent event, float pitch)
	{
		Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(event, pitch));
	}

	public static void drawTexturedRect(MatrixStack matrixStack, int x, int y, int w, int h, Color4I col, float u0, float v0, float u1, float v1)
	{
		if (u0 == u1 || v0 == v1)
		{
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			addRectToBuffer(matrixStack, buffer, x, y, w, h, col);
			tessellator.draw();
		}
		else
		{
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
			addRectToBufferWithUV(matrixStack, buffer, x, y, w, h, col, u0, v0, u1, v1);
			tessellator.draw();
		}
	}

	public static void addRectToBuffer(MatrixStack matrixStack, BufferBuilder buffer, int x, int y, int w, int h, Color4I col)
	{
		if (w <= 0 || h <= 0)
		{
			return;
		}

		Matrix4f m = matrixStack.getLast().getMatrix();
		int r = col.redi();
		int g = col.greeni();
		int b = col.bluei();
		int a = col.alphai();
		buffer.pos(m, x, y + h, 0).color(r, g, b, a).endVertex();
		buffer.pos(m, x + w, y + h, 0).color(r, g, b, a).endVertex();
		buffer.pos(m, x + w, y, 0).color(r, g, b, a).endVertex();
		buffer.pos(m, x, y, 0).color(r, g, b, a).endVertex();
	}

	public static void addRectToBufferWithUV(MatrixStack matrixStack, BufferBuilder buffer, int x, int y, int w, int h, Color4I col, float u0, float v0, float u1, float v1)
	{
		if (w <= 0 || h <= 0)
		{
			return;
		}

		Matrix4f m = matrixStack.getLast().getMatrix();
		int r = col.redi();
		int g = col.greeni();
		int b = col.bluei();
		int a = col.alphai();
		buffer.pos(m, x, y + h, 0).color(r, g, b, a).tex(u0, v1).endVertex();
		buffer.pos(m, x + w, y + h, 0).color(r, g, b, a).tex(u1, v1).endVertex();
		buffer.pos(m, x + w, y, 0).color(r, g, b, a).tex(u1, v0).endVertex();
		buffer.pos(m, x, y, 0).color(r, g, b, a).tex(u0, v0).endVertex();
	}

	public static void drawHollowRect(MatrixStack matrixStack, int x, int y, int w, int h, Color4I col, boolean roundEdges)
	{
		if (w <= 1 || h <= 1 || col.isEmpty())
		{
			col.draw(matrixStack, x, y, w, h);
			return;
		}

		RenderSystem.disableTexture();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		addRectToBuffer(matrixStack, buffer, x, y + 1, 1, h - 2, col);
		addRectToBuffer(matrixStack, buffer, x + w - 1, y + 1, 1, h - 2, col);

		if (roundEdges)
		{
			addRectToBuffer(matrixStack, buffer, x + 1, y, w - 2, 1, col);
			addRectToBuffer(matrixStack, buffer, x + 1, y + h - 1, w - 2, 1, col);
		}
		else
		{
			addRectToBuffer(matrixStack, buffer, x, y, w, 1, col);
			addRectToBuffer(matrixStack, buffer, x, y + h - 1, w, 1, col);
		}

		tessellator.draw();
		RenderSystem.enableTexture();
	}

	public static void drawRectWithShade(MatrixStack matrixStack, int x, int y, int w, int h, Color4I col, int intensity)
	{
		RenderSystem.disableTexture();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		addRectToBuffer(matrixStack, buffer, x, y, w - 1, 1, col);
		addRectToBuffer(matrixStack, buffer, x, y + 1, 1, h - 1, col);
		col = col.mutable().addBrightness(-intensity);
		addRectToBuffer(matrixStack, buffer, x + w - 1, y, 1, 1, col);
		addRectToBuffer(matrixStack, buffer, x, y + h - 1, 1, 1, col);
		col = col.mutable().addBrightness(-intensity);
		addRectToBuffer(matrixStack, buffer, x + w - 1, y + 1, 1, h - 2, col);
		addRectToBuffer(matrixStack, buffer, x + 1, y + h - 1, w - 1, 1, col);
		tessellator.draw();
		RenderSystem.enableTexture();
	}

	public static boolean drawItem(MatrixStack matrixStack, ItemStack stack, double x, double y, float scaleX, float scaleY, boolean renderOverlay, @Nullable String text)
	{
		if (stack.isEmpty() || scaleX == 0D || scaleY == 0D)
		{
			return false;
		}

		Minecraft mc = Minecraft.getInstance();
		Tessellator tessellator = Tessellator.getInstance();
		ItemRenderer itemRenderer = mc.getItemRenderer();

		matrixStack.push();
		matrixStack.translate(x, y, 0);
		matrixStack.scale(scaleX, scaleY, 1F);

		mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		mc.getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmapDirect(false, false);
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		matrixStack.translate(8, 8, itemRenderer.zLevel);
		matrixStack.scale(1, -1, 1);
		matrixStack.scale(16, 16, 16);
		IRenderTypeBuffer.Impl renderTypeBufferImpl = mc.getRenderTypeBuffers().getBufferSource();

		IBakedModel bakedModel = itemRenderer.getItemModelWithOverrides(stack, mc.world, mc.player);

		boolean flatLight = !bakedModel.isSideLit();

		if (flatLight)
		{
			RenderHelper.setupGuiFlatDiffuseLighting();
		}

		itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.GUI, false, matrixStack, renderTypeBufferImpl, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
		renderTypeBufferImpl.finish();
		RenderSystem.enableDepthTest();

		if (flatLight)
		{
			RenderHelper.setupGui3DDiffuseLighting();
		}

		RenderSystem.disableAlphaTest();
		RenderSystem.disableRescaleNormal();

		if (renderOverlay)
		{
			FontRenderer fr = stack.getItem().getFontRenderer(stack);

			if (fr == null)
			{
				fr = mc.fontRenderer;
			}

			if (stack.getCount() != 1 || text != null)
			{
				String s = text == null ? String.valueOf(stack.getCount()) : text;
				matrixStack.translate(0, 0, itemRenderer.zLevel + 20);
				fr.renderString(s, (float) (19 - 2 - fr.getStringWidth(s)), (float) (6 + 3), 16777215, true, matrixStack.getLast().getMatrix(), renderTypeBufferImpl, false, 0, 15728880);
				renderTypeBufferImpl.finish();
			}

			if (stack.getItem().showDurabilityBar(stack))
			{
				RenderSystem.disableDepthTest();
				RenderSystem.disableTexture();
				RenderSystem.disableAlphaTest();
				RenderSystem.disableBlend();
				double health = stack.getItem().getDurabilityForDisplay(stack);
				int i = Math.round(13.0F - (float) health * 13.0F);
				int j = stack.getItem().getRGBDurabilityForDisplay(stack);
				draw(matrixStack, tessellator, 2, 13, 13, 2, 0, 0, 0, 255);
				draw(matrixStack, tessellator, 2, 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
				RenderSystem.enableBlend();
				RenderSystem.enableAlphaTest();
				RenderSystem.enableTexture();
				RenderSystem.enableDepthTest();
			}

			float f3 = mc.player == null ? 0.0F : mc.player.getCooldownTracker().getCooldown(stack.getItem(), mc.getRenderPartialTicks());

			if (f3 > 0.0F)
			{
				RenderSystem.disableDepthTest();
				RenderSystem.disableTexture();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				draw(matrixStack, tessellator, 0, MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
				RenderSystem.enableTexture();
				RenderSystem.enableDepthTest();
			}
		}

		matrixStack.pop();
		return true;
	}

	private static void draw(MatrixStack matrixStack, Tessellator tessellator, int x, int y, int width, int height, int red, int green, int blue, int alpha)
	{
		Matrix4f m = matrixStack.getLast().getMatrix();
		BufferBuilder renderer = tessellator.getBuffer();
		renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		renderer.pos(m, x, y, 0).color(red, green, blue, alpha).endVertex();
		renderer.pos(m, x, y + height, 0).color(red, green, blue, alpha).endVertex();
		renderer.pos(m, x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
		renderer.pos(m, x + width, y, 0).color(red, green, blue, alpha).endVertex();
		tessellator.draw();
	}

	public static void pushScissor(MainWindow screen, int x, int y, int w, int h)
	{
		if (SCISSOR.isEmpty())
		{
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
		}

		Scissor scissor = SCISSOR.isEmpty() ? new Scissor(x, y, w, h) : SCISSOR.lastElement().crop(x, y, w, h);
		SCISSOR.push(scissor);
		scissor.scissor(screen);
	}

	public static void popScissor(MainWindow screen)
	{
		SCISSOR.pop();

		if (SCISSOR.isEmpty())
		{
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}
		else
		{
			SCISSOR.lastElement().scissor(screen);
		}
	}

	public static String clickEventToString(@Nullable ClickEvent event)
	{
		if (event == null)
		{
			return "";
		}

		switch (event.getAction())
		{
			case OPEN_URL:
			case CHANGE_PAGE:
				return event.getValue();
			case OPEN_FILE:
				return "file:" + event.getValue();
			case RUN_COMMAND:
				return "command:" + event.getValue();
			case SUGGEST_COMMAND:
				return "suggest_command:" + event.getValue();
			default:
				return "";
		}
	}

	public static void addStackTooltip(ItemStack stack, List<ITextComponent> list)
	{
		addStackTooltip(stack, list, null);
	}

	public static void addStackTooltip(ItemStack stack, List<ITextComponent> list, @Nullable ITextComponent prefix)
	{
		List<ITextComponent> tooltip = stack.getTooltip(Minecraft.getInstance().player, Minecraft.getInstance().gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
		list.add(prefix == null ? tooltip.get(0).deepCopy().mergeStyle(stack.getRarity().color) : prefix.deepCopy().append(tooltip.get(0)));

		for (int i = 1; i < tooltip.size(); i++)
		{
			list.add(new StringTextComponent("").mergeStyle(TextFormatting.GRAY).append(tooltip.get(i)));
		}
	}
}