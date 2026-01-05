package dev.ftb.mods.ftblibrary.config.manager;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.net.SyncConfigFromServerPacket;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public enum ConfigManager {
    INSTANCE;

    final Map<String,TrackedConfig> pendingClient = new HashMap<>();
    final Map<String,TrackedConfig> pendingServer = new HashMap<>();

    // concurrent because startup configs can be loaded in mod construction threads
    private final Map<String, TrackedConfig> trackedConfigs = new ConcurrentHashMap<>();
    private boolean inited = false;

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        if (inited) {
            throw new IllegalStateException("already initialised!");
        }

        LifecycleEvent.SERVER_BEFORE_START.register(this::onServerStarting);
        PlayerEvent.PLAYER_JOIN.register(this::onPlayerLogin);

        inited = true;
    }

    /**
     * Register a client config.
     * @see #registerClientConfig(SNBTConfig, String, BooleanConsumer)
     *
     * @param config the {@link SNBTConfig} object, typically created by {@code SNBTConfig.create()}
     * @param groupPrefix a group prefix for translation purposes; should start with your mod ID
     * @return the same config object
     */
    public SNBTConfig registerClientConfig(SNBTConfig config, String groupPrefix) {
        return registerClientConfig(config, groupPrefix, TrackedConfig.NO_ACTION);
    }

    /**
     * Register a client config. Client configs are loaded on client setup, specifically when the
     * Architectury {@code ClientLifecycleEvent.CLIENT_SETUP} event is fired. This method does not need to be called
     * on the server, though it does not hurt to do so.
     *
     * @param config the {@link SNBTConfig} object, typically statically created by {@code SNBTConfig.create()}
     * @param groupPrefix a group prefix for translation purposes; should start with your mod ID
     * @param onEdited a BooleanConsumer which is called when the config is edited via GUI, or changed via sync from server
     * @return the same config object
     */
    public SNBTConfig registerClientConfig(SNBTConfig config, String groupPrefix, BooleanConsumer onEdited) {
        pendingClient.put(config.key, TrackedConfig.createForRegistration(groupPrefix, ConfigType.CLIENT, config, false, onEdited));
        return config;
    }

    /**
     * @see #registerServerConfig(SNBTConfig, String, boolean, BooleanConsumer)
     *
     * @param config the {@link SNBTConfig} object, typically statically created by {@code SNBTConfig.create()}
     * @param groupPrefix a group prefix for translation purposes; should start with your mod ID
     * @param sync if true, this config is automatically sync'd to clients when players log in
     * @return the same config object
     */
    public SNBTConfig registerServerConfig(SNBTConfig config, String groupPrefix, boolean sync) {
        return registerServerConfig(config, groupPrefix, sync, TrackedConfig.NO_ACTION);
    }

    /**
     * Register a server config. Server configs are loaded on server startup, specifically when the
     * Architectury {@code LifecycleEvent.SERVER_BEFORE_START} event is fired.
     *
     * @param config the {@link SNBTConfig} object, typically created by {@code SNBTConfig.create()}
     * @param groupPrefix a group prefix for translation purposes; should start with your mod ID
     * @param sync if true, this config is automatically sync'd to clients when players log in
     * @param onEdited a BooleanConsumer which is called when the config is changed via sync from client
     *
     * @return the same config object
     */
    public SNBTConfig registerServerConfig(SNBTConfig config, String groupPrefix, boolean sync, BooleanConsumer onEdited) {
        pendingServer.put(config.key, TrackedConfig.createForRegistration(groupPrefix, ConfigType.SERVER, config, sync, onEdited));
        return config;
    }

    /**
     * Register a startup config. Startup configs are loaded immediately, i.e. as soon as this method is called
     * (typically from the mod constructor). Startup configs are loaded on both client and server, not
     * synchronized, and not editable in-game. This should only be used for configuring the setup phase of mods,
     * before the client or server are ready for use.
     *
     * @param config the {@link SNBTConfig} object, typically created by {@code SNBTConfig.create()}
     * @param groupPrefix a group prefix for translation purposes; should start with your mod ID
     */
    public SNBTConfig registerStartupConfig(SNBTConfig config, String groupPrefix) {
        var tc = TrackedConfig.createForRegistration(groupPrefix, ConfigType.SERVER, config, false, TrackedConfig.NO_ACTION);
        findAndLoad(config.key, tc, ConfigUtil.LOCAL_DIR::resolve);
        return config;
    }

    public void save(String key) {
        TrackedConfig tc = trackedConfigs.get(key);
        if (tc == null) {
            throw new IllegalArgumentException("Unknown tracked config: " + key);
        }

        try {
            SNBT.tryWrite(tc.loadedFrom, Util.make(new SNBTCompoundTag(), tc.config::write));
            FTBLibrary.LOGGER.debug("saved config name={} path={}", key, tc.loadedFrom);
        } catch (IOException e) {
            FTBLibrary.LOGGER.error("failed to save config {}: {}", tc, e.getMessage());
        }
    }

    /**
     * Called when a server config is sync'd to the client on player login.
     *
     * @param serverConfigName name of the server config, expected to be registered on client
     * @param tag the config settings
     */
    public void syncFromServer(String serverConfigName, CompoundTag tag) {
        TrackedConfig tc = trackedConfigs.get(serverConfigName);
        if (tc != null) {
            tc.config.read(SNBTCompoundTag.of(tag));
            tc.onEdited.accept(false);
            FTBLibrary.LOGGER.info("received server config settings for config: {}", serverConfigName);
        } else {
            FTBLibrary.LOGGER.error("received unknown config name {} from server!", serverConfigName);
        }
    }

    /**
     * Called when a player applies changes to a server config via GUI.
     *
     * @param serverConfigName name of the server config, expected to be registered on server
     * @param tag the config settings
     * @param playerName player who made the changes
     */
    public void syncFromClient(String serverConfigName, CompoundTag tag, String playerName) {
        TrackedConfig tc = trackedConfigs.get(serverConfigName);
        if (tc != null) {
            tc.config.read(SNBTCompoundTag.of(tag));
            tc.onEdited.accept(true);
            save(serverConfigName);
            FTBLibrary.LOGGER.info("received client config settings from {} for config: {}", playerName, serverConfigName);
        } else {
            FTBLibrary.LOGGER.error("player {} tried to sync an unknown config {} from client!", playerName, serverConfigName);
        }
    }

    private void onServerStarting(MinecraftServer server) {
        pendingServer.forEach((key, config) ->
                findAndLoad(key, config, fileName -> server.getWorldPath(ConfigUtil.SERVER_CONFIG_DIR).resolve(fileName))
        );
        pendingServer.clear();
    }

    private void onPlayerLogin(ServerPlayer serverPlayer) {
        trackedConfigs.forEach((name, tc) -> {
            if (tc.synced) {
                NetworkHelper.sendTo(serverPlayer, SyncConfigFromServerPacket.create(tc.config));
            }
        });
    }

    void findAndLoad(String key, TrackedConfig protoTc, Function<String, Path> overridePathSupplier) {
        String fileName = key + ".snbt";

        Path primaryPath = ConfigUtil.CONFIG_DIR.resolve(fileName);
        Path overridePath = overridePathSupplier.apply(fileName);

        if (!Files.exists(primaryPath) && Files.exists(overridePath)) {
            // likely to happen when first migrating to new config standard; no file in the .../config/ directory yet
            try {
                Files.move(overridePath, primaryPath);
                FTBLibrary.LOGGER.info("config migration: moved {} to {}", overridePath, primaryPath);
            } catch (IOException e) {
                FTBLibrary.LOGGER.error("can't move {} to {}: {}", overridePath, primaryPath, e.getMessage());
            }
        } else {
            checkForIdenticalConfigFiles(key, primaryPath, overridePath);
        }

        if (Files.exists(overridePath)) {
            // an override exists in .../<world>/serverconfig/<name>.snbt, use that
            protoTc.config.load(overridePath);
            track(key, protoTc.promoteToFull(overridePath));
        } else {
            // no override; just load the config from the primary config path: .../config/<name>.snbt
            protoTc.config.load(primaryPath);
            track(key, protoTc.promoteToFull(primaryPath));
        }
    }

    private static void checkForIdenticalConfigFiles(String key, Path primaryPath, Path overridePath) {
        try {
            if (Files.exists(primaryPath) && Files.exists(overridePath) && Files.mismatch(primaryPath, overridePath) == -1L) {
                FTBLibrary.LOGGER.info("{} and {} are identical; deleting {}", primaryPath, overridePath, overridePath);
                Files.delete(overridePath);
            }
        } catch (IOException e) {
            FTBLibrary.LOGGER.error("Caught exception while examining configs for {}: {}", key, e.getMessage());
        }
    }

    void track(String key, TrackedConfig trackedConfig) {
        trackedConfigs.put(key, trackedConfig);
        FTBLibrary.LOGGER.debug("tracking config {}, loaded from {}", key, trackedConfig.loadedFrom);
    }

    public void editedOnClient(String key) {
        TrackedConfig tc = trackedConfigs.get(key);
        if (tc != null) {
            tc.onEdited.accept(false);
        }
    }

    /**
     * Create an editable ConfigGroup for this config, suitable for passing to EditConfigScreen.
     *
     * @param configName the config name, as previously registered
     * @return a ConfigGroup object, or {@code Optional.empty()} if the config is not known or is a startup config
     */
    public Optional<ConfigGroup> createConfigGroup(String configName) {
        TrackedConfig tc = trackedConfigs.get(configName);
        return tc != null && tc.configType != ConfigType.STARTUP ?
                Optional.of(ConfigUtil.makeConfigEditGroup(tc.config, tc.groupPrefix, tc.configType == ConfigType.SERVER)) :
                Optional.empty();
    }

    /**
     * A config plus information about its file path, sync status etc.
     *
     * @param loadedFrom path the config has been loaded from
     * @param config     the config itself
     * @param synced     true if a server config which is sync'd to clients, false otherwise
     * @param onEdited   a BooleanConsumer called when config has been edited;
     *                   receives true if server-side (i.e. config received from client after GUI editing),
     *                   false if client-side (i.e. config has just been edited via GUI)
     */
    record TrackedConfig(Path loadedFrom, ConfigType configType, SNBTConfig config, boolean synced, BooleanConsumer onEdited, String groupPrefix) {
        static final BooleanConsumer NO_ACTION = isServer -> {};

        /**
         * Created client-side for server configs which are sync'd to the client. No file path since server configs
         * are never saved to disk on the client.
         *
         * @return the client-side mirror of a server config
         */
        TrackedConfig clientMirrorOfServerConfig() {
            return new TrackedConfig(null, ConfigType.SERVER, config, false, onEdited, groupPrefix);
        }

        /**
         * Proto-tracked-config created during registration, before the actual file path is known.
         * @param config the config object
         * @param sync true if server config that is sync'd to client
         * @param onChanged called when config is changed
         * @return a new proto-tracked-config object
         */
        static TrackedConfig createForRegistration(String groupPrefix, ConfigType configType, SNBTConfig config, boolean sync, BooleanConsumer onChanged) {
            return new TrackedConfig(null, configType, config, sync, onChanged, groupPrefix);
        }

        /**
         * Promotes a proto-tracked-config to a full one when the config is actually loaded from disk.
         *
         * @param path the load path
         * @return a new, full, tracked config
         */
        TrackedConfig promoteToFull(Path path) {
            return new TrackedConfig(path, configType, config, synced, onEdited, groupPrefix);
        }
    }

    enum ConfigType {
        SERVER,
        CLIENT,
        STARTUP
    }
}
