package dev.ftb.mods.ftblibrary;

import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.config.FTBLibraryServerConfig;
import dev.ftb.mods.ftblibrary.nbtedit.NBTEditResponseHandlers;
import dev.ftb.mods.ftblibrary.net.EditConfigPacket;
import dev.ftb.mods.ftblibrary.net.EditNBTPacket;
import dev.ftb.mods.ftblibrary.net.OpenTestScreenPacket;
import dev.ftb.mods.ftblibrary.platform.Mod;
import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.RegistryHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

public class FTBLibraryCommands {
    public static final Map<UUID, CompoundTag> EDITING_NBT = new HashMap<>();

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignoredCtx, Commands.CommandSelection ignoredType) {
        var command = literal(FTBLibrary.MOD_ID)
                .then(literal("gamemode")
                        .requires(commandSource -> commandSource.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(context -> {
                            if (!context.getSource().getPlayerOrException().isCreative()) {
                                context.getSource().getPlayerOrException().setGameMode(GameType.CREATIVE);
                            } else {
                                context.getSource().getPlayerOrException().setGameMode(GameType.SURVIVAL);
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("rain")
                        .requires(commandSource -> commandSource.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            level.setRainLevel(level.isRaining() ? 0F : 1F);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("nbtedit")
                        .requires(commandSource -> commandSource.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(literal("block")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> editNBT(context, (info, tag) -> editBlockNBT(context, info, tag)))
                                )
                        )
                        .then(literal("entity")
                                .then(Commands.argument("entity", EntityArgument.entity())
                                        .executes(context -> editNBT(context, (info, tag) -> editEntityNBT(context, info, tag)))
                                )
                        )
                        .then(literal("player")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> editNBT(context, (info, tag) -> editPlayerNBT(context, info, tag)))
                                )
                        )
                        .then(literal("item")
                                .executes(context -> editNBT(context, (info, tag) -> editItemNBT(context, info, tag)))
                        )
                )
                .then(literal("clientconfig")
                        .requires(CommandSourceStack::isPlayer)
                        .executes(context -> {
                            Server2PlayNetworking.send(context.getSource().getPlayerOrException(), new EditConfigPacket(FTBLibraryClientConfig.KEY));
                            return Command.SINGLE_SUCCESS;
                        })
                );

        if (ModUtils.isDevMode()) {
            command.then(literal("test_screen")
                    .executes(context -> {
                        Server2PlayNetworking.send(context.getSource().getPlayerOrException(), OpenTestScreenPacket.INSTANCE);
                        return Command.SINGLE_SUCCESS;
                    })
            );
            command.then(literal("serverconfig")
                    .requires(cs -> cs.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                    .executes(context -> {
                        Server2PlayNetworking.send(context.getSource().getPlayerOrException(), new EditConfigPacket(FTBLibraryServerConfig.KEY));
                        return Command.SINGLE_SUCCESS;
                    })
            );
        }

        dispatcher.register(command);
    }

    private static int editNBT(CommandContext<CommandSourceStack> context, NBTEditCallback data) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var info = new CompoundTag();
        var tag = new CompoundTag();
        data.accept(info, tag);

        if (!info.isEmpty()) {
            EDITING_NBT.put(player.getUUID(), info);
            Server2PlayNetworking.send(player, new EditNBTPacket(info, tag));
            return Command.SINGLE_SUCCESS;
        }

        return 0;
    }

    private static void editItemNBT(CommandContext<CommandSourceStack> context, CompoundTag info, CompoundTag tag) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        info.putString("type", "item");
        ItemStack.CODEC.encodeStart(player.level().registryAccess().createSerializationContext(NbtOps.INSTANCE), stack)
                .ifSuccess(res -> {
                    if (res instanceof CompoundTag t) tag.merge(t);
                });
        var key = RegistryHelper.getIdentifier(stack.getItem(), Registries.ITEM);
        info.put("text", InfoBuilder.create(context)
                .add("Class", Component.literal(stack.getItem().getClass().getName()))
                .add("ID", Component.literal(key == null ? "null" : key.toString()))
                .add("Mod", Component.literal(key == null ? "null" : Platform.get().getMod(key.getNamespace()).map(Mod::name).orElse("Unknown")))
                .build());
    }

    private static void editPlayerNBT(CommandContext<CommandSourceStack> context, CompoundTag info, CompoundTag tag) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "player");

        info.putString("type", NBTEditResponseHandlers.PLAYER);
        info.store("id", UUIDUtil.CODEC, player.getUUID());

        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.level().registryAccess());
        player.saveWithoutId(output);
        tag.merge(output.buildResult());
        tag.remove("id");

        info.put("text", InfoBuilder.create(context)
                .add("Name", player.getName())
                .add("Display Name", player.getDisplayName())
                .add("UUID", Component.literal(player.getUUID().toString()))
                .build());
        info.putString("title", player.getGameProfile().name());
    }

    private static void editEntityNBT(CommandContext<CommandSourceStack> context, CompoundTag info, CompoundTag tag) throws CommandSyntaxException {
        var entity = EntityArgument.getEntity(context, "entity");

        if (entity instanceof Player) {
            return;
        }

        info.putString("type", NBTEditResponseHandlers.ENTITY);
        info.putInt("id", entity.getId());

        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
        entity.save(output);
        tag.merge(output.buildResult());

        var key = RegistryHelper.getIdentifier(entity.getType(), Registries.ENTITY_TYPE);
        info.put("text", InfoBuilder.create(context)
                .add("Class", Component.literal(entity.getClass().getName()))
                .add("ID", Component.literal(key == null ? "null" : key.toString()))
                .add("Mod", Component.literal(key == null ? "null" : Platform.get().getMod(key.getNamespace()).map(Mod::name).orElse("Unknown")))
                .build());

        String name = entity.getDisplayName() == null ? "?" : entity.getDisplayName().getString();
        info.putString("title", name);
    }

    private static void editBlockNBT(CommandContext<CommandSourceStack> context, CompoundTag info, CompoundTag tag) throws CommandSyntaxException {
        var pos = BlockPosArgument.getSpawnablePos(context, "pos");
        var blockState = context.getSource().getLevel().getBlockState(pos);
        var blockEntity = context.getSource().getLevel().getBlockEntity(pos);

        if (blockEntity == null) {
            context.getSource().sendFailure(Component.literal("Not a block entity: ").append(blockState.getBlock().toString()).withStyle(ChatFormatting.RED));
            return;
        }

        info.putString("type", NBTEditResponseHandlers.BLOCK);
        BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).ifSuccess(nbt -> info.put("pos", nbt));
        tag.merge(blockEntity.saveWithFullMetadata(context.getSource().getLevel().registryAccess()));
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
        info.putString("id", tag.getString("id").orElseThrow());
        tag.remove("id");

        var key = RegistryHelper.getIdentifier(blockEntity.getType(), Registries.BLOCK_ENTITY_TYPE);
        info.put("text", InfoBuilder.create(context)
                .add("Class", Component.literal(blockEntity.getClass().getName()))
                .add("ID", Component.literal(key == null ? "null" : key.toString()))
                .add("Block", Component.literal(String.valueOf(RegistryHelper.getIdentifier(blockEntity.getBlockState().getBlock(), Registries.BLOCK))))
                .add("Block Class", Component.literal(blockEntity.getBlockState().getBlock().getClass().getName()))
                .add("Position", Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"))
                .add("Mod", Component.literal(key == null ? "null" : Platform.get().getMod(key.getNamespace()).map(Mod::name).orElse("Unknown")))
                .add("Ticking", Component.literal(isTicking(blockEntity) ? "true" : "false"))
                .build());

        var title = blockEntity instanceof Nameable n ? n.getDisplayName() : null;
        if (title == null) {
            title = Component.literal(blockEntity.getClass().getSimpleName());
        }
        info.putString("title", title.getString());
    }

    private static boolean isTicking(BlockEntity be) {
        return be.getBlockState().getBlock() instanceof EntityBlock eb && eb.getTicker(be.getLevel(), be.getBlockState(), be.getType()) != null;
    }

    private interface NBTEditCallback {
        void accept(CompoundTag info, CompoundTag tag) throws CommandSyntaxException;
    }

    public record InfoBuilder(ListTag list, HolderLookup.Provider provider) {
        public static InfoBuilder create(CommandContext<CommandSourceStack> context) {
            return new InfoBuilder(new ListTag(), context.getSource().registryAccess());
        }

        public InfoBuilder add(String key, Component value) {
            return add(Component.literal(key), value);
        }

        public InfoBuilder add(Component key, Component value) {
            ComponentSerialization.CODEC.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE),
                            key.copy().withStyle(ChatFormatting.BLUE).append(": ").append(value.copy().withStyle(ChatFormatting.GOLD)))
                    .ifSuccess(json -> list.add(StringTag.valueOf(json.toString())));
            return this;
        }

        public ListTag build() {
            return list;
        }
    }
}
