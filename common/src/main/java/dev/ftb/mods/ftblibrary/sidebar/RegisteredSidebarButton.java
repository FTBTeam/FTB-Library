package dev.ftb.mods.ftblibrary.sidebar;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.api.sidebar.ButtonOverlayRender;
import dev.ftb.mods.ftblibrary.api.sidebar.SidebarButton;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.misc.LoadingScreen;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import net.minecraft.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class RegisteredSidebarButton implements SidebarButton {
    private final SidebarButtonData data;
    private final Identifier id;
    private final String langKey;
    private final Component tooltip;
    private final List<ButtonOverlayRender> extraRenderers;
    private Supplier<List<Component>> tooltipOverride;
    private boolean visible = true;
    private boolean forceHidden = false;

    public RegisteredSidebarButton(Identifier id, SidebarButtonData data) {
        this.id = id;
        this.data = data;
        this.langKey = Util.makeDescriptionId("sidebar_button", id);
        tooltip = Component.translatable(langKey + ".tooltip");
        if (data.requiresOp()) {
            addVisibilityCondition(ClientUtils.IS_CLIENT_OP);
        }
        data.requiredMods().ifPresent(mods -> addVisibilityCondition(() -> mods.stream().allMatch(Platform::isModLoaded)));
        extraRenderers = new ArrayList<>();
        if (!data.envCondition().isEmpty()) {
            if (data.envCondition().startsWith("!")) {
                addVisibilityCondition(() -> !System.getenv().containsKey(data.envCondition().substring(1)));
            } else {
                addVisibilityCondition(() -> System.getenv().containsKey(data.envCondition()));
            }
        }
    }

    public SidebarButtonData getData() {
        return data;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    public String getLangKey() {
        return langKey;
    }

    public List<Component> getTooltip(boolean shift) {
        if (tooltipOverride != null) {
            return tooltipOverride.get();
        } else {
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(Component.translatable(langKey));
            if (shift) {
                tooltips.add(tooltip);
            }
            Optional<List<Component>> components = shift ? data.shiftTooltip() : data.tooltip();
            components.ifPresent(tooltips::addAll);
            return tooltips;
        }
    }

    public void clickButton(boolean shift) {
        if (data.loadingScreen()) {
            new LoadingScreen(Component.translatable(getLangKey())).openGui();
        }

        boolean canShift = shift && data.shiftClickEvent().isPresent();
        List<String> clickEvents = canShift ? data.shiftClickEvent().get() : data.clickEvents();
        for (String event : clickEvents) {
            GuiHelper.BLANK_GUI.handleClick(event);
        }
    }

    public boolean canSee() {
        return !forceHidden && visible;
    }

    public void setForceHidden(boolean forceHidden) {
        this.forceHidden = forceHidden;
    }

    @Override
    public void addVisibilityCondition(BooleanSupplier condition) {
        visible = visible && condition.getAsBoolean();
    }

    @Override
    public void addOverlayRender(ButtonOverlayRender renderer) {
        extraRenderers.add(renderer);
    }

    @Override
    public void setTooltipOverride(Supplier<List<Component>> tooltipOverride) {
        this.tooltipOverride = tooltipOverride;
    }

    public List<ButtonOverlayRender> getExtraRenderers() {
        return extraRenderers;
    }
}

