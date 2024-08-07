package dev.ftb.mods.ftblibrary.sidebar;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringSidebarMapValue;
import dev.ftb.mods.ftblibrary.util.MapUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public enum SidebarButtonManager implements ResourceManagerReloadListener {
	INSTANCE;

	private static final Logger LOGGER = LogUtils.getLogger();

	private final Map<ResourceLocation, SidebarButton> buttons = new HashMap<>();

	private final List<SidebarGuiButton> buttonList = new ArrayList<>();

	private JsonElement readJson(Resource resource) {
		try (BufferedReader reader = resource.openAsReader()) {
			return JsonParser.parseReader(reader);
		} catch (JsonParseException | IOException e) {
			LOGGER.error("can't read {}: {}", resource.sourcePackId(), e.getMessage());
		}
		return JsonNull.INSTANCE;
	}

	public Collection<SidebarButton> getButtons() {
		return buttons.values();
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		buttons.clear();

		//Read the button and group json files and register them to their 'registry' map
		loadResources(manager, "sidebar_buttons", SidebarButtonData.CODEC, (id, buttonData) -> buttons.put(id, new SidebarButton(id, buttonData)));

		buttonList.clear();
		int y = 0;
		int x = 0;
		for (SidebarButton buttonEntry : buttons.values()) {
			StringSidebarMapValue.SideButtonInfo buttonSettings = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().get(buttonEntry.getId().toString());
			if(buttonSettings == null) {
				buttonSettings = new StringSidebarMapValue.SideButtonInfo(true, x, y);
				FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().put(buttonEntry.getId().toString(), buttonSettings);
				FTBLibraryClientConfig.save();
			}
            buttonList.add(new SidebarGuiButton(new GridLocation(buttonSettings.xPos(), buttonSettings.yPos()), buttonSettings.enabled(), buttonEntry));
			x++;
			if(x >= 4) {
				x = 0;
				y++;
			}
        }
		FTBLibraryClientConfig.save();
	}

	private <T> void loadResources(ResourceManager manager, String path, Codec<T> codec, BiConsumer<ResourceLocation, T> consumer) {
		Map<ResourceLocation, Resource> resourceLocationResourceMap = manager.listResources(path, name -> name.getPath().endsWith(".json"));
		for (Map.Entry<ResourceLocation, Resource> resource : resourceLocationResourceMap.entrySet()) {
			JsonElement jsonElement = readJson(resource.getValue());
			DataResult<T> parse = codec.parse(JsonOps.INSTANCE, jsonElement);
			if (parse.error().isPresent()) {
				FTBLibrary.LOGGER.error("Failed to parse json: {}", parse.error().get().message());
			} else {
				T result = parse.result().get();
				ResourceLocation key = resource.getKey();
				String path1 = key.getPath();
				ResourceLocation fixed = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), path1.replace(path + "/", "").replace(".json", ""));
				consumer.accept(fixed, result);
			}
		}
	}

	public void saveConfigFromButtonList() {
		Map<Integer, List<SidebarGuiButton>> buttonMap = new HashMap<>();
		for (SidebarGuiButton button : getButtonList()) {
			int y = button.isEnabled() ? button.getGirdLocation().y() : -1;
            buttonMap.computeIfAbsent(y, k -> new LinkedList<>()).add(button);
		}

		int y = 0;
		for (Map.Entry<Integer, List<SidebarGuiButton>> integerListEntry : MapUtils.sortMapByKey(buttonMap).entrySet()) {
			if(integerListEntry.getKey() == -1) {
				for (SidebarGuiButton button : integerListEntry.getValue()) {
					button.setGridLocation(-1, -1);
					FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().put(button.getSidebarButton().getId().toString(), new StringSidebarMapValue.SideButtonInfo(false, -1, -1));
				}
			}
			int x = 0;
			integerListEntry.getValue()
					.sort(Comparator.comparingInt((SidebarGuiButton button) -> button.getGirdLocation().x()));
			List<SidebarGuiButton> value = integerListEntry.getValue();
			for (SidebarGuiButton sidebarButton : value) {
				if(sidebarButton.isEnabled()) {
					sidebarButton.setGridLocation(x, y);
					FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().put(sidebarButton.getSidebarButton().getId().toString(), new StringSidebarMapValue.SideButtonInfo(sidebarButton.isEnabled(), x, y));
					x++;
				}
			}
			if(x != 0) {
				y++;
			}
		}

		for (SidebarGuiButton button : buttonList) {
			StringSidebarMapValue.SideButtonInfo buttonSettings = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().get(button.getSidebarButton().getId().toString());
			if(buttonSettings != null) {
				FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().put(button.getSidebarButton().getId().toString(), new StringSidebarMapValue.SideButtonInfo(button.isEnabled(), button.getGirdLocation().x(), button.getGirdLocation().y()));
			}
		}
		FTBLibraryClientConfig.save();
	}

	public List<SidebarGuiButton> getButtonList() {
		return buttonList;
	}

	public List<SidebarGuiButton> getEnabledButtonList(boolean all) {
		return buttonList.stream()
				.filter(SidebarGuiButton::isEnabled)
				.filter(button -> all || button.getSidebarButton().canSee())
				.toList();
	}

	public List<SidebarGuiButton> getDisabledButtonList(boolean all) {
		return buttonList.stream()
				.filter(button -> !button.isEnabled())
				.filter(button -> all || button.getSidebarButton().canSee())
				.collect(Collectors.toList());
	}

}
