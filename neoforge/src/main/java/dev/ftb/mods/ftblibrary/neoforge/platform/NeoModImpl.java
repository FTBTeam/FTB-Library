package dev.ftb.mods.ftblibrary.neoforge.platform;

import dev.ftb.mods.ftblibrary.platform.Mod;
import net.neoforged.neoforgespi.language.IModInfo;

public class NeoModImpl implements Mod {
    private final IModInfo mod;

    public NeoModImpl(IModInfo mod) {
        this.mod = mod;
    }

    @Override
    public String modId() {
        return this.mod.getModId();
    }

    @Override
    public String version() {
        // TODO: I think this is wrong
        return this.mod.getVersion().getQualifier();
    }

    @Override
    public String name() {
        return this.mod.getDisplayName();
    }

    @Override
    public String description() {
        return this.mod.getDescription();
    }
}
