package dev.ftb.mods.ftblibrary.ui;

import net.minecraft.util.Mth;

import java.util.function.Predicate;

public class IntTextBox extends TextBox {
    // note: empty text and text with only '-' need to be allowed so numbers can be typed (treat as 0)
    private static final Predicate<String> IS_NUMBER = s -> s.matches("^-?[0-9]*$");

    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

    public IntTextBox(Panel panel) {
        super(panel);
        setFilter(IS_NUMBER);
        setStrictValidity(true);
    }

    public int getIntValue() {
        String text = getText();
        if (text.isEmpty() || text.equals("-")) {
            return Mth.clamp(0, min, max);
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return Mth.clamp(0, min, max);
        }
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean mouseScrolled(double scroll) {
        if (allowInput()) {
            setAmount(getIntValue() + (int) scroll);
            return true;
        }
        return false;
    }

    public void setAmount(int amount) {
        setText(String.valueOf(amount));
    }

    @Override
    public void onTextChanged() {
        ensureValue();
    }

    public void ensureValue() {
        int amount = getIntValue();

        if (amount < min) {
            setAmount(min);
        } else if (amount > max) {
            setAmount(max);
        }
    }
}