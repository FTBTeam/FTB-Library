package dev.ftb.mods.ftblibrary.fabric.platform;

import dev.ftb.mods.ftblibrary.core.mixin.fabric.KeyMappingAccessor;
import dev.ftb.mods.ftblibrary.fabric.PlayerDisplayNameCache;
import dev.ftb.mods.ftblibrary.platform.Misc;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
        // 81000? see https://github.com/FabricMC/fabric-api/issues/1166
        // TODO: Helper methods to do something like what TechReborn supports https://github.com/TechReborn/TechReborn/blob/79d0b16b15bac13f1fefcdbdab044a65d06297c2/RebornCore/src/main/java/reborncore/common/util/FluidTextHelper.java
        return FluidConstants.BUCKET;
    }

    @Override
    public boolean isFakePlayer(Player player) {
        // This is kinda meh but it's a relatively sane approach to detecting fake players
        // as they will typically extend ServerPlayer but not be exactly an instance of ServerPlayer.
        // Credit to Architectury for this approach:
        return player instanceof ServerPlayer && player.getClass() != ServerPlayer.class;
    }
}
