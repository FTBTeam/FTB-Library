package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutput;
import java.io.IOException;

public class SpecialTag implements Tag {
	public static final SpecialTag TRUE = new SpecialTag(ByteTag.valueOf(true));
	public static final SpecialTag FALSE = new SpecialTag(ByteTag.valueOf(false));
	public static final SpecialTag NAN_D = new SpecialTag(DoubleTag.valueOf(Double.NaN));
	public static final SpecialTag POS_INFINITY_D = new SpecialTag(DoubleTag.valueOf(Double.POSITIVE_INFINITY));
	public static final SpecialTag NEG_INFINITY_D = new SpecialTag(DoubleTag.valueOf(Double.NEGATIVE_INFINITY));
	public static final SpecialTag NAN_F = new SpecialTag(FloatTag.valueOf(Float.NaN));
	public static final SpecialTag POS_INFINITY_F = new SpecialTag(FloatTag.valueOf(Float.POSITIVE_INFINITY));
	public static final SpecialTag NEG_INFINITY_F = new SpecialTag(FloatTag.valueOf(Float.NEGATIVE_INFINITY));

	@Nullable
	public static Tag unwrap(@Nullable Tag t) {
		if (t instanceof SpecialTag) {
			return ((SpecialTag) t).wrappedTag;
		}

		return t;
	}

	public final Tag wrappedTag;

	public SpecialTag(Tag t) {
		wrappedTag = t;
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		wrappedTag.write(dataOutput);
	}

	@Override
	public byte getId() {
		return wrappedTag.getId();
	}

	@Override
	public TagType<?> getType() {
		return wrappedTag.getType();
	}

	@Override
	public Tag copy() {
		return this;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		wrappedTag.accept(tagVisitor);
	}

	@Override
	public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
		return wrappedTag.accept(streamTagVisitor);
	}

	@Override
	public String getAsString() {
		return wrappedTag.getAsString();
	}

	@Override
	public String toString() {
		return wrappedTag.toString();
	}
}
