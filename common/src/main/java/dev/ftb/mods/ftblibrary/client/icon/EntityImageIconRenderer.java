package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.EntityImageIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public enum EntityImageIconRenderer implements IconRenderer<EntityImageIcon> {
    INSTANCE;

    @Override
    public void render(EntityImageIcon icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        var pose = graphics.pose();

        float drawWidth = icon.getDrawWidth(w);
        float drawHeight = icon.getDrawHeight(h);

        float scaleX = w / drawWidth;
        float scaleY = h / drawHeight;

        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(scaleX, scaleY);

        IconHelper.renderIcon(icon.getMainIcon(), graphics, 0, 0, (int) drawWidth, (int) drawHeight);

        icon.children().forEach(pair -> {
            Icon<?> subIcon = pair.getLeft();
            EntityImageIcon.ChildIconData child = pair.getRight();
            pose.pushMatrix();
            child.offset().ifPresent(offset -> pose.translate(offset.x(), offset.y()));
            IconHelper.renderIcon(subIcon, graphics, 0, 0, child.slice().width(), child.slice().height());
            pose.popMatrix();
        });

        pose.popMatrix();
    }
}
