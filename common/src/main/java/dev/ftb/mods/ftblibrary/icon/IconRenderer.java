package dev.ftb.mods.ftblibrary.icon;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import me.shedaniel.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;

/**
 * This class is cursed
 *
 * @author LatvianModder
 * @author elytra
 * @see <a href='https://github.com/elytra/BlockRenderer'>elytra/BlockRenderer</a>
 */
public final class IconRenderer<T> {
	private static IconRenderer<PixelBuffer> defaultIconRenderer;

	public static <E> IconRenderer<E> create(Function<PixelBuffer, E> factory) {
		IconRenderer<E> iconRenderer = new IconRenderer<>(factory);
		FTBLibraryClient.ICON_RENDERERS.add(iconRenderer);
		return iconRenderer;
	}

	public static IconRenderer<PixelBuffer> createDefault() {
		if (defaultIconRenderer == null) {
			defaultIconRenderer = create(pixelBuffer -> pixelBuffer);
		}

		return defaultIconRenderer;
	}

	private static class IconCallbackPair<E> implements Runnable {
		private IconRenderer<E> iconRenderer;
		private Icon icon;
		private ImageCallback<E> callback;

		@Override
		public void run() {
			iconRenderer.queue.add(this);
		}
	}

	private final Function<PixelBuffer, T> factory;
	private final Collection<IconCallbackPair<T>> queue;
	private T nullImage;
	private Map<Icon, T> imageCache;

	private IconRenderer(Function<PixelBuffer, T> f) {
		factory = f;
		queue = new LinkedList<>();
		nullImage = null;
		imageCache = new HashMap<>();
	}

	public void clearCache() {
		imageCache = new HashMap<>();
	}

	public T getNullImage() {
		if (nullImage == null) {
			ResourceLocation rl;

			if (Platform.isModLoaded("ftbquests")) {
				rl = new ResourceLocation("ftbquests:textures/item/missing_item.png");
			} else {
				rl = new ResourceLocation(FTBLibrary.MOD_ID, "textures/icons/cancel.png");
			}

			try (InputStream stream = Minecraft.getInstance().getResourceManager().getResource(rl).getInputStream()) {
				nullImage = factory.apply(PixelBuffer.from(stream));
			} catch (Exception ex) {
			}
		}

		return nullImage;
	}

	public boolean load(@Nullable Icon icon, ImageCallback<T> callback) {
		if (icon == null) {
			callback.imageLoaded(false, null);
			return true;
		} else if (icon.isEmpty()) {
			callback.imageLoaded(false, getNullImage());
			return true;
		}

		T image = imageCache.get(icon);

		if (image != null) {
			callback.imageLoaded(false, image);
			return true;
		}

		if (icon.hasPixelBuffer()) {
			PixelBuffer buffer = icon.createPixelBuffer();

			if (buffer == null) {
				image = getNullImage();
			} else {
				image = factory.apply(buffer);
				// JavaFX
				// image = new WritableImage(w, h);
				// ((WritableImage) image).getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), buffer.getPixels(), 0, w);
			}

			imageCache.put(icon, image);
			callback.imageLoaded(false, image);
			return true;
		}

		imageCache.put(icon, getNullImage());
		callback.imageLoaded(false, getNullImage());

		IconCallbackPair<T> pair = new IconCallbackPair<>();
		pair.iconRenderer = this;
		pair.icon = icon;
		pair.callback = callback;
		Minecraft.getInstance().execute(pair);

		return false;
	}

	/**
	 * Modified version of BlockRenderer mod code
	 */
	@SuppressWarnings("deprecation")
	public void render() {
		if (queue.isEmpty()) {
			return;
		}

		Object[] queued = queue.toArray();
		queue.clear();

		Minecraft mc = Minecraft.getInstance();
		com.mojang.blaze3d.platform.Window res = mc.getWindow();
		int size = Math.min(Math.min(res.getWidth(), res.getHeight()), 64);

		//FIXME: Translate and scale the matrix?
		PoseStack matrixStack = new PoseStack();
		//FIXME: Check if its needed, used to be mc.entityRenderer.setupOverlayRendering();
		//res.loadGUIRenderMatrix(Minecraft.IS_RUNNING_ON_MAC);
		RenderSystem.pushMatrix();
		RenderSystem.enableLighting();
		double scale = size / (16D * res.getGuiScale());
		RenderSystem.translated(0, 0, -(scale * 100D));
		RenderSystem.scaled(scale, scale, scale);

		float oldZLevel = mc.getItemRenderer().blitOffset;
		mc.getItemRenderer().blitOffset = -50;

		RenderSystem.enableRescaleNormal();
		RenderSystem.enableColorMaterial();
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		RenderSystem.disableAlphaTest();

		PixelBuffer buffer = new PixelBuffer(size, size);
		AffineTransform at = new AffineTransform();
		at.concatenate(AffineTransform.getScaleInstance(1, -1));
		at.concatenate(AffineTransform.getTranslateInstance(0, -size));
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

		for (Object pair0 : queued) {
			IconCallbackPair<T> pair = (IconCallbackPair<T>) pair0;
			RenderSystem.pushMatrix();
			RenderSystem.clearColor(0F, 0F, 0F, 0F);
			RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);
			pair.icon.drawStatic(matrixStack, 0, 0, 16, 16);
			RenderSystem.popMatrix();

			try {
				ByteBuffer buf = BufferUtils.createByteBuffer(size * size * 4);
				GL11.glReadBuffer(GL11.GL_BACK);
				GL11.glGetError();
				//RenderSystem.getError(); //FIXME: For some reason it throws error here, but it still works. Calling this to not spam console
				GL11.glReadPixels(0, res.getHeight() - size, size, size, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buf);
				buf.asIntBuffer().get(buffer.getPixels());
				img.setRGB(0, 0, size, size, buffer.getPixels(), 0, size);
				BufferedImage flipped = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = flipped.createGraphics(); // TODO: Figure out a better way to flip pixels, most likely directly with array
				g.transform(at);
				g.drawImage(img, 0, 0, null);
				g.dispose();
				flipped.getRGB(0, 0, size, size, buffer.getPixels(), 0, size);
				T image = factory.apply(buffer);
				imageCache.put(pair.icon, image);
				pair.callback.imageLoaded(true, image);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		RenderSystem.disableLighting();
		RenderSystem.disableColorMaterial();
		RenderSystem.disableDepthTest();
		RenderSystem.disableBlend();
		RenderSystem.popMatrix();
		mc.getItemRenderer().blitOffset = oldZLevel;
	}
}