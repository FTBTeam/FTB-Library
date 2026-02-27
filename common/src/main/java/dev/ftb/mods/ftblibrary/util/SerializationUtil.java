package dev.ftb.mods.ftblibrary.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.Optional;

public class SerializationUtil {
    public static Optional<String> serializeComponent(Component component, HolderLookup.Provider provider) {
        return ComponentSerialization.CODEC.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), component)
                .result().map(JsonElement::toString);
    }

    public static Optional<Component> deserializeComponent(String string, HolderLookup.Provider provider) {
        try {
            JsonElement json = JsonParser.parseString(string);
            return ComponentSerialization.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), json).result();
        } catch (JsonSyntaxException ex) {
            return Optional.empty();
        }
    }
}
