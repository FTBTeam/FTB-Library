package dev.ftb.mods.ftblibrary.fabric.platform;

import dev.ftb.mods.ftblibrary.core.mixin.fabric.KeyMappingAccessor;
import dev.ftb.mods.ftblibrary.fabric.PlayerDisplayNameCache;
import dev.ftb.mods.ftblibrary.platform.Misc;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class FabricMiscImpl implements Misc {
    @Override
    public boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode) {
        return keyCode != InputConstants.UNKNOWN && keyCode.equals(((KeyMappingAccessor) keyBinding).getKey());
    }

    @Override
    public Component componentWithLinks(String message) {
        return Component.literal(message);
    }

    @Override
    public void refreshDisplayName(Player player) {
        ((PlayerDisplayNameCache) player).clearCachedDisplayName();
    }

    @Override
    public long bucketFluidAmount() {
        return 81000;// TODO: What does this value mean.
    }
}
