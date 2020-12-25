package com.feed_the_beast.mods.ftbguilibrary.event;

import me.shedaniel.architectury.ExpectPlatform;
import me.shedaniel.architectury.utils.PlatformExpectedError;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public final class EventHandler
{
	private EventHandler()
	{
	}

	@ExpectPlatform
	@Environment(EnvType.CLIENT)
	private void registerRenderTickEvents() {
		throw new PlatformExpectedError();
	}

	@ExpectPlatform
	@Environment(EnvType.CLIENT)
	private void registerGuiInitEvents() {
		throw new PlatformExpectedError();
	}
}
