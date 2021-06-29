package dev.ftb.mods.ftblibrary.snbt;

class SNBTTagProperties {
	public static final SNBTTagProperties DEFAULT = new SNBTTagProperties();
	public static final int TYPE_FALSE = 1;
	public static final int TYPE_TRUE = 2;

	int valueType = 0;
	String comment = "";
	boolean singleLine;
}
