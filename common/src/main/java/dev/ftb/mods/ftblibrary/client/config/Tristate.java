package dev.ftb.mods.ftblibrary.client.config;

import de.marhali.json5.Json5Object;
import de.marhali.json5.Json5Primitive;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.util.NameMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

public enum Tristate {
    FALSE("false", "false", InteractionResult.FAIL, Color4I.rgb(0xD52834), Color4I.rgb(0x991A24),1, Icons.ACCEPT_GRAY),
    TRUE("true", "true", InteractionResult.SUCCESS, Color4I.rgb(0x33AA33), Color4I.rgb(0x158015),0, Icons.ACCEPT),
    DEFAULT("default", "Default", InteractionResult.PASS, Color4I.rgb(0x0094FF), Color4I.rgb(0x0065A0), 2, Icons.SETTINGS);

    public static final Tristate[] VALUES = values();
    public static final NameMap<Tristate> NAME_MAP = NameMap.of(DEFAULT, VALUES).id(v -> v.name).name(v -> Component.literal(v.displayName)).color(v -> v.colorHi).icon(v -> v.icon).create();
    public final String name;
    public final String displayName;
    public final InteractionResult result;
    public final Color4I colorHi;
    public final Color4I colorLo;
    public final Icon<?> icon;
    private final int opposite;

    Tristate(String name, String displayName, InteractionResult result, Color4I colorHi, Color4I colorLo, int opposite, Icon<?> icon) {
        this.name = name;
        this.displayName = displayName;
        this.result = result;
        this.colorHi = colorHi;
        this.colorLo = colorLo;
        this.opposite = opposite;
        this.icon = icon;
    }

    public static Tristate read(Json5Object json, String key) {
        return json.get(key) instanceof Json5Primitive p && p.isBoolean() ?
                Tristate.ofBoolean(p.getAsBoolean()) :
                DEFAULT;
    }

    public static Tristate read(CompoundTag nbt, String key) {
        return nbt.contains(key) ? nbt.getBooleanOr(key, false) ? TRUE : FALSE : DEFAULT;
    }

    public static Tristate read(FriendlyByteBuf buffer) {
        return VALUES[buffer.readUnsignedByte()];
    }

    public static Tristate ofBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    public boolean isTrue() {
        return this == TRUE;
    }

    public boolean isFalse() {
        return this == FALSE;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }

    public boolean get(boolean def) {
        return isDefault() ? def : isTrue();
    }

    public Tristate getOpposite() {
        return NAME_MAP.get(opposite);
    }

    public String toString() {
        return name;
    }

    public void write(Json5Object json, String key) {
        if (!isDefault()) {
            json.addProperty(key, isTrue());
        }
    }

    public void write(CompoundTag nbt, String key) {
        if (!isDefault()) {
            nbt.putBoolean(key, isTrue());
        }
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(ordinal());
    }

    public Color4I getColor(Theme theme) {
        return theme.hasDarkBackground() ? colorHi : colorLo;
    }
}
