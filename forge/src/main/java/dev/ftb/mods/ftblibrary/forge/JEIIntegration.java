package dev.ftb.mods.ftblibrary.forge;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.config.ui.ItemSearchMode;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.item.FTBLibraryItems;
import dev.ftb.mods.ftblibrary.item.forge.FluidContainerItem;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.util.WrappedIngredient;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

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
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.registerSubtypeInterpreter(FTBLibraryItems.FLUID_CONTAINER.get(), FluidContainerItem::getFluidStackHash);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGlobalGuiHandler(this);
	}

	@Override
	@NotNull
	public Collection<Rect2i> getGuiExtraAreas() {
		Screen currentScreen = Minecraft.getInstance().screen;

		if (FTBLibraryClient.areButtonsVisible(currentScreen)) {
			return Collections.singleton(SidebarGroupGuiButton.lastDrawnArea);
		}

		return Collections.emptySet();
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse(double mouseX, double mouseY) {
		Screen currentScreen = Minecraft.getInstance().screen;

		if (currentScreen instanceof IScreenWrapper) {
			return WrappedIngredient.unwrap(((IScreenWrapper) currentScreen).getGui().getIngredientUnderMouse());
		}

		return null;
	}

	private static final ItemSearchMode JEI_ITEMS = new ItemSearchMode() {
		@Override
		public Icon getIcon() {
			return ItemIcon.getItemIcon(Items.APPLE);
		}

		@Override
		public MutableComponent getDisplayName() {
			return new TranslatableComponent("ftblibrary.select_item.list_mode.jei");
		}

		@Override
		public Collection<ItemStack> getAllItems() {
			if (runtime == null) {
				return Collections.emptySet();
			}

			return runtime.getIngredientManager().getAllIngredients(VanillaTypes.ITEM);
		}
	};

	static {
		SelectItemStackScreen.modes.add(0, JEI_ITEMS);
	}
}