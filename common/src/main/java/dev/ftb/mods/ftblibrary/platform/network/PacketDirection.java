package dev.ftb.mods.ftblibrary.platform.network;

public enum PacketDirection {
    CLIENT_BOUND,
    SERVER_BOUND;

    public boolean isClientBound() {
        return this == CLIENT_BOUND;
    }

    public boolean isServerBound() {
        return this == SERVER_BOUND;
    }
}
