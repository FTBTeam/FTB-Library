package dev.ftb.mods.ftblibrary.config.ui.resource;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public interface ResourceSearchMode<T> {
    ResourceSearchMode<ItemStack> ALL_ITEMS = new SearchMode<>(Component.translatable("ftblibrary.select_item.list_mode.all"), Icons.COMPASS) {
        private List<SelectableResource<ItemStack>> allItemsCache = null;

        @Override
        public Collection<? extends SelectableResource<ItemStack>> getAllResources() {
            if (allItemsCache == null) {
                CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS, false, ClientUtils.registryAccess());
                allItemsCache = CreativeModeTabs.searchTab().getDisplayItems().stream()
                        .map(SelectableResource::item)
                        .toList();
            }
            return allItemsCache;
        }
    };
    ResourceSearchMode<ItemStack> INVENTORY = new SearchMode<>(Component.translatable("ftblibrary.select_item.list_mode.inv"), ItemIcon.ofItem(Items.CHEST)) {
        @Override
        public Collection<? extends SelectableResource<ItemStack>> getAllResources() {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return Collections.emptySet();
            }

            var invSize = player.getInventory().getContainerSize();
            List<SelectableResource<ItemStack>> items = new ArrayList<>(invSize);
            for (var i = 0; i < invSize; i++) {
                var stack = player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    items.add(SelectableResource.item(stack));
                }
            }
            return items;
        }
    };
    ResourceSearchMode<FluidStack> ALL_FLUIDS = new SearchMode<>(Component.translatable("ftblibrary.select_fluid.list_mode.all"), ItemIcon.ofItem(Items.COMPASS)) {
        private List<SelectableResource<FluidStack>> allFluidsCache = null;

        @Override
        public Collection<? extends SelectableResource<FluidStack>> getAllResources() {
            if (allFluidsCache == null) {
                List<SelectableResource<FluidStack>> fluidstacks = new ArrayList<>();
                BuiltInRegistries.FLUID.forEach(f -> {
                    if (f.isSource(f.defaultFluidState())) {
                        fluidstacks.add(SelectableResource.fluid(FluidStack.create(f, FluidStackHooks.bucketAmount())));
                    }
                });
                allFluidsCache = List.copyOf(fluidstacks);
            }
            return allFluidsCache;
        }
    };
    ResourceSearchMode<EntityType<?>> ENTITY_FACES = new SearchMode<>(Component.translatable("ftblibrary.select_entity.all_entities"), Icons.PLAYER) {
        private List<SelectableResource<EntityType<?>>> allTypesCache = null;

        @Override
        public Collection<? extends SelectableResource<EntityType<?>>> getAllResources() {
            if (allTypesCache == null) {
                List<SelectableResource<EntityType<?>>> types = new ArrayList<>();
                BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
                    if (entityType.create(Minecraft.getInstance().level, EntitySpawnReason.LOAD) instanceof LivingEntity) {
                        types.add(new EntityFaceResource(entityType));
                    }
                });
                allTypesCache = types.stream().sorted().toList();
            }
            return allTypesCache;
        }
    };
    ResourceSearchMode<Identifier> IMAGES = new SearchMode<>(Component.translatable("ftblibrary.select_image.all_images"), Icons.ART) {
        private List<ImageResource> cachedImages = null;

        @Override
        public void clearCache() {
            cachedImages = null;
        }

        @Override
        public Collection<? extends SelectableResource<Identifier>> getAllResources() {
            if (cachedImages == null) {
                List<Identifier> images = new ArrayList<>();

                StringUtils.ignoreIdentifierErrors = true;
                Map<Identifier, Resource> textures = Collections.emptyMap();

                try {
                    textures = Minecraft.getInstance().getResourceManager().listResources("textures", t -> t.getPath().endsWith(".png"));
                } catch (Exception ex) {
                    FTBLibrary.LOGGER.error("A mod has a broken resource preventing this list from loading: {}", String.valueOf(ex));
                }

                StringUtils.ignoreIdentifierErrors = false;

                textures.keySet().forEach(rl -> Identifier.read(rl.toString()).result().ifPresentOrElse(
                        images::add,
                        () -> FTBLibrary.LOGGER.warn("Image {} has invalid path! Report this to author of '{}'!", rl, rl.getNamespace())
                ));

                TextureAtlas blockAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
                cachedImages = images.stream().sorted().map(res -> {
                    // shorten <mod>:textures/A/B.png to <mod>:A/B
                    Identifier res1 = Identifier.fromNamespaceAndPath(res.getNamespace(), res.getPath().substring(9, res.getPath().length() - 4));
                    TextureAtlasSprite sprite = blockAtlas.getSprite(res1);
                    if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
                        res1 = res;
                    }
                    return new ImageResource(res1);
                }).toList();
            }
            return cachedImages;
        }
    };

    abstract class SearchMode<T> implements ResourceSearchMode<T> {
        private final Component name;
        private final Icon<?> icon;

        protected SearchMode(Component name, Icon<?> icon) {
            this.name = name;
            this.icon = icon;
        }

        @Override
        public Component getDisplayName() {
            return name;
        }

        @Override
        public Icon<?> getIcon() {
            return icon;
        }
    }

    /**
     * The icon used to represent this mode, for example on buttons and other widgets.
     */
    Icon<?> getIcon();

    /**
     * The name used to describe this mode.
     */
    Component getDisplayName();

    /**
     * Gets an *unfiltered* collection of all items available in the current search mode.
     */
    Collection<? extends SelectableResource<T>> getAllResources();

    /**
     * Clears any cached data, if necessary. Called on a resource reload. Override if cached resources need to be
     * cleared.
     */
    default void clearCache(){
    }
}
