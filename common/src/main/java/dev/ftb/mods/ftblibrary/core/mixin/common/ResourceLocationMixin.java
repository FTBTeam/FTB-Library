package dev.ftb.mods.ftblibrary.core.mixin.common;

import dev.ftb.mods.ftblibrary.util.StringUtils;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResourceLocation.class)
public class ResourceLocationMixin {
	@Inject(method = {"validPathChar", "validNamespaceChar"}, at = @At("HEAD"), cancellable = true)
	private static void validCharFTBJ(char c, CallbackInfoReturnable<Boolean> ci) {
		if (StringUtils.ignoreResourceLocationErrors) {
			ci.setReturnValue(true);
		}
	}

	@Inject(method = {"isValidPath", "isValidNamespace"}, at = @At("HEAD"), cancellable = true)
	private static void validStringFTBJ(String s, CallbackInfoReturnable<Boolean> ci) {
		if (StringUtils.ignoreResourceLocationErrors) {
			ci.setReturnValue(true);
		}
	}
}