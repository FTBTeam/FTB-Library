package dev.ftb.mods.ftblibrary.core.mixin.common;

import dev.ftb.mods.ftblibrary.core.CompoundContainerFTBL;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author LatvianModder
 */
@Mixin(CompoundContainer.class)
public abstract class CompoundContainerMixin implements CompoundContainerFTBL {
	@Override
	@Accessor("container1")
	public abstract Container getContainer1FTBL();

	@Override
	@Accessor("container2")
	public abstract Container getContainer2FTBL();
}
