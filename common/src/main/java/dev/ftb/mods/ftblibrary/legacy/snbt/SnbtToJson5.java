package dev.ftb.mods.ftblibrary.legacy.snbt;

import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.marhali.json5.Json5Object;
import de.marhali.json5.Json5Primitive;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
//        var file = Path.of(userHome + "/downloads/ftb-skies-2-aero-main/config/ftbquests/quests/chapters/actually_additions.snbt");
        var file = Path.of(userHome + "/Dev/dev.ftb/tooling/snbt-converter/test/data/ftbteambases-server.snbt");
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
        Map<String, Boolean> booleans = new HashMap<>();

        StringBuilder currentComment = null;
        boolean seenFirstNoneCommentLine = false;
        boolean insideBlockComment = false;
        Deque<String> keyStack = new ArrayDeque<>();
        for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            String trimmedLine = line.trim();

            var isComment = insideBlockComment || isCommentLine(line);
            if (isComment) {
                // Active "inside" block comment state so we know how to handle lines inside a multi-line block comment.
                if (insideBlockComment) {
                    // A previously opened /* block closes once we see its matching */.
                    if (trimmedLine.contains("*/")) {
                        insideBlockComment = false;
                    }
                } else if (trimmedLine.startsWith("/*") && !trimmedLine.contains("*/")) {
                    insideBlockComment = true;
                }

                // If we haven't seen a non-comment line yet, we should skip this line and not add it to the output.
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

            // If we haven't seen a non-comment line yet, we should skip this line and not add it to the output.
            if (!trimmedLine.isEmpty()) {
                seenFirstNoneCommentLine = true;
            }

            // We're not a comment. We should check and commit if we're a key.
            var keyEnd = trimmedLine.indexOf(": ");
            var isKey = keyEnd > 0 && looksLikeKey(trimmedLine.substring(0, keyEnd).trim());
            if (isKey) {
                var key = trimmedLine.substring(0, keyEnd).trim();
                var fullKey = keyStack.isEmpty() ? key : String.join(".", keyStack) + "." + key;

                // If we have a current comment, we should commit it to the comments map.
                if (currentComment != null) {
                    comments.put(fullKey, currentComment.toString());
                    currentComment = null;
                }

                // Poor man's check for the value, then to see if that original value was a boolean.
                var value = trimmedLine.substring(keyEnd + 2).trim();

                // Continue to push forwards when trailing commas are present "key: true," or "key: false,"
                while (value.endsWith(",")) {
                    value = value.substring(0, value.length() - 1).trim();
                }

                // If we're a true or false, we shouldn't default back to a 0/1 number like vanilla's system does.
                // So we'll store the original boolean and restore it later on.
                if (value.equals("true") || value.equals("false")) {
                    booleans.put(fullKey, Boolean.parseBoolean(value));
                }

                // If we're a key with an opening brace, we should push the key onto the stack,
                // regardless of whether it had a comment attached to it.
                if (trimmedLine.endsWith("{")) {
                    keyStack.add(key);
                }
            }

            // If we're a closing brace, we should pop the last key from the stack.
            if (trimmedLine.startsWith("}")) {
                if (!keyStack.isEmpty()) {
                    keyStack.removeLast();
                }
            }

            // Add commas, but not to lines that open a multi-line block aka compound, list or typed array
            if (trimmedLine.endsWith("[") ||
                    trimmedLine.endsWith("{") ||
                    trimmedLine.endsWith(";") ||
                    (trimmedLine.startsWith("{") && !trimmedLine.endsWith("}")) ||
                    i == lines.length - 1 ||
                    trimmedLine.isEmpty()
            ) {
                output.append(line).append("\n");
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
        attachBooleans(asObject, booleans, "");
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

    /// Recursively restores real JSON5 booleans for keys that were written as literal true/false,
    /// but got flattened into plain 0/1 numbers by vanilla's SNBT parser.
    ///
    /// @param asObject the json5 object to restore booleans in
    /// @param booleans the map of keys to their original boolean value
    /// @param lastKey the last key in the hierarchy, used to build the full key
    private static void attachBooleans(Json5Object asObject, Map<String, Boolean> booleans, String lastKey) {
        List<String> keysToRestore = new ArrayList<>();

        for (var entry : asObject.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var fullKey = lastKey.isEmpty() ? key : lastKey + "." + key;
            if (booleans.containsKey(fullKey)) {
                keysToRestore.add(key);
            } else if (value.isJson5Object()) {
                attachBooleans(value.getAsJson5Object(), booleans, fullKey);
            }
        }

        // Replace after iterating, rather than while iterating over entrySet().
        for (var key : keysToRestore) {
            var fullKey = lastKey.isEmpty() ? key : lastKey + "." + key;
            asObject.add(key, Json5Primitive.fromBoolean(booleans.get(fullKey)));
        }
    }

    /// Trims out the comment markers and whitespace from a comment line.
    ///
    /// @param commentLine the comment line
    /// @return the normalized comment
    private static String normalizeComment(String commentLine) {
        var comment = commentLine.trim();

        if (comment.endsWith("*/")) {
            comment = comment.substring(0, comment.length() - 2).trim();
        }

        if (comment.startsWith("/*") || comment.startsWith("//")) {
            comment = comment.substring(2);
        } else if (comment.startsWith("#") || comment.startsWith("*")) {
            comment = comment.substring(1);
        }

        return comment.trim();
    }

    /// Checks whether a candidate key (the text before the first ": ") is actually a key, and not
    /// just a quoted string value (e.g. a list element like "type: item") that happens to contain ": ".
    ///
    /// @param candidate the trimmed text before the first ": " on the line
    /// @return true if the candidate looks like a real (bare or fully-quoted) key
    private static boolean looksLikeKey(String candidate) {
        if (candidate.isEmpty()) {
            return false;
        }

        var first = candidate.charAt(0);
        if (first == '"' || first == '\'') {
            return candidate.length() >= 2 && candidate.charAt(candidate.length() - 1) == first;
        }

        return true;
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
