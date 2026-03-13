package dev.ftb.mods.ftblibrary.icon;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.ItemIconRenderer;
import dev.ftb.mods.ftblibrary.util.Lazy;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class ItemIcon extends Icon<ItemIcon> implements IResourceIcon {
    private final ItemStack stack;

    private ItemIcon(ItemStack is) {
        stack = is;
    }

    public static Icon<?> ofItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return empty();
        } else if (stack.getItem() instanceof CustomIconItem c) {
            return c.getCustomIcon(stack);
        }

        return new ItemIcon(stack);
    }

    public static Icon<?> ofItem(Item item) {
        return item == Items.AIR ? empty() : ofItemStack(item.getDefaultInstance());
    }

    public static Icon<?> parse(String lazyStackString) {
        if (lazyStackString.isEmpty()) {
            return empty();
        }

        return new LazyIcon(Lazy.of(() -> {
            var s = lazyStackString.split(" ", 4);
            var stack = new ItemStack(BuiltInRegistries.ITEM.get(Identifier.parse(s[0])).get());

            if (s.length >= 2 && !s[1].equals("1")) {
                stack.setCount(Integer.parseInt(s[1]));
            }

            if (s.length >= 3 && !s[2].equals("0")) {
                stack.setDamageValue(Integer.parseInt(s[2]));
            }

            if (s.length >= 4 && !s[3].equals("null")) {
                try {
                    DataComponentMap.CODEC.parse(NbtOps.INSTANCE, TagParser.parseCompoundFully(s[3]))
                            .resultOrPartial(err -> FTBLibrary.LOGGER.error("can't parse data component map for {}: {}", s[3], err))
                            .ifPresent(stack::applyComponents);
                } catch (CommandSyntaxException ex) {
                    FTBLibrary.LOGGER.error("can't parse data component tag for item icon: {} ({})", lazyStackString, ex.getMessage());
                }
            }

            if (stack.isEmpty()) {
                ItemStack fallback = new ItemStack(Items.BARRIER);
                fallback.set(DataComponents.CUSTOM_NAME, Component.literal(lazyStackString));
                return ofItemStack(fallback);
            }

            return ofItemStack(stack);
        })) {
            @Override
            public String toString() {
                return "item:" + lazyStackString;
            }
        };
    }

    public ItemStack getStack() {
        return stack;
    }

//    @Override
//    public void draw(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
//        var poseStack = graphics.pose();
//        poseStack.pushMatrix();
//        poseStack.translate(x + w / 2F, y + h / 2F);
//
//        if (w != 16 || h != 16) {
//            float s = Math.min(w, h) / 16F;
//            poseStack.scale(s, s);
//        }
//
//        GuiHelper.drawItem(graphics, getStack(), true, null);
//        poseStack.popMatrix();
//    }
//
//    @Override
//    public void drawStatic(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
//        var poseStack = graphics.pose();
//        poseStack.pushMatrix();
//        poseStack.translate(x + w / 2F, y + h / 2F);
//
//        if (w != 16 || h != 16) {
//            float s = Math.min(w, h) / 16F;
//            poseStack.scale(s, s);
//        }
//
//        GuiHelper.drawItem(graphics, getStack(), false, null);
//        poseStack.popMatrix();
//    }
//
//    @Override
//    public void draw3D(GuiGraphicsExtractor graphics) {
//        drawItem3D(graphics, getStack());
//    }

    public String toString() {
        var stack = getStack();
        var builder = new StringBuilder("item:");
        builder.append(RegistrarManager.getId(stack.getItem(), Registries.ITEM));
        var count = stack.getCount();
        var damage = stack.getDamageValue();
        var nbt = DataComponentMap.CODEC.encodeStart(NbtOps.INSTANCE, stack.getComponents()).result()
                .orElse(null);

        if (count > 1 || damage > 0 || nbt != null) {
            builder.append(' ');
            builder.append(count);
        }

        if (damage > 0 || nbt != null) {
            builder.append(' ');
            builder.append(damage);
        }

        if (nbt != null) {
            builder.append(' ');
            builder.append(nbt);
        }

        return builder.toString();
    }

    public int hashCode() {
        return ItemStack.hashItemAndComponents(getStack());
    }

    public boolean equals(Object o) {
        return o == this || o instanceof ItemIcon && ItemStack.matches(getStack(), ((ItemIcon) o).getStack());
    }

    @Override
    @Nullable
    public Object getIngredient() {
        return getStack();
    }

    @Override
    public IconRenderer<ItemIcon> getRenderer() {
        return ItemIconRenderer.INSTANCE;
    }

    @Override
    public Identifier getResourceId() {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }
}
