package dev.ftb.mods.ftblibrary.integration.neoforge;

//@JeiPlugin
public class JEIIntegration /*implements IModPlugin, IGlobalGuiHandler*/ {
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
//	public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(double mouseX, double mouseY) {
//		var currentScreen = Minecraft.getInstance().screen;
//
//		if (currentScreen instanceof IScreenWrapper wrapper && wrapper.getGui().getIngredientUnderMouse().isPresent()) {
//			PositionedIngredient underMouse = wrapper.getGui().getIngredientUnderMouse().get();
//			if (underMouse.ingredient() instanceof ItemStack stack) {
//				Optional<ITypedIngredient<ItemStack>> typed = runtime.getIngredientManager().createTypedIngredient(VanillaTypes.ITEM_STACK, stack);
//				if (typed.isPresent()) {
//					return Optional.of(new ClickableIngredient<>(typed.get(), underMouse.area()));
//				}
//			}/* else if (underMouse.ingredient() instanceof FluidStack stack) {
//				Optional<ITypedIngredient<FluidStack>> typed = runtime.getIngredientManager().createTypedIngredient(ForgeTypes.FLUID_STACK, stack);
//				if (typed.isPresent()) {
//					return Optional.of(new ClickableIngredient<>(typed.get(), underMouse.area()));
//				}
//			}*/
//		}
//
//		return Optional.empty();
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
//			return runtime.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK);
//		}
//	};
//
//	static {
//		if (!ModList.get().isLoaded("roughlyenoughitems")) {
//			SelectItemStackScreen.modes.add(0, JEI_ITEMS);
//		}
//	}
//
//	private record ClickableIngredient<T>(ITypedIngredient<T> typedStack, Rect2i clickedArea) implements IClickableIngredient<T> {
//		@Override
//		public ITypedIngredient<T> getTypedIngredient() {
//			return typedStack;
//		}
//
//		@Override
//		public Rect2i getArea() {
//			return clickedArea;
//		}
//	}
}
