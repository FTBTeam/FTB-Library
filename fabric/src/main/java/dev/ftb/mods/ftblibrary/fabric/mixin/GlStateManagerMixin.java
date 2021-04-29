package dev.ftb.mods.ftblibrary.fabric.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.ftb.mods.ftblibrary.fabric.Variables;
import org.lwjgl.opengl.GL13;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
	@Inject(
			method = "_glMultiTexCoord2f(IFF)V",
			at = @At("RETURN")
	)
	private static void updateLastBrightness(int i, float f, float g, CallbackInfo ci) {
		if (i == GL13.GL_TEXTURE1) {
			Variables.glsm$lastBrightnessX = f;
			Variables.glsm$lastBrightnessY = g;
		}
	}
}
