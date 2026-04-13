package dev.ftb.mods.ftblibrary.fabric.platform;

import dev.ftb.mods.ftblibrary.platform.Mod;
import net.fabricmc.loader.api.ModContainer;

public class FabricModImpl implements Mod {
    private final ModContainer container;

    public static FabricModImpl of(ModContainer modContainer) {
        return new FabricModImpl(modContainer);
    }

    private FabricModImpl(ModContainer container) {
        this.container = container;
    }

    @Override
    public String modId() {
        return container.getMetadata().getId();
    }

    @Override
    public String version() {
        return container.getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public String name() {
        return container.getMetadata().getName();
    }

    @Override
    public String description() {
        return container.getMetadata().getDescription();
    }
}
