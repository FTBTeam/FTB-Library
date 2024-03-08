package dev.ftb.mods.ftblibrary.nbtedit;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.*;
import dev.ftb.mods.ftblibrary.config.ui.EditStringConfigOverlay;
import dev.ftb.mods.ftblibrary.icon.*;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractThreePanelScreen;
import dev.ftb.mods.ftblibrary.ui.misc.SimpleToast;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static dev.ftb.mods.ftblibrary.util.TextComponentUtils.hotkeyTooltip;

public class NBTEditorScreen extends AbstractThreePanelScreen<NBTEditorScreen.NBTPanel> {
	private static final int TOP_PANEL_H = 20;

	public static final Icon NBT_BYTE = getIcon("byte");
	public static final Icon NBT_SHORT = getIcon("short");
	public static final Icon NBT_INT = getIcon("int");
	public static final Icon NBT_LONG = getIcon("long");
	public static final Icon NBT_FLOAT = getIcon("float");
	public static final Icon NBT_DOUBLE = getIcon("double");
	public static final Icon NBT_STRING = getIcon("string");
	public static final Icon NBT_LIST = getIcon("list");
	public static final Icon NBT_LIST_CLOSED = getIcon("list").combineWith(getIcon("map_closed").withColor(Color4I.rgba(0xC0FFFFFF)));
	public static final Icon NBT_LIST_OPEN = getIcon("list");
	public static final Icon NBT_MAP = getIcon("map");
	public static final Icon NBT_MAP_CLOSED = getIcon("map").combineWith(getIcon("map_closed").withColor(Color4I.rgba(0xC0FFFFFF)));
	public static final Icon NBT_MAP_OPEN = getIcon("map");
	public static final Icon NBT_BYTE_ARRAY = getIcon("byte_array");
	public static final Icon NBT_BYTE_ARRAY_CLOSED = getIcon("byte_array_closed");
	public static final Icon NBT_BYTE_ARRAY_OPEN = getIcon("byte_array_open");
	public static final Icon NBT_INT_ARRAY = getIcon("int_array");
	public static final Icon NBT_INT_ARRAY_CLOSED = getIcon("int_array_closed");
	public static final Icon NBT_INT_ARRAY_OPEN = getIcon("int_array_open");

	private final CompoundTag info;
	private final NBTCallback callback;
	private final ButtonNBTMap buttonNBTRoot;
	private ButtonNBT selected;
	public final Panel panelTopLeft, panelTopRight;
	private boolean accepted = false;

	public NBTEditorScreen(CompoundTag info, CompoundTag nbt, NBTCallback callback) {
		super();

		this.info = info;
		this.callback = callback;

		panelTopLeft = new TopLeftPanel();
		panelTopRight = new TopRightPanel();

		buttonNBTRoot = new ButtonNBTMap(mainPanel, null, getInfoTitle(info), nbt);
		buttonNBTRoot.updateChildren(true);
		buttonNBTRoot.setCollapsedTree(true);
		buttonNBTRoot.setCollapsed(false);
		setSelected(buttonNBTRoot);
	}

	private String getInfoTitle(CompoundTag info) {
		if (info.contains("title")) {
			MutableComponent title = Component.Serializer.fromJson(info.getString("title"));
			if (title != null) return title.getString();
		} else if (info.contains("type")) {
			return info.getString("type").toUpperCase();
		}
		return "ROOT";
	}

	private void collapseAll(boolean collapse) {
		for (var w : mainPanel.getWidgets()) {
			if (w instanceof ButtonNBTCollection collection) {
				collection.setCollapsedTree(collapse);
			}
		}

		mainPanel.refreshWidgets();
	}

	@Override
	protected void doCancel() {
		getGui().closeGui();
	}

	@Override
	protected void doAccept() {
		accepted = true;
		getGui().closeGui();
	}

	@Override
	protected int getTopPanelHeight() {
		return TOP_PANEL_H;
	}

	@Override
	protected NBTPanel createMainPanel() {
		return new NBTPanel();
	}

	@Override
	protected Panel createTopPanel() {
		return new CustomTopPanel();
	}

	private void setSelected(@NotNull ButtonNBT newSelected) {
		ButtonNBT prevSelected = selected;
		selected = newSelected;
		if (prevSelected != null) prevSelected.updateTitle();
		selected.updateTitle();
	}

	@Override
	public boolean onInit() {
		return setSizeProportional(0.75f, 0.9f);
	}

