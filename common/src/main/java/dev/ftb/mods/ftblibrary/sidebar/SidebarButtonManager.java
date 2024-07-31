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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public enum SidebarButtonManager implements ResourceManagerReloadListener {
	INSTANCE;

	private static final Logger LOGGER = LogUtils.getLogger();

	private final List<StoredInfo<SidebarButtonGroup>> groups = new ArrayList<>();
	private final List<StoredInfo<SidebarButton>> buttons = new ArrayList<>();

	private JsonElement readJson(Resource resource) {
		try (BufferedReader reader = resource.openAsReader()) {
			return JsonParser.parseReader(reader);
		} catch (JsonParseException | IOException e) {
			LOGGER.error("can't read {}: {}", resource.sourcePackId(), e.getMessage());
		}
		return JsonNull.INSTANCE;
	}

	public List<StoredInfo<SidebarButton>> getButtons() {
		return buttons;
	}

//	public Map<SidebarButtonGroup, List<SidebarButton>> getButtonGroups() {
//		Map<SidebarButtonGroup, List<SidebarButton>> map = new HashMap<>();
//		for (SidebarButton value : buttons.values()) {
//			SidebarButtonGroup group = groups.get(value.group());
//			if(group != null) {
//				if(!map.containsKey(group)) {
//					map.put(group, new ArrayList<>());
//				}
//				map.get(group).add(value);
//
//			}else {
//				//Todo This should never happen in theory
//			}
//		}
//		return map.entrySet().stream().sorted(Comparator.comparingInt(o -> o.getKey().y())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));
//	}

	public SidebarButtonGroup getGroup(ResourceLocation id) {
        return groups.stream()
				.filter(group -> group.id().equals(id))
				.findFirst()
				.map(StoredInfo::value)
				.orElse(null);
    }

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		groups.clear();
		buttons.clear();

		loadResources(manager, "sidebar_buttons/groups", SidebarButtonGroup.CODEC, groups::add);
		loadResources(manager, "sidebar_buttons/buttons", SidebarButton.CODEC, (result) -> {
			SidebarButtonGroup group = getGroup(result.value().group());
			if(group != null) {
				buttons.add(result);
			}else {
				LOGGER.error("Button {} not found in config", result.id());
			}
		});

		Map<Integer, List<StoredInfo<SidebarButton>>> buttonMap = new HashMap<>();
		for (StoredInfo<SidebarButton> storedInfo : this.buttons) {
			buttonMap.computeIfAbsent(getGroup(storedInfo.value().group()).y(), k -> new ArrayList<>()).add(storedInfo);
		}

		//sort buttonMap by key
		buttonMap = buttonMap.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));

		int y = 0;
		for (List<StoredInfo<SidebarButton>> storedInfoList : buttonMap.values()) {
			int x = 0;
			storedInfoList.sort(Comparator.comparingInt(o -> o.value().x()));
            for (StoredInfo<SidebarButton> sidebarButtonStoredInfo : storedInfoList) {
                SidebarButton button = sidebarButtonStoredInfo.value();
                SidebarButtonGroup group = getGroup(button.group());
                StringSidebarMapValue.SideButtonInfo buttonSettings = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().get(sidebarButtonStoredInfo.id().toString());
                if (buttonSettings == null) {
                    FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().put(sidebarButtonStoredInfo.id().toString(), new StringSidebarMapValue.SideButtonInfo(true, x, y));
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

	private <T> void loadResources(ResourceManager manager, String path, Codec<T> codec, Consumer<StoredInfo<T>> consumer) {
		Map<ResourceLocation, Resource> resourceLocationResourceMap = manager.listResources(path, name -> name.getPath().endsWith(".json"));
		for (Map.Entry<ResourceLocation, Resource> resource : resourceLocationResourceMap.entrySet()) {
			JsonElement jsonElement = readJson(resource.getValue());
			DataResult<T> parse = codec.parse(JsonOps.INSTANCE, jsonElement);
			if (parse.error().isPresent()) {
				FTBLibrary.LOGGER.error("Failed to parse json: {}", parse.error().get().message());
			} else {
				T result = parse.result().get();
				ResourceLocation key = resource.getKey();
				ResourceLocation fixed = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath().replace(path + "/", "").replace(".json", ""));
				consumer.accept(new StoredInfo<>(fixed, result));
			}
		}
	}


	private final List<SidebarGuiButton> buttonList = new ArrayList<>();

	public void refreshButtonList() {
		buttonList.clear();
		for (StoredInfo<SidebarButton> buttonEntry : getButtons()) {
			ResourceLocation id = buttonEntry.id();
			SidebarButton button = buttonEntry.value();

			StringSidebarMapValue.SideButtonInfo buttonSettings = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().get(id.toString());
			if(buttonSettings != null) {
				if(buttonSettings.enabled()) {
					SidebarGuiButton e = new SidebarGuiButton(buttonSettings.xPos(), buttonSettings.yPos(), true, id, button);
					buttonList.add(e);
				}

			}else {
				//Todo this should not be possable
				LOGGER.error("Button {} not found in config", id);
			}
		}

		for (Map.Entry<String, StringSidebarMapValue.SideButtonInfo> stringSideButtonInfoEntry : FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().entrySet()) {
			StringSidebarMapValue.SideButtonInfo buttonSettings = stringSideButtonInfoEntry.getValue();
			if(buttonSettings != null && buttonSettings.enabled()) {
				for (SidebarGuiButton button : buttonList) {
					if(button.isEnabled()) {
						if(!button.getButtonId().toString().equals(stringSideButtonInfoEntry.getKey())) {
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

	public void saveConfigFromButtonList() {
		for (SidebarGuiButton button : buttonList) {
			StringSidebarMapValue.SideButtonInfo buttonSettings = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().get(button.getButtonId().toString());
			if(buttonSettings != null) {
				FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().put(button.getButtonId().toString(), new StringSidebarMapValue.SideButtonInfo(button.isEnabled(), button.getGridX(), button.getGridY()));
			}
		}
		FTBLibraryClientConfig.save();
		refreshButtonList();

	}

	public List<SidebarGuiButton> getButtonList() {
		return buttonList;
	}


	//		var o = new JsonObject();
//
//		for (var group : groups) {
//			for (var button : group.getButtons()) {
//				var o1 = o.getAsJsonObject(button.getId().getNamespace());
//
//				if (o1 == null) {
//					o1 = new JsonObject();
//					o.add(button.getId().getNamespace(), o1);
//				}
//
//				o1.addProperty(button.getId().getPath(), button.getConfig());
//			}
//		}
//
//		var file = Platform.getConfigFolder().resolve("sidebar_buttons.json").toFile();
//
//		try (var writer = new FileWriter(file)) {
//			var gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
//			var jsonWriter = new JsonWriter(writer);
//			jsonWriter.setIndent("\t");
//			gson.toJson(o, jsonWriter);
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}

	public record StoredInfo<T>(ResourceLocation id, T value) {}

}
