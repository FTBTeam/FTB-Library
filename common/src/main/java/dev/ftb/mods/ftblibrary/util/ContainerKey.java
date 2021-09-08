package dev.ftb.mods.ftblibrary.util;

import dev.ftb.mods.ftblibrary.core.CompoundContainerFTBL;
import net.minecraft.world.Container;

public final class ContainerKey {
	public final Container container;

	public ContainerKey(Container c) {
		container = c;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ContainerKey that = (ContainerKey) o;

		if (container instanceof CompoundContainerFTBL && that.container instanceof CompoundContainerFTBL) {
			CompoundContainerFTBL a = (CompoundContainerFTBL) container;
			CompoundContainerFTBL b = (CompoundContainerFTBL) that.container;
			Container a1 = a.getContainer1FTBL();
			Container a2 = a.getContainer2FTBL();
			Container b1 = b.getContainer1FTBL();
			Container b2 = b.getContainer2FTBL();
			return a1 == b1 && a2 == b2 || a1 == b2 && a2 == b1;
		}

		return container == that.container;
	}

	@Override
	public int hashCode() {
		if (container instanceof CompoundContainerFTBL) {
			CompoundContainerFTBL c = (CompoundContainerFTBL) container;
			return c.getContainer1FTBL().hashCode() ^ c.getContainer2FTBL().hashCode();
		}

		return container.hashCode();
	}
}
