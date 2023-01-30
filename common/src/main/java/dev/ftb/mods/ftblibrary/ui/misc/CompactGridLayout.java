package dev.ftb.mods.ftblibrary.ui.misc;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

/**
 * @author LatvianModder
 */
public class CompactGridLayout implements WidgetLayout {
	private static final int[][] LAYOUTS = {
			{1},
			{2},
			{3},
			{4},
			{3, 2},
			{3, 3},
			{4, 3},
			{4, 4},
			{3, 3, 3},
			{3, 4, 3},
			{4, 3, 4},
			{4, 4, 4},
			{4, 3, 3, 3},
			{3, 4, 4, 3},
			{4, 4, 4, 3},
			{4, 4, 4, 4}
	};

	private final int size;

	public CompactGridLayout(int s) {
		size = s;
	}

	@Override
	public int align(Panel panel) {
		var nWidgets = panel.getWidgets().size();

		if (nWidgets == 0) {
			return 0;
		} else if (nWidgets > LAYOUTS.length) {
			for (var i = 0; i < nWidgets; i++) {
				panel.getWidgets().get(i).setPosAndSize((i % 4) * size, (i / 4) * size, size, size);
			}

			return (nWidgets / 4) * size;
		}

		int[] layout = LAYOUTS[nWidgets - 1];
		var max = 0;
		for (var v : layout) {
			max = Math.max(max, v);
		}

		var off = 0;

		for (var l = 0; l < layout.length; l++) {
			var o = ((layout[l] % 2) == (max % 2)) ? 0 : size / 2;

			for (var i = 0; i < layout[l]; i++) {
				panel.getWidgets().get(off + i).setPosAndSize(o + i * size, l * size, size, size);
			}

			off += layout[l];
		}

		return layout.length * size;
	}
}