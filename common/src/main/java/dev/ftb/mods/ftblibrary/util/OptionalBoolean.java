package dev.ftb.mods.ftblibrary.util;

import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class OptionalBoolean {
	public static final OptionalBoolean EMPTY = new OptionalBoolean(null);
	public static final OptionalBoolean TRUE = new OptionalBoolean(true);
	public static final OptionalBoolean FALSE = new OptionalBoolean(false);

	public static OptionalBoolean ofNullable(@Nullable Boolean v) {
		return v == null ? EMPTY : of(v);
	}

	public static OptionalBoolean of(boolean v) {
		return v ? TRUE : FALSE;
	}

	@FunctionalInterface
	public interface Consumer {
		void accept(boolean value);
	}

	private final Boolean value;

	private OptionalBoolean(@Nullable Boolean v) {
		value = v;
	}

	public boolean orElse(boolean b) {
		return value == null ? b : value;
	}

	public boolean get() {
		if (value == null) {
			throw new NoSuchElementException("No value present");
		}

		return value;
	}

	public boolean isPresent() {
		return value != null;
	}

	public void ifPresent(Consumer consumer) {
		if (value != null) {
			consumer.accept(value);
		}
	}

	public boolean orElseGet(BooleanSupplier other) {
		return value != null ? value : other.getAsBoolean();
	}

	public <X extends Throwable> boolean orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (value != null) {
			return value;
		} else {
			throw exceptionSupplier.get();
		}
	}
}