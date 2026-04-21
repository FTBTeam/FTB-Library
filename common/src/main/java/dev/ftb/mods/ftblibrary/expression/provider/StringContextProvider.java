package dev.ftb.mods.ftblibrary.expression.provider;

public class StringContextProvider extends ContextProvider {
    public StringContextProvider() {
        super("str");
    }

    public int length(String str) {
        return str.length();
    }

    public boolean equalsIgnoreCase(String str1, String str2) {
        return str1.equalsIgnoreCase(str2);
    }

    public String lower(String str) {
        return str.toLowerCase();
    }

    public String upper(String str) {
        return str.toUpperCase();
    }

    public String trim(String str) {
        return str.trim();
    }

    public boolean startsWith(String str, String prefix) {
        return str.startsWith(prefix);
    }

    public boolean endsWith(String str, String suffix) {
        return str.endsWith(suffix);
    }
}
