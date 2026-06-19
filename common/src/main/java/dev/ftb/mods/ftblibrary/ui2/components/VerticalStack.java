package dev.ftb.mods.ftblibrary.ui2.components;

import java.util.ArrayList;
import java.util.List;

public class VerticalStack extends Component {
    private List<Component> elements = new ArrayList<>();

    public VerticalStack() {
        super("vstack");

    }

    @Override
    void render(int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {

    }
}
