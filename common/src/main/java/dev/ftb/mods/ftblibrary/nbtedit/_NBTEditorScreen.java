package dev.ftb.mods.ftblibrary.nbtedit;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.DoubleConfig;
import dev.ftb.mods.ftblibrary.config.IntConfig;
import dev.ftb.mods.ftblibrary.config.LongConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconWithBorder;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.net.EditNBTResponsePacket;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.PanelScrollBar;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.NBTUtils;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.WrappedIngredient;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.shedaniel.architectury.utils.NbtType;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class _NBTEditorScreen extends BaseScreen {
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
			setTitle(new TextComponent(key));
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

	public class ButtonNumericTag extends ButtonNBT {
		private Tag nbt;

		public ButtonNumericTag(Panel panel, ButtonNBTCollection b, String k, Tag n) {
			super(panel, b, k);
			nbt = n;

			switch (nbt.getId()) {
				case NbtType.BYTE:
					setIcon(NBT_BYTE);
					break;
				case NbtType.SHORT:
					setIcon(NBT_SHORT);
					break;
				case NbtType.INT:
					setIcon(NBT_INT);
					break;
				case NbtType.LONG:
					setIcon(NBT_LONG);
					break;
				case NbtType.FLOAT:
					setIcon(NBT_FLOAT);
					break;
				case NbtType.DOUBLE:
				case NbtType.NUMBER:
					setIcon(NBT_DOUBLE);
					break;
				case NbtType.STRING:
					setIcon(NBT_STRING);
					break;
			}

			parent.setTag(key, nbt);
			updateTitle();
		}

		public void updateTitle() {
			Object title = "";

			switch (nbt.getId()) {
				case NbtType.BYTE:
				case NbtType.SHORT:
				case NbtType.INT:
					title = ((NumericTag) nbt).getAsInt();
					break;
				case NbtType.LONG:
					title = ((NumericTag) nbt).getAsLong();
					break;
				case NbtType.FLOAT:
				case NbtType.DOUBLE:
				case NbtType.NUMBER:
					title = ((NumericTag) nbt).getAsDouble();
					break;
				case NbtType.STRING:
					title = nbt.getAsString();
					break;
			}

			setTitle(new TextComponent(key + ": " + title));
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

		public void edit() {
			switch (nbt.getId()) {
				case NbtType.BYTE:
				case NbtType.SHORT:
				case NbtType.INT:
					IntConfig intConfig = new IntConfig(Integer.MIN_VALUE, Integer.MAX_VALUE);
					EditConfigFromStringScreen.open(intConfig, ((NumericTag) nbt).getAsInt(), 0, accepted -> onCallback(intConfig, accepted));
					break;
				case NbtType.LONG:
					LongConfig longConfig = new LongConfig(Long.MIN_VALUE, Long.MAX_VALUE);
					EditConfigFromStringScreen.open(longConfig, ((NumericTag) nbt).getAsLong(), 0L, accepted -> onCallback(longConfig, accepted));
					break;
				case NbtType.FLOAT:
				case NbtType.DOUBLE:
				case NbtType.NUMBER:
					DoubleConfig doubleConfig = new DoubleConfig(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
					EditConfigFromStringScreen.open(doubleConfig, ((NumericTag) nbt).getAsDouble(), 0D, accepted -> onCallback(doubleConfig, accepted));
					break;
				case NbtType.STRING:
					StringConfig stringConfig = new StringConfig();
					EditConfigFromStringScreen.open(stringConfig, nbt.getAsString(), "", accepted -> onCallback(stringConfig, accepted));
					break;
			}
		}

		public void onCallback(ConfigValue<?> value, boolean set) {
			if (set) {
				switch (nbt.getId()) {
					case NbtType.BYTE:
					case NbtType.SHORT:
					case NbtType.INT:
						nbt = IntTag.valueOf(((Number) value.value).intValue());
						break;
					case NbtType.LONG:
						nbt = LongTag.valueOf(((Number) value.value).longValue());
						break;
					case NbtType.FLOAT:
					case NbtType.DOUBLE:
					case NbtType.NUMBER:
						nbt = DoubleTag.valueOf(((Number) value.value).doubleValue());
						break;
					case NbtType.STRING:
						nbt = StringTag.valueOf(value.value.toString());
						break;
				}

				parent.setTag(key, nbt);
				updateTitle();
			}

			_NBTEditorScreen.this.openGui();
		}

		@Override
		public CompoundTag copy() {
			CompoundTag n = new CompoundTag();
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
				for (ButtonNBT button : children.values()) {
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

		public void setCollapsed(boolean c) {
			collapsed = c;
			setIcon(collapsed ? iconClosed : iconOpen);
		}

		public void setCollapsedTree(boolean c) {
			setCollapsed(c);

			for (ButtonNBT button : children.values()) {
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
		private Icon hoverIcon = Icon.EMPTY;

		public ButtonNBTMap(Panel panel, @Nullable ButtonNBTCollection b, String key, CompoundTag m) {
			super(panel, b, key, NBT_MAP_OPEN, NBT_MAP_CLOSED);
			map = m;
		}

		@Override
		public void updateChildren(boolean first) {
			children.clear();
			List<String> list = new ArrayList<>(map.getAllKeys());
			list.sort(StringUtils.IGNORE_CASE_COMPARATOR);

			for (String s : list) {
				ButtonNBT nbt = getFrom(this, s);
				children.put(s, nbt);
				nbt.updateChildren(first);
			}

			updateHoverIcon();

			if (first && !hoverIcon.isEmpty()) {
				setCollapsed(true);
			}
		}

		private void updateHoverIcon() {
			hoverIcon = Icon.EMPTY;

			if (map.contains("id", NbtType.STRING) && map.contains("Count", NbtType.NUMBER)) {
				ItemStack stack = ItemStack.of(map);

				if (!stack.isEmpty()) {
					hoverIcon = ItemIcon.getItemIcon(stack);
				}
			}

			setWidth(12 + getTheme().getStringWidth(getTitle()) + (hoverIcon.isEmpty() ? 0 : 10));
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			if (this == buttonNBTRoot) {
				ListTag infoList = info.getList("text", NbtType.STRING);

				if (infoList.size() > 0) {
					list.add(new TranslatableComponent("gui.info").append(":"));

					for (int i = 0; i < infoList.size(); i++) {
						MutableComponent component = TextComponent.Serializer.fromJson(infoList.getString(i));

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
			CompoundTag nbt = map.copy();

			if (this == buttonNBTRoot) {
				ListTag infoList1 = new ListTag();
				ListTag infoList0 = info.getList("text", NbtType.STRING);

				if (infoList0.size() > 0) {
					for (int i = 0; i < infoList0.size(); i++) {
						MutableComponent component = TextComponent.Serializer.fromJson(infoList0.getString(i));

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
			for (int i = 0; i < list.size(); i++) {
				String s = Integer.toString(i);
				ButtonNBT nbt = getFrom(this, s);
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
			int id = Integer.parseInt(k);

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
			CompoundTag n = new CompoundTag();
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
			for (int i = 0; i < list.size(); i++) {
				String s = Integer.toString(i);
				ButtonNBT nbt = getFrom(this, s);
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
			int id = Integer.parseInt(k);

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
			return id == NbtType.BYTE;
		}

		@Override
		public CompoundTag copy() {
			CompoundTag n = new CompoundTag();
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
			for (int i = 0; i < list.size(); i++) {
				String s = Integer.toString(i);
				ButtonNBT nbt = getFrom(this, s);
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
			int id = Integer.parseInt(k);

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
			return id == NbtType.INT;
		}

		@Override
		public CompoundTag copy() {
			CompoundTag n = new CompoundTag();
			n.put(key, new IntArrayTag(list.toIntArray()));
			return n;
		}
	}

	private ButtonNBT getFrom(ButtonNBTCollection b, String key) {
		Tag nbt = b.getTag(key);

		switch (nbt.getId()) {
			case NbtType.COMPOUND:
				return new ButtonNBTMap(panelNbt, b, key, (CompoundTag) nbt);
			case NbtType.LIST:
				return new ButtonNBTList(panelNbt, b, key, (ListTag) nbt);
			case NbtType.BYTE_ARRAY:
				return new ButtonNBTByteArray(panelNbt, b, key, (ByteArrayTag) nbt);
			case NbtType.INT_ARRAY:
				return new ButtonNBTIntArray(panelNbt, b, key, (IntArrayTag) nbt);
			default:
				return new ButtonNumericTag(panelNbt, b, key, nbt);
		}
	}

	public SimpleButton newTag(Panel panel, String t, Icon icon, Supplier<Tag> supplier) {
		return new SimpleButton(panel, new TextComponent(t), icon, (gui, button) ->
		{
			if (selected instanceof ButtonNBTMap) {
				StringConfig value = new StringConfig(Pattern.compile("^.+$"));
				EditConfigFromStringScreen.open(value, "", "", set -> {
					if (set && !value.value.isEmpty()) {
						((ButtonNBTCollection) selected).setTag(value.value, supplier.get());
						selected.updateChildren(false);
						panelNbt.refreshWidgets();
					}

					_NBTEditorScreen.this.openGui();
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

	public _NBTEditorScreen(CompoundTag i, CompoundTag nbt) {
		info = i;

		panelTopLeft = new Panel(this) {
			@Override
			public void addWidgets() {
				add(new SimpleButton(this, new TranslatableComponent("selectServer.delete"), selected == buttonNBTRoot ? Icons.REMOVE_GRAY : Icons.REMOVE, (widget, button) -> {
					if (selected != buttonNBTRoot) {
						selected.parent.setTag(selected.key, null);
						selected.parent.updateChildren(false);
						selected = selected.parent;
						panelNbt.refreshWidgets();
						panelTopLeft.refreshWidgets();
					}
				}));

				boolean canRename = selected.parent instanceof ButtonNBTMap;

				add(new SimpleButton(this, new TranslatableComponent("gui.rename"), canRename ? Icons.INFO : Icons.INFO_GRAY, (gui, button) -> {
					if (canRename) {
						StringConfig value = new StringConfig();
						EditConfigFromStringScreen.open(value, selected.key, "", set -> {
							if (set && !value.value.isEmpty()) {
								ButtonNBTCollection parent = selected.parent;
								String s0 = selected.key;
								Tag nbt = parent.getTag(s0);
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

				if (selected instanceof ButtonNumericTag) {
					add(new SimpleButton(this, new TranslatableComponent("selectServer.edit"), Icons.FEATHER, (widget, button) -> ((ButtonNumericTag) selected).edit()));
				}

				if (selected.canCreateNew(NbtType.COMPOUND)) {
					add(newTag(this, "Compound", NBT_MAP, CompoundTag::new));
				}

				if (selected.canCreateNew(NbtType.LIST)) {
					add(newTag(this, "List", NBT_LIST, ListTag::new));
				}

				if (selected.canCreateNew(NbtType.STRING)) {
					add(newTag(this, "String", NBT_STRING, () -> StringTag.valueOf("")));
				}

				if (selected.canCreateNew(NbtType.BYTE)) {
					add(newTag(this, "Byte", NBT_BYTE, () -> ByteTag.valueOf((byte) 0)));
				}

				if (selected.canCreateNew(NbtType.SHORT)) {
					add(newTag(this, "Short", NBT_SHORT, () -> ShortTag.valueOf((short) 0)));
				}

				if (selected.canCreateNew(NbtType.INT)) {
					add(newTag(this, "Int", NBT_INT, () -> IntTag.valueOf(0)));
				}

				if (selected.canCreateNew(NbtType.LONG)) {
					add(newTag(this, "Long", NBT_LONG, () -> LongTag.valueOf(0L)));
				}

				if (selected.canCreateNew(NbtType.FLOAT)) {
					add(newTag(this, "Float", NBT_FLOAT, () -> FloatTag.valueOf(0F)));
				}

				if (selected.canCreateNew(NbtType.DOUBLE)) {
					add(newTag(this, "Double", NBT_DOUBLE, () -> DoubleTag.valueOf(0D)));
				}

				if (selected.canCreateNew(NbtType.BYTE_ARRAY)) {
					add(newTag(this, "Byte Array", NBT_BYTE_ARRAY, () -> new ByteArrayTag(new byte[0])));
				}

				if (selected.canCreateNew(NbtType.INT_ARRAY)) {
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
				add(new SimpleButton(this, new TranslatableComponent("gui.copy"), ItemIcon.getItemIcon(Items.PAPER), (widget, button) -> setClipboardString(selected.copy().toString())));

				add(new SimpleButton(this, new TranslatableComponent("gui.collapse_all"), Icons.REMOVE, (widget, button) ->
				{
					for (Widget w : panelNbt.widgets) {
						if (w instanceof ButtonNBTCollection) {
							((ButtonNBTCollection) w).setCollapsed(true);
						}
					}

					scroll.setValue(0);
					panelNbt.refreshWidgets();
				}));

				add(new SimpleButton(this, new TranslatableComponent("gui.expand_all"), Icons.ADD, (widget, button) ->
				{
					for (Widget w : panelNbt.widgets) {
						if (w instanceof ButtonNBTCollection) {
							((ButtonNBTCollection) w).setCollapsed(false);
						}
					}

					scroll.setValue(0);
					panelNbt.refreshWidgets();
				}));

				add(new SimpleButton(this, new TranslatableComponent("gui.cancel"), Icons.CANCEL, (widget, button) ->
				{
					shouldClose = 2;
					widget.getGui().closeGui();
				}));

				add(new SimpleButton(this, new TranslatableComponent("gui.accept"), Icons.ACCEPT, (widget, button) ->
				{
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

		buttonNBTRoot = new ButtonNBTMap(panelNbt, null, info.contains("title") ? TextComponent.Serializer.fromJson(info.getString("title")).getString() : "ROOT", nbt);
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
