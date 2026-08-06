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

public class SnbtToJson5 {
    static void main() throws IOException {
        var userHome = System.getProperty("user.home");
//        var file = Path.of(userHome + "/downloads/ftb-skies-2-aero-main/config/ftbbackups3-server.snbt");
        var file = Path.of(userHome + "/downloads/ftb-skies-2-aero-main/config/ftbquests/quests/chapters/actually_additions.snbt");
        var fileData = Files.readString(file);
        var jsonData = convert(fileData);
        System.out.printf("%s", jsonData.toString());
    }

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

    private static String normalizeComment(String cmt) {
        var comment = cmt.trim();
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

    private static boolean isCommentLine(String line) {
        String trimmedLine = line.trim();
        return trimmedLine.startsWith("/*") ||
                trimmedLine.endsWith("*/") ||
                trimmedLine.startsWith("//") ||
                trimmedLine.startsWith("#");
    }
}
