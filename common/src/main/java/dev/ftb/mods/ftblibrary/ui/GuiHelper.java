package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
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
    public static final BaseScreen BLANK_GUI = new BaseScreen() {
        @Override
        public void addWidgets() {
        }

        @Override
        public void alignWidgets() {
        }
    };
    private static final BiFunction<Color4I, Boolean, Color4I> BRIGHTEN = Util.memoize((col, outset) -> col.addBrightness(outset ? 0.15f : -0.1f));
    private static final BiFunction<Color4I, Boolean, Color4I> DARKEN = Util.memoize((col, outset) -> col.addBrightness(outset ? -0.1f : 0.15f));

    public static void setupDrawing() {
        // TODO: Validate
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.blendFunc(770, 771);
        // TODO: [1.21.6] This isn't a thing anymore
//        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
//        RenderSystem.enableDepthTest();
        // Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
    }

    public static void playSound(SoundEvent event, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, pitch));
    }

    public static void drawTexturedRect(GuiGraphics graphics, VertexConsumer buffer, int x, int y, int w, int h, Color4I col, float u0, float v0, float u1, float v1) {
// TODO: [1.21.6] This isn't a thing anymore
        //        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        addRectToBufferWithUV(graphics, buffer, x, y, w, h, col, u0, v0, u1, v1);
    }

    public static void addRectToBuffer(GuiGraphics graphics, VertexConsumer buffer, int x, int y, int w, int h, Color4I col) {
        if (w <= 0 || h <= 0) {
            return;
        }

        // TODO: this is more right.
        graphics.fill(RenderPipelines.GUI, x, y, x + w, y + h, col.rgba());
        // TODO: [1.21.6] Add back but with the rect from graphics
//        var m = graphics.pose().last().pose();
//        var r = col.redi();
//        var g = col.greeni();
//        var b = col.bluei();
//        var a = col.alphai();
//        buffer.addVertex(m, x, y + h, 0).setColor(r, g, b, a);
//        buffer.addVertex(m, x + w, y + h, 0).setColor(r, g, b, a);
//        buffer.addVertex(m, x + w, y, 0).setColor(r, g, b, a);
//        buffer.addVertex(m, x, y, 0).setColor(r, g, b, a);
    }

    public static void addRectToBufferWithUV(GuiGraphics graphics, VertexConsumer buffer, int x, int y, int w, int h, Color4I col, float u0, float v0, float u1, float v1) {
        if (w <= 0 || h <= 0) {
            return;
        }

        // TODO: This is not right
        graphics.fill(RenderPipelines.GUI, x, y, x + w, y + h, col.rgba());
        // TODO: [1.21.6] Add back but with the rect from graphics
//        var m = graphics.pose().last().pose();
//        var r = col.redi();
//        var g = col.greeni();
//        var b = col.bluei();
//        var a = col.alphai();
//        buffer.addVertex(m, x, y + h, 0).setUv(u0, v1).setColor(r, g, b, a);
//        buffer.addVertex(m, x + w, y + h, 0).setUv(u1, v1).setColor(r, g, b, a);
//        buffer.addVertex(m, x + w, y, 0).setUv(u1, v0).setColor(r, g, b, a);
//        buffer.addVertex(m, x, y, 0).setUv(u0, v0).setColor(r, g, b, a);
    }

    public static void drawHollowRect(GuiGraphics graphics, int x, int y, int w, int h, Color4I col, boolean roundEdges) {
        if (w <= 1 || h <= 1 || col.isEmpty()) {
            col.draw(graphics, x, y, w, h);
            return;
        }

        // TODO: [1.21.6] This doesn't mimic the old behavior
        graphics.renderOutline(x, y, w, h, col.rgba());

        // TODO: [1.21.6] This might but the locations are borked
//        graphics.fill(x, y + 1, 1, h - 2, col.rgba());
//        graphics.fill(x + w - 1, y + 1, 1, h - 2, col.rgba());
//
//        if (roundEdges) {
//            graphics.fill(x + 1, y, w - 2, 1, col.rgba());
//            graphics.fill(x + 1, y + h - 1, w - 2, 1, col.rgba());
//        } else {
//            graphics.fill(x, y, w, 1, col.rgba());
//            graphics.fill(x, y + h - 1, w, 1, col.rgba());
//        }

        // TODO: [1.21.6] Add back
//        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
//        VertexConsumer buffer = bufferSource.getBuffer(RenderType.gui());
//
//        addRectToBuffer(graphics, buffer, x, y + 1, 1, h - 2, col);
//        addRectToBuffer(graphics, buffer, x + w - 1, y + 1, 1, h - 2, col);
//
//        if (roundEdges) {
//            addRectToBuffer(graphics, buffer, x + 1, y, w - 2, 1, col);
//            addRectToBuffer(graphics, buffer, x + 1, y + h - 1, w - 2, 1, col);
//        } else {
//            addRectToBuffer(graphics, buffer, x, y, w, 1, col);
//            addRectToBuffer(graphics, buffer, x, y + h - 1, w, 1, col);
//        }
    }

    public static void drawRectWithShade(GuiGraphics graphics, int x, int y, int w, int h, Color4I col, int intensity) {
        graphics.fill(x, y, x + w, y + h, col.rgba());

        // TODO: [1.21.6] Add back
//        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
//        VertexConsumer buffer = bufferSource.getBuffer(RenderType.gui());
//
//        addRectToBuffer(graphics, buffer, x, y, w - 1, 1, col);
//        addRectToBuffer(graphics, buffer, x, y + 1, 1, h - 1, col);
//        col = col.mutable().addBrightness(-intensity);
//        addRectToBuffer(graphics, buffer, x + w - 1, y, 1, 1, col);
//        addRectToBuffer(graphics, buffer, x, y + h - 1, 1, 1, col);
//        col = col.mutable().addBrightness(-intensity);
//        addRectToBuffer(graphics, buffer, x + w - 1, y + 1, 1, h - 2, col);
//        addRectToBuffer(graphics, buffer, x + 1, y + h - 1, w - 1, 1, col);
    }

    public static void drawGradientRect(GuiGraphics graphics, int x, int y, int w, int h, Color4I col1, Color4I col2) {
        graphics.fillGradient(x, y, x + w, y + h, col1.rgba(), col2.rgba());
    }

    public static void drawItem(GuiGraphics graphics, ItemStack stack, int hash, boolean renderOverlay, @Nullable String text) {
        if (stack.isEmpty()) {
            return;
        }

        // TODO: [1.21.6] Migrate to graphics.renderItem
        var mc = Minecraft.getInstance();

        graphics.pose().pushMatrix();
        graphics.pose().translate(-8, -8);
        graphics.renderItem(stack, 0, 0);
        if (renderOverlay) {
            graphics.renderItemDecorations(mc.font, stack, 0, 0, text);
        }
        graphics.pose().popMatrix();
    }

//    private static void draw(GuiGraphics graphics, Tesselator t, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
//        if (width <= 0 || height <= 0) {
//            return;
//        }
//
//        var m = graphics.pose().last().pose();
//        var buffer = t.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
//        buffer.addVertex(m, x, y, 0).setColor(red, green, blue, alpha);
//        buffer.addVertex(m, x, y + height, 0).setColor(red, green, blue, alpha);
//        buffer.addVertex(m, x + width, y + height, 0).setColor(red, green, blue, alpha);
//        buffer.addVertex(m, x + width, y, 0).setColor(red, green, blue, alpha);
//        BufferUploader.drawWithShader(buffer.buildOrThrow());
//    }

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
