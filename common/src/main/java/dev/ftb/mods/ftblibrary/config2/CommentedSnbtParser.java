package dev.ftb.mods.ftblibrary.config2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;

/**
 * Middleman parser class for injecting & stripping comments from an otherwise standard SNBT input
 */
public class CommentedSnbtParser {
    public static CompoundTag read(String input) {
        return new CompoundTag();
    }

    public static String write(CompoundTag tag) {
        var outputText = (new SnbtPrinterTagVisitor()).visit(tag);

        // Inject comments...

        return outputText;
    }
}
