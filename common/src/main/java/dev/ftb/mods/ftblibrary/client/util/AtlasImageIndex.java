package dev.ftb.mods.ftblibrary.client.util;

import dev.ftb.mods.ftblibrary.mixin.common.SpriteSourceListAccess;
import dev.ftb.mods.ftblibrary.mixin.common.TextureAtlasAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An index of every known image resource, recording which texture atlas (if any) each one is stitched into.
 * <p>
 * The atlas membership is worked out by re-running each atlas's sprite sources (as defined in
 * {@code assets/<ns>/atlases/<name>.json}) against a recording resource manager, which captures the file behind each
 * sprite id. The result is reconciled against the sprites the atlas actually stitched, so filtered-out files are
 * not reported as atlas members, and sprites with no backing file (unstitched/generated) are reported separately.
 * <p>
 * Build this only after resources have finished reloading (atlases are stitched as part of the reload).
 */
public final class AtlasImageIndex {
    /**
     *  A sprite within an atlas.
     *
     * @param atlasId the atlas definition, e.g. {@code minecraft:blocks}
     * @param spriteId identifier of the sprite within this atlas
     **/
    public record AtlasSprite(Identifier atlasId, Identifier spriteId) {
        @Override
        public String toString() {
            return atlasId + "#" + spriteId;
        }
    }

    /**
     * An image resource.
     *
     * @param fileId the full resource path (e.g. {@code minecraft:textures/block/stone.png}), null for a sprite which has no single backing file
     * @param atlasSprites possibly empty (for non-atlas images) list of atlas sprites for this image
     */
    public record ImageEntry(@Nullable Identifier fileId, List<AtlasSprite> atlasSprites) {
        public boolean isStandalone() {
            return atlasSprites.isEmpty();
        }
    }

    private record AtlasStats(int stitched, int fromFiles, int fileless, int notStitched) {}

    private final List<ImageEntry> entries;
    private final Map<Identifier, AtlasStats> atlasStats;

    private AtlasImageIndex(List<ImageEntry> entries, Map<Identifier, AtlasStats> atlasStats) {
        this.entries = entries;
        this.atlasStats = atlasStats;
    }

    /**
     * @return Every image file (atlas-member or standalone) plus any file-less atlas sprites.
     */
    public List<ImageEntry> entries() {
        return entries;
    }

    /**
     * @return Image files which are not part of any atlas.
     */
    public List<ImageEntry> standalone() {
        return entries.stream().filter(e -> e.fileId() != null && e.isStandalone()).toList();
    }

    /**
     * @return Atlas sprites which are not backed by a single image file.
     */
    public List<ImageEntry> filelessSprites() {
        return entries.stream().filter(e -> e.fileId() == null).toList();
    }

    /**
     * The ids to offer for selection: the sprite id for each sprite in an atlas (once, even if it's in several), or
     * the full file id for standalone images.
     */
    public List<Identifier> selectableIds() {
        Set<Identifier> ids = new LinkedHashSet<>();
        for (ImageEntry e : entries) {
            if (e.isStandalone()) {
                if (e.fileId() != null) {
                    ids.add(e.fileId());
                }
            } else {
                e.atlasSprites().forEach(as -> ids.add(as.spriteId()));
            }
        }
        return List.copyOf(ids);
    }

    public static AtlasImageIndex build(ResourceManager resourceManager) {
        Map<Identifier, List<AtlasSprite>> byFile = new HashMap<>();
        List<AtlasSprite> fileless = new ArrayList<>();
        Map<Identifier, AtlasStats> stats = new TreeMap<>();

        // shared by all atlases, so the (expensive) scan of every pack's textures is only done once
        RecordingResourceManager recorder = new RecordingResourceManager(resourceManager);

        Minecraft.getInstance().getAtlasManager().forEach((atlasId, atlas) -> {
            Set<Identifier> stitched = new HashSet<>(((TextureAtlasAccess) atlas).getTexturesByName().keySet());
            stitched.remove(MissingTextureAtlasSprite.getLocation());

            int total = stitched.size();
            Map<Identifier, @Nullable Identifier> spriteToFile = collectSprites(resourceManager, recorder, atlasId);

            int fromFiles = 0, notStitched = 0;
            for (var e : spriteToFile.entrySet()) {
                if (!stitched.remove(e.getKey())) {
                    notStitched++;
                    continue;
                }
                AtlasSprite as = new AtlasSprite(atlasId, e.getKey());
                if (e.getValue() != null) {
                    byFile.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(as);
                    fromFiles++;
                } else {
                    fileless.add(as);
                }
            }
            // whatever is left was stitched but not seen by us (e.g. produced by a source we couldn't observe)
            stitched.forEach(id -> fileless.add(new AtlasSprite(atlasId, id)));

            stats.put(atlasId, new AtlasStats(total, fromFiles, total - fromFiles, notStitched));
        });

        List<ImageEntry> entries = new ArrayList<>();
        recorder.listResources("textures", id -> id.getPath().endsWith(".png") || id.getPath().endsWith(".webp"))
                .keySet().stream().sorted()
                .forEach(file -> entries.add(new ImageEntry(file, List.copyOf(byFile.getOrDefault(file, List.of())))));
        fileless.forEach(as -> entries.add(new ImageEntry(null, List.of(as))));

        return new AtlasImageIndex(List.copyOf(entries), stats);
    }

