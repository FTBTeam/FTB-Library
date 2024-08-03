package dev.ftb.mods.ftblibrary.sidebar;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringSidebarMapValue;
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

	private final Map<ResourceLocation, SidebarButtonGroup> groups = new HashMap<>();
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
		groups.clear();
		buttons.clear();

		//Read the button and group json files and register them to their 'registry' map
		loadResources(manager, "sidebar_buttons/groups", SidebarButtonGroup.CODEC, groups::put);
		loadResources(manager, "sidebar_buttons/buttons", SidebarButtonData.CODEC, (id, buttonData) -> {
			SidebarButtonGroup group = groups.get(buttonData.group());
			if(group != null) {
				buttons.put(id, new SidebarButton(id, buttonData));
			}else {
				LOGGER.error("Could not register button {} as group {} does not exist", id, buttonData.group());
			}
		});

		buttonList.clear();
		for (SidebarButton buttonEntry : getButtons()) {
			StringSidebarMapValue.SideButtonInfo buttonSettings = getOrCreateButtonSettings(buttonEntry);
            buttonList.add(new SidebarGuiButton(new GridLocation(buttonSettings.xPos(), buttonSettings.yPos()), buttonSettings.enabled(), buttonEntry));
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


	private StringSidebarMapValue.SideButtonInfo getOrCreateButtonSettings(SidebarButton button) {
		StringSidebarMapValue.SideButtonInfo buttonSettings = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().get(button.getId().toString());
		if(buttonSettings == null) {
			buttonSettings = new StringSidebarMapValue.SideButtonInfo(true, button.getData().x(), groups.get(button.getData().group()).y());
			FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().put(button.getId().toString(), buttonSettings);
			FTBLibraryClientConfig.save();
		}
		return buttonSettings;
	}

	private boolean isRegistered(ResourceLocation id) {
		return buttons.values().stream().anyMatch(button -> button.getId().equals(id));
	}

	public void saveConfigFromButtonList() {

		Map<Integer, List<SidebarGuiButton>> buttonMap = new HashMap<>();
		for (SidebarGuiButton button : getButtonList()) {
			int y = button.isEnabled() ? button.getGirdLocation().y() : -1;
            buttonMap.computeIfAbsent(y, k -> new LinkedList<>()).add(button);
		}

		int y = 0;
		for (Map.Entry<Integer, List<SidebarGuiButton>> integerListEntry : Utils.sortMapByKey(buttonMap).entrySet()) {
			if(integerListEntry.getKey() == -1) {
				for (SidebarGuiButton button : integerListEntry.getValue()) {
					FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().put(button.getSidebarButton().getId().toString(), new StringSidebarMapValue.SideButtonInfo(false, -1, -1));
				}
			}
			int x = 0;
			integerListEntry.getValue()
					.sort(Comparator.comparingInt((SidebarGuiButton button) -> button.getGirdLocation().x()));
			List<SidebarGuiButton> value = integerListEntry.getValue();
			for (SidebarGuiButton sidebarButton : value) {
				if(sidebarButton.isEnabled()) {
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
//
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


	public Map<SidebarButtonGroup, List<SidebarButton>> getButtonGroups() {
		Map<SidebarButtonGroup, List<SidebarButton>> buttonMap = new HashMap<>();
		for (SidebarButton buttonData : this.buttons.values()) {
			buttonMap.computeIfAbsent(groups.get(buttonData.getData().group()), k -> new ArrayList<>()).add(buttonData);
		}

		return Utils.sortMapByKey(buttonMap);
	}



	//Todo cleanup
	public static class Utils {

		public static <K, V> Map<K, V> sortMapByKey(Map<K, V> map, Comparator<K> comparator) {
			return map.entrySet().stream()
					.sorted(Map.Entry.comparingByKey(comparator))
					.collect(Collectors.toMap(
							Map.Entry::getKey,
							Map.Entry::getValue,
							(a, b) -> a,
							HashMap::new
					));
		}

		public static<K extends Comparable<? super K>, V> Map<K, V> sortMapByKey(Map<K, V> map) {
			return map.entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.collect(Collectors.toMap(
							Map.Entry::getKey,
							Map.Entry::getValue,
							(a, b) -> a,
							HashMap::new
					));
		}
	}

}
