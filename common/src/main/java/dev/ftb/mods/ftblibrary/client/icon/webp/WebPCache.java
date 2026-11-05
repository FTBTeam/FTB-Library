package dev.ftb.mods.ftblibrary.client.icon.webp;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.icon.webp.WebPAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads and caches parsed WebP images (from resource packs, files or URLs) off the render thread.
 */
public final class WebPCache {
    private static final int MAX_BYTES = 16 * 1024 * 1024;
    private static final int TIMEOUT_MS = 10_000;

    private static final ExecutorService LOADER = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "FTBLibrary WebP loader");
        t.setDaemon(true);
        return t;
    });
    private static final Map<String, Entry> ENTRIES = new ConcurrentHashMap<>();

    private WebPCache() {
    }

    /**
     * Get (starting a load if necessary) the cache entry for a resource id or URI.
     */
    public static Entry get(Identifier id, @Nullable URI uri) {
        String key = uri != null ? uri.toString() : id.toString();
        return ENTRIES.computeIfAbsent(key, k -> {
            Entry entry = new Entry();
            LOADER.execute(() -> entry.load(id, uri));
            return entry;
        });
    }

    /**
     * Forget everything, e.g. on resource reload. Existing entries' textures are the owners' responsibility.
     */
    public static void clear() {
        ENTRIES.clear();
    }

    private static byte[] readAll(InputStream in) throws IOException {
        try (in) {
            byte[] data = in.readNBytes(MAX_BYTES + 1);
            if (data.length > MAX_BYTES) {
                throw new IOException("WebP file too large");
            }
            return data;
        }
    }

    public static class Entry {
        private volatile @Nullable WebPAnimation animation;
        private volatile boolean failed;
        private @Nullable Identifier firstFrameId;

        private void load(Identifier id, @Nullable URI uri) {
            try {
                byte[] data;
                if (uri != null) {
                    URLConnection conn = uri.toURL().openConnection(Minecraft.getInstance().getProxy());
                    conn.setConnectTimeout(TIMEOUT_MS);
                    conn.setReadTimeout(TIMEOUT_MS);
                    data = readAll(conn.getInputStream());
                } else {
                    data = readAll(Minecraft.getInstance().getResourceManager().getResource(id).orElseThrow().open());
                }
                animation = WebPAnimation.parse(data);
            } catch (Exception e) {
                FTBLibrary.LOGGER.warn("Failed to load WebP image {}: {}", uri != null ? uri : id, e.toString());
                failed = true;
            }
        }

        public @Nullable WebPAnimation getAnimation() {
            return animation;
        }

        public boolean isFailed() {
            return failed;
        }

        /**
         * @return the registered texture id of the (static) first frame, or null if not available (yet). Render thread only.
         */
        public @Nullable Identifier getFirstFrameTexture(Identifier id) {
            if (firstFrameId == null) {
                WebPAnimation anim = animation;
                if (anim == null) {
                    return null;
                }
                try {
                    WebPTexture tex = new WebPTexture(anim.getWidth(), anim.getHeight());
                    tex.uploadFrame(anim.decodeFirstFrame());
                    firstFrameId = id;
                    Minecraft.getInstance().getTextureManager().register(id, tex);
                } catch (Exception e) {
                    FTBLibrary.LOGGER.warn("Failed to decode WebP image {}: {}", id, e.toString());
                    failed = true;
                    animation = null;
                    return null;
                }
            }
            return firstFrameId;
        }
    }
}
