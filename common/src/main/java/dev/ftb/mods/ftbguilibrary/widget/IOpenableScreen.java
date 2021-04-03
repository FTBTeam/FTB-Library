package dev.ftb.mods.ftbguilibrary.widget;

import dev.ftb.mods.ftbguilibrary.utils.ClientUtils;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public interface IOpenableScreen extends Runnable {
	void openGui();

	default void openGuiLater() {
		ClientUtils.runLater(this);
	}

	default void closeGui() {
		closeGui(true);
	}

	default void closeGui(boolean openPrevScreen) {
	}

	default void openContextMenu(@Nullable Panel panel) {
		if (this instanceof Widget) {
			((Widget) this).getGui().openContextMenu(panel);
		}
	}

	default void closeContextMenu() {
		if (this instanceof Widget) {
			((Widget) this).getGui().closeContextMenu();
		} else {
			openContextMenu(null);
		}
	}

	@Override
	default void run() {
		if (ClientUtils.getCurrentGuiAs(IOpenableScreen.class) != this) {
			openGui();
		}
	}

	default Runnable openAfter(Runnable runnable) {
		return () -> {
			runnable.run();
			IOpenableScreen.this.run();
		};
	}
}