	@Override
	public void onClosed() {
		super.onClosed();

		callback.handle(accepted, buttonNBTRoot.map);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

	@Override
	public boolean keyPressed(Key key) {
		if ((key.is(InputConstants.KEY_RETURN) || key.is(InputConstants.KEY_NUMPADENTER)) && key.modifiers.shift()) {
			doAccept();
			return true;
		} else if (key.is(InputConstants.KEY_ADD) || key.is(InputConstants.KEY_EQUALS)) {
			collapseAll(false);
		} else if (key.is(InputConstants.KEY_MINUS) || key.is(GLFW.GLFW_KEY_KP_SUBTRACT)) {
			collapseAll(true);
		} else if (key.is(InputConstants.KEY_C) && key.modifiers.control()) {
			copyToClipboard();
		}
		return super.keyPressed(key);
	}

	private void copyToClipboard() {
		setClipboardString(selected.toNBT().toString());
		SimpleToast.info(Component.translatable("ftblibrary.gui.nbt_copied"), Component.literal(" "));
	}

	private ButtonNBT makeNBTButton(ButtonNBTCollection parent, String key) {
		var nbt = parent.getTag(key);

		return switch (nbt.getId()) {
			case Tag.TAG_COMPOUND -> new ButtonNBTMap(mainPanel, parent, key, (CompoundTag) nbt);
			case Tag.TAG_LIST -> new ButtonNBTList(mainPanel, parent, key, (ListTag) nbt);
			case Tag.TAG_BYTE_ARRAY -> new ButtonNBTByteArray(mainPanel, parent, key, (ByteArrayTag) nbt);
			case Tag.TAG_INT_ARRAY -> new ButtonNBTIntArray(mainPanel, parent, key, (IntArrayTag) nbt);
			default -> new ButtonBasicTag(mainPanel, parent, key, nbt);
		};
	}

	public SimpleButton newTag(Panel panel, String title, Icon icon, Supplier<Tag> supplier) {
		return new SimpleButton(panel, Component.literal(title), icon, (btn, mb) -> {
			if (selected instanceof ButtonNBTMap) {
				var value = new StringConfig(Pattern.compile("^.+$"));
				var overlay = new EditStringConfigOverlay<>(this, value, accepted -> {
					if (accepted && !value.getValue().isEmpty()) {
						((ButtonNBTCollection) selected).setTag(value.getValue(), supplier.get());
						selected.updateChildren(false);
						mainPanel.refreshWidgets();
					}
					NBTEditorScreen.this.openGui();
				});
				overlay.setPos(btn.posX, btn.posY + btn.height + 4);
				getGui().pushModalPanel(overlay);
			} else if (selected instanceof ButtonNBTCollection) {
				((ButtonNBTCollection) selected).setTag("-1", supplier.get());
				selected.updateChildren(false);
				mainPanel.refreshWidgets();
			}
		}) {
			@Override
			public void drawBackground(GuiGraphics stack, Theme theme, int x, int y, int w, int h) {
				IconWithBorder.BUTTON_ROUND_GRAY.draw(stack, x, y, w, h);
			}
		};
	}

	private static Icon getIcon(String name) {
		return Icon.getIcon(FTBLibrary.MOD_ID + ":textures/icons/nbt/" + name + ".png");
	}

	/**********************************************************************************************/

	public interface NBTCallback {
		void handle(boolean accepted, CompoundTag nbt);
	}

	public abstract class ButtonNBT extends Button {
		protected final ButtonNBTCollection parent;
		protected String key;

		public ButtonNBT(Panel panel, @Nullable ButtonNBTCollection parent, String key) {
			super(panel);

			this.parent = parent;
			this.key = key;

			setPosAndSize(parent == null ? 0 : parent.posX + 10, 0, 10, 10);
			setTitle(Component.literal(this.key));
		}

		public abstract CompoundTag toNBT();

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
		public void draw(GuiGraphics pose, Theme theme, int x, int y, int w, int h) {
			if (isSelected()) {
				Color4I.WHITE.withAlpha(64).draw(pose, x, y, w, h);
			}

			IconWithBorder.BUTTON_ROUND_GRAY.draw(pose, x + 1, y + 1, 8, 8);
			drawIcon(pose, theme, x + 1, y + 1, 8, 8);
			theme.drawString(pose, getTitle(), x + 11, y + 1);
		}

		public boolean isSelected() {
			return this == selected;
		}

		public void updateTitle() {
		}
	}

	public class ButtonBasicTag extends ButtonNBT {
		private Tag nbt;

		public ButtonBasicTag(Panel panel, ButtonNBTCollection parent, String key, Tag nbt) {
			super(panel, parent, key);
			this.nbt = nbt;

			switch (this.nbt.getId()) {
				case Tag.TAG_BYTE -> setIcon(NBT_BYTE);
				case Tag.TAG_SHORT -> setIcon(NBT_SHORT);
				case Tag.TAG_INT -> setIcon(NBT_INT);
				case Tag.TAG_LONG -> setIcon(NBT_LONG);
				case Tag.TAG_FLOAT -> setIcon(NBT_FLOAT);
				case Tag.TAG_DOUBLE, Tag.TAG_ANY_NUMERIC -> setIcon(NBT_DOUBLE);
				case Tag.TAG_STRING -> setIcon(NBT_STRING);
			}

			this.parent.setTag(this.key, this.nbt);
			updateTitle();
		}

		@Override
		public void updateTitle() {
			Object value = switch (nbt.getId()) {
				case Tag.TAG_BYTE, Tag.TAG_SHORT, Tag.TAG_INT -> ((NumericTag) nbt).getAsInt();
				case Tag.TAG_LONG -> ((NumericTag) nbt).getAsLong();
				case Tag.TAG_FLOAT, Tag.TAG_DOUBLE, Tag.TAG_ANY_NUMERIC -> ((NumericTag) nbt).getAsDouble();
				case Tag.TAG_STRING -> nbt.getAsString();
				default -> "";
			};

			ChatFormatting k = isSelected() ? ChatFormatting.WHITE : ChatFormatting.GRAY;
			ChatFormatting v = isSelected() ? ChatFormatting.AQUA : ChatFormatting.DARK_AQUA;
			Component text = Component.literal(key).withStyle(k).append(": ").append(Component.literal(value.toString()).withStyle(v));

			setTitle(text);
			setWidth(12 + getTheme().getStringWidth(text));
		}

		@Override
		public void onClicked(MouseButton button) {
			setSelected(this);
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
				case Tag.TAG_BYTE, Tag.TAG_SHORT, Tag.TAG_INT ->
						openEditOverlay(new IntConfig(Integer.MIN_VALUE, Integer.MAX_VALUE), ((NumericTag) nbt).getAsInt());
				case Tag.TAG_LONG ->
						openEditOverlay(new LongConfig(Long.MIN_VALUE, Long.MAX_VALUE), ((NumericTag) nbt).getAsLong());
				case Tag.TAG_FLOAT, Tag.TAG_DOUBLE, Tag.TAG_ANY_NUMERIC ->
						openEditOverlay(new DoubleConfig(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), ((NumericTag) nbt).getAsDouble());
				case Tag.TAG_STRING ->
						openEditOverlay(new StringConfig(), nbt.getAsString());
			}
		}

		private <T> void openEditOverlay(ConfigFromString<T> config, T val) {
			config.setValue(val);
			getGui().pushModalPanel(
					new EditStringConfigOverlay<>(getGui(), config, accepted -> onCallback(config, accepted))
							.atMousePosition()
			);
		}

		public void onCallback(ConfigValue<?> value, boolean accepted) {
			if (accepted) {
				switch (nbt.getId()) {
					case Tag.TAG_BYTE, Tag.TAG_SHORT, Tag.TAG_INT ->
							nbt = IntTag.valueOf(((Number) value.getValue()).intValue());
					case Tag.TAG_LONG ->
							nbt = LongTag.valueOf(((Number) value.getValue()).longValue());
					case Tag.TAG_FLOAT, Tag.TAG_DOUBLE, Tag.TAG_ANY_NUMERIC ->
							nbt = DoubleTag.valueOf(((Number) value.getValue()).doubleValue());
					case Tag.TAG_STRING ->
							nbt = StringTag.valueOf(value.getValue().toString());
				}

				parent.setTag(key, nbt);
				updateTitle();
			}

			NBTEditorScreen.this.openGui();
		}

		@Override
		public CompoundTag toNBT() {
			return Util.make(new CompoundTag(), t -> t.put(key, nbt));
		}
	}

