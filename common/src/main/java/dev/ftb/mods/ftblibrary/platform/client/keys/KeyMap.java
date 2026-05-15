package dev.ftb.mods.ftblibrary.platform.client.keys;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public interface KeyMap {
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
    KeyMapping createKeyBinding(KeyMappingConfig config);

    /// Get a displayable string for the key and possible modifiers for a {@code KeyMapping} object
    ///
    /// @param keyMapping the keymapping object
    Component getKeyMappingDisplayName(KeyMapping keyMapping);

    boolean matches(KeyMapping mapping, KeyEvent event);
}
