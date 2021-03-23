package dev.ftb.mods.ftbguilibrary;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;

/**
 * @author LatvianModder
 */
public class FTBGUILibraryCommands {
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection type) {
		dispatcher.register(Commands.literal("ftbguilibrary")
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
							long addDay = (24000L - (context.getSource().getLevel().getDayTime() % 24000L) + 6000L) % 24000L;

							if (addDay != 0L) {
								for (ServerLevel world : context.getSource().getServer().getAllLevels()) {
									world.setDayTime(world.getDayTime() + addDay);
								}
							}

							return 1;
						})
				)
				.then(Commands.literal("night")
						.executes(context -> {
							long addDay = (24000L - (context.getSource().getLevel().getDayTime() % 24000L) + 18000L) % 24000L;

							if (addDay != 0L) {
								for (ServerLevel world : context.getSource().getServer().getAllLevels()) {
									world.setDayTime(world.getDayTime() + addDay);
								}
							}

							return 1;
						})
				)
		);
	}
}