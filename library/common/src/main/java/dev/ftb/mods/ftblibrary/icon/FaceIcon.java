package dev.ftb.mods.ftblibrary.icon;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.DefaultPlayerSkin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

public class FaceIcon extends Icon {
    private static final Logger LOGGER = LoggerFactory.getLogger(FaceIcon.class);
    private static final HashMap<UUID, FaceIcon> CACHE = new HashMap<>();
    public final GameProfile profile;
    public Icon skin;
    public Icon head;
    public Icon hat;
    private FaceIcon(GameProfile p) {
        profile = p;
        skin = new ImageIcon(DefaultPlayerSkin.get(profile.getId()).texture());
        head = skin.withUV(8F, 8F, 8F, 8F, 64F, 64F);
        hat = Icon.empty();

        Minecraft.getInstance().getSkinManager().getOrLoad(profile).whenComplete((playerSkin, throwable) -> {
            if (playerSkin != null) {
                var texture = playerSkin.texture();
                skin = new ImageIcon(texture);
                head = skin.withUV(8F, 8F, 8F, 8F, 64F, 64F);
                hat = skin.withUV(40F, 8F, 8F, 8F, 64F, 64F);
            } else if (throwable != null) {
                LOGGER.warn("Failed to load skin for {}: {} ", profile.getName(), throwable.getMessage());
            } else {
                LOGGER.warn("Failed to load skin for {} ?", profile.getName());
            }
        });
    }

    public static FaceIcon getFace(GameProfile profile) {
        var icon = CACHE.get(profile.getId());

        if (icon == null) {
            icon = new FaceIcon(profile);
            CACHE.put(profile.getId(), icon);
        }

        return icon;
    }

    @Override
    public void draw(GuiGraphics poseStack, int x, int y, int w, int h) {
        head.draw(poseStack, x, y, w, h);
        hat.draw(poseStack, x, y, w, h);
    }
}