    /** Run the atlas's sprite sources, returning sprite id -> backing file (null if there's no single backing file) */
    private static Map<Identifier, @Nullable Identifier> collectSprites(ResourceManager resourceManager, RecordingResourceManager recorder, Identifier atlasId) {
        Map<Identifier, @Nullable Identifier> result = new LinkedHashMap<>();

        SpriteSource.Output output = new SpriteSource.Output() {
            @Override
            public void add(Identifier id, Resource resource) {
                result.put(id, recorder.fileFor(resource));
            }

            @Override
            public void add(Identifier id, SpriteSource.DiscardableLoader sprite) {
                // never loaded here; release any image data the loader may be holding
                sprite.discard();
                result.put(id, null);
            }

            @Override
            public void removeAll(Predicate<Identifier> predicate) {
                result.keySet().removeIf(predicate);
            }
        };

        for (SpriteSource source : ((SpriteSourceListAccess) SpriteSourceList.load(resourceManager, atlasId)).getSources()) {
            try {
                source.run(recorder, output);
            } catch (Exception ex) {
                LogManager.getLogger(AtlasImageIndex.class)
                        .warn("Failed to run sprite source {} for atlas {}: {}", source, atlasId, ex.toString());
            }
        }
        return result;
    }

    public void logSummary(Logger logger) {
        logger.debug("Image index: {} entries", entries.size());
        atlasStats.forEach((id, s) -> logger.debug("  atlas {}: {} sprites ({} from files, {} without a single file); {} files not stitched",
                id, s.stitched(), s.fromFiles(), s.fileless(), s.notStitched()));

        long files = entries.stream().filter(e -> e.fileId() != null).count();
        long inAtlas = entries.stream().filter(e -> e.fileId() != null && !e.isStandalone()).count();
        long multi = entries.stream().filter(e -> e.atlasSprites().size() > 1).count();
        logger.debug("  {} image files: {} in an atlas ({} in more than one), {} standalone", files, inAtlas, multi, files - inAtlas);

        Map<String, Integer> standaloneByNs = new TreeMap<>();
        standalone().forEach(e -> standaloneByNs.merge(e.fileId().getNamespace(), 1, Integer::sum));
        logger.debug("  standalone images by namespace: {}", standaloneByNs);
        entries.stream().filter(e -> e.atlasSprites().size() > 1).limit(20)
                .forEach(e -> logger.debug("  multi-atlas file {}: {}", e.fileId(), e.atlasSprites()));
    }

    /**
     * Wraps a resource manager, remembering the path each returned {@link Resource} was found at. Sprite sources only
     * hand the {@code Resource} to their output, not its path; since the same instance is passed through, an identity
     * lookup recovers the path.
     * <p>
     * Listings under {@code textures} are served from a single scan of the real resource manager, held in memory. Each
     * sprite source would otherwise rescan every pack for its own directory, which is very slow with many mods.
     */
    private static class RecordingResourceManager implements ResourceManager {
        private final ResourceManager delegate;
        private final Map<Resource, Identifier> paths = new IdentityHashMap<>();
        @Nullable
        private NavigableMap<Identifier, Resource> allTextures = null;

        RecordingResourceManager(ResourceManager delegate) {
            this.delegate = delegate;
        }

        @Nullable
        Identifier fileFor(Resource resource) {
            return paths.get(resource);
        }

        @Override
        public Set<String> getNamespaces() {
            return delegate.getNamespaces();
        }

        @Override
        public Optional<Resource> getResource(Identifier location) {
            var res = delegate.getResource(location);
            res.ifPresent(r -> paths.put(r, location));
            return res;
        }

        @Override
        public List<Resource> getResourceStack(Identifier location) {
            var res = delegate.getResourceStack(location);
            res.forEach(r -> paths.put(r, location));
            return res;
        }

        @Override
        public Map<Identifier, Resource> listResources(String directory, Predicate<Identifier> filter) {
            if (directory.equals("textures") || directory.startsWith("textures/")) {
                return listTextures(directory + "/", filter);
            }
            var res = delegate.listResources(directory, filter);
            res.forEach((id, r) -> paths.put(r, id));
            return res;
        }

        private Map<Identifier, Resource> listTextures(String prefix, Predicate<Identifier> filter) {
            if (allTextures == null) {
                allTextures = new TreeMap<>(delegate.listResources("textures", id -> true));
                allTextures.forEach((id, r) -> paths.put(r, id));
            }
            Map<Identifier, Resource> result = new TreeMap<>();
            allTextures.forEach((id, r) -> {
                if (id.getPath().startsWith(prefix) && filter.test(id)) {
                    result.put(id, r);
                }
            });
            return result;
        }

        @Override
        public Map<Identifier, List<Resource>> listResourceStacks(String directory, Predicate<Identifier> filter) {
            var res = delegate.listResourceStacks(directory, filter);
            res.forEach((id, l) -> l.forEach(r -> paths.put(r, id)));
            return res;
        }

        @Override
        public Stream<PackResources> listPacks() {
            return delegate.listPacks();
        }
    }
}
