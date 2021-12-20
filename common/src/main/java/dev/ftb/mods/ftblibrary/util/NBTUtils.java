package dev.ftb.mods.ftblibrary.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.DataOutputStream;

public class NBTUtils {
	public static long getSizeInBytes(CompoundTag nbt, boolean compressed) {
		try {
			var byteCounter = new ByteCounterOutputStream();

			if (compressed) {
				NbtIo.writeCompressed(nbt, byteCounter);
			} else {
				NbtIo.write(nbt, new DataOutputStream(byteCounter));
			}

			return byteCounter.getSize();
		} catch (Exception ex) {
			return -1L;
		}
	}
}
