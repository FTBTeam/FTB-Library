package dev.ftb.mods.ftblibrary.legacy.snbt;

import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.marhali.json5.Json5Object;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/// Legacy support utility class for converting FTB SNBT files to JSON5.
///
/// To reduce the amount of work needed to do the conversion, we utilize Minecraft's built-in SNBT parser by first converting
/// our home-grown SNBT format into a "valid" SNBT format. The built-in parser is luckily very lenient and will accept our
/// slightly funky output.
///
/// Most of the bulk of the implementation is correctly pulling comments from the SNBT file and
/// attaching them to the correct keys in the JSON5 output.
public class SnbtToJson5 {
    static void main() throws IOException {
        var userHome = System.getProperty("user.home");
//        var file = Path.of(userHome + "/downloads/ftb-skies-2-aero-main/config/ftbbackups3-server.snbt");
        var file = Path.of(userHome + "/downloads/ftb-skies-2-aero-main/config/ftbquests/quests/chapters/actually_additions.snbt");
        var fileData = Files.readString(file);
        var jsonData = convert(fileData);
        System.out.printf("%s", jsonData.toString());
    }

    /// Convert an FTB SNBT string to a JSON5 object.
    /// @param input the FTB SNBT string
    /// @return the JSON5 object
    /// @throws RuntimeException if the input is not valid SNBT (TODO: Harden exception handling)
    public static Json5Object convert(String input) {
        // Take in an FTB SNBT string and add ',' to the end of each line except the comments, etc.
        StringBuilder output = new StringBuilder();
        String[] lines = input.split("\n");

        StringBuilder fileComment = new StringBuilder();
        Map<String, String> comments = new HashMap<>();

        StringBuilder currentComment = null;
        boolean seenFirstNoneCommentLine = false;
        Deque<String> keyStack = new ArrayDeque<>();
        for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            String trimmedLine = line.trim();

            var isComment = isCommentLine(line);
            if (isComment) {
                if (!seenFirstNoneCommentLine) {
                    fileComment.append(normalizeComment(line)).append("\n");
                    continue;
                }

                if (currentComment == null) {
                    currentComment = new StringBuilder();
                }

                currentComment.append(normalizeComment(line)).append("\n");
                continue;
            }

            // We're not a comment. We should check and commit if we're a key.
            var isKey = trimmedLine.indexOf(": ") > 0;
            if (isKey) {
                // If we have a current comment, we should commit it to the comments map.
                if (currentComment != null) {
                    var key = trimmedLine.substring(0, trimmedLine.indexOf(": ")).trim();
                    var fullKey = String.join(".", keyStack) + (keyStack.isEmpty() ? "" : ".") + key;
                    comments.put(fullKey, currentComment.toString());
                    currentComment = null;

                    // If we're a key with an opening brace, we should push the key onto the stack.
                    if (trimmedLine.endsWith("{")) {
                        keyStack.add(key);
                    }
                }
            }

            // If we're a closing brace, we should pop the last key from the stack.
            if (trimmedLine.startsWith("}")) {
                if (!keyStack.isEmpty()) {
                    keyStack.removeLast();
                }
            }

            // Add commas
            if (// Ending } should have a comma if it's not the last line
                    trimmedLine.startsWith("{") ||
                    trimmedLine.endsWith("[") ||
                    trimmedLine.endsWith("{") ||
                    i == lines.length - 1 ||
                    line.trim().isEmpty()
            ) {
                output.append(line).append("\n");
                if (!seenFirstNoneCommentLine) {
                    seenFirstNoneCommentLine = true;
                }
            } else {
                output.append(line).append(",\n");
            }
        }

        // If we have a dangling comment, we discard it as it shouldn't have ever been there.
        CompoundTag compoundTag;
        try {
            compoundTag = TagParser.parseCompoundFully(output.toString());
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        // So now we have a comments map, and a parsed compound tag. We can now convert to json 5 using JSON5Ops
        var json5 = NbtOps.INSTANCE.convertTo(Json5Ops.INSTANCE, compoundTag);

        // Now we have the json5 instance, we need to attach the comments back to the keys.
        if (!fileComment.isEmpty()) {
            json5.setComment(fileComment.toString());
        }

        if (!json5.isJson5Object()) {
            throw new RuntimeException("Expected json5 object");
        }

        var asObject = json5.getAsJson5Object();
        attachComments(asObject, comments, "");

        return asObject;
    }

    /// Recursively attach comments to the json5 object based on the comments map.
    ///
    /// @param asObject the json5 object to attach comments to
    /// @param comments the map of comments to attach
    /// @param lastKey the last key in the hierarchy, used to build the full key
    private static void attachComments(Json5Object asObject, Map<String, String> comments, String lastKey) {
        for (var entry : asObject.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var fullKey = lastKey.isEmpty() ? key : lastKey + "." + key;
            if (comments.containsKey(fullKey)) {
                value.setComment(comments.get(fullKey));
            }
            if (value.isJson5Object()) {
                attachComments(value.getAsJson5Object(), comments, fullKey);
            }
        }
    }

    /// Trims out the comment markers and whitespace from a comment line.
    ///
    /// @param commentLine the comment line
    /// @return the normalized comment
    private static String normalizeComment(String commentLine) {
        var comment = commentLine.trim();
        // Remove the comment markers, trim, and return
        int sliceStart = 0;
        int sliceEnd = comment.length();
        if (comment.startsWith("/*") || comment.startsWith("//")) {
            sliceStart = 2;
        } else if (comment.startsWith("#")) {
            sliceStart = 1;
        }

        if (comment.endsWith("*/")) {
            sliceEnd = comment.length() - 2;
        }

        return comment.substring(sliceStart, sliceEnd).trim();
    }

    /// Checks if a line is a comment line per the FTB SNBT format.
    ///
    /// @param line the line to check
    /// @return true if the line is a comment line, false otherwise
    private static boolean isCommentLine(String line) {
        String trimmedLine = line.trim();
        return trimmedLine.startsWith("/*") ||
                trimmedLine.endsWith("*/") ||
                trimmedLine.startsWith("//") ||
                trimmedLine.startsWith("#");
    }
}
