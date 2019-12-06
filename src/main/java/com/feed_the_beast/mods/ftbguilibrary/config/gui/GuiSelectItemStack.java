package com.feed_the_beast.mods.ftbguilibrary.config.gui;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigCallback;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigInt;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigItemStack;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigNBT;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.BlankPanel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.PanelScrollBar;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.TextBox;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetLayout;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetType;
import com.feed_the_beast.mods.ftbguilibrary.widget.WrappedIngredient;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author LatvianModder
 */
public class GuiSelectItemStack extends GuiBase
{
	private static boolean allItems = true;

	private class ItemStackButton extends Button
	{
		private final ItemStack stack;

		private ItemStackButton(Panel panel, ItemStack is)
		{
			super(panel, "", GuiIcons.BARRIER);
			setSize(18, 18);
			stack = is;
			title = null;
			icon = null;
		}

		public boolean shouldAdd(String search, String mod)
		{
			if (search.isEmpty())
			{
				return true;
			}

			if (!mod.isEmpty())
			{
				return stack.getItem().getRegistryName().getNamespace().contains(mod);
			}

			return stack.getDisplayName().getString().toLowerCase().contains(search);
		}

		@Override
		public String getTitle()
		{
			if (title == null)
			{
				title = stack.getDisplayName().getFormattedText();
			}

			return title;
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
		}

		@Override
		public WidgetType getWidgetType()
		{
			return stack.getItem() == current.getItem() && Objects.equals(stack.getTag(), current.getTag()) ? WidgetType.MOUSE_OVER : super.getWidgetType();
		}

		@Override
		public void drawBackground(Theme theme, int x, int y, int w, int h)
		{
			(getWidgetType() == WidgetType.MOUSE_OVER ? Color4I.LIGHT_GREEN.withAlpha(70) : Color4I.BLACK.withAlpha(50)).draw(x, y, w, h);
		}

		@Override
		public void drawIcon(Theme theme, int x, int y, int w, int h)
		{
			GuiHelper.drawItem(stack, x, y, w / 16D, h / 16D, true);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();
			current = stack.copy();
		}

		@Override
		public Object getIngredientUnderMouse()
		{
			return new WrappedIngredient(stack).tooltip();
		}
	}

	private class ButtonSwitchMode extends Button
	{
		private final Icon ICON_ALL = ItemIcon.getItemIcon(Items.COMPASS);
		private final Icon ICON_INV = ItemIcon.getItemIcon(Blocks.CHEST);

		public ButtonSwitchMode(Panel panel)
		{
			super(panel);
		}

		@Override
		public void drawIcon(Theme theme, int x, int y, int w, int h)
		{
			(allItems ? ICON_ALL : ICON_INV).draw(x, y, w, h);
		}

		@Override
		public String getTitle()
		{
			return I18n.format("ftbguilibrary.select_item.list_mode", TextFormatting.GRAY + (allItems ? I18n.format("ftbguilibrary.select_item.list_mode.all") : I18n.format("ftbguilibrary.select_item.list_mode.inv"))) + TextFormatting.DARK_GRAY + " [" + (panelStacks.widgets.size() - 1) + "]";
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();
			allItems = !allItems;
			panelStacks.refreshWidgets();
		}
	}

	private abstract class ButtonStackConfig extends Button
	{
		public ButtonStackConfig(Panel panel, String title, Icon icon)
		{
			super(panel, title, icon);
		}

		@Override
		public WidgetType getWidgetType()
		{
			return current.isEmpty() ? WidgetType.DISABLED : super.getWidgetType();
		}
	}

	private class ButtonEditData extends Button
	{
		public ButtonEditData(Panel panel)
		{
			super(panel, "", GuiIcons.BUG);
		}

		@Override
		public void drawIcon(Theme theme, int x, int y, int w, int h)
		{
			GuiHelper.drawItem(current, x, y, w / 16D, h / 16D, true);
		}

		@Override
		public String getTitle()
		{
			return current.getDisplayName().getFormattedText();
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();
			ConfigNBT c = new ConfigNBT();
			GuiEditConfigFromString.open(c, current.serializeNBT(), config.defaultValue.serializeNBT(), accepted -> {
				if (accepted)
				{
					current = ItemStack.read(c.value);
				}

				run();
			});
		}
	}

