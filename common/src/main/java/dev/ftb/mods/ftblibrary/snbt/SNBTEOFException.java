package dev.ftb.mods.ftblibrary.snbt;

public class SNBTEOFException extends SNBTSyntaxException {
	public SNBTEOFException() {
		super("Unexpected end of file!");
	}
}
