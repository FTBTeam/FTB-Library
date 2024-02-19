package dev.ftb.mods.ftblibrary.ui.misc;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ImageConfig;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SelectImageScreen extends AbstractButtonListScreen {
	private final ImageConfig imageConfig;
	private final ConfigCallback callback;
	private final SimpleTextButton refreshButton;

	private static List<ImageDetails> cachedImages = null;

	public SelectImageScreen(ImageConfig imageConfig, ConfigCallback callback) {
		this.imageConfig = imageConfig;
		this.callback = callback;

		setTitle(Component.literal("Select Image"));
		setHasSearchBox(true);
		focus();
		setBorder(1, 1, 1);

		refreshButton = new SimpleTextButton(this, Component.translatable("ftblibrary.select_image.rescan"), Icons.REFRESH) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();
				clearCachedImages();
				refreshWidgets();
			}
		};
		refreshButton.setSize(20, 20);
	}

	@Override
	public boolean onInit() {
		return setSizeProportional(0.5f, 0.8f);
	}

	private List<ImageDetails> getImageList() {
		if (cachedImages == null) {
			List<ResourceLocation> images = new ArrayList<>();

			StringUtils.ignoreResourceLocationErrors = true;
			Map<ResourceLocation,Resource> textures = Collections.emptyMap();

			try {
				textures = Minecraft.getInstance().getResourceManager().listResources("textures", t -> t.getPath().endsWith(".png"));
			} catch (Exception ex) {
				FTBLibrary.LOGGER.error("A mod has broken resource preventing this list from loading: " + ex);
			}

			StringUtils.ignoreResourceLocationErrors = false;

			textures.keySet().forEach(rl -> {
				if (!ResourceLocation.isValidResourceLocation(rl.toString())) {
					FTBLibrary.LOGGER.warn("Image " + rl + " has invalid path! Report this to author of '" + rl.getNamespace() + "'!");
				} else if (isValidImage(rl)) {
					images.add(rl);
				}
			});

			cachedImages = images.stream().sorted().map(res -> {
				// shorten <mod>:textures/A/B.png to <mod>:A/B
				ResourceLocation res1 = new ResourceLocation(res.getNamespace(), res.getPath().substring(9, res.getPath().length() - 4));
				TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(res1);
				if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
					res1 = res;
				}
				return new ImageDetails(
						Component.literal(res1.getNamespace()).withStyle(ChatFormatting.GOLD).append(":")
								.append(Component.literal(res1.getPath()).withStyle(ChatFormatting.YELLOW)),
						Icon.getIcon(res1)
				);
			}).toList();
		}
		return cachedImages;
	}

	@Override
	public void alignWidgets() {
		super.alignWidgets();

		refreshButton.setPos(width + 2, 0);
	}

	@Override
	protected void doCancel() {
		callback.save(false);
	}

	@Override
	protected void doAccept() {
		callback.save(true);
	}

	@Override
	public void addWidgets() {
		super.addWidgets();

		add(refreshButton);
	}

	public static void clearCachedImages() {
		cachedImages = null;
	}

	public boolean allowNone() {
		return true;
	}

	public boolean isValidImage(ResourceLocation id) {
		return !id.getPath().startsWith("textures/font/");
	}

	@Override
	public void addButtons(Panel panel) {
		if (allowNone()) {
			panel.add(new SimpleTextButton(panel, Component.literal("None"), Icon.empty()) {
				@Override
				public void onClicked(MouseButton mouseButton) {
					playClickSound();
					boolean changed = imageConfig.setCurrentValue("");
					callback.save(changed);
				}
			});
		}

		for (var res : getImageList()) {
			panel.add(new SimpleTextButton(panel, res.label, res.icon) {
				@Override
				public void onClicked(MouseButton mouseButton) {
					playClickSound();
					boolean changed = imageConfig.setCurrentValue(res.icon.toString());
					callback.save(changed);
				}
			});
		}
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			callback.save(false);
			return true;
		}

		return false;
	}

	private record ImageDetails(Component label, Icon icon) {
	}

	public enum ResourceListener implements ResourceManagerReloadListener {
		INSTANCE;

		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
			SelectImageScreen.clearCachedImages();
		}
	}
}
