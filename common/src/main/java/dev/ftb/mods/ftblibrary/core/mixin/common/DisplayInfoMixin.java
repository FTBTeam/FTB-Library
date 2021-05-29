package dev.ftb.mods.ftblibrary.core.mixin.common;

import dev.ftb.mods.ftblibrary.core.DisplayInfoFTBL;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author LatvianModder
 */
@Mixin(DisplayInfo.class)
public abstract class DisplayInfoMixin implements DisplayInfoFTBL {
	@Override
	@Accessor("icon")
	public abstract ItemStack getIconStackFTBL();
}
