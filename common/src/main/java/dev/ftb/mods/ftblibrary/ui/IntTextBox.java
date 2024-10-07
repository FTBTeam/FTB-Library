package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;

import java.util.function.Predicate;

public class IntTextBox extends TextBox {

    private static final Predicate<String> IS_NUMBER = s -> s.matches("^[0-9-]+$");
    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

    public IntTextBox(Panel panel) {
        super(panel);
        setFilter(IS_NUMBER);
    }

    public int getIntValue() {
        return Integer.parseInt(getText());
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
        if(allowInput()) {
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

    @Override
    public boolean charTyped(char c, KeyModifiers modifiers) {
        if (Character.isDigit(c)) {
            return super.charTyped(c, modifiers);
        }
        return false;
    }
}