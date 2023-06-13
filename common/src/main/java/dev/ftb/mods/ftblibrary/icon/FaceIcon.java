package dev.ftb.mods.ftblibrary.icon;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.DefaultPlayerSkin;

import java.util.HashMap;
import java.util.UUID;

public class FaceIcon extends Icon {
	private static final HashMap<UUID, FaceIcon> CACHE = new HashMap<>();

	public static FaceIcon getFace(GameProfile profile) {
		var icon = CACHE.get(profile.getId());

		if (icon == null) {
			icon = new FaceIcon(profile);
			CACHE.put(profile.getId(), icon);
		}

		return icon;
	}

	public final GameProfile profile;
	public Icon skin;
	public Icon head;
	public Icon hat;

	private FaceIcon(GameProfile p) {
		profile = p;
		skin = new ImageIcon(DefaultPlayerSkin.getDefaultSkin(profile.getId()));
		head = skin.withUV(8F, 8F, 8F, 8F, 64F, 64F);
		hat = Icon.empty();

		Minecraft.getInstance().getSkinManager().registerSkins(profile, (type, resourceLocation, minecraftProfileTexture) -> {
			if (type == MinecraftProfileTexture.Type.SKIN) {
				skin = new ImageIcon(resourceLocation);
				head = skin.withUV(8F, 8F, 8F, 8F, 64F, 64F);
				hat = skin.withUV(40F, 8F, 8F, 8F, 64F, 64F);
			}
		}, true);
	}

	@Override
	public void draw(GuiGraphics poseStack, int x, int y, int w, int h) {
		head.draw(poseStack, x, y, w, h);
		hat.draw(poseStack, x, y, w, h);
	}
}
