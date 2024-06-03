package dev.ftb.mods.ftblibrary.util;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public record KnownServerRegistries(List<ResourceLocation> dimension, Map<ResourceLocation,AdvancementInfo> advancements) {
	public static KnownServerRegistries client;
	public static KnownServerRegistries server;

	public static StreamCodec<RegistryFriendlyByteBuf, KnownServerRegistries> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), KnownServerRegistries::dimension,
			ByteBufCodecs.map(LinkedHashMap::new, ResourceLocation.STREAM_CODEC, AdvancementInfo.STREAM_CODEC), KnownServerRegistries::advancements,
			KnownServerRegistries::new
	);

	public static KnownServerRegistries create(MinecraftServer server) {
		List<ResourceLocation> dimensions = new ArrayList<>();
		for (var level : server.getAllLevels()) {
			dimensions.add(level.dimension().location());
		}
		dimensions.sort(null);

		List<AdvancementInfo> advancementList = new ArrayList<>();
        server.getAdvancements().getAllAdvancements().forEach(advancement -> advancement.value().display().ifPresent(display ->
				advancementList.add(new AdvancementInfo(advancement.id(), display.getTitle(), display.getIcon())))
		);
		advancementList.sort(Comparator.comparing(o -> o.id));

		Map<ResourceLocation, AdvancementInfo> map = advancementList.stream()
				.collect(Collectors.toMap(info -> info.id, info -> info, (a, b) -> b, () -> new LinkedHashMap<>(advancementList.size())));

		return new KnownServerRegistries(dimensions, map);
	}

	public record AdvancementInfo(ResourceLocation id, Component name, ItemStack icon) {
		public static StreamCodec<RegistryFriendlyByteBuf, AdvancementInfo> STREAM_CODEC = StreamCodec.composite(
				ResourceLocation.STREAM_CODEC, AdvancementInfo::id,
				ComponentSerialization.STREAM_CODEC, AdvancementInfo::name,
				ItemStack.OPTIONAL_STREAM_CODEC, AdvancementInfo::icon,
				AdvancementInfo::new
		);
	}
}