	private class ButtonCount extends ButtonStackConfig
	{
		public ButtonCount(Panel panel)
		{
			super(panel, I18n.format("ftbguilibrary.select_item.count"), ItemIcon.getItemIcon(Items.PAPER));
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();
			ConfigInt c = new ConfigInt(0, current.getMaxStackSize());
			GuiEditConfigFromString.open(c, current.getCount(), config.defaultValue.getCount(), accepted -> {
				if (accepted)
				{
					current.setCount(c.value);
				}

				run();
			});
		}
	}

	private class ButtonNBT extends ButtonStackConfig
	{
		public ButtonNBT(Panel panel)
		{
			super(panel, I18n.format("ftbguilibrary.select_item.nbt"), ItemIcon.getItemIcon(Items.NAME_TAG));
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();
			ConfigNBT c = new ConfigNBT();
			GuiEditConfigFromString.open(c, current.getTag(), config.defaultValue.getTag(), accepted -> {
				if (accepted)
				{
					current.setTag(c.value);
				}

				run();
			});
		}
	}

	private class ButtonCaps extends ButtonStackConfig
	{
		public ButtonCaps(Panel panel)
		{
			super(panel, I18n.format("ftbguilibrary.select_item.caps"), ItemIcon.getItemIcon(Blocks.ANVIL));
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();

			final CompoundNBT nbt = current.serializeNBT();
			ConfigNBT c = new ConfigNBT();

			GuiEditConfigFromString.open(c, (CompoundNBT) nbt.get("ForgeCaps"), (CompoundNBT) config.defaultValue.serializeNBT().get("ForgeCaps"), accepted -> {
				if (accepted)
				{
					if (c.value == null || c.value.isEmpty())
					{
						nbt.remove("ForgeCaps");
					}
					else
					{
						nbt.put("ForgeCaps", c.value);
					}

					current = ItemStack.read(nbt);
				}

				GuiSelectItemStack.this.run();
			});
		}
	}

	private class ThreadItemList extends Thread
	{
		private final String search;

		public ThreadItemList()
		{
			super("Item Search Thread");
			setDaemon(true);
			search = searchBox.getText().toLowerCase();
		}

		@Override
		public void run()
		{
			List<Widget> widgets = new ArrayList<>();
			NonNullList<ItemStack> list = NonNullList.create();

			if (allItems)
			{
				for (Item item : ForgeRegistries.ITEMS)
				{
					item.fillItemGroup(ItemGroup.SEARCH, list);
				}

				list.add(new ItemStack(Blocks.COMMAND_BLOCK));
				list.add(new ItemStack(Blocks.BARRIER));
				list.add(new ItemStack(Blocks.STRUCTURE_VOID));
			}
			else
			{
				for (int i = 0; i < Minecraft.getInstance().player.inventory.getSizeInventory(); i++)
				{
					ItemStack stack = Minecraft.getInstance().player.inventory.getStackInSlot(i);

					if (!stack.isEmpty())
					{
						list.add(stack);
					}
				}
			}

			String mod = "";

			if (search.startsWith("@"))
			{
				mod = search.substring(1);
			}

			ItemStackButton button = new ItemStackButton(panelStacks, ItemStack.EMPTY);

			if (config.allowEmpty && button.shouldAdd(search, mod))
			{
				widgets.add(new ItemStackButton(panelStacks, ItemStack.EMPTY));
			}

			for (ItemStack stack : list)
			{
				if (!stack.isEmpty())
				{
					button = new ItemStackButton(panelStacks, stack);

					if (button.shouldAdd(search, mod))
					{
						widgets.add(button);
					}
				}
			}

			for (int i = 0; i < widgets.size(); i++)
			{
				widgets.get(i).setPos(1 + (i % 9) * 19, 1 + (i / 9) * 19);
			}

			newStackWidgets = widgets;
		}
	}

	private final ConfigItemStack config;
	private final ConfigCallback callback;
	private ItemStack current;
	private final Button buttonCancel, buttonAccept;
	private final Panel panelStacks;
	private final PanelScrollBar scrollBar;
	private TextBox searchBox;
	private final Panel tabs;
	private ThreadItemList threadItemList;
	private List<Widget> newStackWidgets;
	public long update = Long.MAX_VALUE;

