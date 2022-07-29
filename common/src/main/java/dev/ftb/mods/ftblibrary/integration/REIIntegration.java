package dev.ftb.mods.ftblibrary.integration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import dev.ftb.mods.ftblibrary.config.ui.ItemSearchMode;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButton;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonManager;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class REIIntegration implements REIClientPlugin {
	public static final ResourceLocation ID = new ResourceLocation("ftblibrary", "sidebar_button");

	private static final ItemSearchMode REI_ITEMS = new ItemSearchMode() {
		@Override
		public Icon getIcon() {
			return ItemIcon.getItemIcon(Items.GLOW_BERRIES);
		}

		@Override
		public MutableComponent getDisplayName() {
			return new TranslatableComponent("ftblibrary.select_item.list_mode.rei");
		}

		@Override
		public Collection<ItemStack> getAllItems() {
			return CollectionUtils.filterAndMap(
					EntryRegistry.getInstance().getPreFilteredList(),
					stack -> stack.getType().equals(VanillaEntryTypes.ITEM),
					stack -> stack.castValue()
			);
		}
	};

	static {
		SelectItemStackScreen.modes.add(0, REI_ITEMS);
	}

	@Override
	public void registerFavorites(FavoriteEntryType.Registry registry) {
		registry.register(ID, SidebarButtonType.INSTANCE);
		for (var group : SidebarButtonManager.INSTANCE.groups) {
			List<SidebarButtonEntry> buttons = CollectionUtils.map(group.getButtons(), SidebarButtonEntry::new);
			if (!buttons.isEmpty()) {
				registry.getOrCrateSection(new TranslatableComponent(group.getLangKey()))
						.add(group.isPinned(), buttons.toArray(SidebarButtonEntry[]::new));
			}
		}
	}

	private enum SidebarButtonType implements FavoriteEntryType<SidebarButtonEntry> {
		INSTANCE;

		@Override
		public CompoundTag save(SidebarButtonEntry entry, CompoundTag tag) {
			tag.putString("id", entry.button.id.toString());
			tag.putString("json", new Gson().toJson(entry.button.json));
			return tag;
		}

		@Override
		public DataResult<SidebarButtonEntry> read(CompoundTag object) {
			var id = new ResourceLocation(object.getString("id"));
			var json = (JsonObject) JsonParser.parseString(object.getString("json"));
			return DataResult.success(new SidebarButtonEntry(new SidebarButton(id, null, json)), Lifecycle.stable());
		}

		@Override
		public DataResult<SidebarButtonEntry> fromArgs(Object... args) {
			if (args.length == 0) {
				return DataResult.error("Cannot create SidebarButtonEntry from empty args!");
			}
			if (!(args[0] instanceof ResourceLocation id)) {
				return DataResult.error("Creation of SidebarButtonEntry from args expected ResourceLocation as the first argument!");
			}
			if (!(args[1] instanceof SidebarButton) && !(args[1] instanceof JsonObject)) {
				return DataResult.error("Creation of SidebarButtonEntry from args expected SidebarButton or JsonObject as the second argument!");
			}
			return DataResult.success(new SidebarButtonEntry(args[1] instanceof SidebarButton button ? button : new SidebarButton(id, null, (JsonObject) args[1])), Lifecycle.stable());
		}
	}

	private static class SidebarButtonEntry extends FavoriteEntry {
		private final SidebarButton button;

		public SidebarButtonEntry(SidebarButton button) {
			this.button = button;
		}

		@Override
		public boolean isInvalid() {
			for (var group : SidebarButtonManager.INSTANCE.groups) {
				for (var groupButton : group.getButtons()) {
					if (groupButton.id.equals(button.id) && groupButton.isActuallyVisible()) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public Renderer getRenderer(boolean showcase) {
			return new Renderer() {
				@Override
				public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
					GuiHelper.setupDrawing();
					button.getIcon().draw(matrices, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
				}

				@Override
				@Nullable
				public Tooltip getTooltip(TooltipContext context) {
					List<String> list = new ArrayList<>();
					list.add(I18n.get(button.getLangKey()));

					if (button.getTooltipHandler() != null) {
						button.getTooltipHandler().accept(list);
					}

					return Tooltip.create(context.getPoint(), CollectionUtils.map(list, ImmutableTextComponent::new));
				}

				@Override
				public int getZ() {
					return 0;
				}

				@Override
				public void setZ(int z) {

				}
			};
		}

		@Override
		public boolean doAction(int button) {
			this.button.onClicked(Screen.hasShiftDown());
			return true;
		}

		@Override
		public long hashIgnoreAmount() {
			return this.button.id.hashCode();
		}

		@Override
		public FavoriteEntry copy() {
			return new SidebarButtonEntry(new SidebarButton(button.id, null, button.json));
		}

		@Override
		public ResourceLocation getType() {
			return ID;
		}

		@Override
		public boolean isSame(FavoriteEntry other) {
			if (other instanceof SidebarButtonEntry entry) {
				return entry.button.id.equals(button.id);
			}
			return false;
		}
	}
}
