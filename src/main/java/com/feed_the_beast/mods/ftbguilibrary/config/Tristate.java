package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.eventbus.api.Event;

/**
 * @author LatvianModder
 */
public enum Tristate
{
	FALSE("false", "false", Event.Result.DENY, ConfigBoolean.COLOR_FALSE, 1),
	TRUE("true", "true", Event.Result.ALLOW, ConfigBoolean.COLOR_TRUE, 0),
	DEFAULT("default", "Default", Event.Result.DEFAULT, ConfigEnum.COLOR, 2);

	public static final Tristate[] VALUES = values();
	public static final NameMap<Tristate> NAME_MAP = NameMap.createWithNameAndColor(DEFAULT, value -> new StringTextComponent(value.displayName), value -> value.color, VALUES);

	public static Tristate read(CompoundNBT nbt, String key)
	{
		return nbt.contains(key, Constants.NBT.TAG_BYTE) ? VALUES[MathHelper.clamp(nbt.getByte(key), 0, 2)] : DEFAULT;
	}

	public final String name;
	public final String displayName;
	public final Event.Result result;
	public final Color4I color;
	private final int opposite;

	Tristate(String n, String dn, Event.Result r, Color4I c, int o)
	{
		name = n;
		displayName = dn;
		result = r;
		color = c;
		opposite = o;
	}

	public boolean isTrue()
	{
		return this == TRUE;
	}

	public boolean isFalse()
	{
		return this == FALSE;
	}

	public boolean isDefault()
	{
		return this == DEFAULT;
	}

	public boolean get(boolean def)
	{
		return isDefault() ? def : isTrue();
	}

	public Tristate getOpposite()
	{
		return NAME_MAP.get(opposite);
	}

	public String toString()
	{
		return name;
	}

	public void write(CompoundNBT nbt, String key)
	{
		nbt.putByte(key, (byte) ordinal());
	}
}