package dev.ftb.mods.ftblibrary.integration;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.config.ui.ResourceSearchMode;
import dev.ftb.mods.ftblibrary.config.ui.SelectableResource;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;

public class REIIntegration implements REIClientPlugin {
	public static final ResourceLocation ID = FTBLibrary.rl("sidebar_button");

	private static final ResourceSearchMode<ItemStack> REI_ITEMS = new ResourceSearchMode<>() {
		@Override
		public Icon getIcon() {
			return ItemIcon.getItemIcon(Items.GLOW_BERRIES);
		}

		@Override
		public MutableComponent getDisplayName() {
			return Component.translatable("ftblibrary.select_item.list_mode.rei");
		}

		@Override
		public Collection<? extends SelectableResource<ItemStack>> getAllResources() {
			return CollectionUtils.filterAndMap(
					EntryRegistry.getInstance().getPreFilteredList(),
					stack -> stack.getType().equals(VanillaEntryTypes.ITEM),
					stack -> SelectableResource.item(stack.castValue())
			);
		}
	};

	static {
		SelectItemStackScreen.KNOWN_MODES.prependMode(REI_ITEMS);
	}

//	@Override
//	public void registerFavorites(FavoriteEntryType.Registry registry) {
//		registry.register(ID, SidebarButtonType.INSTANCE);
//		for (Map.Entry<SidebarButtonGroup, List<SidebarButton>> entry : SidebarButtonManager.INSTANCE.getButtonGroups().entrySet()) {
//			List<SidebarButtonEntry> buttons = entry.getValue()
//					.stream().map(SidebarButtonEntry::new).toList();
//            SidebarButtonGroup group = entry.getKey();
//			if (!buttons.isEmpty()) {
//				registry.getOrCrateSection(Component.translatable(group.getLangKey()))
//						.add(group.isPinned(), buttons.toArray(new SidebarButtonEntry[0]));
//			}
//		}
//	}
//
//	private static SidebarButton createSidebarButton(ResourceLocation id, JsonObject json) {
//		DataResult<SidebarButtonData> parse = SidebarButtonData.CODEC.parse(JsonOps.INSTANCE, json);
//		if (parse.error().isPresent()) {
//			FTBLibrary.LOGGER.error("Failed to parse json: {}", parse.error().get().message());
//		} else {
//			SidebarButtonData sidebarButtonData = parse.result().get();
//			SidebarButton sidebarButton = new SidebarButton(id, sidebarButtonData);
//			SidebarButtonCreatedEvent.EVENT.invoker().accept(new SidebarButtonCreatedEvent(sidebarButton));
//			return sidebarButton;
//		}
//		return null;
//	}
//
//	private enum SidebarButtonType implements FavoriteEntryType<SidebarButtonEntry> {
//		INSTANCE;
//
//		@Override
//		public CompoundTag save(SidebarButtonEntry entry, CompoundTag tag) {
//			tag.putString("id", entry.button.getId().toString());
//            DataResult<Tag> encode = SidebarButtonData.CODEC.encode(entry.button.getData(), NbtOps.INSTANCE, null);
//            encode.result().ifPresent(t -> tag.put("json", t));
//			return tag;
//		}
//
//		@Override
//		public DataResult<SidebarButtonEntry> read(CompoundTag object) {
//			ResourceLocation id = ResourceLocation.parse(object.getString("id"));
//            DataResult<Pair<SidebarButtonData, Tag>> json = SidebarButtonData.CODEC.decode(NbtOps.INSTANCE, object.get("json"));
//            return json.map(pair -> new SidebarButtonEntry(new SidebarButton(id, pair.getFirst())));
//		}
//
//		@Override
//		//Todo fix this
//		public DataResult<SidebarButtonEntry> fromArgs(Object... args) {
//			if (args.length == 0) {
//				return DataResult.error(() -> "Cannot create SidebarButtonEntry from empty args!");
//			}
//			if (!(args[0] instanceof ResourceLocation id)) {
//				return DataResult.error(() -> "Creation of SidebarButtonEntry from args expected ResourceLocation as the first argument!");
//			}
//			if (!(args[1] instanceof SidebarButton button) && !(args[1] instanceof JsonObject)) {
//				return DataResult.error(() -> "Creation of SidebarButtonEntry from args expected SidebarButton or JsonObject as the second argument!");
//			}
//			return DataResult.success(new SidebarButtonEntry(args[1] instanceof SidebarButton button ? button : createSidebarButton(id, (JsonObject) args[1])), Lifecycle.stable());
//		}
//	}
//
//	private static class SidebarButtonEntry extends FavoriteEntry {
//		private final SidebarButton button;
//
//		public SidebarButtonEntry(SidebarButton button) {
//			this.button = button;
//		}
//
//		@Override
//		public boolean isInvalid() {
//			return button.canSee();
//		}
//
//		@Override
//		public Renderer getRenderer(boolean showcase) {
//			return new Renderer() {
//				@Override
//				public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
//					GuiHelper.setupDrawing();
//					if(bounds.getWidth() > 0 && bounds.getHeight() > 0) {
//						button.getData().icon().draw(graphics, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
//						for (ButtonOverlayRender extraRenderer : button.getExtraRenderers()) {
//							extraRenderer.render(graphics, Minecraft.getInstance().font, 16);
//						}
//					}
//				}
//
//				@Override
//				@Nullable
//				public Tooltip getTooltip(TooltipContext context) {
//					return Tooltip.create(context.getPoint(), button.getTooltip());
//				}
//			};
//		}
//
//		@Override
//		public boolean doAction(int button) {
//			this.button.clickButton(Screen.hasShiftDown());
//			return true;
//		}
//
//		@Override
//		public long hashIgnoreAmount() {
//			return this.button.getId().hashCode();
//		}
//
//		@Override
//		public FavoriteEntry copy() {
//			//Todo fix this?
//			return new SidebarButtonEntry(button);
//		}
//
//		@Override
//		public ResourceLocation getType() {
//			return ID;
//		}
//
//		@Override
//		public boolean isSame(FavoriteEntry other) {
//			if (other instanceof SidebarButtonEntry entry) {
//				return entry.button.getId().equals(button.getId());
//			}
//			return false;
//		}
//	}
}
