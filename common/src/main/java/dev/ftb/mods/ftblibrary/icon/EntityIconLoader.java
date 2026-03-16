package dev.ftb.mods.ftblibrary.icon;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonManager;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.RegistryHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Handles loading of Entity {@link Icon} Json or Image files
 * Loads all entity types except for MISC, which only show on the map if a json / image exists
 */
public class EntityIconLoader extends SimplePreparableReloadListener<Map<EntityType<?>, EntityIconLoader.EntityIconSettings>> {
    public static final Icon<?> NORMAL = Icon.getIcon("ftblibrary:textures/faces/normal.png");
    public static final Icon<?> HOSTILE = Icon.getIcon("ftblibrary:textures/faces/hostile.png");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<EntityType<?>, Map<Identifier, Icon<?>>> ICON_CACHE = new HashMap<>();
    private static final Map<EntityType<?>, EntityIconSettings> ENTITY_SETTINGS = new HashMap<>();
    private static final Set<EntityType<?>> DYNAMIC_JSON_TEXTURES = new HashSet<>();

    public static final EntityIconLoader INSTANCE = new EntityIconLoader();

    private EntityIconLoader() {
    }

    @Override
    protected Map<EntityType<?>, EntityIconSettings> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<EntityType<?>, EntityIconSettings> map = new HashMap<>();

        DYNAMIC_JSON_TEXTURES.clear();
        Registry<EntityType<?>> registry = RegistryHelper.getRegistry(Registries.ENTITY_TYPE);
        if (registry == null) {
            LOGGER.error("Failed to get EntityType registry, skipping loading entity icons");
            return map;
        }

