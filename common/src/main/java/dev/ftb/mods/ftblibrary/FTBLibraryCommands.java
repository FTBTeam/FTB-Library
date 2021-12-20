package dev.ftb.mods.ftblibrary;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.Registries;
import dev.ftb.mods.ftblibrary.net.EditNBTPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class FTBLibraryCommands {
	public static final Map<UUID, CompoundTag> EDITING_NBT = new HashMap<>();

	private interface NBTEditCallback {
		void accept(CompoundTag info, CompoundTag tag) throws CommandSyntaxException;
	}

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection type) {
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
							context.getSource().getLevel().setWeatherParameters(1000000, 0, !context.getSource().getLevel().isRaining(), false);
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
										.executes(context -> editNBT(context, (info, tag) -> {
											var pos = BlockPosArgument.getSpawnablePos(context, "pos");
											var blockEntity = context.getSource().getLevel().getBlockEntity(pos);

											if (blockEntity == null) {
												return;
											}

											info.putString("type", "block");
											info.putInt("x", pos.getX());
											info.putInt("y", pos.getY());
											info.putInt("z", pos.getZ());
											tag.merge(blockEntity.saveWithFullMetadata());
											tag.remove("x");
											tag.remove("y");
											tag.remove("z");
											info.putString("id", tag.getString("id"));
											tag.remove("id");

											var list = new ListTag();
											addInfo(list, new TextComponent("Class"), new TextComponent(blockEntity.getClass().getName()));
											var key = Registries.getId(blockEntity.getType(), Registry.BLOCK_ENTITY_TYPE_REGISTRY);
											addInfo(list, new TextComponent("ID"), new TextComponent(key == null ? "null" : key.toString()));
											addInfo(list, new TextComponent("Block"), new TextComponent(String.valueOf(Registries.getId(blockEntity.getBlockState().getBlock(), Registry.BLOCK_REGISTRY))));
											addInfo(list, new TextComponent("Block Class"), new TextComponent(blockEntity.getBlockState().getBlock().getClass().getName()));
											addInfo(list, new TextComponent("Position"), new TextComponent("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"));
											addInfo(list, new TextComponent("Mod"), new TextComponent(key == null ? "null" : Platform.getOptionalMod(key.getNamespace()).map(Mod::getName).orElse("Unknown")));
											addInfo(list, new TextComponent("Ticking"), new TextComponent(blockEntity instanceof TickingBlockEntity ? "true" : "false"));
											info.put("text", list);

											var title = blockEntity instanceof Nameable ? ((Nameable) blockEntity).getDisplayName() : null;

											if (title == null) {
												title = new TextComponent(blockEntity.getClass().getSimpleName());
											}

											info.putString("title", Component.Serializer.toJson(title));
										}))
								)
						)
						.then(Commands.literal("entity")
								.then(Commands.argument("entity", EntityArgument.entity())
										.executes(context -> editNBT(context, (info, tag) -> {
											var entity = EntityArgument.getEntity(context, "entity");

											if (entity instanceof Player) {
												return;
											}

											info.putString("type", "entity");
											info.putInt("id", entity.getId());

											entity.save(tag);

											var list = new ListTag();
											addInfo(list, new TextComponent("Class"), new TextComponent(entity.getClass().getName()));
											var key = Registries.getId(entity.getType(), Registry.ENTITY_TYPE_REGISTRY);
											addInfo(list, new TextComponent("ID"), new TextComponent(key == null ? "null" : key.toString()));
											addInfo(list, new TextComponent("Mod"), new TextComponent(key == null ? "null" : Platform.getOptionalMod(key.getNamespace()).map(Mod::getName).orElse("Unknown")));
											info.put("text", list);
											info.putString("title", Component.Serializer.toJson(entity.getDisplayName()));
										}))
								)
						)
						.then(Commands.literal("player")
								.then(Commands.argument("player", EntityArgument.player())
										.executes(context -> editNBT(context, (info, tag) -> {
											var player = EntityArgument.getPlayer(context, "player");

											info.putString("type", "player");
											info.putUUID("id", player.getUUID());

											player.saveWithoutId(tag);
											tag.remove("id");

											var list = new ListTag();
											addInfo(list, new TextComponent("Name"), player.getName());
											addInfo(list, new TextComponent("Display Name"), player.getDisplayName());
											addInfo(list, new TextComponent("UUID"), new TextComponent(player.getUUID().toString()));
											// addInfo(list, new TextComponent("FTB Library Team"), new TextComponent(p.team.getId()));
											info.put("text", list);
											info.putString("title", Component.Serializer.toJson(player.getDisplayName()));
										}))
								)
						)
						.then(Commands.literal("item")
								.executes(context -> editNBT(context, (info, tag) -> {
									var player = context.getSource().getPlayerOrException();
									info.putString("type", "item");
									player.getItemInHand(InteractionHand.MAIN_HAND).save(tag);
								}))
						)
				);

		if (Platform.isDevelopmentEnvironment()) {
			command.then(Commands.literal("test_screen")
					.executes(context -> {
						FTBLibrary.PROXY.testScreen();
						return 1;
					})
			);
		}

		dispatcher.register(command);
	}

	private static void addInfo(ListTag list, Component key, Component value) {
		list.add(StringTag.valueOf(Component.Serializer.toJson(key.copy().withStyle(ChatFormatting.BLUE).append(": ").append(value.copy().withStyle(ChatFormatting.GOLD)))));
	}

	private static int editNBT(CommandContext<CommandSourceStack> context, NBTEditCallback data) throws CommandSyntaxException {
		var player = context.getSource().getPlayerOrException();
		var info = new CompoundTag();
		var tag = new CompoundTag();
		data.accept(info, tag);

		if (!info.isEmpty()) {
			EDITING_NBT.put(player.getUUID(), info);
			new EditNBTPacket(info, tag).sendTo(player);
			return 1;
		}

		return 0;
	}
}