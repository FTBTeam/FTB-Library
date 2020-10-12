package com.feed_the_beast.mods.ftbguilibrary.icon;

import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author LatvianModder
 */
public class ItemIcon extends Icon
{
	private static class LazyItemIcon extends ItemIcon
	{
		private final String lazyStackString;
		private boolean createdStack;

		private LazyItemIcon(String s)
		{
			super(ItemStack.EMPTY);
			lazyStackString = s;
		}

		@Override
		public ItemStack getStack()
		{
			if (!createdStack)
			{
				String[] s = lazyStackString.split(" ", 4);
				stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(s[0])));

				if (s.length >= 2 && !s[1].equals("1"))
				{
					stack.setCount(Integer.parseInt(s[1]));
				}

				if (s.length >= 3 && !s[2].equals("0"))
				{
					stack.setDamage(Integer.parseInt(s[2]));
				}

				if (s.length >= 4 && !s[3].equals("null"))
				{
					try
					{
						stack.setTag(JsonToNBT.getTagFromJson(s[3]));
					}
					catch (CommandSyntaxException ex)
					{
						ex.printStackTrace();
					}
				}

				createdStack = true;

				if (/* FIXME: FTBLibConfig.debugging.print_more_errors && */stack.isEmpty())
				{
					stack = new ItemStack(Items.BARRIER);
					stack.setDisplayName(new StringTextComponent(lazyStackString));
				}
			}

			return stack;
		}

		public String toString()
		{
			return "item:" + lazyStackString;
		}
	}

	ItemStack stack;

	public static Icon getItemIcon(ItemStack stack)
	{
		return stack.isEmpty() ? EMPTY : new ItemIcon(stack);
	}

	public static Icon getItemIcon(Item item)
	{
		return getItemIcon(new ItemStack(item));
	}

	public static Icon getItemIcon(Block block)
	{
		return getItemIcon(new ItemStack(block));
	}

	public static Icon getItemIcon(String lazyStackString)
	{
		return lazyStackString.isEmpty() ? EMPTY : new LazyItemIcon(lazyStackString);
	}

	private ItemIcon(ItemStack is)
	{
		stack = is;
	}

	public ItemStack getStack()
	{
		return stack;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void draw(MatrixStack matrixStack, int x, int y, int w, int h)
	{
		matrixStack.push();
		matrixStack.translate(0, 0, 100);
		GuiHelper.drawItem(matrixStack, getStack(), x, y, w / 16F, h / 16F, true, null);
		matrixStack.pop();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void drawStatic(MatrixStack matrixStack, int x, int y, int w, int h)
	{
		matrixStack.push();
		matrixStack.translate(0, 0, 100);
		GuiHelper.drawItem(matrixStack, getStack(), x, y, w / 16F, h / 16F, false, null);
		matrixStack.pop();
	}

	@OnlyIn(Dist.CLIENT)
	public static void drawItem3D(MatrixStack matrixStack, ItemStack stack)
	{
		//FIXME: Draw flat 3D item
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void draw3D(MatrixStack matrixStack)
	{
		drawItem3D(matrixStack, getStack());
	}

	public String toString()
	{
		ItemStack is = getStack();
		StringBuilder builder = new StringBuilder("item:");
		builder.append(is.getItem().getRegistryName());
		int count = is.getCount();
		int damage = is.getDamage();
		CompoundNBT nbt = is.getTag();

		if (count > 1 || damage > 0 || nbt != null)
		{
			builder.append(' ');
			builder.append(count);
		}

		if (damage > 0 || nbt != null)
		{
			builder.append(' ');
			builder.append(damage);
		}

		if (nbt != null)
		{
			builder.append(' ');
			builder.append(nbt);
		}

		return builder.toString();
	}

	public int hashCode()
	{
		ItemStack stack = getStack();
		int h = stack.getItem().hashCode();
		h = h * 31 + stack.getCount();
		h = h + stack.getDamage() * 31;
		h = h * 31 + Objects.hashCode(stack.getTag());
		return h;
	}

	public boolean equals(Object o)
	{
		return o == this || o instanceof ItemIcon && ItemStack.areItemStacksEqual(getStack(), ((ItemIcon) o).getStack());
	}

	@Override
	@Nullable
	public Object getIngredient()
	{
		return getStack();
	}
}