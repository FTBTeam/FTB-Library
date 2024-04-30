package dev.ftb.mods.ftblibrary.icon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class ItemIcon extends Icon implements IResourceIcon {
	private final ItemStack stack;

	public static Icon getItemIcon(ItemStack stack) {
		if (stack.isEmpty()) {
			return empty();
		} else if (stack.getItem() instanceof CustomIconItem) {
			return ((CustomIconItem) stack.getItem()).getCustomIcon(stack);
		}

		return new ItemIcon(stack);
	}

	public static Icon getItemIcon(Item item) {
		return item == Items.AIR ? empty() : getItemIcon(item.getDefaultInstance());
	}

	public static Icon getItemIcon(String lazyStackString) {
		if (lazyStackString.isEmpty()) {
			return empty();
		}

		return new LazyIcon(() -> {
			var s = lazyStackString.split(" ", 4);
			var stack = BuiltInRegistries.ITEM.get(new ResourceLocation(s[0])).getDefaultInstance();

			if (s.length >= 2 && !s[1].equals("1")) {
				stack.setCount(Integer.parseInt(s[1]));
			}

			if (s.length >= 3 && !s[2].equals("0")) {
				stack.setDamageValue(Integer.parseInt(s[2]));
			}

			if (s.length >= 4 && !s[3].equals("null")) {
				try {
					DataComponentMap.CODEC.parse(NbtOps.INSTANCE, TagParser.parseTag(s[3]))
							.ifSuccess(stack::applyComponents);
				} catch (CommandSyntaxException ex) {
					ex.printStackTrace();
				}
			}

			if (stack.isEmpty()) {
				ItemStack fallback = new ItemStack(Items.BARRIER);
				fallback.set(DataComponents.CUSTOM_NAME, Component.literal(lazyStackString));
				return getItemIcon(fallback);
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
	public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		poseStack.translate(x + w / 2D, y + h / 2D, 100);

		if (w != 16 || h != 16) {
			int s = Math.min(w, h);
			poseStack.scale(s / 16F, s / 16F, s / 16F);
		}

		GuiHelper.drawItem(graphics, getStack(), 0, true, null);
		poseStack.popPose();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void drawStatic(GuiGraphics graphics, int x, int y, int w, int h) {
		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		poseStack.translate(x + w / 2D, y + h / 2D, 100);

		if (w != 16 || h != 16) {
			int s = Math.min(w, h);
			poseStack.scale(s / 16F, s / 16F, s / 16F);
		}

		GuiHelper.drawItem(graphics, getStack(), 0, false, null);
		poseStack.popPose();
	}

	@Environment(EnvType.CLIENT)
	public static void drawItem3D(GuiGraphics graphics, ItemStack stack) {
		//FIXME: Draw flat 3D item
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, 240, OverlayTexture.NO_OVERLAY, graphics.pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Minecraft.getInstance().level, 0);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw3D(GuiGraphics graphics) {
		drawItem3D(graphics, getStack());
	}

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
	public ResourceLocation getResourceLocation() {
		return BuiltInRegistries.ITEM.getKey(stack.getItem());
	}
}
