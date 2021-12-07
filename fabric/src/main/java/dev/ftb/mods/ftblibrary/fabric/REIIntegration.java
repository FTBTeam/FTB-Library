package dev.ftb.mods.ftblibrary.fabric;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;

public class REIIntegration implements REIClientPlugin {
	/*
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
		SelectItemStackScreen.modes.add(0, REI_ITEMS);
	}
	 */
}
