package dev.ftb.mods.ftblibrary.core.mixin.fabric;

import dev.ftb.mods.ftblibrary.fabric.PlayerDisplayNameCache;
import dev.ftb.mods.ftblibrary.fabric.PlayerDisplayNameCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Player.class)
public class PlayerMixin implements PlayerDisplayNameCache {
    private Component cachedDisplayName;

    @Inject(method = "getDisplayName", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable<Component> cir, MutableComponent mutableComponent) {
        if (cachedDisplayName == null) {
            //noinspection ConstantConditions
            cachedDisplayName = PlayerDisplayNameCallback.EVENT.invoker().modifyDisplayName((Player) (Object) this, mutableComponent);
        }
        if (cachedDisplayName != null) {
            cir.setReturnValue(cachedDisplayName);
        }
    }

    @Override
    public void clearCachedDisplayName() {
        this.cachedDisplayName = null;
    }
}
