package dev.ftb.mods.ftblibrary.ui.misc;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ImageConfig;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class SelectImageScreen extends ButtonListBaseScreen {
	private final ImageConfig imageConfig;
	private final ConfigCallback callback;
	private final List<ResourceLocation> images;

	public SelectImageScreen(ImageConfig i, ConfigCallback c) {
		imageConfig = i;
		callback = c;
		setTitle(Component.literal("Select Image"));
		setHasSearchBox(true);
		focus();
		setBorder(1, 1, 1);

		images = new ArrayList<>();

		StringUtils.ignoreResourceLocationErrors = true;
		Map<ResourceLocation, Resource> textures = Collections.emptyMap();

		try {
			textures = Minecraft.getInstance().getResourceManager().listResources("textures", t -> t.getPath().endsWith(".png"));
		} catch (Exception ex) {
			FTBLibrary.LOGGER.error("A mod has broken resource preventing this list from loading: " + ex);
		}

		StringUtils.ignoreResourceLocationErrors = false;

		for (var rl : textures.entrySet()) {
			if (!ResourceLocation.isValidResourceLocation(rl.getKey().toString())) {
				FTBLibrary.LOGGER.warn("Image " + rl.getKey() + " has invalid path! Report this to author of '" + rl.getKey().getNamespace() + "'!");
			} else if (isValidImage(rl.getKey())) {
				images.add(rl.getKey());
			}
		}

		images.sort(null);
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
			panel.add(new SimpleTextButton(panel, Component.literal("None"), Icon.EMPTY) {
				@Override
				public void onClicked(MouseButton mouseButton) {
					playClickSound();
					imageConfig.setCurrentValue("");
					callback.save(true);
				}
			});
		}

		for (var res : images) {
			panel.add(new SimpleTextButton(panel, Component.literal("").append(Component.literal(res.getNamespace()).withStyle(ChatFormatting.GOLD)).append(":").append(Component.literal(res.getPath().substring(9, res.getPath().length() - 4)).withStyle(ChatFormatting.YELLOW)), Icon.getIcon(res.toString())) {
				@Override
				public void onClicked(MouseButton mouseButton) {
					playClickSound();
					imageConfig.setCurrentValue(res.toString());
					callback.save(true);
				}
			});
		}
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			callback.save(false);
			return false;
		}

		return false;
	}
}
