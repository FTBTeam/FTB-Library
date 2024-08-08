package dev.ftb.mods.ftblibrary.sidebar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;


public record SidebarButtonData(
		Icon icon,
		boolean defaultEnabled,
		List<String> clickEvents,
		Optional<List<String>> shiftClickEvent,
		boolean loadingScreen,
		Optional<List<Component>> tooltip,
		Optional<List<Component>> shiftTooltip,
		boolean requiresOp,
		Optional<List<String>> requiredMods,
		int sortIndex) implements Comparable<SidebarButtonData> {

	public static final Codec<SidebarButtonData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        Icon.CODEC.fieldOf("icon").forGetter(SidebarButtonData::icon),
		Codec.BOOL.fieldOf("default_enabled").orElse(true).forGetter(SidebarButtonData::defaultEnabled),
		Codec.STRING.listOf(1, Integer.MAX_VALUE).fieldOf("click").forGetter(SidebarButtonData::clickEvents),
		Codec.STRING.listOf(1, Integer.MAX_VALUE).optionalFieldOf("shift_click").forGetter(SidebarButtonData::shiftClickEvent),
		Codec.BOOL.fieldOf("loading_screen").orElse(false).forGetter(SidebarButtonData::loadingScreen),
		ComponentSerialization.CODEC.listOf().optionalFieldOf("tooltip").forGetter(SidebarButtonData::tooltip),
		ComponentSerialization.CODEC.listOf().optionalFieldOf("shift_tooltip").forGetter(SidebarButtonData::shiftTooltip),
		Codec.BOOL.fieldOf("requires_op").orElse(false).forGetter(SidebarButtonData::requiresOp),
		Codec.STRING.listOf(1, Integer.MAX_VALUE).optionalFieldOf("required_mods").forGetter(SidebarButtonData::requiredMods),
		Codec.INT.fieldOf("sort_index").orElse(0).forGetter(SidebarButtonData::sortIndex)
    ).apply(builder, SidebarButtonData::new));

	@Override
	public int compareTo(@NotNull SidebarButtonData o) {
		return Integer.compare(sortIndex, o.sortIndex);
	}
}
