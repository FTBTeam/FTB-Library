package dev.ftb.mods.ftblibrary.sidebar;

public record GridLocation(int x, int y) {

		public static final GridLocation OUT_OF_BOUNDS = new GridLocation(-1, -1);

		public boolean isOutOfBounds() {
			return x < 0 || y < 0;
		}
	}