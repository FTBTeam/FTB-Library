package dev.ftb.mods.ftblibrary.sidebar;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftblibrary.FTBLibrary;
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
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public enum SidebarButtonManager implements ResourceManagerReloadListener {
	INSTANCE;

	private static final Logger LOGGER = LogUtils.getLogger();

	private final Map<ResourceLocation, SidebarButtonGroup> groups = new HashMap<>();
	private final Map<ResourceLocation, SidebarButton> buttons = new HashMap<>();

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

		Map<SidebarButtonGroup, List<SidebarButton>> buttonMap = getButtonGroups();

		int y = 0;
		for (Map.Entry<SidebarButtonGroup, List<SidebarButton>> buttonGroupListEntry : buttonMap.entrySet()) {
			int x = 0;
			buttonGroupListEntry.getValue().sort(Comparator.comparingInt(button -> button.getData().x()));
            for (SidebarButton sidebarButton : buttonGroupListEntry.getValue()) {
                SidebarButtonData button = sidebarButton.getData();
                StringSidebarMapValue.SideButtonInfo buttonSettings = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().get(sidebarButton.getId().toString());
                if (buttonSettings == null) {
                    FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().put(sidebarButton.getId().toString(), new StringSidebarMapValue.SideButtonInfo(button.defaultEnabled(), x, y));
                }
                x++;
            }
			if(x != 0) {
				y++;
			}
		}

		FTBLibraryClientConfig.save();

		refreshButtonList();
	}


