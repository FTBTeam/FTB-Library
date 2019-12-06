package com.feed_the_beast.mods.ftbguilibrary;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigCallback;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigFluid;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigNBT;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.command.Commands;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class FTBGUILibraryTest
{
	public void init()
	{
		MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
	}

	private void serverStarting(FMLServerStartingEvent event)
	{
		event.getCommandDispatcher().register(Commands.literal("ftb_gui_library_test").executes(context -> {
			ConfigGroup group = new ConfigGroup("test");
			TestClass testClass = new TestClass();
			group.savedCallback = testClass;
			testClass.getConfig(group);
			Minecraft.getInstance().enqueue(new GuiEditConfig(group));
			return Command.SINGLE_SUCCESS;
		}));
	}

	public static class TestClass implements ConfigCallback
	{
		public boolean boolValue = false;
		public int intValue = 10;
		public ItemStack itemStackValue = ItemStack.EMPTY;
		public String stringValue = "abc";
		public List<String> stringListValue = new ArrayList<>();
		public CompoundNBT nbtValue = null;
		public Fluid fluidValue = Fluids.EMPTY;

		public void getConfig(ConfigGroup group)
		{
			group.addBool("bool", boolValue, v -> boolValue = v, false);
			group.addInt("int", intValue, v -> intValue = v, 10, -2, 5000000);
			group.addItemStack("item_stack", itemStackValue, v -> itemStackValue = v, ItemStack.EMPTY, false, true);
			group.add("nbt", new ConfigNBT(), nbtValue, v -> nbtValue = v, null);
			group.add("fluid", new ConfigFluid(true), fluidValue, v -> fluidValue = v, Fluids.EMPTY);

			ConfigGroup group1 = group.getGroup("sub.group");
			group1.addString("string", stringValue, v -> stringValue = v, "abc");
			group1.addList("string_list", stringListValue, new ConfigString(Pattern.compile("\\w+")), "T");
		}

		@Override
		public void save(boolean accepted)
		{
			ClientUtils.getCurrentGuiAs(GuiEditConfig.class).closeGui();
		}
	}
}