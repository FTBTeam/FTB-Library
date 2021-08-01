package dev.ftb.mods.ftblibrary.core.mixin.common;

import dev.ftb.mods.ftblibrary.util.StringUtils;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResourceLocation.class)
public class ResourceLocationMixin {
	@Inject(method = {"isValidPath", "isValidNamespace"}, at = @At("HEAD"), cancellable = true)
	private static void validateCharFTBJ(String string, CallbackInfoReturnable<Boolean> ci) {
		if (StringUtils.ignoreResourceLocationErrors) {
			ci.setReturnValue(true);
		}
	}
}