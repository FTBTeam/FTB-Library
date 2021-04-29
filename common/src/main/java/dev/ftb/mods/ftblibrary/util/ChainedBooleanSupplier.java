package dev.ftb.mods.ftblibrary.util;

import java.util.function.BooleanSupplier;

/**
 * @author LatvianModder
 */
@FunctionalInterface
public interface ChainedBooleanSupplier extends BooleanSupplier {
	ChainedBooleanSupplier TRUE = () -> true;
	ChainedBooleanSupplier FALSE = () -> false;

	default ChainedBooleanSupplier not() {
		return () -> !getAsBoolean();
	}

	default ChainedBooleanSupplier or(BooleanSupplier supplier) {
		return () -> getAsBoolean() || supplier.getAsBoolean();
	}

	default ChainedBooleanSupplier and(BooleanSupplier supplier) {
		return () -> getAsBoolean() && supplier.getAsBoolean();
	}

	default ChainedBooleanSupplier xor(BooleanSupplier supplier) {
		return () -> getAsBoolean() != supplier.getAsBoolean();
	}
}