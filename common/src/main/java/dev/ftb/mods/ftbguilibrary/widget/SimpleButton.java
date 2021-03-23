package dev.ftb.mods.ftbguilibrary.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbguilibrary.icon.Icon;
import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class SimpleButton extends Button {
	public interface Callback {
		void onClicked(SimpleButton widget, MouseButton button);
	}

	private final Callback consumer;

	public SimpleButton(Panel panel, Component text, Icon icon, Callback c) {
		super(panel, text, icon);
		consumer = c;
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		consumer.onClicked(this, button);
	}
}