	public abstract class ButtonNBTCollection extends ButtonNBT {
		public boolean collapsed;
		public final Map<String, ButtonNBT> children;
		public final Icon iconOpen, iconClosed;

		public ButtonNBTCollection(Panel panel, @Nullable ButtonNBTCollection parent, String key, Icon open, Icon closed) {
			super(panel, parent, key);
			iconOpen = open;
			iconClosed = closed;
			setCollapsed(false);
			setWidth(width + 2 + getTheme().getStringWidth(key));
			children = new LinkedHashMap<>();
			updateTitle();
		}

		@Override
		public void addChildren() {
			if (!collapsed) {
				for (var button : children.values()) {
					mainPanel.add(button);
					button.addChildren();
				}
			}
		}

		@Override
		public void onClicked(MouseButton button) {
			if (getMouseX() <= getX() + height) {
				setCollapsed(!collapsed);
				mainPanel.refreshWidgets();
			} else {
				setSelected(this);
				panelTopLeft.refreshWidgets();
			}
		}

		@Override
		public boolean mouseDoubleClicked(MouseButton button) {
			if (isMouseOver()) {
				setCollapsed(!collapsed);
				mainPanel.refreshWidgets();
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
				if (button instanceof ButtonNBTCollection collection) {
					collection.setCollapsedTree(c);
				}
			}
		}

