package dev.ftb.mods.ftblibrary.core.mixin.fabric;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
@Debug(export = true)
public abstract class AbstractContainerScreenMixin extends Screen {

    protected AbstractContainerScreenMixin(Component component) {
        super(component);
    }

    @Inject(at = @At("HEAD"), method = "mouseReleased")
    public void onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        super.mouseReleased(mouseX, mouseY, button);
    }
}
