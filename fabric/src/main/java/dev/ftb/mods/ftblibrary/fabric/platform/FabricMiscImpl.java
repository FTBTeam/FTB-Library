package dev.ftb.mods.ftblibrary.fabric.platform;

import dev.ftb.mods.ftblibrary.core.mixin.fabric.KeyMappingAccessor;
import dev.ftb.mods.ftblibrary.fabric.PlayerDisplayNameCache;
import dev.ftb.mods.ftblibrary.platform.Misc;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
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
        // why 81000?  see https://github.com/FabricMC/fabric-api/issues/1166
        return FluidConstants.BUCKET;
    }
}
