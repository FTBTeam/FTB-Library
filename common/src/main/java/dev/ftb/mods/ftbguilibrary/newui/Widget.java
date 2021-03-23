package dev.ftb.mods.ftbguilibrary.newui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbguilibrary.newui.event.KeyCharEvent;
import dev.ftb.mods.ftbguilibrary.newui.event.KeyPressedEvent;
import dev.ftb.mods.ftbguilibrary.newui.event.KeyReleasedEvent;
import dev.ftb.mods.ftbguilibrary.newui.event.MousePressedEvent;
import dev.ftb.mods.ftbguilibrary.newui.event.MouseReleasedEvent;
import dev.ftb.mods.ftbguilibrary.newui.event.MouseScrolledEvent;
import dev.ftb.mods.ftbguilibrary.newui.event.PositionUpdateEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author LatvianModder
 */
public abstract class Widget {
	public UI ui = null;
	public Panel panel = null;
	public String type = "unknown";
	public String id = "unknown";
	public Set<String> classes = Collections.emptySet();

	public double rx = 0;
	public double ry = 0;
	public double width = 16;
	public double height = 16;

	public double x = 0;
	public double y = 0;
	public boolean mouseOver = false;

	public <T extends Widget> T classes(String... s) {
		classes = new HashSet<>(Arrays.asList(s));
		return (T) this;
	}

	public void updatePosition(PositionUpdateEvent event) {
		x = panel == null ? rx : (panel.x + rx);
		y = panel == null ? ry : (panel.y + ry);
		mouseOver = event.mouseOver(x, y, width, height);

		if (mouseOver) {
			event.widgetUnderMouse = this;
		}
	}

	public void draw(PoseStack matrixStack) {
	}

	public void tick() {
	}

	public boolean mousePressed(MousePressedEvent event) {
		return false;
	}

	public void mouseReleased(MouseReleasedEvent event) {
	}

	public boolean mouseScrolled(MouseScrolledEvent event) {
		return false;
	}

	public boolean keyPressed(KeyPressedEvent key) {
		return false;
	}

	public void keyReleased(KeyReleasedEvent key) {
	}

	public boolean charTyped(KeyCharEvent event) {
		return false;
	}

	public boolean isEnabled() {
		return true;
	}
}