package dev.ftb.mods.ftblibrary.platform.client.input;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.function.Consumer;

public interface Input {
    /// Register one or more keymappings with vanilla.
    ///
    /// **IMPORTANT**: all keymappings for any single keymapping category _must_ be registered in a single call to this
    /// method, or you will get a "Category is already registered" exception from vanilla. It is fine to call this method
    /// multiple times, as long as the same category isn't passed to more than one invocation of this method.
    ///
    /// @param modId your mod's unique ID
    /// @param keyMappings one or more keymapping objects
    /// @throws IllegalArgumentException if the same category is passed to multiple cals of this method, or if the
    /// method is called with no keymappings
    void registerKeyMapping(String modId, KeyMapping... keyMappings);

    /// Create a `KeyMapping` object in a platform-independent fashion. The returned object may be some subclass
    /// or extension of the vanilla `KeyMapping` object, but can be used as one.
    ///
    /// @param config defines the setup for the keymapping object
    /// @return a new keymapping object
    KeyMapping createKeyMapping(KeyMappingConfig config);

    /// Get a displayable string for the key and possible modifiers for a {@code KeyMapping} object
    ///
    /// @param keyMapping the keymapping object
    Component getKeyMappingDisplayName(KeyMapping keyMapping);

    /// Check if the key event matches the given key mapping. This differs from [KeyMapping#matches(KeyEvent)]
    /// in that modifiers, if supported in the current environment, are also checked.
    ///
    /// @param mapping the KeyMapping to check
    /// @param event the event representing a keypress with possible modifier
    /// @return true if there's a match
    ///
    /// @implNote modifiers are supported if the platform is NeoForge, or if the platform is Fabric with the Amecs
    /// mod installed
    boolean matches(KeyMapping mapping, KeyEvent event);

    @ApiStatus.Internal
    static void registerCategories(Collection<KeyMapping.Category> categories, Consumer<KeyMapping.Category> consumer) {
        for (var c : categories) {
            try {
                consumer.accept(c);
            } catch (IllegalArgumentException e) {
                FTBLibrary.LOGGER.warn("ignoring attempt to register duplicate keymapping category '{}'", c.id());
            }
        }
    }
}
