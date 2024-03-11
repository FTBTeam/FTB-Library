package dev.ftb.mods.ftblibrary.integration.neoforge;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.config.ui.ResourceSearchMode;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectableResource;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@JeiPlugin
public class JEIIntegration implements IModPlugin, IGlobalGuiHandler {
	public static IJeiRuntime runtime = null;

	@Override
	public void onRuntimeAvailable(IJeiRuntime r) {
		runtime = r;
	}

	@Override
	@NotNull
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(FTBLibrary.MOD_ID, "jei");
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		if (ModList.get().isLoaded("roughlyenoughitems")) {
			return;
		}
		registration.addGlobalGuiHandler(this);
	}

	@Override
	@NotNull
	public Collection<Rect2i> getGuiExtraAreas() {
		var currentScreen = Minecraft.getInstance().screen;

		if (FTBLibraryClient.areButtonsVisible(currentScreen)) {
			return Collections.singleton(SidebarGroupGuiButton.lastDrawnArea);
		}

		return Collections.emptySet();
	}

	@Override
	public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(double mouseX, double mouseY) {
		var currentScreen = Minecraft.getInstance().screen;

		if (currentScreen instanceof IScreenWrapper wrapper && wrapper.getGui().getIngredientUnderMouse().isPresent()) {
			PositionedIngredient underMouse = wrapper.getGui().getIngredientUnderMouse().get();
			if (underMouse.ingredient() instanceof ItemStack stack) {
				Optional<ITypedIngredient<ItemStack>> typed = runtime.getIngredientManager().createTypedIngredient(VanillaTypes.ITEM_STACK, stack);
				if (typed.isPresent()) {
					return Optional.of(new ClickableIngredient<>(typed.get(), underMouse.area()));
				}
			} else if (underMouse.ingredient() instanceof FluidStack stack) {
				Optional<ITypedIngredient<FluidStack>> typed = runtime.getIngredientManager().createTypedIngredient(NeoForgeTypes.FLUID_STACK, stack);
				if (typed.isPresent()) {
					return Optional.of(new ClickableIngredient<>(typed.get(), underMouse.area()));
				}
			}
		}

		return Optional.empty();
	}

    private static final ResourceSearchMode<ItemStack> JEI_ITEMS = new ResourceSearchMode<>() {
        @Override
        public Icon getIcon() {
            return ItemIcon.getItemIcon(Items.APPLE);
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
		if (!ModList.get().isLoaded("roughlyenoughitems")) {
            SelectItemStackScreen.KNOWN_MODES.prependMode(JEI_ITEMS);
		}
	}

	private record ClickableIngredient<T>(ITypedIngredient<T> typedStack, Rect2i clickedArea) implements IClickableIngredient<T> {
		@Override
		public ITypedIngredient<T> getTypedIngredient() {
			return typedStack;
		}

		@Override
		public Rect2i getArea() {
			return clickedArea;
		}
	}
}
