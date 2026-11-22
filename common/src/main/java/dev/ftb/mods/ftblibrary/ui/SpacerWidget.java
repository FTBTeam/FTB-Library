package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.ui.layout.Flex;

/**
 * A widget with no visuals or input behaviour, purely to provide spacing between other GUI elements. Primarily
 * intended for use with managed layouts: {@link WidgetLayout}
 */
public class SpacerWidget extends Widget {
    private final boolean horizontal;

    private SpacerWidget(Panel parent, int w, int h, boolean horizontal) {
        super(parent);

        this.horizontal = horizontal;
        setSize(w, h);
    }

    public static SpacerWidget vertical(Panel parent, int h) {
        return new SpacerWidget(parent, 1, h, false);
    }

    public static SpacerWidget horizontal(Panel parent, int w) {
        return new SpacerWidget(parent, w, 1, true);
    }

    public SpacerWidget expand() {
        return expand(1);
    }

    public SpacerWidget expand(int weight) {
        setLayoutHints(getLayoutHints()
                .withHFlex(horizontal ? Flex.RESIZE : Flex.INTRINSIC)
                .withVFlex(horizontal ? Flex.INTRINSIC : Flex.RESIZE)
                .withWeight(weight)
        );
        return this;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean shouldDraw() {
        return false;
    }
}
