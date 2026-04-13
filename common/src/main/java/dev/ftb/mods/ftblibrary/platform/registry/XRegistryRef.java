package dev.ftb.mods.ftblibrary.platform.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public interface XRegistryRef<T> extends Supplier<T> {
    Identifier identifier();

    ResourceKey<T> resourceKey();
}
