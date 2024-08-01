package dev.ftb.mods.ftblibrary;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.net.EditConfigPacket;
import dev.ftb.mods.ftblibrary.net.EditNBTPacket;
import dev.ftb.mods.ftblibrary.ui.misc.UITesting;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class FTBLibraryCommands {
	public static final Map<UUID, CompoundTag> EDITING_NBT = new HashMap<>();

	private interface NBTEditCallback {
		void accept(CompoundTag info, CompoundTag tag) throws CommandSyntaxException;
	}

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection type) {
		var command = Commands.literal("ftblibrary")
				.requires(commandSource -> commandSource.hasPermission(2))
				.then(Commands.literal("gamemode")
						.executes(context -> {
							if (!context.getSource().getPlayerOrException().isCreative()) {
								context.getSource().getPlayerOrException().setGameMode(GameType.CREATIVE);
							} else {
								context.getSource().getPlayerOrException().setGameMode(GameType.SURVIVAL);
							}

							return 1;
						})
				)
				.then(Commands.literal("rain")
						.executes(context -> {
							if (context.getSource().getLevel().isRaining()) {
								context.getSource().getLevel().setWeatherParameters(6000, 0, false, false); // clear
							} else {
								context.getSource().getLevel().setWeatherParameters(0, 6000, true, false);// rain
							}
							return 1;
						})
				)
				.then(Commands.literal("day")
						.executes(context -> {
							var addDay = (24000L - (context.getSource().getLevel().getDayTime() % 24000L) + 6000L) % 24000L;

							if (addDay != 0L) {
								for (var world : context.getSource().getServer().getAllLevels()) {
									world.setDayTime(world.getDayTime() + addDay);
								}
							}

							return 1;
						})
				)
				.then(Commands.literal("night")
						.executes(context -> {
							var addDay = (24000L - (context.getSource().getLevel().getDayTime() % 24000L) + 18000L) % 24000L;

							if (addDay != 0L) {
								for (var world : context.getSource().getServer().getAllLevels()) {
									world.setDayTime(world.getDayTime() + addDay);
								}
							}

							return 1;
						})
				)
				.then(Commands.literal("nbtedit")
						.then(Commands.literal("block")
								.then(Commands.argument("pos", BlockPosArgument.blockPos())
										.executes(context -> editNBT(context, (info, tag) -> editBlockNBT(context, info, tag)))
								)
						)
						.then(Commands.literal("entity")
								.then(Commands.argument("entity", EntityArgument.entity())
										.executes(context -> editNBT(context, (info, tag) -> editEntityNBT(context, info, tag)))
								)
						)
						.then(Commands.literal("player")
								.then(Commands.argument("player", EntityArgument.player())
										.executes(context -> editNBT(context, (info, tag) -> editPlayerNBT(context, info, tag)))
								)
						)
						.then(Commands.literal("item")
								.executes(context -> editNBT(context, (info, tag) -> editItemNBT(context, info, tag)))
						)
				)
				.then(Commands.literal("clientconfig")
						.requires(CommandSourceStack::isPlayer)
						.executes(context -> {
							NetworkManager.sendToPlayer(context.getSource().getPlayerOrException(), new EditConfigPacket(true));
							return 1;
						})
				);

		if (Platform.isDevelopmentEnvironment()) {
			command.then(Commands.literal("test_screen")
					.executes(context -> {
						if (context.getSource().getServer().isDedicatedServer()) {
							context.getSource().sendFailure(Component.literal("Can't do this on dedicated server!").withStyle(ChatFormatting.RED));
						} else {
							UITesting.openTestScreen();
						}
						return 1;
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
			return 1;
		}

		return 0;
	}

	private static void editItemNBT(CommandContext<CommandSourceStack> context, CompoundTag info, CompoundTag tag) throws CommandSyntaxException {
		var player = context.getSource().getPlayerOrException();
		info.putString("type", "item");
		Tag res = player.getItemInHand(InteractionHand.MAIN_HAND).save(player.level().registryAccess(), tag);
		if (res instanceof  CompoundTag t) tag.merge(t);
	}

	private static void editPlayerNBT(CommandContext<CommandSourceStack> context, CompoundTag info, CompoundTag tag) throws CommandSyntaxException {
		var player = EntityArgument.getPlayer(context, "player");

		info.putString("type", "player");
		info.putUUID("id", player.getUUID());

		player.saveWithoutId(tag);
		tag.remove("id");

		info.put("text", InfoBuilder.create(context)
				.add(Component.literal("Name"), player.getName())
				.add(Component.literal("Display Name"), player.getDisplayName())
				.add(Component.literal("UUID"), Component.literal(player.getUUID().toString()))
				.build());
		info.putString("title", Component.Serializer.toJson(player.getDisplayName(), player.level().registryAccess()));
	}

	private static void editEntityNBT(CommandContext<CommandSourceStack> context, CompoundTag info, CompoundTag tag) throws CommandSyntaxException {
		var entity = EntityArgument.getEntity(context, "entity");

		if (entity instanceof Player) {
			return;
		}

		info.putString("type", "entity");
		info.putInt("id", entity.getId());

		entity.save(tag);

		var key = RegistrarManager.getId(entity.getType(), Registries.ENTITY_TYPE);
		info.put("text", InfoBuilder.create(context)
				.add(Component.literal("Class"), Component.literal(entity.getClass().getName()))
				.add(Component.literal("ID"), Component.literal(key == null ? "null" : key.toString()))
				.add(Component.literal("Mod"), Component.literal(key == null ? "null" : Platform.getOptionalMod(key.getNamespace()).map(Mod::getName).orElse("Unknown")))
				.build());
		info.putString("title", Component.Serializer.toJson(entity.getDisplayName(), entity.level().registryAccess()));
	}

	private static void editBlockNBT(CommandContext<CommandSourceStack> context, CompoundTag info, CompoundTag tag) throws CommandSyntaxException {
		var pos = BlockPosArgument.getSpawnablePos(context, "pos");
		var blockEntity = context.getSource().getLevel().getBlockEntity(pos);

		if (blockEntity == null) {
			return;
		}

		info.putString("type", "block");
		info.putInt("x", pos.getX());
		info.putInt("y", pos.getY());
		info.putInt("z", pos.getZ());
		tag.merge(blockEntity.saveWithFullMetadata(context.getSource().getLevel().registryAccess()));
		tag.remove("x");
		tag.remove("y");
		tag.remove("z");
		info.putString("id", tag.getString("id"));
		tag.remove("id");

		var key = RegistrarManager.getId(blockEntity.getType(), Registries.BLOCK_ENTITY_TYPE);
		info.put("text", InfoBuilder.create(context)
				.add(Component.literal("Class"), Component.literal(blockEntity.getClass().getName()))
				.add(Component.literal("ID"), Component.literal(key == null ? "null" : key.toString()))
				.add(Component.literal("Block"), Component.literal(String.valueOf(RegistrarManager.getId(blockEntity.getBlockState().getBlock(), Registries.BLOCK))))
				.add(Component.literal("Block Class"), Component.literal(blockEntity.getBlockState().getBlock().getClass().getName()))
				.add(Component.literal("Position"), Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"))
				.add(Component.literal("Mod"), Component.literal(key == null ? "null" : Platform.getOptionalMod(key.getNamespace()).map(Mod::getName).orElse("Unknown")))
				.add(Component.literal("Ticking"), Component.literal(blockEntity instanceof TickingBlockEntity ? "true" : "false"))
				.build());

		var title = blockEntity instanceof Nameable ? ((Nameable) blockEntity).getDisplayName() : null;

		if (title == null) {
			title = Component.literal(blockEntity.getClass().getSimpleName());
		}

		info.putString("title", Component.Serializer.toJson(title, context.getSource().registryAccess()));
	}

	private record InfoBuilder(ListTag list, HolderLookup.Provider provider) {
		static InfoBuilder create(CommandContext<CommandSourceStack> context) {
			return new InfoBuilder(new ListTag(), context.getSource().registryAccess());
		}

		private InfoBuilder add(Component key, Component value) {
			list.add(StringTag.valueOf(Component.Serializer.toJson(
							key.copy().withStyle(ChatFormatting.BLUE).append(": ").append(value.copy().withStyle(ChatFormatting.GOLD)),
							provider)
					)
			);
			return this;
		}

		private ListTag build() {
			return list;
		}
	}
}
