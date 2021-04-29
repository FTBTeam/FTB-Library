package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.CompoundTag;

import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * @author LatvianModder
 */
public class OrderedCompoundTag extends CompoundTag {
	public HashSet<String> booleanKeys;
	public boolean singleLine;

	public OrderedCompoundTag() {
		super(new LinkedHashMap<>());
		singleLine = false;
	}

	@Override
	public void putBoolean(String key, boolean value) {
		if (booleanKeys == null) {
			booleanKeys = new HashSet<>();
		}

		booleanKeys.add(key);
		super.putBoolean(key, value);
	}
}