		public abstract Tag getTag(String key);

		public abstract void setTag(String key, @Nullable Tag base);
	}

	public class ButtonNBTMap extends ButtonNBTCollection {
		private final CompoundTag map;
		private Icon hoverIcon = Icon.empty();

		public ButtonNBTMap(Panel panel, @Nullable ButtonNBTCollection parent, String key, CompoundTag map) {
			super(panel, parent, key, NBT_MAP_OPEN, NBT_MAP_CLOSED);
			this.map = map;
		}

		@Override
		public void updateTitle() {
			setTitle(Component.literal(key).withStyle(isSelected() ? ChatFormatting.GREEN : ChatFormatting.DARK_GREEN));
		}

		@Override
		public void updateChildren(boolean first) {
			children.clear();

			map.getAllKeys().stream().sorted(StringUtils.IGNORE_CASE_COMPARATOR).forEach(key -> {
				var nbt = makeNBTButton(this, key);
				children.put(key, nbt);
				nbt.updateChildren(first);
			});

			updateHoverIcon();

			if (first && !hoverIcon.isEmpty()) {
				setCollapsed(true);
			}
		}

		private void updateHoverIcon() {
			hoverIcon = Icon.empty();

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

				if (!infoList.isEmpty()) {
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
		public void draw(GuiGraphics pose, Theme theme, int x, int y, int w, int h) {
			super.draw(pose, theme, x, y, w, h);

			if (!hoverIcon.isEmpty()) {
				hoverIcon.draw(pose, x + 12 + theme.getStringWidth(getTitle()), y + 1, 8, 8);
			}
		}

		@Override
		public Optional<PositionedIngredient> getIngredientUnderMouse() {
			return PositionedIngredient.of(hoverIcon.getIngredient(), this, true);
		}

		@Override
		public Tag getTag(String key) {
			return map.get(key);
		}

		@Override
		public void setTag(String key, @Nullable Tag base) {
			if (base != null) {
				map.put(key, base);
			} else {
				map.remove(key);
			}

			updateHoverIcon();

			if (parent != null) {
				parent.setTag(this.key, map);
			}
		}

		@Override
		public boolean canCreateNew(int id) {
			return true;
		}

		@Override
		public CompoundTag toNBT() {
			var nbt = map.copy();

			if (this == buttonNBTRoot) {
				var infoList1 = new ListTag();
				var infoList0 = info.getList("text", Tag.TAG_STRING);

				if (!infoList0.isEmpty()) {
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
		public void updateTitle() {
			setTitle(Component.literal(key).withStyle(isSelected() ? ChatFormatting.YELLOW : ChatFormatting.GOLD));
		}

		@Override
		public void updateChildren(boolean first) {
			children.clear();
			for (var i = 0; i < list.size(); i++) {
				var s = Integer.toString(i);
				var nbt = makeNBTButton(this, s);
				children.put(s, nbt);
				nbt.updateChildren(first);
			}
		}

		@Override
		public Tag getTag(String key) {
			return list.get(Integer.parseInt(key));
		}

		@Override
		public void setTag(String key, @Nullable Tag base) {
			var id = Integer.parseInt(key);

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
				parent.setTag(this.key, list);
			}
		}

		@Override
		public boolean canCreateNew(int id) {
			return list.isEmpty() || list.getElementType() == id;
		}

		@Override
		public CompoundTag toNBT() {
			return Util.make(new CompoundTag(), t -> t.put(key, list));
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
				var nbt = makeNBTButton(this, s);
				children.put(s, nbt);
				nbt.updateChildren(first);
			}
		}

		@Override
		public Tag getTag(String key) {
			return ByteTag.valueOf(list.getByte(Integer.parseInt(key)));
		}

		@Override
		public void setTag(String key, @Nullable Tag base) {
			var id = Integer.parseInt(key);

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
				parent.setTag(this.key, new ByteArrayTag(list.toByteArray()));
			}
		}

		@Override
		public boolean canCreateNew(int id) {
			return id == Tag.TAG_BYTE;
		}

		@Override
		public CompoundTag toNBT() {
			return Util.make(new CompoundTag(), t -> t.put(key, new ByteArrayTag(list.toByteArray())));
		}
	}

	public class ButtonNBTIntArray extends ButtonNBTCollection {
		private final IntArrayList list;

		public ButtonNBTIntArray(Panel panel, ButtonNBTCollection parent, String key, IntArrayTag l) {
			super(panel, parent, key, NBT_INT_ARRAY_OPEN, NBT_INT_ARRAY_CLOSED);
			list = new IntArrayList(l.getAsIntArray());
		}

		@Override
		public void updateChildren(boolean first) {
			children.clear();
			for (var i = 0; i < list.size(); i++) {
				var s = Integer.toString(i);
				var nbt = makeNBTButton(this, s);
				children.put(s, nbt);
				nbt.updateChildren(first);
			}
		}

		@Override
		public Tag getTag(String key) {
			return IntTag.valueOf(list.getInt(Integer.parseInt(key)));
		}

		@Override
		public void setTag(String key, @Nullable Tag base) {
			var id = Integer.parseInt(key);

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
				parent.setTag(this.key, new IntArrayTag(list.toIntArray()));
			}
		}

		@Override
		public boolean canCreateNew(int id) {
			return id == Tag.TAG_INT;
		}

		@Override
		public CompoundTag toNBT() {
			return Util.make(new CompoundTag(), t -> t.put(key, new IntArrayTag(list.toIntArray())));
		}
	}
	private class TopLeftPanel extends Panel {
		public TopLeftPanel() {
			super(topPanel);
		}

