package dev.ftb.mods.ftblibrary.icon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.registry.registries.Registries;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author LatvianModder
 */
public class ItemIcon extends Icon {
	private final ItemStack stack;

	public static Icon getItemIcon(ItemStack stack) {
		if (stack.isEmpty()) {
			return EMPTY;
		} else if (stack.getItem() instanceof CustomIconItem) {
			return ((CustomIconItem) stack.getItem()).getCustomIcon(stack);
		}

		return new ItemIcon(stack);
	}

	public static Icon getItemIcon(Item item) {
		return item == Items.AIR ? EMPTY : getItemIcon(new ItemStack(item));
	}

	public static Icon getItemIcon(String lazyStackString) {
		if (lazyStackString.isEmpty()) {
			return EMPTY;
		}

		return new LazyIcon(() -> {
			String[] s = lazyStackString.split(" ", 4);
			ItemStack stack = new ItemStack(Registry.ITEM.get(new ResourceLocation(s[0])));

			if (s.length >= 2 && !s[1].equals("1")) {
				stack.setCount(Integer.parseInt(s[1]));
			}

			if (s.length >= 3 && !s[2].equals("0")) {
				stack.setDamageValue(Integer.parseInt(s[2]));
			}

			if (s.length >= 4 && !s[3].equals("null")) {
				try {
					stack.setTag(TagParser.parseTag(s[3]));
				} catch (CommandSyntaxException ex) {
					ex.printStackTrace();
				}
			}

			if (stack.isEmpty()) {
				stack = new ItemStack(Items.BARRIER);
				stack.setHoverName(new TextComponent(lazyStackString));
			}

			return getItemIcon(stack);
		}) {
			@Override
			public String toString() {
				return "item:" + lazyStackString;
			}
		};
	}

	private ItemIcon(ItemStack is) {
		stack = is;
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(PoseStack matrixStack, int x, int y, int w, int h) {
		matrixStack.pushPose();
		matrixStack.translate(0, 0, 100);
		GuiHelper.drawItem(matrixStack, getStack(), x, y, w / 16F, h / 16F, true, null);
		matrixStack.popPose();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void drawStatic(PoseStack matrixStack, int x, int y, int w, int h) {
		matrixStack.pushPose();
		matrixStack.translate(0, 0, 100);
		GuiHelper.drawItem(matrixStack, getStack(), x, y, w / 16F, h / 16F, false, null);
		matrixStack.popPose();
	}

	@Environment(EnvType.CLIENT)
	public static void drawItem3D(PoseStack matrixStack, ItemStack stack) {
		//FIXME: Draw flat 3D item
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, 240, OverlayTexture.NO_OVERLAY, matrixStack, Minecraft.getInstance().renderBuffers().bufferSource(), 0);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw3D(PoseStack matrixStack) {
		drawItem3D(matrixStack, getStack());
	}

	public String toString() {
		ItemStack is = getStack();
		StringBuilder builder = new StringBuilder("item:");
		builder.append(Registries.getId(is.getItem(), Registry.ITEM_REGISTRY));
		int count = is.getCount();
		int damage = is.getDamageValue();
		CompoundTag nbt = is.getTag();

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
		ItemStack stack = getStack();
		int h = stack.getItem().hashCode();
		h = h * 31 + stack.getCount();
		h = h * 31 + Objects.hashCode(stack.getTag());
		return h;
	}

	public boolean equals(Object o) {
		return o == this || o instanceof ItemIcon && ItemStack.matches(getStack(), ((ItemIcon) o).getStack());
	}

	@Override
	@Nullable
	public Object getIngredient() {
		return getStack();
	}
}