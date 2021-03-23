package dev.ftb.mods.ftbguilibrary.icon;

import javafx.scene.image.Image;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
@FunctionalInterface
public interface ImageCallback {
	void imageLoaded(boolean queued, @Nullable Image image);
}