		@Override
		public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			super.draw(graphics, theme, x, y, w, h);
		}

		@Override
		public void addWidgets() {
			add(new SimpleButton(this, Component.translatable("selectServer.delete"),
					selected == buttonNBTRoot ? Icons.BIN.combineWith(Color4I.rgba(0xc0202020)) : Icons.BIN, (widget, button) -> deleteTag()));

			var canRename = selected.parent instanceof ButtonNBTMap;

			Icon renameIcon = Icons.NOTES;
			add(new SimpleButton(this, Component.translatable("ftblibrary.gui.edit_tag_name"), canRename ? renameIcon : renameIcon.combineWith(Color4I.rgba(0xc0202020)), (btn, mb) -> {
				if (canRename) {
					getGui().pushModalPanel(makeRenameOverlay(btn));
				}
			}));

			if (selected instanceof ButtonBasicTag) {
				add(new SimpleButton(this, Component.translatable("ftblibrary.gui.edit_tag_value"), Icons.FEATHER,
						(widget, button) -> ((ButtonBasicTag) selected).edit()));
			}

			List<Widget> addBtns = buildAddButtons();
			if (!addBtns.isEmpty()) {
				TextField addLabel = new TextField(this).setText(Component.literal("   ").append(Component.translatable("gui.add")))
						.addFlags(Theme.CENTERED_V);
				add(addLabel);
				addAll(addBtns);
			}
		}

		private void deleteTag() {
			if (selected != buttonNBTRoot && selected.parent != null) {
				selected.parent.setTag(selected.key, null);
				selected.parent.updateChildren(false);
				setSelected(selected.parent);
				mainPanel.refreshWidgets();
				panelTopLeft.refreshWidgets();
			}
		}

