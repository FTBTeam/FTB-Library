package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.config.ui.ItemSearchMode;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;

public class REIIntegration implements REIPluginV0 {
	@Override
	public ResourceLocation getPluginIdentifier() {
		return new ResourceLocation(FTBLibrary.MOD_ID, "rei");
	}

	@Override
	public void registerBounds(DisplayHelper displayHelper) {
		displayHelper.getBaseBoundsHandler().registerExclusionZones(Screen.class, () -> {
			Screen currentScreen = Minecraft.getInstance().screen;

			if (FTBLibraryClient.areButtonsVisible(currentScreen)) {
				Rect2i area = SidebarGroupGuiButton.lastDrawnArea;
				return Collections.singletonList(new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
			}

			return Collections.emptyList();
		});
	}

	private static final ItemSearchMode REI_ITEMS = new ItemSearchMode() {
		@Override
		public Icon getIcon() {
			return ItemIcon.getItemIcon(Items.APPLE);
		}

		@Override
		public MutableComponent getDisplayName() {
			return new TranslatableComponent("ftblibrary.select_item.list_mode.rei");
		}

		@Override
		public Collection<ItemStack> getAllItems() {
			return EntryRegistry.getInstance().getEntryStacks()
					.map(EntryStack::getItemStack)
					.filter(Objects::nonNull)
					.collect(Collectors.toCollection(LinkedHashSet::new));
		}
	};

	static {
		SelectItemStackScreen.modes.add(1, REI_ITEMS);
	}
}
