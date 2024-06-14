package dev.ftb.mods.ftblibrary.sidebar;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public enum SidebarButtonManager implements ResourceManagerReloadListener {
	INSTANCE;

	private final List<SidebarButtonGroup> groups = new ArrayList<>();

	public List<SidebarButtonGroup> getGroups() {
		return groups;
	}

	private JsonElement readJson(Resource resource) {
		try (var reader = resource.openAsReader()) {
			return JsonParser.parseReader(reader);
		} catch (JsonParseException | IOException e) {
			FTBLibrary.LOGGER.warn("can't read {}: {}", resource.sourcePackId(), e.getMessage());
		}

		return JsonNull.INSTANCE;
	}

	private JsonElement readJson(File file) {
		try (var reader = new FileReader(file)) {
			return JsonParser.parseReader(reader);
		} catch (JsonParseException | IOException e) {
			FTBLibrary.LOGGER.warn("can't read {}: {}", file.getAbsolutePath(), e.getMessage());
		}

		return JsonNull.INSTANCE;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		groups.clear();

		var element = readJson(Platform.getConfigFolder().resolve("sidebar_buttons.json").toFile());
		JsonObject sidebarButtonConfig;

		if (element.isJsonObject()) {
			sidebarButtonConfig = element.getAsJsonObject();
		} else {
			sidebarButtonConfig = new JsonObject();
		}

		Map<ResourceLocation, SidebarButtonGroup> groupMap = new HashMap<>();

		for (var domain : manager.getNamespaces()) {
			try {
				// TODO: Use an alternative way to register sidebar groups because jsons are a bit messy
				for (var resource : manager.getResourceStack(ResourceLocation.fromNamespaceAndPath(domain, "sidebar_button_groups.json"))) {
					var json = readJson(resource);

					for (var entry : json.getAsJsonObject().entrySet()) {
						if (entry.getValue().isJsonObject()) {
							var groupJson = entry.getValue().getAsJsonObject();
							var y = 0;
							var pinned = true;

							if (groupJson.has("y")) {
								y = groupJson.get("y").getAsInt();
							}

							if(groupJson.has("pinned")) {
								pinned = groupJson.get("pinned").getAsBoolean();
							}

							var group = new SidebarButtonGroup(ResourceLocation.fromNamespaceAndPath(domain, entry.getKey()), y, pinned);
							groupMap.put(group.getId(), group);
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		for (var domain : manager.getNamespaces()) {
			try {
				for (var resource : manager.getResourceStack(ResourceLocation.fromNamespaceAndPath(domain, "sidebar_buttons.json"))) {
					var json = readJson(resource);

					if (json.isJsonObject()) {
						for (var entry : json.getAsJsonObject().entrySet()) {
							if (entry.getValue().isJsonObject()) {
								var buttonJson = entry.getValue().getAsJsonObject();

								if (!buttonJson.has("group")) {
									continue;
								}

								if (/*!FTBLibConfig.debugging.dev_sidebar_buttons && */buttonJson.has("dev_only") && buttonJson.get("dev_only").getAsBoolean()) {
									continue;
								}

								var group = groupMap.get(ResourceLocation.parse(buttonJson.get("group").getAsString()));

								if (group == null) {
									continue;
								}

								var button = new SidebarButton(ResourceLocation.fromNamespaceAndPath(domain, entry.getKey()), group, buttonJson);

								group.getButtons().add(button);

								if (sidebarButtonConfig.has(button.getId().getNamespace())) {
									var e = sidebarButtonConfig.get(button.getId().getNamespace());

									if (e.isJsonObject() && e.getAsJsonObject().has(button.getId().getPath())) {
										button.setConfig(e.getAsJsonObject().get(button.getId().getPath()).getAsBoolean());
									}
								} else if (sidebarButtonConfig.has(button.getId().toString())) {
									button.setConfig(sidebarButtonConfig.get(button.getId().toString()).getAsBoolean());
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		for (var group : groupMap.values()) {
			if (!group.getButtons().isEmpty()) {
				group.getButtons().sort(null);
				groups.add(group);
			}
		}

		groups.sort(null);

		for (var group : groups) {
			for (var button : group.getButtons()) {
				SidebarButtonCreatedEvent.EVENT.invoker().accept(new SidebarButtonCreatedEvent(button));
			}
		}

		saveConfig();
	}

	public void saveConfig() {
		var o = new JsonObject();

		for (var group : groups) {
			for (var button : group.getButtons()) {
				var o1 = o.getAsJsonObject(button.getId().getNamespace());

				if (o1 == null) {
					o1 = new JsonObject();
					o.add(button.getId().getNamespace(), o1);
				}

				o1.addProperty(button.getId().getPath(), button.getConfig());
			}
		}

		var file = Platform.getConfigFolder().resolve("sidebar_buttons.json").toFile();

		try (var writer = new FileWriter(file)) {
			var gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			var jsonWriter = new JsonWriter(writer);
			jsonWriter.setIndent("\t");
			gson.toJson(o, jsonWriter);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
