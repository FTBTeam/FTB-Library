package dev.ftb.mods.ftblibrary.ui.misc;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * @author LatvianModder
 */
public class SimpleToast implements Toast {
	private boolean hasPlayedSound = false;

	@Override
	public Visibility render(GuiGraphics graphics, ToastComponent gui, long delta) {
		GuiHelper.setupDrawing();
		var mc = gui.getMinecraft();

		graphics.blit(TEXTURE, 0, 0, 0, 0, 160, 32);

		var list = mc.font.split(getSubtitle(), 125);
		var i = isImportant() ? 0x00FF88FF : 0x00FFFF00;

		if (list.size() == 1) {
			graphics.drawString(mc.font, getTitle(), 30, 7, i | 0xFF000000, true);
			graphics.drawString(mc.font, list.get(0), 30, 18, -1);
		} else {
			if (delta < 1500L) {
				var k = Mth.floor(Mth.clamp((float) (1500L - delta) / 300F, 0F, 1F) * 255F) << 24 | 67108864;
				graphics.drawString(mc.font, getTitle(), 30, 11, i | k, true);
			} else {
				var i1 = Mth.floor(Mth.clamp((float) (delta - 1500L) / 300F, 0F, 1F) * 252F) << 24 | 67108864;
				var l = 16 - list.size() * mc.font.lineHeight / 2;

				for (var s : list) {
					graphics.drawString(mc.font, s, 30, l, 0x00FFFFFF | i1, true);
					l += mc.font.lineHeight;
				}
			}
		}

		if (!hasPlayedSound && delta > 0L) {
			hasPlayedSound = true;
			playSound(mc.getSoundManager());
		}

		getIcon().draw(graphics, 8, 8, 16, 16);
		return delta >= 5000L * gui.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
	}

	public Component getTitle() {
		return Component.literal("<error>");
	}

	public Component getSubtitle() {
		return Component.empty();
	}

	public boolean isImportant() {
		return false;
	}

	public Icon getIcon() {
		return Icons.INFO;
	}

	public void playSound(SoundManager handler) {
	}

	public static void info(Component title, Component subtitle) {
		Minecraft.getInstance().getToasts().addToast(
		new SimpleToast() {
			@Override
			public Component getTitle() {
				return title;
			}

			@Override
			public Component getSubtitle() {
				return subtitle;
			}
		});
	}

	public static void error(Component title, Component subtitle) {
		Minecraft.getInstance().getToasts().addToast(
				new SimpleToast() {
					@Override
					public Component getTitle() {
						return title;
					}

					@Override
					public Component getSubtitle() {
						return subtitle;
					}

					@Override
					public Icon getIcon() {
						return Icons.BARRIER;
					}
				});
	}
}
