package dev.ftb.mods.ftblibrary.sidebar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.api.sidebar.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringSidebarMapValue;
import dev.ftb.mods.ftblibrary.util.MapUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
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


public class SidebarButtonManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final SidebarButtonManager INSTANCE = new SidebarButtonManager();
    private final Map<ResourceLocation, RegisteredSidebarButton> buttons = new HashMap<>();
    private final List<SidebarGuiButton> buttonList = new ArrayList<>();

    public SidebarButtonManager() {
        super(GSON, "sidebar_buttons");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        buttons.clear();

        // Read the button and group json files and register them to their 'registry' map
        loadResources(object, SidebarButtonData.CODEC, (id, buttonData) -> buttons.put(id, new RegisteredSidebarButton(id, buttonData)));

        buttonList.clear();
        List<RegisteredSidebarButton> sortedButtons = buttons.values().stream().sorted(Comparator.comparingInt(value -> value.getData().sortIndex())).toList();

        int y = 0;
        int x = 0;

        for (RegisteredSidebarButton buttonEntry : sortedButtons) {
            StringSidebarMapValue.SideButtonInfo buttonSettings = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().get(buttonEntry.getId().toString());
            if (buttonSettings == null) {
                buttonSettings = new StringSidebarMapValue.SideButtonInfo(true, x, y);
                FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().put(buttonEntry.getId().toString(), buttonSettings);
                FTBLibraryClientConfig.save();
            }
            buttonList.add(new SidebarGuiButton(new GridLocation(buttonSettings.xPos(), buttonSettings.yPos()), buttonSettings.enabled(), buttonEntry));

            x++;
            if (x >= 4) {
                x = 0;
                y++;
            }
        }

        for (RegisteredSidebarButton value : buttons.values()) {
            SidebarButtonCreatedEvent.EVENT.invoker().accept(new SidebarButtonCreatedEvent(value));
        }
        FTBLibraryClientConfig.save();
    }

    private <T> void loadResources(Map<ResourceLocation, JsonElement> objects, Codec<T> codec, BiConsumer<ResourceLocation, T> consumer) {
        for (Map.Entry<ResourceLocation, JsonElement> resource : objects.entrySet()) {
            JsonElement jsonElement = resource.getValue();
            DataResult<T> parse = codec.parse(JsonOps.INSTANCE, jsonElement);

            if (parse.error().isPresent()) {
                FTBLibrary.LOGGER.error("Failed to parse json: {}", parse.error().get().message());
            } else {
                T result = parse.result().get();
                ResourceLocation key = resource.getKey();
                String path1 = key.getPath();
                ResourceLocation fixed = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath());
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
            if (integerListEntry.getKey() == -1) {
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
                if (sidebarButton.isEnabled()) {
                    sidebarButton.setGridLocation(x, y);
                    FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().put(sidebarButton.getSidebarButton().getId().toString(), new StringSidebarMapValue.SideButtonInfo(sidebarButton.isEnabled(), x, y));
                    x++;
                }
            }
            if (x != 0) {
                y++;
            }
        }

        for (SidebarGuiButton button : buttonList) {
            StringSidebarMapValue.SideButtonInfo buttonSettings = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get().get(button.getSidebarButton().getId().toString());
            if (buttonSettings != null) {
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

    public Collection<RegisteredSidebarButton> getButtons() {
        return buttons.values();
    }
}
