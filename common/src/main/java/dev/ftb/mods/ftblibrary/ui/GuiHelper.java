package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public class GuiHelper {
    public static final BaseScreen BLANK_GUI = new BaseScreen(null) {
        @Override
        public void addWidgets() {
        }
    };

    private static final BiFunction<Color4I, Boolean, Color4I> BRIGHTEN = Util.memoize((col, outset) -> col.addBrightness(outset ? 0.15f : -0.1f));
    private static final BiFunction<Color4I, Boolean, Color4I> DARKEN = Util.memoize((col, outset) -> col.addBrightness(outset ? -0.1f : 0.15f));


    public static void playSound(SoundEvent event, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, pitch));
    }

    public static void drawHollowRect(GuiGraphics graphics, int x, int y, int w, int h, Color4I col, boolean roundEdges) {
        if (w <= 1 || h <= 1 || col.isEmpty()) {
            col.draw(graphics, x, y, w, h);
            return;
        }

        graphics.hLine(x + 1, x + w - 2, y, col.rgba());
        graphics.hLine(x + 1, x + w - 2, y + h - 1, col.rgba());

        if (roundEdges) {
            graphics.vLine(x, y, y + h - 1, col.rgba());
            graphics.vLine(x + w - 1, y, y + h - 1, col.rgba());
        } else {
            graphics.vLine(x, y - 1, y + h, col.rgba());
            graphics.vLine(x + w - 1, y - 1, y + h, col.rgba());
        }
    }

    public static void drawGradientRect(GuiGraphics graphics, int x, int y, int w, int h, Color4I col1, Color4I col2) {
        graphics.fillGradient(x, y, x + w, y + h, col1.rgba(), col2.rgba());
    }

    public static void drawItem(GuiGraphics graphics, ItemStack stack, boolean renderOverlay, @Nullable String text) {
        if (!stack.isEmpty()) {
            var mc = Minecraft.getInstance();
            graphics.pose().pushMatrix();
            graphics.pose().translate(-8, -8);
            graphics.renderItem(stack, 0, 0);
            if (renderOverlay) {
                graphics.renderItemDecorations(mc.font, stack, 0, 0, text);
            }
            graphics.pose().popMatrix();
        }
    }

    public static String clickEventToString(@Nullable ClickEvent event) {
        if (event == null) {
            return "";
        }

        return switch (event) {
            case ClickEvent.OpenUrl openUrl -> openUrl.uri().toString();
            case ClickEvent.ChangePage page -> String.valueOf(page.page());
            case ClickEvent.OpenFile file -> "file:" + file.path();
            case ClickEvent.RunCommand runCommand -> "command:" + runCommand.command();
            case ClickEvent.SuggestCommand suggestCommand -> "suggest_command:" + suggestCommand.command();
            default -> "";
        };
    }

    public static void addStackTooltip(ItemStack stack, List<Component> list) {
        addStackTooltip(stack, list, null);
    }

    public static void addStackTooltip(ItemStack stack, List<Component> list, @Nullable Component prefix) {
        var tooltip = stack.getTooltipLines(Item.TooltipContext.of(
                        Minecraft.getInstance().level),
                Minecraft.getInstance().player,
                Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL
        );
        list.add(prefix == null ? tooltip.get(0).copy().withStyle(stack.getRarity().color()) : prefix.copy().append(tooltip.get(0)));

        for (var i = 1; i < tooltip.size(); i++) {
            list.add(Component.literal("").withStyle(ChatFormatting.GRAY).append(tooltip.get(i)));
        }
    }

    public static void drawBorderedPanel(GuiGraphics graphics, int x, int y, int w, int h, Color4I color, boolean outset) {
        w--;
        h--;

        Color4I hi = BRIGHTEN.apply(color, outset);
        Color4I lo = DARKEN.apply(color, outset);

        graphics.fill(x, y, x + w, y + h, color.rgba());
        graphics.hLine(x, x + w - 1, y, hi.rgba());
        graphics.hLine(x + w, x + w, y, color.rgba());
        graphics.vLine(x, y, y + h, hi.rgba());
        graphics.vLine(x, y + h, y + h, color.rgba());
        graphics.hLine(x + 1, x + w, y + h, lo.rgba());
        graphics.vLine(x + w, y, y + h, lo.rgba());
    }
}
