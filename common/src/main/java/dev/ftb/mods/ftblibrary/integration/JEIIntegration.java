package dev.ftb.mods.ftblibrary.integration;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.ResourceSearchMode;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectableResource;
import dev.ftb.mods.ftblibrary.client.gui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.client.util.PositionedIngredient;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.integration.platform.PlatformIntegrations;
import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.platform.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@JeiPlugin
public class JEIIntegration implements IModPlugin, IGlobalGuiHandler {
    public static IJeiRuntime runtime = null;
    private static final ResourceSearchMode<ItemStack> JEI_ITEMS = new ResourceSearchMode<>() {
        @Override
        public Icon<?> getIcon() {
            return ItemIcon.ofItem(Items.APPLE);
        }

        @Override
        public MutableComponent getDisplayName() {
            return Component.translatable("ftblibrary.select_item.list_mode.jei");
        }

        @Override
        public Collection<? extends SelectableResource<ItemStack>> getAllResources() {
            if (runtime == null) {
                return Collections.emptySet();
            }

            return runtime.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK).stream()
                    .map(SelectableResource::item)
                    .toList();
        }
    };

    static {
        SelectItemStackScreen.KNOWN_MODES.prependMode(JEI_ITEMS);
    }

    public static Optional<IClickableIngredient<?>> handleExtraIngredientTypes(IJeiRuntime runtime, PositionedIngredient underMouse) {
        throw new AssertionError();
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime r) {
        runtime = r;
    }

    @Override
    public Identifier getPluginUid() {
        return FTBLibrary.id("jei");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGlobalGuiHandler(this);
    }

    @Override
    public Collection<Rect2i> getGuiExtraAreas() {
        var currentScreen = Minecraft.getInstance().screen;

        if (FTBLibraryClient.areButtonsVisible(currentScreen)) {
            return Collections.singleton(SidebarGroupGuiButton.lastDrawnArea);
        }

        return Collections.emptySet();
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory builder, double mouseX, double mouseY) {
        var currentScreen = Minecraft.getInstance().screen;

        if (currentScreen instanceof IScreenWrapper wrapper && wrapper.getGui().getIngredientUnderMouse().isPresent()) {
            PositionedIngredient underMouse = wrapper.getGui().getIngredientUnderMouse().get();
            if (underMouse.ingredient() instanceof ItemStack stack) {
                Optional<ITypedIngredient<ItemStack>> typed = runtime.getIngredientManager().createTypedIngredient(VanillaTypes.ITEM_STACK, stack, false);
                if (typed.isPresent()) {
                    return Optional.of(new ClickableIngredient<>(typed.get(), underMouse.area()));
                }
            } else if (underMouse.ingredient() instanceof FluidStack ourFluidStack) {
                Optional<ITypedIngredient<FluidStack>> typed = runtime.getIngredientManager().createTypedIngredient(FLUID_STACK, ourFluidStack, false);
                if (typed.isPresent()) {
                    return Optional.of(new JEIIntegration.ClickableIngredient<>(typed.get(), underMouse.area()));
                }
            } else {
                // Allow us to fallback onto Fluid handlers for the native implementations
                return PlatformIntegrations.INSTANCE.jei().getClickableIngredientUnderMouse(runtime, underMouse);
            }
        }

        return Optional.empty();
    }

    public record ClickableIngredient<T>(ITypedIngredient<T> typedStack,
                                         Rect2i clickedArea) implements IClickableIngredient<T> {
        @Override
        public ITypedIngredient<T> getTypedIngredient() {
            return typedStack;
        }

        @Override
        public Rect2i getArea() {
            return clickedArea;
        }
    }

    public static final IIngredientTypeWithSubtypes<Fluid, FluidStack> FLUID_STACK = new IIngredientTypeWithSubtypes<>() {
        @Override
        public String getUid() {
            return "ftb_fluid_stack";
        }

        @Override
        public Class<? extends FluidStack> getIngredientClass() {
            return FluidStack.class;
        }

        @Override
        public Class<? extends Fluid> getIngredientBaseClass() {
            return Fluid.class;
        }

        @Override
        public Fluid getBase(FluidStack ingredient) {
            return ingredient.fluid();
        }

        @Override
        public FluidStack getDefaultIngredient(Fluid base) {
            return new FluidStack(base, Platform.get().misc().bucketFluidAmount());
        }
    };
}
