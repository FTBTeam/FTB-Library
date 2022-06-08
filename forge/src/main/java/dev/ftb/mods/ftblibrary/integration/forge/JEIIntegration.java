package dev.ftb.mods.ftblibrary.integration.forge;

//@JeiPlugin
//public class JEIIntegration implements IModPlugin, IGlobalGuiHandler {
//	public static IJeiRuntime runtime = null;
//
//	@Override
//	public void onRuntimeAvailable(IJeiRuntime r) {
//		runtime = r;
//	}
//
//	@Override
//	@NotNull
//	public ResourceLocation getPluginUid() {
//		return new ResourceLocation(FTBLibrary.MOD_ID, "jei");
//	}
//
//	@Override
//	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
//		if (ModList.get().isLoaded("roughlyenoughitems")) {
//			return;
//		}
//		registration.addGlobalGuiHandler(this);
//	}
//
//	@Override
//	@NotNull
//	public Collection<Rect2i> getGuiExtraAreas() {
//		var currentScreen = Minecraft.getInstance().screen;
//
//		if (FTBLibraryClient.areButtonsVisible(currentScreen)) {
//			return Collections.singleton(SidebarGroupGuiButton.lastDrawnArea);
//		}
//
//		return Collections.emptySet();
//	}
//
//	@Override
//	@Nullable
//	public Object getIngredientUnderMouse(double mouseX, double mouseY) {
//		var currentScreen = Minecraft.getInstance().screen;
//
//		if (currentScreen instanceof IScreenWrapper) {
//			return WrappedIngredient.unwrap(((IScreenWrapper) currentScreen).getGui().getIngredientUnderMouse());
//		}
//
//		return null;
//	}
//
//	private static final ItemSearchMode JEI_ITEMS = new ItemSearchMode() {
//		@Override
//		public Icon getIcon() {
//			return ItemIcon.getItemIcon(Items.APPLE);
//		}
//
//		@Override
//		public MutableComponent getDisplayName() {
//			return Component.translatable("ftblibrary.select_item.list_mode.jei");
//		}
//
//		@Override
//		public Collection<ItemStack> getAllItems() {
//			if (runtime == null) {
//				return Collections.emptySet();
//			}
//
//			return runtime.getIngredientManager().getAllIngredients(VanillaTypes.ITEM);
//		}
//	};
//
//	static {
//		if (!ModList.get().isLoaded("roughlyenoughitems")) {
//			SelectItemStackScreen.modes.add(0, JEI_ITEMS);
//		}
//	}
//}
