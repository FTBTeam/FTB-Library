package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.FaceIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.DefaultPlayerSkin;

public enum FaceIconRenderer implements IconRenderer<FaceIcon> {
    INSTANCE;

    public static void requestFaceFromSkinManager(FaceIcon faceIcon) {
        Minecraft.getInstance().getSkinManager().get(faceIcon.profile).whenComplete((playerSkin, throwable) -> {
            if (playerSkin.isPresent()) {
                faceIcon.setSkin(new ImageIcon(playerSkin.get().body().texturePath()));
            } else if (throwable != null) {
                FaceIcon.LOGGER.warn("Failed to load skin for {}: {} ", faceIcon.profile.name(), throwable.getMessage());
            } else {
                FaceIcon.LOGGER.warn("Failed to load skin for {} ?", faceIcon.profile.name());
            }
        });
    }

    public static FaceIcon.FaceData getDefaultFaceData(FaceIcon faceIcon) {
        Icon<?> defaultFace = new ImageIcon(DefaultPlayerSkin.get(faceIcon.profile.id()).body().texturePath());
        return new FaceIcon.FaceData(defaultFace, defaultFace.withUV(8F, 8F, 8F, 8F, 64F, 64F), Icon.empty());
    }

    @Override
    public void render(FaceIcon icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        IconHelper.renderIcon(icon.getHead(), graphics, x, y, w, h);
        IconHelper.renderIcon(icon.getHat(), graphics, x, y, w, h);
    }
}
