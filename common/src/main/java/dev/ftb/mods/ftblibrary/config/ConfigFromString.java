package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.config.ui.EditStringConfigOverlay;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class ConfigFromString<T> extends ConfigValue<T> {
	public abstract boolean parse(@Nullable Consumer<T> callback, String string);

	public String getStringFromValue(@Nullable T v) {
		return String.valueOf(v);
	}

	@Override
	public Component getStringForGUI(@Nullable T v) {
		return Component.literal(getStringFromValue(v));
	}

	@Override
	public void onClicked(Widget clicked, MouseButton button, ConfigCallback callback) {
		BaseScreen gui = clicked.getGui();

		EditStringConfigOverlay.PosProvider.Offset offset = clicked instanceof EditStringConfigOverlay.PosProvider p ?
				p.getOverlayOffset() :
				EditStringConfigOverlay.PosProvider.Offset.NONE;
		int xPos = clicked.getX() - gui.getX() + offset.x();
		int yPos = clicked.getY() - gui.getY() + offset.y();
		int availableWidth = gui.width - xPos - 2;

		EditStringConfigOverlay<T> panel = new EditStringConfigOverlay<>(gui, this, callback, availableWidth);
		panel.setPos(xPos, yPos);
		gui.pushModalPanel(panel);
	}

	public boolean canScroll() {
		return false;
	}

	public Optional<T> scrollValue(T currentValue, boolean forward) {
		return Optional.empty();
	}
}
