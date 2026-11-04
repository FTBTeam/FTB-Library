package dev.ftb.mods.ftblibrary.core.mixin.common;

import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SpriteSourceList.class)
public interface SpriteSourceListAccess {
    @Accessor("sources")
    List<SpriteSource> getSources();
}
