package dev.ftb.mods.ftblibrary.util;

import dev.architectury.platform.Platform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModUtils {
    private static final Map<String, String> modId2Name = new HashMap<>();

    public static Optional<String> getModName(String modId) {
        if (modId2Name.isEmpty()) {
            Platform.getMods().forEach(mod -> modId2Name.put(mod.getModId(), mod.getName()));
        }

        return Optional.ofNullable(modId2Name.get(modId));
    }

    public static Optional<String> getModName(Item item) {
        return getModName(BuiltInRegistries.ITEM.getKey(item).getNamespace());
    }

    public static Optional<String> getModName(Fluid fluid) {
        return getModName(BuiltInRegistries.FLUID.getKey(fluid).getNamespace());
    }
}
