package dev.ftb.mods.ftblibrary.api.client;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FTBLibraryClientApi {
    private static FTBLibraryClientApi INSTANCE;

    public static FTBLibraryClientApi get() {
        if (INSTANCE == null) {
            INSTANCE = new FTBLibraryClientApi();
        }

        return INSTANCE;
    }


    public Set<String> screenSidebarBlacklist = new HashSet<>();

    private FTBLibraryClientApi() {
    }

    public void addSidebarScreenBlacklist(String ...screenClass) {
        Collections.addAll(screenSidebarBlacklist, screenClass);
    }

    public boolean isSidebarScreenBlacklisted(@Nullable Screen screen) {
        return screen != null && screenSidebarBlacklist.contains(screen.getClass().getName());
    }

    public Set<String> getSidebarBlacklist() {
        return ImmutableSet.copyOf(screenSidebarBlacklist);
    }
}
