package dev.ftb.mods.ftblibrary.mixin.common;

import dev.ftb.mods.ftblibrary.client.util.AtlasSpriteLookup;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {
    // The sprite map is only ever replaced in upload() (restitch) or emptied in clearTextureData(). This is done
    // with a mixin rather than NeoForge's TextureAtlasStitchedEvent so it works on all platforms.
    @Inject(method = {"upload", "clearTextureData"}, at = @At("TAIL"))
    private void ftblibrary$onSpritesChanged(CallbackInfo ci) {
        AtlasSpriteLookup.invalidate();
    }
}
