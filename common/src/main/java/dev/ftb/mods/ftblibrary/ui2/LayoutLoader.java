package dev.ftb.mods.ftblibrary.ui2;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class LayoutLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    public static final LayoutLoader INSTANCE = new LayoutLoader();

    private final Map<String, Layout> layouts = new HashMap<>();

    private LayoutLoader() {
        super(GSON, "layouts");
    }

    @Nullable
    public Layout findTemplate(String template) {
        return layouts.get(template);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                throw new IllegalArgumentException("Layout file must be a JSON object: " + entry.getKey());
            }

            var jsonObject = entry.getValue().getAsJsonObject();

            String name = entry.getKey().getPath();
            // TODO: improve type safety. Do some validation of the json structure before we blindly trust it.
            Layout layout = GSON.fromJson(entry.getValue(), Layout.class);

            if (layouts.containsKey(name)) {
                var hasReplaceFlag = jsonObject.has("replace") && jsonObject.get("replace").getAsBoolean();
                if (!hasReplaceFlag) {
                    throw new IllegalArgumentException("Duplicate layout name found: " + name + " and does not indicate it should replace the existing one. Add \"replace\": true to the layout file to replace the existing layout.");
                }
            }

            layouts.put(name, layout);
        }
    }
}
