package dev.ftb.mods.ftblibrary;

import com.google.gson.Gson;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
											addInfo(list, Component.literal("Class"), Component.literal(blockEntity.getClass().getName()));
											var key = Registries.getId(blockEntity.getType(), Registry.BLOCK_ENTITY_TYPE_REGISTRY);
											addInfo(list, Component.literal("ID"), Component.literal(key == null ? "null" : key.toString()));
											addInfo(list, Component.literal("Block"), Component.literal(String.valueOf(Registries.getId(blockEntity.getBlockState().getBlock(), Registry.BLOCK_REGISTRY))));
											addInfo(list, Component.literal("Block Class"), Component.literal(blockEntity.getBlockState().getBlock().getClass().getName()));
											addInfo(list, Component.literal("Position"), Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"));
											addInfo(list, Component.literal("Mod"), Component.literal(key == null ? "null" : Platform.getOptionalMod(key.getNamespace()).map(Mod::getName).orElse("Unknown")));
											addInfo(list, Component.literal("Ticking"), Component.literal(blockEntity instanceof TickingBlockEntity ? "true" : "false"));
											info.put("text", list);

											var title = blockEntity instanceof Nameable ? ((Nameable) blockEntity).getDisplayName() : null;

											if (title == null) {
												title = Component.literal(blockEntity.getClass().getSimpleName());
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
											addInfo(list, Component.literal("Class"), Component.literal(entity.getClass().getName()));
											var key = Registries.getId(entity.getType(), Registry.ENTITY_TYPE_REGISTRY);
											addInfo(list, Component.literal("ID"), Component.literal(key == null ? "null" : key.toString()));
											addInfo(list, Component.literal("Mod"), Component.literal(key == null ? "null" : Platform.getOptionalMod(key.getNamespace()).map(Mod::getName).orElse("Unknown")));
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
											addInfo(list, Component.literal("Name"), player.getName());
											addInfo(list, Component.literal("Display Name"), player.getDisplayName());
											addInfo(list, Component.literal("UUID"), Component.literal(player.getUUID().toString()));
											// addInfo(list, Component.literal("FTB Library Team"), Component.literal(p.team.getId()));
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
				).then(Commands.literal("generate_loot_tables").executes(FTBLibraryCommands::generateLootTables));

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

	private static int generateLootTables(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();

		var player = source.getPlayerOrException();
		HitResult pick = player.pick(30, 1.0F, true);
		if (pick.getType() != HitResult.Type.BLOCK) {
			source.sendFailure(Component.literal("You must be facing a valid block"));
			return 0;
		}

		BlockHitResult trace = (BlockHitResult) pick;
		var level = source.getLevel();

		var blockEntity = level.getBlockEntity(trace.getBlockPos());
		if (!(blockEntity instanceof ChestBlockEntity) && !(blockEntity instanceof BarrelBlockEntity)) {
			source.sendFailure(Component.literal("You must be facing a chest or barrel"));
			return 0;
		}

		var chest = ((RandomizableContainerBlockEntity) blockEntity);
		var items = new ArrayList<ItemStack>();
		for (int i = 0; i < chest.getContainerSize(); i ++) {
			var item = chest.getItem(i);
			if (!item.isEmpty()) {
				items.add(item);
			}
		}

		try {
			var tablePool = LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F));

			for (ItemStack e : items) {
				LootPoolSingletonContainer.Builder<?> itemBuilder = LootItem.lootTableItem(e.getItem()).setWeight(1);

				// If there is more than one item in the slot, use it to generate a possibility range
				if (e.getCount() > 1) {
					itemBuilder.apply(SetItemCountFunction.setCount(UniformGenerator.between(0, e.getCount())));
				}

				CompoundTag itemTag = e.getOrCreateTag();
				// If you're copying then we don't need to do anything special
				if (itemTag.contains("copy") || e.getItem() instanceof EnchantedBookItem || (e.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock)) {
					itemBuilder.apply(SetNbtFunction.setTag(itemTag));
				} else {
					// If the item is enchanted, support random, copy and leveled random
					if (e.isEnchanted()) {
						if (itemTag.contains("level")) {
							String range = itemTag.getString("level");
							if (range.contains(",")) {
								String[] split = range.split(",");
								itemBuilder.apply(EnchantWithLevelsFunction.enchantWithLevels(UniformGenerator.between(Float.parseFloat(split[0]), Float.parseFloat(split[1]))));
							} else {
								itemBuilder.apply(EnchantWithLevelsFunction.enchantWithLevels(UniformGenerator.between(0, Float.parseFloat(range))));
							}
						} else if (itemTag.contains("set")) {
							SetEnchantmentsFunction.Builder enchantBuilder = new SetEnchantmentsFunction.Builder();
							EnchantmentHelper.getEnchantments(e).forEach((enchant, l) -> enchantBuilder.withEnchantment(enchant, ConstantValue.exactly(l)));
							itemBuilder.apply(enchantBuilder);
						} else {
							itemBuilder.apply(EnchantRandomlyFunction.randomApplicableEnchantment());
						}
					}
				}

				tablePool.add(itemBuilder);
			}

			// Use the loot table serializer to generate the loot table output json
			var lootTable = LootTable.lootTable().withPool(tablePool);
			Gson gson = Deserializers.createLootTableSerializer()
					.setPrettyPrinting()
					.create();

			String output = gson.toJson(lootTable.build());

			// Put into the moddata path in the servers root
			Path path = source.getServer().getServerDirectory().toPath();

			// If the chest is named and contains a / we'll infer it's output path based on it.
			Path outputDir = path.resolve("moddata/ftb-library/generated/");
			String outputFileName = "loot-" + (blockEntity instanceof ChestBlockEntity ? "chest" : "barrel") + "-" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + ".json";

			Component customName = chest.getCustomName();
			if (customName != null && customName.getString().contains("/") && !customName.getString().contains("..")) {
				String chestPathName = customName.getString();
				if (chestPathName.chars().filter(c -> c == '/').count() == 2) {
					// [0] == modname, [1] == type of loot (chests etc), [2] == file name
					String[] pathParts = chestPathName.split("/");
					outputFileName = String.format("%s.json", pathParts[2]);
					outputDir = path.resolve(String.format("kubejs/%s/loot_tables/%s/", pathParts[0], pathParts[1]));
				}
			}

			if (!Files.exists(outputDir)) {
				Files.createDirectories(outputDir);
			}

			Files.writeString(outputDir.resolve(outputFileName), output);
			source.sendSuccess(Component.literal("Loot table stored at " + outputDir.resolve(outputFileName).toString().replace(path.toAbsolutePath().toString(), "")), true);
		} catch (Exception e) {
			source.sendFailure(Component.literal("Something went wrong, check the logs"));
			FTBLibrary.LOGGER.error(e);
			return 0;
		}
		return 1;
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
