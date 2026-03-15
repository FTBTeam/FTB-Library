package dev.ftb.mods.ftblibrary.neoforge.platform;

import dev.ftb.mods.ftblibrary.platform.Misc;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.FakePlayer;

public class NeoMiscImpl implements Misc {
    @Override
    public boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode) {
        return keyBinding.isActiveAndMatches(keyCode);
    }

    @Override
    public Component componentWithLinks(String message) {
        return CommonHooks.newChatWithLinks(message);
    }

    @Override
    public void refreshDisplayName(Player player) {
        player.refreshDisplayName();
    }

    @Override
    public long bucketFluidAmount() {
        return 1_000;
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer || (player instanceof ServerPlayer && player.getClass() != ServerPlayer.class);
    }
}
