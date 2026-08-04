package dev.ftb.mods.ftblibrary.integration.docsmod;

import dev.ftb.mods.ftblibrary.FTBLibrary;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public enum DocsModRegistry {
    INSTANCE;

    private static final Map<String, DocsMod> registry = new ConcurrentHashMap<>();

    public void registerDocsMod(String id, DocsMod mod) {
        // typically called from FTB XMod Compat
        registry.put(id, mod);

        FTBLibrary.LOGGER.info("registered docs provider mod: {}", id);
    }

    public Optional<DocsMod> getDocsMod(String id) {
        return Optional.ofNullable(registry.get(id));
    }
}
