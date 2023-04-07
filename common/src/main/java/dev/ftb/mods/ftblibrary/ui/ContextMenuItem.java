package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

/**
 * @author LatvianModder
 */
public class ContextMenuItem implements Comparable<ContextMenuItem> {
	public static final ContextMenuItem SEPARATOR = new ContextMenuItem(Component.empty(), Color4I.EMPTY, () -> {
	}) {
		@Override
		public Widget createWidget(ContextMenu panel) {
			return new ContextMenu.CSeperator(panel);
		}
	};

	public static final BooleanSupplier TRUE = () -> true;
	public static final BooleanSupplier FALSE = () -> false;

	public Component title;
	public Icon icon;
	public Runnable callback;
	public BooleanSupplier enabled = TRUE;
	public Component yesNoText = Component.literal("");
	public boolean closeMenu = true;

	public ContextMenuItem(Component t, Icon i, @Nullable Runnable c) {
		title = t;
		icon = i;
		callback = c;
	}

	public void addMouseOverText(TooltipList list) {
	}

	public void drawIcon(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		icon.draw(matrixStack, x, y, w, h);
	}

	public ContextMenuItem setEnabled(boolean v) {
		return setEnabled(v ? TRUE : FALSE);
	}

	public ContextMenuItem setEnabled(BooleanSupplier v) {
		enabled = v;
		return this;
	}

	public ContextMenuItem setYesNo(Component s) {
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
