package dev.ftb.mods.ftblibrary.ui.misc;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

/**
 * @author LatvianModder
 */
public class CompactGridLayout implements WidgetLayout {
	public static final int[][] LAYOUTS = {
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

	public final int size;

	public CompactGridLayout(int s) {
		size = s;
	}

	@Override
	public int align(Panel panel) {
		int s = panel.widgets.size();

		if (s <= 0) {
			return 0;
		} else if (s > LAYOUTS.length) {
			for (int i = 0; i < s; i++) {
				panel.widgets.get(i).setPosAndSize((i % 4) * size, (i / 4) * size, size, size);
			}

			return (s / 4) * size;
		}

		int[] layout = LAYOUTS[s - 1];

		int m = 0;

		for (int v : layout) {
			m = Math.max(m, v);
		}

		int off = 0;

		for (int l = 0; l < layout.length; l++) {
			int o = ((layout[l] % 2) == (m % 2)) ? 0 : size / 2;

			for (int i = 0; i < layout[l]; i++) {
				panel.widgets.get(off + i).setPosAndSize(o + i * size, l * size, size, size);
			}

			off += layout[l];
		}

		return layout.length * size;
	}
}