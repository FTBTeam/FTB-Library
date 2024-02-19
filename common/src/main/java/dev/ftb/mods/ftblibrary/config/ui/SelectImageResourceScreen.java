package dev.ftb.mods.ftblibrary.config.ui;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.ResourceConfigValue;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SelectImageResourceScreen extends ResourceSelectorScreen<ResourceLocation> {
    private static final ResourceSearchMode<ResourceLocation> ALL_IMAGES = new AllImagesMode();

    public static final SearchModeIndex<ResourceSearchMode<ResourceLocation>> KNOWN_MODES = new SearchModeIndex<>();
    private static final SelectableResource<ResourceLocation> NO_IMAGE = new SelectableResource.ImageResource(ImageResourceConfig.NONE);

    static {
        KNOWN_MODES.appendMode(ALL_IMAGES);
    }

    private static List<SelectableResource.ImageResource> cachedImages = null;

    public SelectImageResourceScreen(ResourceConfigValue<ResourceLocation> config, ConfigCallback callback) {
        super(config, callback);
    }

    @Override
    protected ResourceSelectorScreen<ResourceLocation>.ResourceButton makeResourceButton(Panel panel, @Nullable SelectableResource<ResourceLocation> resource) {
        return new ImageButton(panel, Objects.requireNonNullElse(resource, NO_IMAGE));
    }

    @Override
    protected SearchModeIndex<ResourceSearchMode<ResourceLocation>> getSearchModeIndex() {
        return KNOWN_MODES;
    }

    private static boolean isValidImage(ResourceLocation id) {
        return !id.getPath().startsWith("textures/font/");
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
            ModUtils.getModName(getStack().getNamespace())
                    .ifPresent(name -> list.add(Component.literal(name).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
        }
    }

    private static class AllImagesMode implements ResourceSearchMode<ResourceLocation> {
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
                    if (!ResourceLocation.isValidResourceLocation(rl.toString())) {
                        FTBLibrary.LOGGER.warn("Image " + rl + " has invalid path! Report this to author of '" + rl.getNamespace() + "'!");
                    } else if (isValidImage(rl)) {
                        images.add(rl);
                    }
                });

                cachedImages = images.stream().sorted().map(res -> {
                    // shorten <mod>:textures/A/B.png to <mod>:A/B
                    ResourceLocation res1 = new ResourceLocation(res.getNamespace(), res.getPath().substring(9, res.getPath().length() - 4));
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

}
