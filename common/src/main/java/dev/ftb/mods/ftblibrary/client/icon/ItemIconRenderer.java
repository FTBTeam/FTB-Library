package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.client.gui.GuiHelper;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public enum ItemIconRenderer implements IconRenderer<ItemIcon> {
    INSTANCE;

    @Override
    public void render(ItemIcon icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        doRender(icon, graphics, x, y, w, h, true);
    }

    @Override
    public void renderStatic(ItemIcon icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        doRender(icon, graphics, x, y, w, h, false);
    }

    private static void doRender(ItemIcon icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h, boolean overlay) {
        var poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(x + w / 2F, y + h / 2F);

        if (w != 16 || h != 16) {
            float scale = Math.min(w, h) / 16F;
            poseStack.scale(scale, scale);
        }

        GuiHelper.drawItem(graphics, icon.getStack().create(), overlay, null);
        poseStack.popMatrix();
    }

    public static void drawItem3D(GuiGraphicsExtractor graphics, ItemStack stack) {
        // TODO maybe this can just go...
        graphics.item(stack, 0, 0);
    }
}
