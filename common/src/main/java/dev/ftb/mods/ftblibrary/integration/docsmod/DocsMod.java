package dev.ftb.mods.ftblibrary.integration.docsmod;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface DocsMod {
    void openDocsPage(Player player, Identifier bookId, @Nullable Identifier pageId, String anchor);

    enum None implements DocsMod {
        INSTANCE;

        @Override
        public void openDocsPage(Player player, Identifier bookId, @Nullable Identifier pageId, String anchor) {
        }
    }
}
