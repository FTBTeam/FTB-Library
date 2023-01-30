package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public class ContextMenuItem implements Comparable<ContextMenuItem> {
	public static final ContextMenuItem SEPARATOR = new ContextMenuItem(Component.empty(), Icon.empty(), () -> {
	}) {
		@Override
		public Widget createWidget(ContextMenu panel) {
			return new ContextMenu.CSeparator(panel);
		}
	};


	private final Component title;
	private final Icon icon;
	private final Runnable callback;

	private boolean enabled = true;
	private Component yesNoText = Component.literal("");
	private boolean closeMenu = true;

	public ContextMenuItem(Component title, Icon icon, @Nullable Runnable callback) {
		this.title = title;
		this.icon = icon;
		this.callback = callback;
	}

	public void addMouseOverText(TooltipList list) {
	}

	public void drawIcon(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		icon.draw(matrixStack, x, y, w, h);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public ContextMenuItem setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public Component getTitle() {
		return title;
	}

	public Icon getIcon() {
		return icon;
	}

	public Component getYesNoText() {
		return yesNoText;
	}

	public ContextMenuItem setYesNoText(Component s) {
		yesNoText = s;
		return this;
	}

	public ContextMenuItem setCloseMenu(boolean v) {
		closeMenu = v;
		return this;
	}

	public Widget createWidget(ContextMenu panel) {
		return new ContextMenu.CButton(panel, this);
	}

	@Override
	public int compareTo(ContextMenuItem o) {
		return title.getString().compareToIgnoreCase(o.title.getString());
	}

	public void onClicked(Panel panel, MouseButton button) {
		if (closeMenu) {
			panel.getGui().closeContextMenu();
		}

		if (callback != null) callback.run();
	}
}
