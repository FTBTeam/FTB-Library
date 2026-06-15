package dev.ftb.mods.ftblibrary.nbtedit;

import com.mojang.brigadier.Command;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.net.EditNBTPacket;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public enum NBTEditResponseHandlers {
    INSTANCE;

    public static final Identifier ITEM = FTBLibrary.id("item");
    public static final Identifier BLOCK = FTBLibrary.id("block");
    public static final Identifier PLAYER = FTBLibrary.id("player");
    public static final Identifier ENTITY = FTBLibrary.id("entity");

    private final Map<Identifier, NBTResponseHandler> MAP = new ConcurrentHashMap<>();
    private final Map<UUID, CompoundTag> currentlyEditing = new HashMap<>();  // players who are currently editing

    public static void registerBuiltinHandlers(HolderLookup.Provider registryAccess) {
        INSTANCE.registerHandler(ITEM, (player, info, data) ->
                ItemStack.CODEC.parse(registryAccess.createSerializationContext(NbtOps.INSTANCE), data)
                        .ifSuccess(stack -> player.setItemInHand(InteractionHand.MAIN_HAND, stack))
        );

        INSTANCE.registerHandler(BLOCK, (player, info, data) -> {
            BlockPos.CODEC.parse(NbtOps.INSTANCE, info.get("pos")).ifSuccess(pos -> {
                if (player.level().isLoaded(pos)) {
                    var blockEntity = player.level().getBlockEntity(pos);
                    if (blockEntity != null) {
                        data.putInt("x", pos.getX());
                        data.putInt("y", pos.getY());
                        data.putInt("z", pos.getZ());
                        data.putString("id", info.getStringOr("id", "UNKNOWN"));
                        blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, blockEntity.getLevel().registryAccess(), data));
                        blockEntity.setChanged();
                        player.level().sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_ALL);
                    }
                }
            });
        });

        INSTANCE.registerHandler(PLAYER, (player, info, data) -> {
            info.read("id", UUIDUtil.CODEC).ifPresent(playerId -> {
                var targetPlayer = player.level().getServer().getPlayerList().getPlayer(playerId);
                if (targetPlayer != null) {
                    UUID uuid = targetPlayer.getUUID();
                    targetPlayer.load(TagValueInput.create(ProblemReporter.DISCARDING, targetPlayer.registryAccess(), data));
                    targetPlayer.setUUID(uuid);
                    targetPlayer.setPos(new Vec3(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ()));
                }
            });
        });

        INSTANCE.registerHandler(ENTITY, (player, info, data) -> {
            var entity = player.level().getEntity(info.getIntOr("id", 0));
            if (entity != null) {
                UUID uuid = entity.getUUID();
                entity.load(TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), data));
                entity.setUUID(uuid);
            }
        });
    }

    public void registerHandler(Identifier name, NBTResponseHandler handler) {
        MAP.put(name, handler);
    }

    public int sendRequestPacket(ServerPlayer player, CompoundTag info, CompoundTag data) {
        if (!info.isEmpty()) {
            currentlyEditing.put(player.getUUID(), info);
            Server2PlayNetworking.send(player, new EditNBTPacket(info, data));
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    public void handleResponse(Identifier name, ServerPlayer player, CompoundTag info, CompoundTag data) {
        if (info.equals(currentlyEditing.remove(player.getUUID()))) {
            MAP.getOrDefault(name, NBTResponseHandler.NONE).handleResponse(player, info, data);
        }
    }

    @FunctionalInterface
    public interface NBTResponseHandler {
        void handleResponse(ServerPlayer player, CompoundTag info, CompoundTag data);

        NBTResponseHandler NONE = (player, info, data) -> {};
    }
}
