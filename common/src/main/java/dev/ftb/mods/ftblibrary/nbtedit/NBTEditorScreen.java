package dev.ftb.mods.ftblibrary.nbtedit;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.*;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.*;
import dev.ftb.mods.ftblibrary.net.EditNBTResponsePacket;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.NBTUtils;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.WrappedIngredient;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class NBTEditorScreen extends BaseScreen {
	private static Icon getIcon(String name) {
		return Icon.getIcon(FTBLibrary.MOD_ID + ":textures/icons/nbt/" + name + ".png");
	}

	public static final Icon NBT_BYTE = getIcon("byte");
	public static final Icon NBT_SHORT = getIcon("short");
	public static final Icon NBT_INT = getIcon("int");
	public static final Icon NBT_LONG = getIcon("long");
	public static final Icon NBT_FLOAT = getIcon("float");
	public static final Icon NBT_DOUBLE = getIcon("double");
	public static final Icon NBT_STRING = getIcon("string");
	public static final Icon NBT_LIST = getIcon("list");
	public static final Icon NBT_LIST_CLOSED = getIcon("list_closed");
	public static final Icon NBT_LIST_OPEN = getIcon("list_open");
	public static final Icon NBT_MAP = getIcon("map");
	public static final Icon NBT_MAP_CLOSED = getIcon("map_closed");
	public static final Icon NBT_MAP_OPEN = getIcon("map_open");
	public static final Icon NBT_BYTE_ARRAY = getIcon("byte_array");
	public static final Icon NBT_BYTE_ARRAY_CLOSED = getIcon("byte_array_closed");
	public static final Icon NBT_BYTE_ARRAY_OPEN = getIcon("byte_array_open");
	public static final Icon NBT_INT_ARRAY = getIcon("int_array");
	public static final Icon NBT_INT_ARRAY_CLOSED = getIcon("int_array_closed");
	public static final Icon NBT_INT_ARRAY_OPEN = getIcon("int_array_open");

	public abstract class ButtonNBT extends Button {
		public final ButtonNBTCollection parent;
		public String key;

		public ButtonNBT(Panel panel, @Nullable ButtonNBTCollection b, String k) {
			super(panel);
			setPosAndSize(b == null ? 0 : b.posX + 10, 0, 10, 10);
			parent = b;
			key = k;
			setTitle(Component.literal(key));
		}

		public abstract CompoundTag copy();

		public void updateChildren(boolean first) {
		}

		public void addChildren() {
		}

		public boolean canCreateNew(int id) {
			return false;
		}

		@Override
		public void addMouseOverText(TooltipList list) {

		}

		@Override
		public void draw(PoseStack pose, Theme theme, int x, int y, int w, int h) {
			if (selected == this) {
				Color4I.WHITE.withAlpha(33).draw(pose, x, y, w, h);
			}

			IconWithBorder.BUTTON_ROUND_GRAY.draw(pose, x + 1, y + 1, 8, 8);
			drawIcon(pose, theme, x + 1, y + 1, 8, 8);
			theme.drawString(pose, getTitle(), x + 11, y + 1);
		}
	}

	public class ButtonBasicTag extends ButtonNBT {
		private Tag nbt;

		public ButtonBasicTag(Panel panel, ButtonNBTCollection b, String k, Tag n) {
			super(panel, b, k);
			nbt = n;

			switch (nbt.getId()) {
				case Tag.TAG_BYTE -> setIcon(NBT_BYTE);
				case Tag.TAG_SHORT -> setIcon(NBT_SHORT);
				case Tag.TAG_INT -> setIcon(NBT_INT);
				case Tag.TAG_LONG -> setIcon(NBT_LONG);
				case Tag.TAG_FLOAT -> setIcon(NBT_FLOAT);
				case Tag.TAG_DOUBLE, Tag.TAG_ANY_NUMERIC -> setIcon(NBT_DOUBLE);
				case Tag.TAG_STRING -> setIcon(NBT_STRING);
			}

			parent.setTag(key, nbt);
			updateTitle();
		}

		public void updateTitle() {
			Object title = switch (nbt.getId()) {
				case Tag.TAG_BYTE, Tag.TAG_SHORT, Tag.TAG_INT -> ((NumericTag) nbt).getAsInt();
				case Tag.TAG_LONG -> ((NumericTag) nbt).getAsLong();
				case Tag.TAG_FLOAT, Tag.TAG_DOUBLE, Tag.TAG_ANY_NUMERIC -> ((NumericTag) nbt).getAsDouble();
				case Tag.TAG_STRING -> nbt.getAsString();
				default -> "";
			};

			setTitle(Component.literal(key + ": " + title));
			setWidth(12 + getTheme().getStringWidth(key + ": " + title));
		}

		@Override
		public void onClicked(MouseButton button) {
			selected = this;
			panelTopLeft.refreshWidgets();

			if (button.isRight()) {
				edit();
			}
		}

		@Override
		public boolean mouseDoubleClicked(MouseButton button) {
			if (isMouseOver()) {
				edit();
				return true;
			}

			return false;
		}

		public void edit() {
			switch (nbt.getId()) {
				case Tag.TAG_BYTE, Tag.TAG_SHORT, Tag.TAG_INT -> {
					var intConfig = new IntConfig(Integer.MIN_VALUE, Integer.MAX_VALUE);
					EditConfigFromStringScreen.open(intConfig, ((NumericTag) nbt).getAsInt(), 0, accepted -> onCallback(intConfig, accepted));
				}
				case Tag.TAG_LONG -> {
					var longConfig = new LongConfig(Long.MIN_VALUE, Long.MAX_VALUE);
					EditConfigFromStringScreen.open(longConfig, ((NumericTag) nbt).getAsLong(), 0L, accepted -> onCallback(longConfig, accepted));
				}
				case Tag.TAG_FLOAT, Tag.TAG_DOUBLE, Tag.TAG_ANY_NUMERIC -> {
					var doubleConfig = new DoubleConfig(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
					EditConfigFromStringScreen.open(doubleConfig, ((NumericTag) nbt).getAsDouble(), 0D, accepted -> onCallback(doubleConfig, accepted));
				}
				case Tag.TAG_STRING -> {
					var stringConfig = new StringConfig();
					EditConfigFromStringScreen.open(stringConfig, nbt.getAsString(), "", accepted -> onCallback(stringConfig, accepted));
				}
			}
		}

		public void onCallback(ConfigValue<?> value, boolean set) {
			if (set) {
				switch (nbt.getId()) {
					case Tag.TAG_BYTE, Tag.TAG_SHORT, Tag.TAG_INT -> nbt = IntTag.valueOf(((Number) value.value).intValue());
					case Tag.TAG_LONG -> nbt = LongTag.valueOf(((Number) value.value).longValue());
					case Tag.TAG_FLOAT, Tag.TAG_DOUBLE, Tag.TAG_ANY_NUMERIC -> nbt = DoubleTag.valueOf(((Number) value.value).doubleValue());
					case Tag.TAG_STRING -> nbt = StringTag.valueOf(value.value.toString());
				}

				parent.setTag(key, nbt);
				updateTitle();
			}

			NBTEditorScreen.this.openGui();
		}

		@Override
		public CompoundTag copy() {
			var n = new CompoundTag();
			n.put(key, nbt);
			return n;
		}
	}

	public abstract class ButtonNBTCollection extends ButtonNBT {
		public boolean collapsed;
		public final Map<String, ButtonNBT> children;
		public final Icon iconOpen, iconClosed;

		public ButtonNBTCollection(Panel panel, @Nullable ButtonNBTCollection b, String key, Icon open, Icon closed) {
			super(panel, b, key);
			iconOpen = open;
			iconClosed = closed;
			setCollapsed(false);
			setWidth(width + 2 + getTheme().getStringWidth(key));
			children = new LinkedHashMap<>();
		}

		@Override
		public void addChildren() {
			if (!collapsed) {
				for (var button : children.values()) {
					panelNbt.add(button);
					button.addChildren();
				}
			}
		}

		@Override
		public void onClicked(MouseButton button) {
			if (getMouseX() <= getX() + height) {
				setCollapsed(!collapsed);
				panelNbt.refreshWidgets();
			} else {
				selected = this;
				panelTopLeft.refreshWidgets();
			}
		}

		@Override
		public boolean mouseDoubleClicked(MouseButton button) {
			if (isMouseOver()) {
				setCollapsed(!collapsed);
				panelNbt.refreshWidgets();
				return true;
			}

			return false;
		}

		public void setCollapsed(boolean c) {
			collapsed = c;
			setIcon(collapsed ? iconClosed : iconOpen);
		}

		public void setCollapsedTree(boolean c) {
			setCollapsed(c);

			for (var button : children.values()) {
				if (button instanceof ButtonNBTCollection) {
					((ButtonNBTCollection) button).setCollapsedTree(c);
				}
			}
		}

		public abstract Tag getTag(String k);

		public abstract void setTag(String k, @Nullable Tag base);
	}

	public class ButtonNBTMap extends ButtonNBTCollection {
		private final CompoundTag map;
		private Icon hoverIcon = Color4I.EMPTY;

		public ButtonNBTMap(Panel panel, @Nullable ButtonNBTCollection b, String key, CompoundTag m) {
			super(panel, b, key, NBT_MAP_OPEN, NBT_MAP_CLOSED);
			map = m;
		}

		@Override
		public void updateChildren(boolean first) {
			children.clear();
			List<String> list = new ArrayList<>(map.getAllKeys());
			list.sort(StringUtils.IGNORE_CASE_COMPARATOR);

			for (var s : list) {
				var nbt = getFrom(this, s);
				children.put(s, nbt);
				nbt.updateChildren(first);
			}

			updateHoverIcon();

			if (first && !hoverIcon.isEmpty()) {
				setCollapsed(true);
			}
		}

		private void updateHoverIcon() {
			hoverIcon = Color4I.EMPTY;

			if (map.contains("id", Tag.TAG_STRING) && map.contains("Count", Tag.TAG_ANY_NUMERIC)) {
				var stack = ItemStack.of(map);

				if (!stack.isEmpty()) {
					hoverIcon = ItemIcon.getItemIcon(stack);
				}
			}

			setWidth(12 + getTheme().getStringWidth(getTitle()) + (hoverIcon.isEmpty() ? 0 : 10));
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			if (this == buttonNBTRoot) {
				var infoList = info.getList("text", Tag.TAG_STRING);

				if (infoList.size() > 0) {
					list.add(Component.translatable("gui.info").append(":"));

					for (var i = 0; i < infoList.size(); i++) {
						var component = Component.Serializer.fromJson(infoList.getString(i));

						if (component != null) {
							list.add(component);
						}
					}
				}
			}
		}

		@Override
		public void draw(PoseStack pose, Theme theme, int x, int y, int w, int h) {
			super.draw(pose, theme, x, y, w, h);

			if (!hoverIcon.isEmpty()) {
				hoverIcon.draw(pose, x + 12 + theme.getStringWidth(getTitle()), y + 1, 8, 8);
			}
		}

		@Override
		@Nullable
		public Object getIngredientUnderMouse() {
			return new WrappedIngredient(hoverIcon.getIngredient()).tooltip();
		}

		@Override
		public Tag getTag(String k) {
			return map.get(k);
		}

		@Override
		public void setTag(String k, @Nullable Tag base) {
			if (base != null) {
				map.put(k, base);
			} else {
				map.remove(k);
			}

			updateHoverIcon();

			if (parent != null) {
				parent.setTag(key, map);
			}
		}

		@Override
		public boolean canCreateNew(int id) {
			return true;
		}

		@Override
		public CompoundTag copy() {
			var nbt = map.copy();

			if (this == buttonNBTRoot) {
				var infoList1 = new ListTag();
				var infoList0 = info.getList("text", Tag.TAG_STRING);

				if (infoList0.size() > 0) {
					for (var i = 0; i < infoList0.size(); i++) {
						var component = Component.Serializer.fromJson(infoList0.getString(i));

						if (component != null) {
							infoList1.add(StringTag.valueOf(component.getString()));
						}
					}

					nbt.put("_", infoList1);
				}
			}

			return nbt;
		}
	}

	public class ButtonNBTList extends ButtonNBTCollection {
		private final ListTag list;

		public ButtonNBTList(Panel panel, ButtonNBTCollection p, String key, ListTag l) {
			super(panel, p, key, NBT_LIST_OPEN, NBT_LIST_CLOSED);
			list = l;
		}

		@Override
		public void updateChildren(boolean first) {
			children.clear();
			for (var i = 0; i < list.size(); i++) {
				var s = Integer.toString(i);
				var nbt = getFrom(this, s);
				children.put(s, nbt);
				nbt.updateChildren(first);
			}
		}

		@Override
		public Tag getTag(String k) {
			return list.get(Integer.parseInt(k));
		}

		@Override
		public void setTag(String k, @Nullable Tag base) {
			var id = Integer.parseInt(k);

			if (id == -1) {
				if (base != null) {
					list.add(base);
				}
			} else if (base != null) {
				list.set(id, base);
			} else {
				list.remove(id);
			}

			if (parent != null) {
				parent.setTag(key, list);
			}
		}

		@Override
		public boolean canCreateNew(int id) {
			return list.isEmpty() || list.getElementType() == id;
		}

		@Override
		public CompoundTag copy() {
			var n = new CompoundTag();
			n.put(key, list);
			return n;
		}
	}

	public class ButtonNBTByteArray extends ButtonNBTCollection {
		private final ByteArrayList list;

		public ButtonNBTByteArray(Panel panel, ButtonNBTCollection p, String key, ByteArrayTag l) {
			super(panel, p, key, NBT_BYTE_ARRAY_OPEN, NBT_BYTE_ARRAY_CLOSED);
			list = new ByteArrayList(l.getAsByteArray());
		}

		@Override
		public void updateChildren(boolean first) {
			children.clear();
			for (var i = 0; i < list.size(); i++) {
				var s = Integer.toString(i);
				var nbt = getFrom(this, s);
				children.put(s, nbt);
				nbt.updateChildren(first);
			}
		}

		@Override
		public Tag getTag(String k) {
			return ByteTag.valueOf(list.getByte(Integer.parseInt(k)));
		}

		@Override
		public void setTag(String k, @Nullable Tag base) {
			var id = Integer.parseInt(k);

			if (id == -1) {
				if (base != null) {
					list.add(((NumericTag) base).getAsByte());
				}
			} else if (base != null) {
				list.set(id, ((NumericTag) base).getAsByte());
			} else {
				list.removeByte(id);
			}

			if (parent != null) {
				parent.setTag(key, new ByteArrayTag(list.toByteArray()));
			}
		}

		@Override
		public boolean canCreateNew(int id) {
			return id == Tag.TAG_BYTE;
		}

		@Override
		public CompoundTag copy() {
			var n = new CompoundTag();
			n.put(key, new ByteArrayTag(list.toByteArray()));
			return n;
		}
	}

	public class ButtonNBTIntArray extends ButtonNBTCollection {
		private final IntArrayList list;

		public ButtonNBTIntArray(Panel panel, ButtonNBTCollection p, String key, IntArrayTag l) {
			super(panel, p, key, NBT_INT_ARRAY_OPEN, NBT_INT_ARRAY_CLOSED);
			list = new IntArrayList(l.getAsIntArray());
		}

		@Override
		public void updateChildren(boolean first) {
			children.clear();
			for (var i = 0; i < list.size(); i++) {
				var s = Integer.toString(i);
				var nbt = getFrom(this, s);
				children.put(s, nbt);
				nbt.updateChildren(first);
			}
		}

		@Override
		public Tag getTag(String k) {
			return IntTag.valueOf(list.getInt(Integer.parseInt(k)));
		}

		@Override
		public void setTag(String k, @Nullable Tag base) {
			var id = Integer.parseInt(k);

			if (id == -1) {
				if (base != null) {
					list.add(((NumericTag) base).getAsInt());
				}
			} else if (base != null) {
				list.set(id, ((NumericTag) base).getAsInt());
			} else {
				list.rem(id);
			}

			if (parent != null) {
				parent.setTag(key, new IntArrayTag(list.toIntArray()));
			}
		}

		@Override
		public boolean canCreateNew(int id) {
			return id == Tag.TAG_INT;
		}

		@Override
		public CompoundTag copy() {
			var n = new CompoundTag();
			n.put(key, new IntArrayTag(list.toIntArray()));
			return n;
		}
	}

	private ButtonNBT getFrom(ButtonNBTCollection b, String key) {
		var nbt = b.getTag(key);

		return switch (nbt.getId()) {
			case Tag.TAG_COMPOUND -> new ButtonNBTMap(panelNbt, b, key, (CompoundTag) nbt);
			case Tag.TAG_LIST -> new ButtonNBTList(panelNbt, b, key, (ListTag) nbt);
			case Tag.TAG_BYTE_ARRAY -> new ButtonNBTByteArray(panelNbt, b, key, (ByteArrayTag) nbt);
			case Tag.TAG_INT_ARRAY -> new ButtonNBTIntArray(panelNbt, b, key, (IntArrayTag) nbt);
			default -> new ButtonBasicTag(panelNbt, b, key, nbt);
		};
	}

	public SimpleButton newTag(Panel panel, String t, Icon icon, Supplier<Tag> supplier) {
		return new SimpleButton(panel, Component.literal(t), icon, (gui, button) -> {
			if (selected instanceof ButtonNBTMap) {
				var value = new StringConfig(Pattern.compile("^.+$"));
				EditConfigFromStringScreen.open(value, "", "", set -> {
					if (set && !value.value.isEmpty()) {
						((ButtonNBTCollection) selected).setTag(value.value, supplier.get());
						selected.updateChildren(false);
						panelNbt.refreshWidgets();
					}

					NBTEditorScreen.this.openGui();
				});
			} else if (selected instanceof ButtonNBTCollection) {
				((ButtonNBTCollection) selected).setTag("-1", supplier.get());
				selected.updateChildren(false);
				panelNbt.refreshWidgets();
			}
		}) {
			@Override
			public void drawBackground(PoseStack stack, Theme theme, int x, int y, int w, int h) {
				IconWithBorder.BUTTON_ROUND_GRAY.draw(stack, x, y, w, h);
			}
		};
	}

	private final CompoundTag info;
	private final ButtonNBTMap buttonNBTRoot;
	private ButtonNBT selected;
	public final Panel panelTopLeft, panelTopRight, panelNbt;
	public final PanelScrollBar scroll;
	private int shouldClose = 0;

	public NBTEditorScreen(CompoundTag i, CompoundTag nbt) {
		info = i;

		panelTopLeft = new Panel(this) {
			@Override
			public void addWidgets() {
				add(new SimpleButton(this, Component.translatable("selectServer.delete"), selected == buttonNBTRoot ? Icons.REMOVE_GRAY : Icons.REMOVE, (widget, button) -> {
					if (selected != buttonNBTRoot) {
						selected.parent.setTag(selected.key, null);
						selected.parent.updateChildren(false);
						selected = selected.parent;
						panelNbt.refreshWidgets();
						panelTopLeft.refreshWidgets();
					}
				}));

				var canRename = selected.parent instanceof ButtonNBTMap;

				add(new SimpleButton(this, Component.translatable("gui.rename"), canRename ? Icons.INFO : Icons.INFO_GRAY, (gui, button) -> {
					if (canRename) {
						var value = new StringConfig();
						EditConfigFromStringScreen.open(value, selected.key, "", set -> {
							if (set && !value.value.isEmpty()) {
								var parent = selected.parent;
								var s0 = selected.key;
								var nbt = parent.getTag(s0);
								parent.setTag(s0, null);
								parent.setTag(value.value, nbt);
								parent.updateChildren(false);
								selected = parent.children.get(value.value);
								panelNbt.refreshWidgets();
							}

							getGui().openGui();
						});
					}
				}));

				if (selected instanceof ButtonBasicTag) {
					add(new SimpleButton(this, Component.translatable("selectServer.edit"), Icons.FEATHER, (widget, button) -> ((ButtonBasicTag) selected).edit()));
				}

				if (selected.canCreateNew(Tag.TAG_COMPOUND)) {
					add(newTag(this, "Compound", NBT_MAP, CompoundTag::new));
				}

				if (selected.canCreateNew(Tag.TAG_LIST)) {
					add(newTag(this, "List", NBT_LIST, ListTag::new));
				}

				if (selected.canCreateNew(Tag.TAG_STRING)) {
					add(newTag(this, "String", NBT_STRING, () -> StringTag.valueOf("")));
				}

				if (selected.canCreateNew(Tag.TAG_BYTE)) {
					add(newTag(this, "Byte", NBT_BYTE, () -> ByteTag.valueOf((byte) 0)));
				}

				if (selected.canCreateNew(Tag.TAG_SHORT)) {
					add(newTag(this, "Short", NBT_SHORT, () -> ShortTag.valueOf((short) 0)));
				}

				if (selected.canCreateNew(Tag.TAG_INT)) {
					add(newTag(this, "Int", NBT_INT, () -> IntTag.valueOf(0)));
				}

				if (selected.canCreateNew(Tag.TAG_LONG)) {
					add(newTag(this, "Long", NBT_LONG, () -> LongTag.valueOf(0L)));
				}

				if (selected.canCreateNew(Tag.TAG_FLOAT)) {
					add(newTag(this, "Float", NBT_FLOAT, () -> FloatTag.valueOf(0F)));
				}

				if (selected.canCreateNew(Tag.TAG_DOUBLE)) {
					add(newTag(this, "Double", NBT_DOUBLE, () -> DoubleTag.valueOf(0D)));
				}

				if (selected.canCreateNew(Tag.TAG_BYTE_ARRAY)) {
					add(newTag(this, "Byte Array", NBT_BYTE_ARRAY, () -> new ByteArrayTag(new byte[0])));
				}

				if (selected.canCreateNew(Tag.TAG_INT_ARRAY)) {
					add(newTag(this, "Int Array", NBT_INT_ARRAY, () -> new IntArrayTag(new int[0])));
				}
			}

			@Override
			public void alignWidgets() {
				setWidth(align(new WidgetLayout.Horizontal(2, 4, 2)));
			}
		};

		panelTopLeft.setPosAndSize(0, 2, 0, 16);

		panelTopRight = new Panel(this) {
			@Override
			public void addWidgets() {
				add(new SimpleButton(this, Component.translatable("gui.copy"), ItemIcon.getItemIcon(Items.PAPER), (widget, button) -> setClipboardString(selected.copy().toString())));

				add(new SimpleButton(this, Component.translatable("gui.collapse_all"), Icons.REMOVE, (widget, button) -> {
					for (var w : panelNbt.widgets) {
						if (w instanceof ButtonNBTCollection) {
							((ButtonNBTCollection) w).setCollapsed(true);
						}
					}

					scroll.setValue(0);
					panelNbt.refreshWidgets();
				}));

				add(new SimpleButton(this, Component.translatable("gui.expand_all"), Icons.ADD, (widget, button) -> {
					for (var w : panelNbt.widgets) {
						if (w instanceof ButtonNBTCollection) {
							((ButtonNBTCollection) w).setCollapsed(false);
						}
					}

					scroll.setValue(0);
					panelNbt.refreshWidgets();
				}));

				add(new SimpleButton(this, Component.translatable("gui.cancel"), Icons.CANCEL, (widget, button) -> {
					shouldClose = 2;
					widget.getGui().closeGui();
				}));

				add(new SimpleButton(this, Component.translatable("gui.accept"), Icons.ACCEPT, (widget, button) -> {
					shouldClose = 1;
					widget.getGui().closeGui();
				}));
			}

			@Override
			public void alignWidgets() {
				setWidth(align(new WidgetLayout.Horizontal(2, 4, 2)));
			}
		};

		panelNbt = new Panel(this) {
			@Override
			public void addWidgets() {
				add(buttonNBTRoot);
				buttonNBTRoot.addChildren();
			}

			@Override
			public void alignWidgets() {
				scroll.setMaxValue(align(WidgetLayout.VERTICAL) + 2);
			}
		};

		buttonNBTRoot = new ButtonNBTMap(panelNbt, null, info.contains("title") ? Component.Serializer.fromJson(info.getString("title")).getString() : "ROOT", nbt);
		buttonNBTRoot.updateChildren(true);
		buttonNBTRoot.setCollapsedTree(true);
		buttonNBTRoot.setCollapsed(false);
		selected = buttonNBTRoot;

		scroll = new PanelScrollBar(this, panelNbt);
	}

	@Override
	public void addWidgets() {
		add(panelTopLeft);
		add(panelTopRight);
		add(panelNbt);
		add(scroll);
	}

	@Override
	public void alignWidgets() {
		panelTopRight.setPosAndSize(width - panelTopRight.width, 2, 0, 16);
		panelTopRight.alignWidgets();
		panelNbt.setPosAndSize(0, 21, width - scroll.width, height - 20);
		panelNbt.alignWidgets();
		scroll.setPosAndSize(width - scroll.width, 20, 16, panelNbt.height);
	}

	@Override
	public boolean onInit() {
		return setFullscreen();
	}

	@Override
	public void onClosed() {
		super.onClosed();

		if (shouldClose == 1) {
			if (NBTUtils.getSizeInBytes(buttonNBTRoot.map, false) >= 30000L) {
				FTBLibrary.LOGGER.error("NBT too large to send!");
			} else {
				new EditNBTResponsePacket(info, buttonNBTRoot.map).sendToServer();
			}
		}
	}

	@Override
	public void drawBackground(PoseStack stack, Theme theme, int x, int y, int w, int h) {
		EditConfigScreen.COLOR_BACKGROUND.draw(stack, 0, 0, w, 20);
	}

	@Override
	public Theme getTheme() {
		return EditConfigScreen.THEME;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}
}
