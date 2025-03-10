package dev.ftb.mods.ftblibrary.sidebar;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.api.sidebar.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringSidebarMapValue.SideButtonInfo;
import dev.ftb.mods.ftblibrary.util.MapUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public class SidebarButtonManager extends SimpleJsonResourceReloadListener<JsonElement> {

    public static final SidebarButtonManager INSTANCE = new SidebarButtonManager();
    private final Map<ResourceLocation, RegisteredSidebarButton> buttons = new HashMap<>();
    private final List<SidebarGuiButton> buttonList = new ArrayList<>();

    public SidebarButtonManager() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("sidebar_buttons"));
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

        var buttonConfig = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get();
        var prevConfig = Map.copyOf(buttonConfig);

        for (RegisteredSidebarButton buttonEntry : sortedButtons) {
            SideButtonInfo buttonSettings = buttonConfig.get(buttonEntry.getId().toString());
            if (buttonSettings == null) {
                buttonSettings = new SideButtonInfo(true, x, y);
                buttonConfig.put(buttonEntry.getId().toString(), buttonSettings);
            }
            buttonList.add(new SidebarGuiButton(new GridLocation(buttonSettings.xPos(), buttonSettings.yPos()), buttonSettings.enabled(), buttonEntry));

            if (++x >= 4) {
                x = 0;
                y++;
            }
        }

        for (RegisteredSidebarButton value : buttons.values()) {
            SidebarButtonCreatedEvent.EVENT.invoker().accept(new SidebarButtonCreatedEvent(value));
        }

        if (!prevConfig.equals(buttonConfig)) {
            FTBLibraryClientConfig.save();
        }
    }

    private <T> void loadResources(Map<ResourceLocation, JsonElement> objects, Codec<T> codec, BiConsumer<ResourceLocation, T> consumer) {
        for (Map.Entry<ResourceLocation, JsonElement> resource : objects.entrySet()) {
            codec.parse(JsonOps.INSTANCE, resource.getValue())
                    .resultOrPartial(err -> FTBLibrary.LOGGER.error("Failed to parse json: {}", err))
                    .ifPresent(result -> {
                        ResourceLocation key = resource.getKey();
                        ResourceLocation fixed = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath());
                        consumer.accept(fixed, result);
                    });
        }
    }

    public void saveConfigFromButtonList() {
        Int2ObjectMap<List<SidebarGuiButton>> buttonMap = new Int2ObjectOpenHashMap<>();
        for (SidebarGuiButton button : getButtonList()) {
            int y = button.isEnabled() ? button.getGridLocation().y() : -1;
            buttonMap.computeIfAbsent(y, k -> new LinkedList<>()).add(button);
        }

        var sidebarConfig = FTBLibraryClientConfig.SIDEBAR_BUTTONS.get();
        var prevConfig = Map.copyOf(sidebarConfig);

        int y = 0;
        for (Map.Entry<Integer, List<SidebarGuiButton>> buttonsByYpos : MapUtils.sortMapByKey(buttonMap).entrySet()) {
            if (buttonsByYpos.getKey() == -1) {
                for (SidebarGuiButton button : buttonsByYpos.getValue()) {
                    button.setGridLocation(-1, -1);
                    sidebarConfig.put(button.toString(), SideButtonInfo.DISABLED);
                }
            }

            int x = 0;
            buttonsByYpos.getValue().sort(Comparator.comparingInt((SidebarGuiButton button) -> button.getGridLocation().x()));

            for (SidebarGuiButton sidebarButton : buttonsByYpos.getValue()) {
                if (sidebarButton.isEnabled()) {
                    sidebarButton.setGridLocation(x, y);
                    sidebarConfig.put(sidebarButton.toString(), SideButtonInfo.at(x, y));
                    x++;
                }
            }
            if (x > 0) {
                y++;
            }
        }

        for (SidebarGuiButton button : buttonList) {
            SideButtonInfo buttonSettings = sidebarConfig.get(button.toString());
            if (buttonSettings != null) {
                sidebarConfig.put(button.toString(), SideButtonInfo.ofButton(button));
            }
        }

        // Map.equals() should be fine here, comparing two maps of String -> SideButtonInfo record
        if (!sidebarConfig.equals(prevConfig)) {
            FTBLibraryClientConfig.save();
        }
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
