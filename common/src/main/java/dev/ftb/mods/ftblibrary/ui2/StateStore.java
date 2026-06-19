package dev.ftb.mods.ftblibrary.ui2;

import java.util.HashMap;
import java.util.Map;

/**
 * State system to store and manage the state of UI components.
 *
 * TODO: This needs to be reactive and be able to notify the children components
 */
public class StateStore {
    private final Map<String, Object> stateMap = new HashMap<>();
}
