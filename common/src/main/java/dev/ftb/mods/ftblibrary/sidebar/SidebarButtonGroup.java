package dev.ftb.mods.ftblibrary.sidebar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;


public record SidebarButtonGroup(int y, boolean isPinned) implements Comparable<SidebarButtonGroup> {


	public static final Codec<SidebarButtonGroup> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			Codec.INT.fieldOf("y").forGetter(SidebarButtonGroup::y),
			Codec.BOOL.fieldOf("isPinned").orElse(false).forGetter(SidebarButtonGroup::isPinned)
	).apply(builder, SidebarButtonGroup::new));


	public String getLangKey() {
		//Todo -unreal
		return Util.makeDescriptionId("sidebar_group", ResourceLocation.fromNamespaceAndPath("ftbquests", "sidebar_group_" + y));
	}

	@Override
	public int compareTo(SidebarButtonGroup group) {
		return y - group.y;
	}
}
