package dev.ftb.mods.ftblibrary.ui2.components;

import java.util.ArrayList;
import java.util.List;

public class VerticalStack extends Component {
    private List<Component> elements = new ArrayList<>();

    public VerticalStack() {
        super("vstack");

    }

    @Override
    public Size sizeThatFits(Size proposal) {
        return null;
    }

    @Override
    public void placeSubviews(int x, int y, int width, int height) {

    }

    @Override
    void render(int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {

    }
}