//		var element = readJson(Platform.getConfigFolder().resolve("sidebar_buttons.json").toFile());
//		JsonObject sidebarButtonConfig;
//
//		if (element.isJsonObject()) {
//			sidebarButtonConfig = element.getAsJsonObject();
//		} else {
//			sidebarButtonConfig = new JsonObject();
//		}
//
//		Map<ResourceLocation, SidebarButtonGroup> groupMap = new HashMap<>();
//
//		for (var domain : manager.getNamespaces()) {
//			try {
//				// TODO: Use an alternative way to register sidebar groups because jsons are a bit messy
//				for (var resource : manager.getResourceStack(ResourceLocation.fromNamespaceAndPath(domain, "sidebar_button_groups.json"))) {
//					var json = readJson(resource);
//
//					for (var entry : json.getAsJsonObject().entrySet()) {
//						if (entry.getValue().isJsonObject()) {
//							var groupJson = entry.getValue().getAsJsonObject();
//							var y = 0;
//							var pinned = true;
//
//							if (groupJson.has("y")) {
//								y = groupJson.get("y").getAsInt();
//							}
//
//							if(groupJson.has("pinned")) {
//								pinned = groupJson.get("pinned").getAsBoolean();
//							}
//
//							var group = new SidebarButtonGroup(ResourceLocation.fromNamespaceAndPath(domain, entry.getKey()), y, pinned);
//							groupMap.put(group.getId(), group);
//						}
//					}
//				}
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		}
//
//		for (String domain : manager.getNamespaces()) {
//			try {
//				for (Resource resource : manager.getResourceStack(ResourceLocation.fromNamespaceAndPath(domain, "sidebar_buttons.json"))) {
//                    JsonElement json = readJson(resource);
//
//					if (json.isJsonObject()) {
//						for (var entry : json.getAsJsonObject().entrySet()) {
//							if (entry.getValue().isJsonObject()) {
//								var buttonJson = entry.getValue().getAsJsonObject();
//
//								if (!buttonJson.has("group")) {
//									continue;
//								}
//
//								if (/*!FTBLibConfig.debugging.dev_sidebar_buttons && */buttonJson.has("dev_only") && buttonJson.get("dev_only").getAsBoolean()) {
//									continue;
//								}
//
//								var group = groupMap.get(ResourceLocation.parse(buttonJson.get("group").getAsString()));
//
//								if (group == null) {
//									continue;
//								}
//
//								var button = new SidebarButton(ResourceLocation.fromNamespaceAndPath(domain, entry.getKey()), group, buttonJson);
//
//								group.getButtons().add(button);
//
//								if (sidebarButtonConfig.has(button.getId().getNamespace())) {
//									var e = sidebarButtonConfig.get(button.getId().getNamespace());
//
//									if (e.isJsonObject() && e.getAsJsonObject().has(button.getId().getPath())) {
//										button.setConfig(e.getAsJsonObject().get(button.getId().getPath()).getAsBoolean());
//									}
//								} else if (sidebarButtonConfig.has(button.getId().toString())) {
//									button.setConfig(sidebarButtonConfig.get(button.getId().toString()).getAsBoolean());
//								}
//							}
//						}
//					}
//				}
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		}
//
//		for (var group : groupMap.values()) {
//			if (!group.getButtons().isEmpty()) {
//				group.getButtons().sort(null);
//				groups.add(group);
//			}
//		}
//
//		groups.sort(null);
//
//		for (var group : groups) {
//			for (var button : group.getButtons()) {
//				SidebarButtonCreatedEvent.EVENT.invoker().accept(new SidebarButtonCreatedEvent(button));
//			}
//		}
//
//		saveConfig();

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


	private final List<SidebarGuiButton> buttonList = new ArrayList<>();

	public void refreshButtonList() {
		buttonList.clear();
		for (SidebarButton buttonEntry : getButtons()) {
//			if(buttonEntry.canSee()) {
				ResourceLocation id = buttonEntry.getId();
				SidebarButtonData button = buttonEntry.getData();

				StringSidebarMapValue.SideButtonInfo buttonSettings = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().get(id.toString());
				if(buttonSettings != null) {
					SidebarGuiButton e = new SidebarGuiButton(new GridLocation(buttonSettings.xPos(), buttonSettings.yPos()), buttonSettings.enabled(), buttonEntry);
					buttonList.add(e);
				}else {
					//Todo this should not be possable
					LOGGER.error("Button {} not found in config", id);
				}
//			}
		}

		for (Map.Entry<String, StringSidebarMapValue.SideButtonInfo> stringSideButtonInfoEntry : FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().entrySet()) {
			if(!isRegistered(ResourceLocation.parse(stringSideButtonInfoEntry.getKey()))) {
				continue;
			}
			StringSidebarMapValue.SideButtonInfo buttonSettings = stringSideButtonInfoEntry.getValue();
			if(buttonSettings != null && buttonSettings.enabled()) {
				for (SidebarGuiButton button : buttonList) {
					if(button.getSidebarButton().canSee() && button.isEnabled()) {
						if(!button.getSidebarButton().getId().toString().equals(stringSideButtonInfoEntry.getKey())) {
							if(button.getGridY() == buttonSettings.yPos() && button.getGridX() == buttonSettings.xPos()) {
								button.setGrid(button.getGridX() + 1, button.getGridY());
							}
						}

					}
				}
			}
		}

		FTBLibraryClientConfig.save();
	}

	private boolean isRegistered(ResourceLocation id) {
		return buttons.values().stream().anyMatch(button -> button.getId().equals(id));
	}

	public void saveConfigFromButtonList() {
		for (SidebarGuiButton button : buttonList) {
			StringSidebarMapValue.SideButtonInfo buttonSettings = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().get(button.getSidebarButton().getId().toString());
			if(buttonSettings != null) {
				FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().put(button.getSidebarButton().getId().toString(), new StringSidebarMapValue.SideButtonInfo(button.isEnabled(), button.getGridX(), button.getGridY()));
			}
		}
		FTBLibraryClientConfig.save();
		refreshButtonList();

	}
//
	public List<SidebarGuiButton> getButtonList() {
		return buttonList;
	}

	public List<SidebarGuiButton> getEnabledButtonList() {
		return buttonList.stream().filter(SidebarGuiButton::isEnabled).collect(Collectors.toList());
	}

	public List<SidebarGuiButton> getDisabledButtonList() {
		return buttonList.stream().filter(button -> !button.isEnabled()).collect(Collectors.toList());
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
