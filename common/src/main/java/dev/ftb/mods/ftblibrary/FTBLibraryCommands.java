package dev.ftb.mods.ftblibrary;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.config.FTBLibraryServerConfig;
import dev.ftb.mods.ftblibrary.nbtedit.NBTEditResponseHandlers;
import dev.ftb.mods.ftblibrary.net.EditConfigPacket;
import dev.ftb.mods.ftblibrary.net.EditNBTPacket;
import dev.ftb.mods.ftblibrary.ui.misc.UITesting;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

public class FTBLibraryCommands {
    public static final Map<UUID, CompoundTag> EDITING_NBT = new HashMap<>();

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignoredCtx, Commands.CommandSelection ignoredType) {
        var command = literal(FTBLibrary.MOD_ID)
                .then(literal("gamemode")
                        .requires(commandSource -> commandSource.hasPermission(Commands.LEVEL_GAMEMASTERS))
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
                        .requires(commandSource -> commandSource.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> {
                            //Use overworld as that controls the weather for the whole server
                            if (context.getSource().getServer().overworld().isRaining()) {
                                context.getSource().getServer().overworld().setWeatherParameters(6000, 0, false, false); // clear
                            } else {
                                context.getSource().getServer().overworld().setWeatherParameters(0, 6000, true, false);// rain
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("day")
                        .requires(commandSource -> commandSource.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> {
                            for (var world : context.getSource().getServer().getAllLevels()) {
                                world.setDayTime(6000L);
                            }
                            context.getSource().getServer().forceTimeSynchronization();
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("night")
                        .requires(commandSource -> commandSource.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> {
                            for (var world : context.getSource().getServer().getAllLevels()) {
                                world.setDayTime(18000L);
                            }
                            context.getSource().getServer().forceTimeSynchronization();
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("nbtedit")
                        .requires(commandSource -> commandSource.hasPermission(Commands.LEVEL_GAMEMASTERS))
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
                            NetworkManager.sendToPlayer(context.getSource().getPlayerOrException(), new EditConfigPacket(FTBLibraryClientConfig.KEY));
                            return Command.SINGLE_SUCCESS;
                        })
                );

        if (ModUtils.isDevMode()) {
            command.then(literal("test_screen")
                    .executes(context -> {
                        if (context.getSource().getServer().isDedicatedServer()) {
                            context.getSource().sendFailure(Component.literal("Can't do this on dedicated server!").withStyle(ChatFormatting.RED));
                        } else {
                            UITesting.openTestScreen();
                        }
                        return Command.SINGLE_SUCCESS;
                    })
            );
            command.then(literal("serverconfig")
                    .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(context -> {
                        NetworkManager.sendToPlayer(context.getSource().getPlayerOrException(), new EditConfigPacket(FTBLibraryServerConfig.KEY));
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
            NetworkManager.sendToPlayer(player, new EditNBTPacket(info, tag));
            return Command.SINGLE_SUCCESS;
        }

        return 0;
    }

    private static void editItemNBT(CommandContext<CommandSourceStack> context, CompoundTag info, CompoundTag tag) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        if (player.getMainHandItem().isEmpty()) {
            return;
        }
        info.putString("type", NBTEditResponseHandlers.ITEM);
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        var key = RegistrarManager.getId(stack.getItem(), Registries.ITEM);
        info.put("text", InfoBuilder.create(context)
                .add("Class", Component.literal(stack.getItem().getClass().getName()))
                .add("ID", Component.literal(key == null ? "null" : key.toString()))
                .add("Mod", Component.literal(key == null ? "null" : Platform.getOptionalMod(key.getNamespace()).map(Mod::getName).orElse("Unknown")))
                .build());
        Tag res = stack.save(player.level().registryAccess(), tag);
        if (res instanceof CompoundTag t) tag.merge(t);
    }

    private static void editPlayerNBT(CommandContext<CommandSourceStack> context, CompoundTag info, CompoundTag tag) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "player");

        info.putString("type", NBTEditResponseHandlers.PLAYER);
        info.putUUID("id", player.getUUID());

        player.saveWithoutId(tag);
        tag.remove("id");

        info.put("text", InfoBuilder.create(context)
                .add("Name", player.getName())
                .add("Display Name", player.getDisplayName())
                .add("UUID", Component.literal(player.getUUID().toString()))
                .build());
        info.putString("title", Component.Serializer.toJson(player.getDisplayName(), player.level().registryAccess()));
    }

    private static void editEntityNBT(CommandContext<CommandSourceStack> context, CompoundTag info, CompoundTag tag) throws CommandSyntaxException {
        var entity = EntityArgument.getEntity(context, "entity");

        if (entity instanceof Player) {
            return;
        }

        info.putString("type", NBTEditResponseHandlers.ENTITY);
        info.putInt("id", entity.getId());

        entity.save(tag);

        var key = RegistrarManager.getId(entity.getType(), Registries.ENTITY_TYPE);
        info.put("text", InfoBuilder.create(context)
                .add("Class", Component.literal(entity.getClass().getName()))
                .add("ID", Component.literal(key == null ? "null" : key.toString()))
                .add("Mod", Component.literal(key == null ? "null" : Platform.getOptionalMod(key.getNamespace()).map(Mod::getName).orElse("Unknown")))
                .build());
        info.putString("title", Component.Serializer.toJson(entity.getDisplayName(), entity.level().registryAccess()));
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
        info.put("pos", NbtUtils.writeBlockPos(pos));
        tag.merge(blockEntity.saveWithFullMetadata(context.getSource().getLevel().registryAccess()));
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
        info.putString("id", tag.getString("id"));
        tag.remove("id");

        var key = RegistrarManager.getId(blockEntity.getType(), Registries.BLOCK_ENTITY_TYPE);
        info.put("text", InfoBuilder.create(context)
                .add("Class", Component.literal(blockEntity.getClass().getName()))
                .add("ID", Component.literal(key == null ? "null" : key.toString()))
                .add("Block", Component.literal(String.valueOf(RegistrarManager.getId(blockEntity.getBlockState().getBlock(), Registries.BLOCK))))
                .add("Block Class", Component.literal(blockEntity.getBlockState().getBlock().getClass().getName()))
                .add("Position", Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"))
                .add("Mod", Component.literal(key == null ? "null" : Platform.getOptionalMod(key.getNamespace()).map(Mod::getName).orElse("Unknown")))
                .add("Ticking", Component.literal(isTicking(blockEntity) ? "true" : "false"))
                .build());

        var title = blockEntity instanceof Nameable n ? n.getDisplayName() : Component.literal(blockEntity.getClass().getSimpleName());
        info.putString("title", Component.Serializer.toJson(title, context.getSource().registryAccess()));
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
            list.add(StringTag.valueOf(Component.Serializer.toJson(
                            key.copy().withStyle(ChatFormatting.BLUE).append(": ").append(value.copy().withStyle(ChatFormatting.GOLD)),
                            provider)
                    )
            );
            return this;
        }

        public ListTag build() {
            return list;
        }
    }
}
