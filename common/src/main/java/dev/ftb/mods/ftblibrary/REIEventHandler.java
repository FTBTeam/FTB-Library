package dev.ftb.mods.ftblibrary;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import net.minecraft.client.Minecraft;

public class REIEventHandler {
    private static boolean reiReady;

    public static void init() {
        ClientLifecycleEvent.CLIENT_STARTED.register(REIEventHandler::clientStarted);
    }

    private static void clientStarted(Minecraft minecraft) {
        reiReady = true;
    }

    static boolean isReiReady() {
        return reiReady;
    }
}
