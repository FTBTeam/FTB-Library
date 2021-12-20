package dev.ftb.mods.ftblibrary.util;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.core.DisplayInfoFTBL;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KnownServerRegistries {
	public static class AdvancementInfo {
		public ResourceLocation id;
		public Component name;
		public ItemStack icon;
	}

	public static KnownServerRegistries client;
	public static KnownServerRegistries server;

	public final List<ResourceLocation> dimensions;
	public final Map<ResourceLocation, AdvancementInfo> advancements;

	public KnownServerRegistries(FriendlyByteBuf buffer) {
		{
			var s = buffer.readVarInt();
			dimensions = new ArrayList<>(s);

			for (var i = 0; i < s; i++) {
				dimensions.add(buffer.readResourceLocation());
			}
		}

		{
			var s = buffer.readVarInt();
			advancements = new LinkedHashMap<>(s);

			for (var i = 0; i < s; i++) {
				var info = new AdvancementInfo();
				info.id = buffer.readResourceLocation();
				info.name = buffer.readComponent();
				info.icon = buffer.readItem();
				advancements.put(info.id, info);
			}
		}

		FTBLibrary.LOGGER.debug("Received server registries");
	}

	public KnownServerRegistries(MinecraftServer server) {
		dimensions = new ArrayList<>();

		for (var level : server.getAllLevels()) {
			dimensions.add(level.dimension().location());
		}

		dimensions.sort(null);

		List<AdvancementInfo> advancementList = new ArrayList<>();

		for (var advancement : server.getAdvancements().getAllAdvancements()) {
			if (advancement.getDisplay() instanceof DisplayInfoFTBL) {
				var info = new AdvancementInfo();
				info.id = advancement.getId();
				info.name = advancement.getDisplay().getTitle();
				info.icon = ((DisplayInfoFTBL) advancement.getDisplay()).getIconStackFTBL();
				advancementList.add(info);
			}
		}

		advancementList.sort(Comparator.comparing(o -> o.id));

		advancements = new LinkedHashMap<>(advancementList.size());

		for (var info : advancementList) {
			advancements.put(info.id, info);
		}
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(dimensions.size());

		for (var id : dimensions) {
			buffer.writeResourceLocation(id);
		}

		buffer.writeVarInt(advancements.size());

		for (var info : advancements.values()) {
			buffer.writeResourceLocation(info.id);
			buffer.writeComponent(info.name);
			buffer.writeItem(info.icon);
		}
	}
}
