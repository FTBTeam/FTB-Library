package dev.ftb.mods.ftblibrary.snbt;

class TagProperties {
	public static final TagProperties DEFAULT = new TagProperties();
	public static final int TYPE_FALSE = 1;
	public static final int TYPE_TRUE = 2;
	public static final int TYPE_UUID = 3; // TODO: Implement

	int valueType = 0;
	String comment = "";
	boolean singleLine;
}
