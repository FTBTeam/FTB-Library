package dev.ftb.mods.ftblibrary.core.mixin.fabric;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Need to make sure mouseReleased is called for widgets
// See https://github.com/FabricMC/fabric/pull/4010
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {

    protected AbstractContainerScreenMixin(Component component) {
        super(component);
    }

    @Inject(at = @At("HEAD"), method = "mouseReleased", cancellable = true)
    public void onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            info.setReturnValue(true);
        }
    }
}