		@NotNull
		private List<Widget> buildAddButtons() {
			List<Widget> addBtns = new ArrayList<>();
			if (selected.canCreateNew(Tag.TAG_COMPOUND)) {
				addBtns.add(newTag(this, "Compound", NBT_MAP, CompoundTag::new));
			}
			if (selected.canCreateNew(Tag.TAG_LIST)) {
				addBtns.add(newTag(this, "List", NBT_LIST, ListTag::new));
			}
			if (selected.canCreateNew(Tag.TAG_STRING)) {
				addBtns.add(newTag(this, "String", NBT_STRING, () -> StringTag.valueOf("")));
			}
			if (selected.canCreateNew(Tag.TAG_BYTE)) {
				addBtns.add(newTag(this, "Byte", NBT_BYTE, () -> ByteTag.valueOf((byte) 0)));
			}
			if (selected.canCreateNew(Tag.TAG_SHORT)) {
				addBtns.add(newTag(this, "Short", NBT_SHORT, () -> ShortTag.valueOf((short) 0)));
			}
			if (selected.canCreateNew(Tag.TAG_INT)) {
				addBtns.add(newTag(this, "Int", NBT_INT, () -> IntTag.valueOf(0)));
			}
			if (selected.canCreateNew(Tag.TAG_LONG)) {
				addBtns.add(newTag(this, "Long", NBT_LONG, () -> LongTag.valueOf(0L)));
			}
			if (selected.canCreateNew(Tag.TAG_FLOAT)) {
				addBtns.add(newTag(this, "Float", NBT_FLOAT, () -> FloatTag.valueOf(0F)));
			}
			if (selected.canCreateNew(Tag.TAG_DOUBLE)) {
				addBtns.add(newTag(this, "Double", NBT_DOUBLE, () -> DoubleTag.valueOf(0D)));
			}
			if (selected.canCreateNew(Tag.TAG_BYTE_ARRAY)) {
				addBtns.add(newTag(this, "Byte Array", NBT_BYTE_ARRAY, () -> new ByteArrayTag(new byte[0])));
			}
			if (selected.canCreateNew(Tag.TAG_INT_ARRAY)) {
				addBtns.add(newTag(this, "Int Array", NBT_INT_ARRAY, () -> new IntArrayTag(new int[0])));
			}
			return addBtns;
		}

		@NotNull
		private EditStringConfigOverlay<String> makeRenameOverlay(SimpleButton button) {
			var value = new StringConfig();
//			int overlayWidth = 100;
			if (selected != null) {
				value.setValue(selected.key);
//				overlayWidth = getTheme().getStringWidth(selected.key) + 100;
			}
			var overlay = new EditStringConfigOverlay<>(this, value, accepted -> {
				if (accepted && !value.getValue().isEmpty() && selected.parent != null) {
					var parent = selected.parent;
					var nbt = parent.getTag(selected.key);
					parent.setTag(selected.key, null);
					parent.setTag(value.getValue(), nbt);
					parent.updateChildren(false);
					setSelected(parent.children.get(value.getValue()));
					mainPanel.refreshWidgets();
				}
				getGui().openGui();
			}, Component.literal("New name"));
			overlay.setPos(button.posX, button.posY + button.height + 4);
			overlay.setExtraZlevel(300);
			return overlay;
		}

		@Override
		public void alignWidgets() {
			setWidth(align(new WidgetLayout.Horizontal(2, 4, 2)));
			widgets.forEach(w -> w.setY((TOP_PANEL_H - w.height) / 2 - 2));
		}
	}

	private class TopRightPanel extends Panel {
		public TopRightPanel() {
			super(topPanel);
		}

		@Override
		public void addWidgets() {
			add(new SimpleButton(this, List.of(Component.translatable("gui.copy"), hotkeyTooltip("Ctrl + C")), ItemIcon.getItemIcon(Items.PAPER), (widget, button) -> copyToClipboard()));

			add(new SimpleButton(this, List.of(Component.translatable("gui.collapse_all"), hotkeyTooltip("-")), Icons.DOWN,
					(widget, button) -> collapseAll(true)));

			add(new SimpleButton(this, List.of(Component.translatable("gui.expand_all"), hotkeyTooltip("="), hotkeyTooltip("+")), Icons.UP,
					(widget, button) -> collapseAll(false)));
		}

		@Override
		public void alignWidgets() {
			setWidth(align(new WidgetLayout.Horizontal(2, 4, 2)));
		}
	}

	private class CustomTopPanel extends TopPanel {
		@Override
		public void addWidgets() {
			add(panelTopLeft);
			add(panelTopRight);
		}

		@Override
		public void alignWidgets() {
			panelTopLeft.setPosAndSize(0, 2, panelTopLeft.width, TOP_PANEL_H);

			panelTopRight.setPosAndSize(width - panelTopRight.width, 2, 0, TOP_PANEL_H);
			panelTopRight.alignWidgets();
		}
	}

	protected class NBTPanel extends Panel {
		public NBTPanel() {
			super(NBTEditorScreen.this);
		}

		@Override
		public void addWidgets() {
			add(buttonNBTRoot);
			buttonNBTRoot.addChildren();
		}

		@Override
		public void alignWidgets() {
			align(WidgetLayout.VERTICAL);
		}
	}
}