	public GuiSelectItemStack(ConfigItemStack c, ConfigCallback cb)
	{
		setSize(211, 150);
		config = c;
		callback = cb;
		current = config.value.isEmpty() ? ItemStack.EMPTY : config.value.copy();

		int bsize = width / 2 - 10;

		buttonCancel = new SimpleTextButton(this, I18n.format("gui.cancel"), Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				playClickSound();
				callback.save(false);
			}

			@Override
			public boolean renderTitleInCenter()
			{
				return true;
			}
		};

		buttonCancel.setPosAndSize(8, height - 24, bsize, 16);

		buttonAccept = new SimpleTextButton(this, I18n.format("gui.accept"), Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				playClickSound();
				config.setCurrentValue(current);
				callback.save(true);
			}

			@Override
			public boolean renderTitleInCenter()
			{
				return true;
			}
		};

		buttonAccept.setPosAndSize(width - bsize - 8, height - 24, bsize, 16);

		panelStacks = new BlankPanel(this)
		{
			@Override
			public void addWidgets()
			{
				update = System.currentTimeMillis() + 200L;
			}

			@Override
			public void drawBackground(Theme theme, int x, int y, int w, int h)
			{
				theme.drawPanelBackground(x, y, w, h);
			}
		};

		panelStacks.setPosAndSize(9, 24, 9 * 19 + 1, 5 * 19 + 1);

		scrollBar = new PanelScrollBar(this, panelStacks);
		scrollBar.setCanAlwaysScroll(true);
		scrollBar.setScrollStep(20);

		searchBox = new TextBox(this)
		{
			@Override
			public void onTextChanged()
			{
				panelStacks.refreshWidgets();
			}
		};

		searchBox.setPosAndSize(8, 7, width - 16, 12);
		searchBox.ghostText = I18n.format("gui.search_box");
		searchBox.setFocused(true);

		tabs = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				add(new ButtonSwitchMode(this));
				add(new ButtonEditData(this));

				if (!config.singleItemOnly)
				{
					add(new ButtonCount(this));
				}

				add(new ButtonNBT(this));
				add(new ButtonCaps(this));
			}

			@Override
			public void alignWidgets()
			{
				for (Widget widget : widgets)
				{
					widget.setSize(20, 20);
				}

				setHeight(align(WidgetLayout.VERTICAL));
			}
		};

		tabs.setPosAndSize(-19, 8, 20, 0);
		threadItemList = new ThreadItemList();
		threadItemList.start();
	}

	@Override
	public void addWidgets()
	{
		add(tabs);
		add(panelStacks);
		add(scrollBar);
		add(searchBox);
		add(buttonCancel);
		add(buttonAccept);
	}

	@Override
	public void onClosed()
	{
		super.onClosed();
		stopSearch();
	}

	private void stopSearch()
	{
		if (threadItemList != null)
		{
			try
			{
				threadItemList.interrupt();
			}
			catch (Exception ex)
			{
			}
		}

		threadItemList = null;
	}

	@Override
	public boolean onClosedByKey(Key key)
	{
		if (super.onClosedByKey(key))
		{
			callback.save(false);
			return false;
		}

		return false;
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		super.drawBackground(theme, x, y, w, h);

		if (newStackWidgets != null)
		{
			panelStacks.widgets.clear();
			panelStacks.addAll(newStackWidgets);
			scrollBar.setPosAndSize(panelStacks.posX + panelStacks.width + 6, panelStacks.posY - 1, 16, panelStacks.height + 2);
			scrollBar.setValue(0);
			scrollBar.setMaxValue(1 + MathHelper.ceil(panelStacks.widgets.size() / 9F) * 19);
			newStackWidgets = null;
		}

		long now = System.currentTimeMillis();

		if (now >= update)
		{
			update = Long.MAX_VALUE;
			stopSearch();
			threadItemList = new ThreadItemList();
			threadItemList.start();
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		Screen screen = getPrevScreen();
		return screen != null && screen.isPauseScreen();
	}
}