package dev.ftb.mods.ftblibrary.mixin.common;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import dev.ftb.mods.ftblibrary.util.text.ExtendableTextColor;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextColor.class)
public class TextColorMixin {
    @Inject(method = "parseColor", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"), cancellable = true)
    private static void onParseColor(String s, CallbackInfoReturnable<DataResult<TextColor>> cir) {
        TextColor color = ExtendableTextColor.getCustomColors().get(s);
        if (color != null) {
            cir.setReturnValue(DataResult.success(color, Lifecycle.stable()));
        }
    }
}
