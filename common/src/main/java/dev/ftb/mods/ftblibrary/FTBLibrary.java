package dev.ftb.mods.ftblibrary;

import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.config.FTBLibraryServerConfig;
import dev.ftb.mods.ftblibrary.config.FTBLibraryStartupConfig;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.items.ModItems;
import dev.ftb.mods.ftblibrary.nbtedit.NBTEditResponseHandlers;
import dev.ftb.mods.ftblibrary.net.FTBLibraryNet;
import dev.ftb.mods.ftblibrary.net.SyncKnownServerRegistriesPacket;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistryRef;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.text.ExtendableTextColor;
import dev.ftb.mods.ftblibrary.util.text.RainbowTextColor;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FTBLibrary {
    public static final String MOD_ID = "ftblibrary";
    public static final String MOD_NAME = "FTB Library";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public FTBLibrary() {
        ConfigManager cfgMgr = ConfigManager.getInstance();
        cfgMgr.init();
        cfgMgr.registerClientConfig(FTBLibraryClientConfig.CONFIG, MOD_ID + ".client_settings");
        if (ModUtils.isDevMode()) {
            cfgMgr.registerStartupConfig(FTBLibraryStartupConfig.CONFIG, MOD_ID + ".startup_settings");
            cfgMgr.registerServerConfig(FTBLibraryServerConfig.CONFIG, MOD_ID + ".server_settings", true, FTBLibraryServerConfig::onChanged);
        }

        FTBLibraryNet.register();
        ModItems.init();

        ExtendableTextColor.addCustomColor("ftb:rainbow", RainbowTextColor.INSTANCE);
    }

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static XRegistryRef<CreativeModeTab> getCreativeModeTab() {
        return ModItems.FTB_LIBRARY_TAB;
    }

    public void serverStarted(MinecraftServer server) {
        KnownServerRegistries.server = KnownServerRegistries.create(server);

        NBTEditResponseHandlers.registerBuiltinHandlers(server.registryAccess());
    }

    public void serverStopped(MinecraftServer server) {
        KnownServerRegistries.server = null;
    }

    public void playerJoined(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("Hello from FTB Library!").withStyle(Style.EMPTY.withColor(RainbowTextColor.INSTANCE)));

        // scheduling this to run a bit later should avoid issues with KnownServerRegistries.server not been init'd yet
        MinecraftServer server = player.level().getServer();
        server.schedule(server.wrapRunnable(() -> {
            if (KnownServerRegistries.server != null) {
                // can be null, e.g. https://github.com/FTBTeam/FTB-Mods-Issues/issues/1387
                Server2PlayNetworking.send(player, new SyncKnownServerRegistriesPacket(KnownServerRegistries.server));
            }
        }));
    }

    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        FTBLibraryCommands.registerCommands(dispatcher, registryAccess, environment);
    }
}
