package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Stack;

/**
 * @author LatvianModder
 */
public class GuiHelper {
	private static class Scissor {
		private final int x, y, w, h;

		private Scissor(int _x, int _y, int _w, int _h) {
			x = _x;
			y = _y;
			w = Math.max(0, _w);
			h = Math.max(0, _h);
		}

		public Scissor crop(int sx, int sy, int sw, int sh) {
			var x0 = Math.max(x, sx);
			var y0 = Math.max(y, sy);
			var x1 = Math.min(x + w, sx + sw);
			var y1 = Math.min(y + h, sy + sh);
			return new Scissor(x0, y0, x1 - x0, y1 - y0);
		}

		public void scissor(Window screen) {
			var scale = screen.getGuiScale();
			var sx = (int) (x * scale);
			var sy = (int) ((screen.getGuiScaledHeight() - (y + h)) * scale);
			var sw = (int) (w * scale);
			var sh = (int) (h * scale);
			GL11.glScissor(sx, sy, sw, sh);
		}
	}

	private static final Stack<Scissor> SCISSOR = new Stack<>();

	public static final BaseScreen BLANK_GUI = new BaseScreen() {
		@Override
		public void addWidgets() {
		}

		@Override
		public void alignWidgets() {
		}
	};

	public static void setupDrawing() {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(770, 771);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.enableDepthTest();
		// Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
	}

