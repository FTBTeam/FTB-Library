package dev.ftb.mods.ftblibrary.snbt;

import java.util.ArrayList;
import java.util.List;

class SNBTBuilder {
    private final List<String> lines = new ArrayList<>();
    private final StringBuilder line = new StringBuilder();
    private String indent = "";
    public int singleLine = 0;

    public void print(Object string) {
        line.append(string);
    }

    public void println() {
        line.insert(0, indent);
        lines.add(line.toString());
        line.setLength(0);
    }

    public void push() {
        indent += "\t";
    }

    public void pop() {
        indent = indent.substring(1);
    }

    public List<String> build() {
        return lines;
    }
}