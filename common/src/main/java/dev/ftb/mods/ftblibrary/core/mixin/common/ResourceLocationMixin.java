package dev.ftb.mods.ftblibrary.core.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Identifier.class)
public class ResourceLocationMixin {
    @ModifyReturnValue(method = {"validPathChar", "validNamespaceChar"}, at = @At("RETURN"))
    private static boolean validCharFTBJ(boolean original) {
        return original || StringUtils.ignoreIdentifierErrors;
    }

    @ModifyReturnValue(method = {"isValidPath", "isValidNamespace"}, at = @At("RETURN"))
    private static boolean validStringFTBJ(boolean original) {
        return original || StringUtils.ignoreIdentifierErrors;
    }
}
