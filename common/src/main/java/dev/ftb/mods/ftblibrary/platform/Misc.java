package dev.ftb.mods.ftblibrary.platform;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public interface Misc {
    boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode);

    Component componentWithLinks(String message);

    void refreshDisplayName(Player player);

    long bucketFluidAmount();
}
