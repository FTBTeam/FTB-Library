package dev.ftb.mods.ftblibrary.sidebar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;


public record SidebarButtonData(
		ResourceLocation group,
		Icon icon,
		int x,
		boolean defaultEnabled,
		List<String> clickEvents,
		Optional<List<String>> shiftClickEvent,
		boolean loadingScreen,
		Optional<Component> tooltip,
		boolean requiresOp,
		Optional<List<String>> requiredMods) implements Comparable<SidebarButtonData> {

	public static final Codec<SidebarButtonData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        ResourceLocation.CODEC.fieldOf("group").forGetter(SidebarButtonData::group),
        Icon.CODEC.fieldOf("icon").forGetter(SidebarButtonData::icon),
		Codec.INT.fieldOf("x").forGetter(SidebarButtonData::x),
		Codec.BOOL.fieldOf("default_enabled").orElse(true).forGetter(SidebarButtonData::defaultEnabled),
		Codec.STRING.listOf(1, Integer.MAX_VALUE).fieldOf("click").forGetter(SidebarButtonData::clickEvents),
		Codec.STRING.listOf(1, Integer.MAX_VALUE).optionalFieldOf("shift_click").forGetter(SidebarButtonData::shiftClickEvent),
		Codec.BOOL.fieldOf("loading_screen").orElse(false).forGetter(SidebarButtonData::loadingScreen),
		ComponentSerialization.CODEC.optionalFieldOf("tooltip").forGetter(SidebarButtonData::tooltip),
		Codec.BOOL.fieldOf("requires_op").orElse(false).forGetter(SidebarButtonData::requiresOp),
		Codec.STRING.listOf(1, Integer.MAX_VALUE).optionalFieldOf("required_mods").forGetter(SidebarButtonData::requiredMods)
    ).apply(builder, SidebarButtonData::new));


	@Override
	public int compareTo(SidebarButtonData button) {
		return x - button.x;
	}
}