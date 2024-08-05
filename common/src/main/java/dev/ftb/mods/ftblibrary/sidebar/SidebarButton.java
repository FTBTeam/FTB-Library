package dev.ftb.mods.ftblibrary.sidebar;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.misc.LoadingScreen;
import dev.ftb.mods.ftblibrary.util.ChainedBooleanSupplier;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SidebarButton {

    private final SidebarButtonData data;
    private final ResourceLocation id;
    private final String langKey;
    private final Component basicTooltip;
    private final List<ExtraRenderer> extraRenderers;
    private Supplier<Component> tooltipOverride;
    private ChainedBooleanSupplier visible = ChainedBooleanSupplier.TRUE;


    public SidebarButton(ResourceLocation id, SidebarButtonData data) {
        this.id = id;
        this.data = data;
        this.langKey = Util.makeDescriptionId("sidebar_button", id);
        basicTooltip = Component.translatable(langKey + ".tooltip");
        if(data.requiresOp()) {
            addVisibilityCondition(ClientUtils.IS_CLIENT_OP);
        }
        data.requiredMods().ifPresent(mods -> addVisibilityCondition(() -> mods.stream().allMatch(Platform::isModLoaded)));
        extraRenderers = new ArrayList<>();
    }


    public void addVisibilityCondition(BooleanSupplier condition) {
        visible = visible.and(condition);
    }


    public SidebarButtonData getData() {
        return data;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getLangKey() {
        return langKey;
    }

    public List<Component> getTooltip() {
        return tooltipOverride == null ? List.of(basicTooltip) : List.of(basicTooltip, tooltipOverride.get());
    }

    public void clickButton(boolean shift) {
        if (data.loadingScreen()) {
            new LoadingScreen(Component.translatable(getLangKey())).openGui();
        }

        for (String event : (shift && data.shiftClickEvent().isPresent() ? data.shiftClickEvent().get() : data.clickEvents())) {
            GuiHelper.BLANK_GUI.handleClick(event);
        }
    }

    public boolean canSee() {
        return visible.getAsBoolean();
    }

    @Deprecated
    public void setCustomTextHandler(Supplier<String> customTextHandler) {
        addExtraRenderer((graphics, font, buttonSize) -> {
            String text = customTextHandler.get();
            if (!text.isEmpty()) {
                var nw = font.width(text);
                Color4I.LIGHT_RED.draw(graphics, buttonSize - nw, -1, nw + 1, 9);
                graphics.drawString(font, text, buttonSize - nw + 1, 0, 0xFFFFFFFF);
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            }
        });
    }

    //Todo fix both of these
    public Supplier<String> getCustomTextHandler() {
        return null;
    }

    public Consumer<List<String>> getTooltipHandler() {
        return null;
    }

    public void addExtraRenderer(ExtraRenderer renderer) {
        extraRenderers.add(renderer);
    }


    public List<ExtraRenderer> getExtraRenderers() {
        return extraRenderers;
    }

    public interface ExtraRenderer {
        void render(GuiGraphics graphics, Font font, int buttonSize);
    }
}

