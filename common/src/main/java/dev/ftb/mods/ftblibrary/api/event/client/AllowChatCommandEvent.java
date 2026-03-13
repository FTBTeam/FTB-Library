package dev.ftb.mods.ftblibrary.api.event.client;

@FunctionalInterface
public interface AllowChatCommandEvent {
    void send(Data data);

    record Data(String message) {
    }
}
