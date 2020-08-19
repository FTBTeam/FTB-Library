package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class ConfigNBT extends ConfigFromString<CompoundNBT>
{
	public static final StringTextComponent EMPTY_NBT = new StringTextComponent("{}");
	public static final StringTextComponent NON_EMPTY_NBT = new StringTextComponent("{...}");

	@Override
	public CompoundNBT copy(CompoundNBT v)
	{
		return v.copy();
	}

	@Override
	public String getStringFromValue(@Nullable CompoundNBT v)
	{
		return v == null ? "null" : v.toString();
	}

	@Override
	public ITextComponent getStringForGUI(@Nullable CompoundNBT v)
	{
		return v == null ? NULL_TEXT : v.isEmpty() ? EMPTY_NBT : NON_EMPTY_NBT;
	}

	@Override
	public boolean parse(@Nullable Consumer<CompoundNBT> callback, String string)
	{
		if (string.equals("null"))
		{
			if (callback != null)
			{
				callback.accept(null);
			}

			return true;
		}

		try
		{
			CompoundNBT nbt = JsonToNBT.getTagFromJson(string);

			if (callback != null)
			{
				callback.accept(nbt);
			}

			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	@Override
	public void addInfo(TooltipList list)
	{
		list.add(info("Value", value == null ? "null" : value.toFormattedComponent()));
		list.add(info("Default", defaultValue == null ? "null" : defaultValue.toFormattedComponent()));
	}
}