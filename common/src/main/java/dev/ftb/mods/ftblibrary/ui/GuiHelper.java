package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
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
			int x0 = Math.max(x, sx);
			int y0 = Math.max(y, sy);
			int x1 = Math.min(x + w, sx + sw);
			int y1 = Math.min(y + h, sy + sh);
			return new Scissor(x0, y0, x1 - x0, y1 - y0);
		}

		public void scissor(Window screen) {
			double scale = screen.getGuiScale();
			int sx = (int) (x * scale);
			int sy = (int) ((screen.getGuiScaledHeight() - (y + h)) * scale);
			int sw = (int) (w * scale);
			int sh = (int) (h * scale);
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
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(770, 771);
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		// Lighting.setupForFlatItems();
	}

	public static void playSound(SoundEvent event, float pitch) {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, pitch));
	}

	public static void drawTexturedRect(PoseStack matrixStack, int x, int y, int w, int h, Color4I col, float u0, float v0, float u1, float v1) {
		RenderSystem.enableTexture();
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		addRectToBufferWithUV(matrixStack, buffer, x, y, w, h, col, u0, v0, u1, v1);
		tesselator.end();
	}

	public static void addRectToBuffer(PoseStack matrixStack, BufferBuilder buffer, int x, int y, int w, int h, Color4I col) {
		if (w <= 0 || h <= 0) {
			return;
		}

		Matrix4f m = matrixStack.last().pose();
		int r = col.redi();
		int g = col.greeni();
		int b = col.bluei();
		int a = col.alphai();
		buffer.vertex(m, x, y + h, 0).color(r, g, b, a).endVertex();
		buffer.vertex(m, x + w, y + h, 0).color(r, g, b, a).endVertex();
		buffer.vertex(m, x + w, y, 0).color(r, g, b, a).endVertex();
		buffer.vertex(m, x, y, 0).color(r, g, b, a).endVertex();
	}

	public static void addRectToBufferWithUV(PoseStack matrixStack, BufferBuilder buffer, int x, int y, int w, int h, Color4I col, float u0, float v0, float u1, float v1) {
		if (w <= 0 || h <= 0) {
			return;
		}

		Matrix4f m = matrixStack.last().pose();
		int r = col.redi();
		int g = col.greeni();
		int b = col.bluei();
		int a = col.alphai();
		buffer.vertex(m, x, y + h, 0).color(r, g, b, a).uv(u0, v1).endVertex();
		buffer.vertex(m, x + w, y + h, 0).color(r, g, b, a).uv(u1, v1).endVertex();
		buffer.vertex(m, x + w, y, 0).color(r, g, b, a).uv(u1, v0).endVertex();
		buffer.vertex(m, x, y, 0).color(r, g, b, a).uv(u0, v0).endVertex();
	}

	public static void drawHollowRect(PoseStack matrixStack, int x, int y, int w, int h, Color4I col, boolean roundEdges) {
		if (w <= 1 || h <= 1 || col.isEmpty()) {
			col.draw(matrixStack, x, y, w, h);
			return;
		}

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.disableTexture();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		addRectToBuffer(matrixStack, buffer, x, y + 1, 1, h - 2, col);
		addRectToBuffer(matrixStack, buffer, x + w - 1, y + 1, 1, h - 2, col);

		if (roundEdges) {
			addRectToBuffer(matrixStack, buffer, x + 1, y, w - 2, 1, col);
			addRectToBuffer(matrixStack, buffer, x + 1, y + h - 1, w - 2, 1, col);
		} else {
			addRectToBuffer(matrixStack, buffer, x, y, w, 1, col);
			addRectToBuffer(matrixStack, buffer, x, y + h - 1, w, 1, col);
		}

		tesselator.end();
		RenderSystem.enableTexture();
	}

	public static void drawRectWithShade(PoseStack matrixStack, int x, int y, int w, int h, Color4I col, int intensity) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.disableTexture();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		addRectToBuffer(matrixStack, buffer, x, y, w - 1, 1, col);
		addRectToBuffer(matrixStack, buffer, x, y + 1, 1, h - 1, col);
		col = col.mutable().addBrightness(-intensity);
		addRectToBuffer(matrixStack, buffer, x + w - 1, y, 1, 1, col);
		addRectToBuffer(matrixStack, buffer, x, y + h - 1, 1, 1, col);
		col = col.mutable().addBrightness(-intensity);
		addRectToBuffer(matrixStack, buffer, x + w - 1, y + 1, 1, h - 2, col);
		addRectToBuffer(matrixStack, buffer, x + 1, y + h - 1, w - 1, 1, col);
		tesselator.end();
		RenderSystem.enableTexture();
	}

	@ExpectPlatform
	public static boolean shouldShowDurability(ItemStack stack) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static double getDamageLevel(ItemStack stack) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static int getDurabilityColor(ItemStack stack) {
		throw new AssertionError();
	}

	public static boolean drawItem(PoseStack matrixStack, ItemStack stack, double x, double y, float scaleX, float scaleY, boolean renderOverlay, @Nullable String text) {
		if (stack.isEmpty() || scaleX == 0D || scaleY == 0D) {
			return false;
		}

		Minecraft mc = Minecraft.getInstance();
		Tesselator tesselator = Tesselator.getInstance();
		ItemRenderer itemRenderer = mc.getItemRenderer();

		matrixStack.pushPose();
		matrixStack.translate(x, y, 0);
		matrixStack.scale(scaleX, scaleY, 1F);

		mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
		RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);

		GuiHelper.setupDrawing();
		matrixStack.translate(8, 8, itemRenderer.blitOffset);
		matrixStack.scale(1, -1, 1);
		matrixStack.scale(16, 16, 16);
		MultiBufferSource.BufferSource renderTypeBufferImpl = mc.renderBuffers().bufferSource();

		BakedModel bakedModel = itemRenderer.getModel(stack, mc.level, mc.player, 0);

		boolean flatLight = !bakedModel.usesBlockLight();

		if (flatLight) {
			Lighting.setupForFlatItems();
		}

		itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, matrixStack, renderTypeBufferImpl, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
		renderTypeBufferImpl.endBatch();
		RenderSystem.enableDepthTest();

		if (flatLight) {
			Lighting.setupFor3DItems();
		}

		if (renderOverlay) {
			Font fr = mc.font;

			if (stack.getCount() != 1 || text != null) {
				String s = text == null ? String.valueOf(stack.getCount()) : text;
				matrixStack.translate(0, 0, itemRenderer.blitOffset + 20);
				fr.drawInBatch(s, (float) (19 - 2 - fr.width(s)), (float) (6 + 3), 16777215, true, matrixStack.last().pose(), renderTypeBufferImpl, false, 0, 15728880);
				renderTypeBufferImpl.endBatch();
			}

			// TODO: add extension point to arch
			if (shouldShowDurability(stack)) {
				RenderSystem.disableDepthTest();
				RenderSystem.disableTexture();
				RenderSystem.disableBlend();
				double health = getDamageLevel(stack);
				int i = Math.round(13.0F - (float) health * 13.0F);
				int j = getDurabilityColor(stack);
				draw(matrixStack, tesselator, 2, 13, 13, 2, 0, 0, 0, 255);
				draw(matrixStack, tesselator, 2, 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
				RenderSystem.enableBlend();
				RenderSystem.enableTexture();
				RenderSystem.enableDepthTest();
			}

			float f3 = mc.player == null ? 0.0F : mc.player.getCooldowns().getCooldownPercent(stack.getItem(), mc.getFrameTime());

			if (f3 > 0.0F) {
				RenderSystem.disableDepthTest();
				RenderSystem.disableTexture();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				draw(matrixStack, tesselator, 0, Mth.floor(16.0F * (1.0F - f3)), 16, Mth.ceil(16.0F * f3), 255, 255, 255, 127);
				RenderSystem.enableTexture();
				RenderSystem.enableDepthTest();
			}
		}

		matrixStack.popPose();
		return true;
	}

	private static void draw(PoseStack matrixStack, Tesselator tesselator, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Matrix4f m = matrixStack.last().pose();
		BufferBuilder renderer = tesselator.getBuilder();
		renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		renderer.vertex(m, x, y, 0).color(red, green, blue, alpha).endVertex();
		renderer.vertex(m, x, y + height, 0).color(red, green, blue, alpha).endVertex();
		renderer.vertex(m, x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
		renderer.vertex(m, x + width, y, 0).color(red, green, blue, alpha).endVertex();
		tesselator.end();
	}

	public static void pushScissor(Window screen, int x, int y, int w, int h) {
		if (SCISSOR.isEmpty()) {
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
		}

		Scissor scissor = SCISSOR.isEmpty() ? new Scissor(x, y, w, h) : SCISSOR.lastElement().crop(x, y, w, h);
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
		List<Component> tooltip = stack.getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
		list.add(prefix == null ? tooltip.get(0).copy().withStyle(stack.getRarity().color) : prefix.copy().append(tooltip.get(0)));

		for (int i = 1; i < tooltip.size(); i++) {
			list.add(new TextComponent("").withStyle(ChatFormatting.GRAY).append(tooltip.get(i)));
		}
	}
}