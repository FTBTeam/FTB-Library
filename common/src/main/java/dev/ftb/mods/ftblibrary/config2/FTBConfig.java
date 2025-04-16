package dev.ftb.mods.ftblibrary.config2;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import dev.architectury.platform.Platform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class FTBConfig extends AbstractValueCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FTBConfig.class);

    private final ResourceLocation id;
    private final ConfigGroup rootGroup = new ConfigGroup(this, "unused");

    public FTBConfig(ResourceLocation id) {
        this.id = id;
    }

    public static FTBConfig create(ResourceLocation id) {
        return new FTBConfig(id);
    }

    public void readFromIo() {
        var configFile = this.configPath();
        if (Files.exists(configFile)) {
            try {
                var inputText = Files.readString(configFile);
                var tag = TagParser.parseCompoundAsArgument(new StringReader(inputText));
                this.rootGroup.fromTag(tag);
            } catch (Exception e) {
                LOGGER.error("Failed to read config file", e);
            }
        } else {
            LOGGER.warn("Config file does not exist: {}", configFile);
        }
    }

    public void writeToIo() {
        // This bit is a bit fun...
        CompoundTag tag = rootGroup.toTag();

        // Now parse the root tag into SNBT
        var outputText = (new SnbtPrinterTagVisitor()).visit(tag);
        try {
            if (Files.notExists(this.configPath().getParent())) {
                Files.createDirectories(this.configPath().getParent());
            }

            Files.writeString(this.configPath(), outputText);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write config file", e);
        }
    }

    /**
     * TOOD: Support overriding the config path
     */
    public Path configPath() {
        return Path.of("/Users/michael/Dev/dev.ftb/mods/FTB-Library/build/").resolve("config/").resolve(this.id.getNamespace() + "/" + this.id.getPath() + ".snbt");
//        return Platform.getConfigFolder().resolve(this.id.getNamespace()).resolve(this.id.getPath() + ".snbt");
    }

    public Path configOverridePath() {
        // TODO: Figure out what this is actually meant to be.
        return Platform.getConfigFolder().resolve(this.id.getNamespace()).resolve(this.id.getPath() + ".snbt");
    }

    @Override
    <T> ConfigValue<T> addValue(String key, T value, Codec<T> codec) {
        return this.rootGroup.addValue(key, value, codec);
    }

    public ConfigGroup group(String key) {
        return this.rootGroup.group(key);
    }
}