        for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : registry.entrySet()) {
            Identifier id = entry.getKey().identifier();
            EntityType<?> entityType = entry.getValue();

            String basePath = getBasePath(id);

            Identifier invisible = FTBLibrary.rl(basePath + ".invisible");

            EntityIconSettings entityIconSettings = null;

            if (resourceManager.getResource(invisible).isPresent()) {
                LOGGER.error("Entity {} is using legacy invisible texture, please update it to use the new system!", id);
                entityIconSettings = EntityIconSettings.legacy();
            }

            Optional<Resource> resource = resourceManager.getResource(FTBLibrary.rl(basePath + ".json"));
            if (resource.isPresent()) {
                entityIconSettings = loadEntitySetting(id, resource.get());
                DYNAMIC_JSON_TEXTURES.add(entityType);
            } else {
                Identifier imgLoc = FTBLibrary.rl(basePath + ".png");
                if (resourceManager.getResource(imgLoc).isPresent()) {
                    entityIconSettings = EntityIconSettings.forImage(imgLoc);
                }
            }

            if (entityIconSettings == null && entityType.getCategory() != MobCategory.MISC) {
                if (ModUtils.isDevMode()) {
                    LOGGER.error("Missing entity icon settings for {}", id);
                }
                entityIconSettings = EntityIconSettings.legacy();
            }

            if (entityIconSettings != null) {
                map.put(entityType, entityIconSettings);
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<EntityType<?>, EntityIconSettings> entityIconDataMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        ICON_CACHE.clear();
        ENTITY_SETTINGS.clear();
        ENTITY_SETTINGS.putAll(entityIconDataMap);
        FTBLibrary.LOGGER.debug("loaded {} entity face icons ({} using dynamic json textures)", entityIconDataMap.size(), DYNAMIC_JSON_TEXTURES.size());
    }

    public static boolean isDynamicTexture(EntityType<?> type) {
        return DYNAMIC_JSON_TEXTURES.contains(type);
    }

    private static String getBasePath(Identifier id) {
        return "textures/faces/" + id.getNamespace() + "/" + id.getPath();
    }

    @Nullable
    private EntityIconSettings loadEntitySetting(Identifier id, Resource resource) {
        try {
            JsonElement jsonElement = GsonHelper.fromJson(GSON, resource.openAsReader(), JsonElement.class);
            DataResult<EntityIconSettings> settings = EntityIconSettings.CODEC.parse(JsonOps.INSTANCE, jsonElement);
            return settings.getOrThrow();
        } catch (IOException e) {
            LOGGER.error("Failed to load entity icon settings for {}", id, e);
        } catch (IllegalStateException e) {
            LOGGER.error("Failed to parse entity icon settings for {}", id, e);
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Entity> Optional<Icon<?>> getIconCache(T entity) {
        EntityRenderer<? super T, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        EntityRenderState state = renderer.createRenderState(entity, 0f);
        if (renderer instanceof LivingEntityRenderer/*<?,?,?>*/ entityRenderer && state instanceof LivingEntityRenderState ls) {
            return getSettings(entity.getType()).map(settings -> settings.useMobTexture ?
                    getOrCreateIcon(entity.getType(), entityRenderer.getTextureLocation(ls), settings) :
                    settings.texture.map(texture -> getOrCreateIcon(entity.getType(), texture, settings)).orElse(null));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<EntityIconSettings> getSettings(EntityType<?> entityType) {
        return Optional.ofNullable(ENTITY_SETTINGS.get(entityType));
    }

    private static Icon<?> getOrCreateIcon(EntityType<?> entityType, Identifier texture, EntityIconSettings settings) {
        return ICON_CACHE
                .computeIfAbsent(entityType, i -> new HashMap<>())
                .computeIfAbsent(texture, t -> new EntityImageIcon(t, settings.mainSlice.orElse(null), settings.children, settings.defaultImageSize.orElse(null)));
    }

    public static Icon<?> getIcon(Entity entity) {
        return getIconCache(entity).orElseGet(() -> entity instanceof Enemy ? EntityIconLoader.HOSTILE : EntityIconLoader.NORMAL);
    }

    public static Icon<?> getIcon(EntityType<?> entityType) {
        Entity entity = entityType.create(ClientUtils.getClientLevel(), EntitySpawnReason.LOAD);
        return entity == null ? EntityIconLoader.NORMAL : getIcon(entity);
    }

    public record EntityIconSettings(
            boolean useMobTexture,
            Optional<Identifier> texture,
            Optional<EntityImageIcon.Slice> mainSlice,
            List<EntityImageIcon.ChildIconData> children,
            WidthHeight widthHeight,
            Optional<WidthHeight> defaultImageSize,
            double scale,
            boolean defaultEnabled)
    {
        private static final EntityIconSettings OLD_HIDDEN = new EntityIconSettings(
                false, Optional.empty(), Optional.empty(),
                List.of(), WidthHeight.DEFAULT, Optional.empty(), 1D, true
        );

        public static final Codec<EntityIconSettings> CODEC = RecordCodecBuilder.<EntityIconSettings>create(builder -> builder.group(
                        Codec.BOOL.optionalFieldOf("use_mob_texture", false).forGetter(s -> s.useMobTexture),
                        Identifier.CODEC.optionalFieldOf("texture").forGetter(s -> s.texture),
                        EntityImageIcon.Slice.CODEC.optionalFieldOf("slice").forGetter(entityIconData -> entityIconData.mainSlice),
                        EntityImageIcon.ChildIconData.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter(entityIconData -> entityIconData.children),
                        WidthHeight.CODEC.optionalFieldOf("size", WidthHeight.DEFAULT).forGetter(s -> s.widthHeight),
                        WidthHeight.CODEC.optionalFieldOf("default_image_size").forGetter(s -> s.defaultImageSize),
                        Codec.DOUBLE.optionalFieldOf("scale", 1D).forGetter(s -> s.scale),
                        Codec.BOOL.optionalFieldOf("default_enabled", true).forGetter(s -> s.defaultEnabled)
                ).apply(builder, EntityIconSettings::new)
        ).validate(settings -> settings.texture().isEmpty() && !settings.useMobTexture ?
                DataResult.error(() -> "Texture is required if use_mob_texture is false") :
                DataResult.success(settings)
        );

        public static EntityIconSettings forImage(Identifier imgLoc) {
            return new EntityIconSettings(false, Optional.of(imgLoc), Optional.empty(),
                    List.of(), WidthHeight.DEFAULT, Optional.empty(), 1D, true);
        }

        public static EntityIconSettings legacy() {
            return OLD_HIDDEN;
        }
    }

    public record WidthHeight(int width, int height) {
        public static final WidthHeight DEFAULT = new WidthHeight(16, 16);

        public static final Codec<WidthHeight> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("width").forGetter(WidthHeight::width),
                Codec.INT.fieldOf("height").forGetter(WidthHeight::height)
        ).apply(instance, WidthHeight::new));
    }
}
