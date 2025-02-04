package dev.ftb.mods.ftblibrary.core.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ResourceLocation.class)
public class ResourceLocationMixin {
    @ModifyReturnValue(method = {"validPathChar", "validNamespaceChar"}, at = @At("RETURN"))
    private static boolean validCharFTBJ(boolean original) {
        return original || StringUtils.ignoreResourceLocationErrors;
    }

    @ModifyReturnValue(method = {"isValidPath", "isValidNamespace"}, at = @At("RETURN"))
    private static boolean validStringFTBJ(boolean original) {
        return original || StringUtils.ignoreResourceLocationErrors;
    }
}
