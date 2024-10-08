package dev.ftb.mods.ftblibrary.config.ui;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.ResourceConfigValue;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SelectImageResourceScreen extends ResourceSelectorScreen<ResourceLocation> {
    private static final SelectableResource<ResourceLocation> NO_IMAGE = new SelectableResource.ImageResource(ImageResourceConfig.NONE);
    private static List<SelectableResource.ImageResource> cachedImages = null;

    public static final SearchModeIndex<ResourceSearchMode<ResourceLocation>> KNOWN_MODES = Util.make(
            new SearchModeIndex<>(), idx -> idx.appendMode(AllImagesMode.INSTANCE)
    );

    public SelectImageResourceScreen(ResourceConfigValue<ResourceLocation> config, ConfigCallback callback) {
        super(config, callback);
    }

    private static void clearCachedImages() {
        cachedImages = null;
    }

    private static boolean isValidImage(ResourceLocation id) {
        return !id.getPath().startsWith("textures/font/");
    }

    @Override
    protected ResourceSelectorScreen<ResourceLocation>.ResourceButton makeResourceButton(Panel panel, @Nullable SelectableResource<ResourceLocation> resource) {
        return new ImageButton(panel, Objects.requireNonNullElse(resource, NO_IMAGE));
    }

    @Override
    protected SearchModeIndex<ResourceSearchMode<ResourceLocation>> getSearchModeIndex() {
        return KNOWN_MODES;
    }

    public enum ResourceListener implements ResourceManagerReloadListener {
        INSTANCE;

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            SelectImageResourceScreen.clearCachedImages();
        }
    }

    private enum AllImagesMode implements ResourceSearchMode<ResourceLocation> {
        INSTANCE;

        @Override
        public Icon getIcon() {
            return Icons.ART;
        }

        @Override
        public MutableComponent getDisplayName() {
            return Component.translatable("ftblibrary.select_image.all_images");
        }

        @Override
        public Collection<? extends SelectableResource<ResourceLocation>> getAllResources() {
            if (cachedImages == null) {
                List<ResourceLocation> images = new ArrayList<>();

                StringUtils.ignoreResourceLocationErrors = true;
                Map<ResourceLocation, Resource> textures = Collections.emptyMap();

                try {
                    textures = Minecraft.getInstance().getResourceManager().listResources("textures", t -> t.getPath().endsWith(".png"));
                } catch (Exception ex) {
                    FTBLibrary.LOGGER.error("A mod has a broken resource preventing this list from loading: " + ex);
                }

                StringUtils.ignoreResourceLocationErrors = false;

                textures.keySet().forEach(rl -> {
                    ResourceLocation.read(rl.toString()).result().ifPresentOrElse(
                            images::add,
                            () -> FTBLibrary.LOGGER.warn("Image {} has invalid path! Report this to author of '{}'!", rl, rl.getNamespace())
                    );
                });

                cachedImages = images.stream().sorted().map(res -> {
                    // shorten <mod>:textures/A/B.png to <mod>:A/B
                    ResourceLocation res1 = ResourceLocation.fromNamespaceAndPath(res.getNamespace(), res.getPath().substring(9, res.getPath().length() - 4));
                    TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(res1);
                    SpriteContents contents = sprite.contents();
                    if (contents.name().equals(MissingTextureAtlasSprite.getLocation())) {
                        res1 = res;
                    }
                    return new SelectableResource.ImageResource(res1);
                }).toList();
            }
            return cachedImages;
        }
    }

    private class ImageButton extends ResourceButton {
        protected ImageButton(Panel panel, SelectableResource<ResourceLocation> resource) {
            super(panel, resource);
        }

        @Override
        public boolean shouldAdd(String search) {
            search = search.toLowerCase();
            if (search.isEmpty()) {
                return true;
            } else if (search.startsWith("@")) {
                return getStack().getNamespace().contains(search.substring(1));
            } else {
                return getStack().getPath().contains(search);
            }
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            Component text = Component.literal(getStack().getNamespace()).withStyle(ChatFormatting.GOLD).append(":")
                    .append(Component.literal(getStack().getPath()).withStyle(ChatFormatting.YELLOW));
            list.add(text);
            if (FTBLibraryClientConfig.IMAGE_MODNAME.get()) {
                ModUtils.getModName(getStack().getNamespace())
                        .ifPresent(name -> list.add(Component.literal(name).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
            }
        }
    }

}