	public static void playSound(SoundEvent event, float pitch) {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, pitch));
	}

	public static void drawTexturedRect(GuiGraphics graphics, int x, int y, int w, int h, Color4I col, float u0, float v0, float u1, float v1) {
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		var buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		addRectToBufferWithUV(graphics, buffer, x, y, w, h, col, u0, v0, u1, v1);
		BufferUploader.drawWithShader(buffer.end());
	}

	public static void addRectToBuffer(GuiGraphics graphics, BufferBuilder buffer, int x, int y, int w, int h, Color4I col) {
		if (w <= 0 || h <= 0) {
			return;
		}

		var m = graphics.pose().last().pose();
		var r = col.redi();
		var g = col.greeni();
		var b = col.bluei();
		var a = col.alphai();
		buffer.vertex(m, x, y + h, 0).color(r, g, b, a).endVertex();
		buffer.vertex(m, x + w, y + h, 0).color(r, g, b, a).endVertex();
		buffer.vertex(m, x + w, y, 0).color(r, g, b, a).endVertex();
		buffer.vertex(m, x, y, 0).color(r, g, b, a).endVertex();
	}

	public static void addRectToBufferWithUV(GuiGraphics graphics, BufferBuilder buffer, int x, int y, int w, int h, Color4I col, float u0, float v0, float u1, float v1) {
		if (w <= 0 || h <= 0) {
			return;
		}

		var m = graphics.pose().last().pose();
		var r = col.redi();
		var g = col.greeni();
		var b = col.bluei();
		var a = col.alphai();
		buffer.vertex(m, x, y + h, 0).color(r, g, b, a).uv(u0, v1).endVertex();
		buffer.vertex(m, x + w, y + h, 0).color(r, g, b, a).uv(u1, v1).endVertex();
		buffer.vertex(m, x + w, y, 0).color(r, g, b, a).uv(u1, v0).endVertex();
		buffer.vertex(m, x, y, 0).color(r, g, b, a).uv(u0, v0).endVertex();
	}

	public static void drawHollowRect(GuiGraphics graphics, int x, int y, int w, int h, Color4I col, boolean roundEdges) {
		if (w <= 1 || h <= 1 || col.isEmpty()) {
			col.draw(graphics, x, y, w, h);
			return;
		}

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		var tesselator = Tesselator.getInstance();
		var buffer = tesselator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		addRectToBuffer(graphics, buffer, x, y + 1, 1, h - 2, col);
		addRectToBuffer(graphics, buffer, x + w - 1, y + 1, 1, h - 2, col);

		if (roundEdges) {
			addRectToBuffer(graphics, buffer, x + 1, y, w - 2, 1, col);
			addRectToBuffer(graphics, buffer, x + 1, y + h - 1, w - 2, 1, col);
		} else {
			addRectToBuffer(graphics, buffer, x, y, w, 1, col);
			addRectToBuffer(graphics, buffer, x, y + h - 1, w, 1, col);
		}

		tesselator.end();
	}

	public static void drawRectWithShade(GuiGraphics graphics, int x, int y, int w, int h, Color4I col, int intensity) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		var tesselator = Tesselator.getInstance();
		var buffer = tesselator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		addRectToBuffer(graphics, buffer, x, y, w - 1, 1, col);
		addRectToBuffer(graphics, buffer, x, y + 1, 1, h - 1, col);
		col = col.mutable().addBrightness(-intensity);
		addRectToBuffer(graphics, buffer, x + w - 1, y, 1, 1, col);
		addRectToBuffer(graphics, buffer, x, y + h - 1, 1, 1, col);
		col = col.mutable().addBrightness(-intensity);
		addRectToBuffer(graphics, buffer, x + w - 1, y + 1, 1, h - 2, col);
		addRectToBuffer(graphics, buffer, x + 1, y + h - 1, w - 1, 1, col);
		tesselator.end();
	}

	public static void drawItem(GuiGraphics graphics, ItemStack stack, int hash, boolean renderOverlay, @Nullable String text) {
		if (stack.isEmpty()) {
			return;
		}

		var mc = Minecraft.getInstance();
		var itemRenderer = mc.getItemRenderer();
		var bakedModel = itemRenderer.getModel(stack, null, mc.player, hash);

		Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		PoseStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushPose();
		modelViewStack.mulPoseMatrix(graphics.pose().last().pose());
		// modelViewStack.translate(x, y, 100.0D + this.blitOffset);
		modelViewStack.scale(1F, -1F, 1F);
		modelViewStack.scale(16F, 16F, 16F);
		RenderSystem.applyModelViewMatrix();
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		var flatLight = !bakedModel.usesBlockLight();

		if (flatLight) {
			Lighting.setupForFlatItems();
		}

		itemRenderer.render(stack, ItemDisplayContext.GUI, false, new PoseStack(), bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, bakedModel);
		bufferSource.endBatch();
		RenderSystem.enableDepthTest();

		if (flatLight) {
			Lighting.setupFor3DItems();
		}

		modelViewStack.popPose();
		RenderSystem.applyModelViewMatrix();

		if (renderOverlay) {
			var t = Tesselator.getInstance();
			var font = mc.font;

			if (stack.getCount() != 1 || text != null) {
				var s = text == null ? String.valueOf(stack.getCount()) : text;
				graphics.pose().pushPose();
				graphics.pose().translate(9D - font.width(s), 1D, 20D);
				font.drawInBatch(s, 0F, 0F, 0xFFFFFF, true, graphics.pose().last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
				bufferSource.endBatch();
				graphics.pose().popPose();
			}

			if (stack.isBarVisible()) {
				RenderSystem.disableDepthTest();
				RenderSystem.disableBlend();
				var barWidth = stack.getBarWidth();
				var barColor = stack.getBarColor();
				draw(graphics, t, -6, 5, 13, 2, 0, 0, 0, 255);
				draw(graphics, t, -6, 5, barWidth, 1, barColor >> 16 & 255, barColor >> 8 & 255, barColor & 255, 255);
				RenderSystem.enableBlend();
				RenderSystem.enableDepthTest();
			}

			var cooldown = mc.player == null ? 0F : mc.player.getCooldowns().getCooldownPercent(stack.getItem(), mc.getFrameTime());

			if (cooldown > 0F) {
				RenderSystem.disableDepthTest();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				draw(graphics, t, -8, Mth.floor(16F * (1F - cooldown)) - 8, 16, Mth.ceil(16F * cooldown), 255, 255, 255, 127);
				RenderSystem.enableDepthTest();
			}
		}
	}

	private static void draw(GuiGraphics graphics, Tesselator t, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
		if (width <= 0 || height <= 0) {
			return;
		}

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		var m = graphics.pose().last().pose();
		var renderer = t.getBuilder();
		renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		renderer.vertex(m, x, y, 0).color(red, green, blue, alpha).endVertex();
		renderer.vertex(m, x, y + height, 0).color(red, green, blue, alpha).endVertex();
		renderer.vertex(m, x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
		renderer.vertex(m, x + width, y, 0).color(red, green, blue, alpha).endVertex();
		t.end();
	}

	public static void pushScissor(Window screen, int x, int y, int w, int h) {
		if (SCISSOR.isEmpty()) {
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
		}

		var scissor = SCISSOR.isEmpty() ? new Scissor(x, y, w, h) : SCISSOR.lastElement().crop(x, y, w, h);
		SCISSOR.push(scissor);
		scissor.scissor(screen);
	}

	public static void popScissor(Window screen) {
		SCISSOR.pop();

		if (SCISSOR.isEmpty()) {
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		} else {
			SCISSOR.lastElement().scissor(screen);
		}
	}

	public static String clickEventToString(@Nullable ClickEvent event) {
		if (event == null) {
			return "";
		}

		return switch (event.getAction()) {
			case OPEN_URL, CHANGE_PAGE -> event.getValue();
			case OPEN_FILE -> "file:" + event.getValue();
			case RUN_COMMAND -> "command:" + event.getValue();
			case SUGGEST_COMMAND -> "suggest_command:" + event.getValue();
			default -> "";
		};
	}

	public static void addStackTooltip(ItemStack stack, List<Component> list) {
		addStackTooltip(stack, list, null);
	}

	public static void addStackTooltip(ItemStack stack, List<Component> list, @Nullable Component prefix) {
		var tooltip = stack.getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
		list.add(prefix == null ? tooltip.get(0).copy().withStyle(stack.getRarity().color) : prefix.copy().append(tooltip.get(0)));

		for (var i = 1; i < tooltip.size(); i++) {
			list.add(Component.literal("").withStyle(ChatFormatting.GRAY).append(tooltip.get(i)));
		}
	}

	public static void drawBorderedPanel(GuiGraphics graphics, int x, int y, int w, int h, Color4I color, boolean outset) {
		w--; h--;

		Color4I hi = color.addBrightness(outset ? 0.15f : -0.1f);
		Color4I lo = color.addBrightness(outset ? -0.1f : 0.15f);

		graphics.fill(x, y, x + w, y + h, color.rgba());
		graphics.hLine(x, x + w - 1, y, hi.rgba());
		graphics.vLine(x, y, y + h, hi.rgba());
		graphics.hLine(x + 1, x + w, y + h, lo.rgba());
		graphics.vLine(x + w, y, y + h, lo.rgba());
	}
}
