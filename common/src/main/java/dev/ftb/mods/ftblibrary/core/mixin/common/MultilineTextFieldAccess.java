package dev.ftb.mods.ftblibrary.core.mixin.common;

import net.minecraft.client.gui.components.MultilineTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultilineTextField.class)
public interface MultilineTextFieldAccess {
    @Accessor("selectCursor")
    @Mutable
    void setSelectCursor(int pos);
}
