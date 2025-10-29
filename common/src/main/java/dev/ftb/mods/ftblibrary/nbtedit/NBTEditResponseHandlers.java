package dev.ftb.mods.ftblibrary.nbtedit;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public enum NBTEditResponseHandlers {
    INSTANCE;

    public static final String ITEM = "item";
    public static final String BLOCK = "block";
    public static final String PLAYER = "player";
    public static final String ENTITY = "entity";

    private final Map<String, NBTResponseHandler> MAP = new ConcurrentHashMap<>();

    public static void registerBuiltinHandlers() {
        INSTANCE.registerHandler(ITEM, (player, info, data) ->
                ItemStack.parse(player.registryAccess(), data)
                        .ifPresent(stack -> player.setItemInHand(InteractionHand.MAIN_HAND, stack))
        );

        INSTANCE.registerHandler(BLOCK, (player, info, data) -> {
            NbtUtils.readBlockPos(info, "pos").ifPresent(pos -> {
                if (player.level().isLoaded(pos)) {
                    var blockEntity = player.level().getBlockEntity(pos);
                    if (blockEntity != null) {
                        data.putInt("x", pos.getX());
                        data.putInt("y", pos.getY());
                        data.putInt("z", pos.getZ());
                        data.putString("id", info.getString("id"));
                        blockEntity.loadWithComponents(data, player.level().registryAccess());
                        blockEntity.setChanged();
                        player.level().sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_ALL);
                    }
                }
            });
//            var pos = new BlockPos(info.getInt("x"), info.getInt("y"), info.getInt("z"));
        });

        INSTANCE.registerHandler(PLAYER, (player, info, data) -> {
            if (player.getServer() != null) {
                var targetPlayer = player.getServer().getPlayerList().getPlayer(info.getUUID("id"));
                if (targetPlayer != null) {
                    UUID uuid = targetPlayer.getUUID();
                    targetPlayer.load(data);
                    targetPlayer.setUUID(uuid);
                    targetPlayer.moveTo(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
                }
            }
        });

        INSTANCE.registerHandler(ENTITY, (player, info, data) -> {
            var entity = player.level().getEntity(info.getInt("id"));
            if (entity != null) {
                UUID uuid = entity.getUUID();
                entity.load(data);
                entity.setUUID(uuid);
            }
        });
    }

    public void registerHandler(String name, NBTResponseHandler handler) {
        MAP.put(name, handler);
    }

    public void handleResponse(String name, ServerPlayer player, CompoundTag info, CompoundTag data) {
        MAP.getOrDefault(name, NBTResponseHandler.NONE).handleResponse(player, info, data);
    }

    @FunctionalInterface
    public interface NBTResponseHandler {
        void handleResponse(ServerPlayer player, CompoundTag info, CompoundTag data);

        NBTResponseHandler NONE = (player, info, data) -> {};
    }
}
