package dev.ftb.mods.ftbguilibrary.icon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbguilibrary.widget.GuiHelper;
import me.shedaniel.architectury.registry.Registries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author LatvianModder
 */
public class ItemIcon extends Icon {
	private static class LazyItemIcon extends ItemIcon {
		private final String lazyStackString;
		private boolean createdStack;

		private LazyItemIcon(String s) {
			super(ItemStack.EMPTY);
			lazyStackString = s;
		}

		@Override
		public ItemStack getStack() {
			if (!createdStack) {
				String[] s = lazyStackString.split(" ", 4);
				stack = new ItemStack(Registry.ITEM.get(new ResourceLocation(s[0])));

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

				createdStack = true;

				if (/* FIXME: FTBLibConfig.debugging.print_more_errors && */stack.isEmpty()) {
					stack = new ItemStack(Items.BARRIER);
					stack.setHoverName(new TextComponent(lazyStackString));
				}
			}

			return stack;
		}

		public String toString() {
			return "item:" + lazyStackString;
		}
	}

	ItemStack stack;

	public static Icon getItemIcon(ItemStack stack) {
		return stack.isEmpty() ? EMPTY : new ItemIcon(stack);
	}

	public static Icon getItemIcon(Item item) {
		return getItemIcon(new ItemStack(item));
	}

	public static Icon getItemIcon(Block block) {
		return getItemIcon(new ItemStack(block));
	}

	public static Icon getItemIcon(String lazyStackString) {
		return lazyStackString.isEmpty() ? EMPTY : new LazyItemIcon(lazyStackString);
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
		h = h + stack.getDamageValue() * 31;
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