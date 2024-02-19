package dev.ftb.mods.ftblibrary.config.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SearchModeIndex<T extends ResourceSearchMode<?>> {
    private final List<T> modes = new ArrayList<>();
    private int modeIdx = 0;

    public void appendMode(T mode) {
        modes.add(mode);
    }

    public void prependMode(T mode) {
        modes.add(0, mode);
    }

    public Optional<T> getCurrentSearchMode() {
        return modeIdx >= 0 && modeIdx < modes.size() ? Optional.of(modes.get(modeIdx)) : Optional.empty();
    }

    public void nextMode() {
        if (!modes.isEmpty()) {
            modeIdx = (modeIdx + 1) % modes.size();
        }
    }
}
