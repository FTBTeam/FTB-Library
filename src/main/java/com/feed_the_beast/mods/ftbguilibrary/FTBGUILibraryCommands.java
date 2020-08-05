package com.feed_the_beast.mods.ftbguilibrary;

import net.minecraft.command.Commands;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBGUILibrary.MOD_ID)
public class FTBGUILibraryCommands
{
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event)
	{
		event.getDispatcher().register(Commands.literal("ftbguilibrary")
				.requires(commandSource -> commandSource.hasPermissionLevel(2))
				.then(Commands.literal("gamemode")
						.executes(context -> {
							if (!context.getSource().asPlayer().isCreative())
							{
								context.getSource().asPlayer().setGameType(GameType.CREATIVE);
							}
							else
							{
								context.getSource().asPlayer().setGameType(GameType.SURVIVAL);
							}

							return 1;
						})
				)
				.then(Commands.literal("rain")
						.executes(context -> {
							context.getSource().getWorld().func_241113_a_(1000000, 0, !context.getSource().getWorld().isRaining(), false);
							return 1;
						})
				)
				.then(Commands.literal("day")
						.executes(context -> {
							long addDay = (24000L - (context.getSource().getWorld().getDayTime() % 24000L) + 6000L) % 24000L;

							if (addDay != 0L)
							{
								for (ServerWorld world : context.getSource().getServer().getWorlds())
								{
									world.func_241114_a_(world.getDayTime() + addDay);
								}
							}

							return 1;
						})
				)
				.then(Commands.literal("night")
						.executes(context -> {
							long addDay = (24000L - (context.getSource().getWorld().getDayTime() % 24000L) + 18000L) % 24000L;

							if (addDay != 0L)
							{
								for (ServerWorld world : context.getSource().getServer().getWorlds())
								{
									world.func_241114_a_(world.getDayTime() + addDay);
								}
							}

							return 1;
						})
				)
		);
	}
}