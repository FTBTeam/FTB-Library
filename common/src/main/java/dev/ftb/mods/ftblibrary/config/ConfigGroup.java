package dev.ftb.mods.ftblibrary.config;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Represent a collection of {@link ConfigValue} objects, possibly recursively nested in one or more subgroups.
 */
public class ConfigGroup implements Comparable<ConfigGroup> {
	private final String id;
	private final ConfigGroup parent;
	private final Map<String, ConfigValue<?>> values;
	private final Map<String, ConfigGroup> subgroups;
	private final ConfigCallback savedCallback;
	private final int displayOrder;
	private String nameKey;

	private ConfigGroup(String id, ConfigGroup parent, ConfigCallback savedCallback, int displayOrder) {
		this.id = id;
		this.parent = parent;
		this.values = new LinkedHashMap<>();
		this.subgroups = new LinkedHashMap<>();
		this.savedCallback = savedCallback;
		this.nameKey = "";
		this.displayOrder = displayOrder;
	}

	/**
	 * Create a new top-level config group
	 * @param id a unique id for this group
	 */
	public ConfigGroup(String id) {
		this(id, null, null, 0);
	}

	/**
	 * Create a new top-level config group
	 * @param id a unique id for this group
	 * @param savedCallback a callback to be run when the {@link #save(boolean)} method is called
	 */
	public ConfigGroup(String id, ConfigCallback savedCallback) {
		this(id, null, savedCallback, 0);
	}

	/**
	 * Get this group's unique id
	 * @return the ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get this group's parent group
	 * @return the parent group; will be null for a top-level group
	 */
	@Nullable
	public ConfigGroup getParent() {
		return parent;
	}

	/**
	 * Get the group's naming key, for translation purposes.
	 * @return the translation key
	 */
	public String getNameKey() {
		return nameKey.isEmpty() ? getPath() : nameKey;
	}

	/**
	 * Set a custom translation key for this group. By default, the translation is built as a path based on the group
	 * hierarchy, e.g. "toplevelgroup_id.subgroup1_id.subgroup2_id".
	 * @param key a custom translation key
	 * @return the group
	 */
	public ConfigGroup setNameKey(String key) {
		nameKey = key;
		return this;
	}

	/**
	 * Get the displayable group name
	 * @return the group name
	 */
	public Component getName() {
		return Component.translatable(getNameKey());
	}

	/**
	 * Get the tooltip text for this group. This depends on a ".tooltip" translation key existing for the group,
	 * which is the result of {@link #getNameKey()} with ".tooltip" appended;
	 * @return the tooltip text, or {@code Component.empty()} if no tooltip translation key exists
	 */
	public Component getTooltip() {
		var t = getNameKey() + ".tooltip";
		return I18n.exists(t) ? Component.translatable(t) : Component.empty();
	}

	/**
	 * Get, or create, a subgroup in this group.
	 *
	 * @param id unique id of the subgroup
	 * @param displayOrder order in which groups are displayed in the GUI (higher numbers come after)
	 * @return the subgroup, which may have just been created
	 */
	public ConfigGroup getOrCreateSubgroup(String id, int displayOrder) {
		var index = id.indexOf('.');

		if (index == -1) {
			return subgroups.computeIfAbsent(id, k -> new ConfigGroup(id, this, null, displayOrder));
		} else {
			return getOrCreateSubgroup(id.substring(0, index), displayOrder).getOrCreateSubgroup(id.substring(index + 1), displayOrder);
		}
	}

	/**
	 * Get, or create, a subgroup in this group.
	 *
	 * @param id unique id of the subgroup
	 * @return the subgroup, which may have just been created
	 */
	public ConfigGroup getOrCreateSubgroup(String id) {
		return getOrCreateSubgroup(id, 0);
	}

	/**
	 * Add a new config item to this group. In general, the various {@code addX()} convenience methods should be used.
	 *
	 * @param id a unique id for this config item
	 * @param type an instance of the config value being added
	 * @param value the initial value
	 * @param setter a consumer to be called to apply changes to the value
	 * @param defaultValue the default value
	 * @return the {@link ConfigValue} just added
	 * @param <T> the raw type
	 * @param <CV> the config value type
	 */
	public <T, CV extends ConfigValue<T>> CV add(String id, CV type, @Nullable T value, Consumer<T> setter, @Nullable T defaultValue) {
		values.put(id, type.init(this, id, value, setter, defaultValue));
		return type;
	}

	/**
	 * Add a new boolean config item to this group.
	 * @param id a unique id for this config item
	 * @param value the initial value
	 * @param setter a consumer to be called to apply changes to the value
	 * @param def the default value
	 * @return the {@link BooleanConfig} just added
	 */
	public BooleanConfig addBool(String id, boolean value, Consumer<Boolean> setter, boolean def) {
		return add(id, new BooleanConfig(), value, setter, def);
	}

	/**
	 * Add a new integer config item to this group.
	 *
	 * @param id a unique id for this config item
	 * @param value the initial value
	 * @param setter a consumer to be called to apply changes to the value
	 * @param def the default value
	 * @param min the minimum permitted value
	 * @param max the maximum permitted value
	 * @return the {@link IntConfig} just added
	 */
	public IntConfig addInt(String id, int value, Consumer<Integer> setter, int def, int min, int max) {
		return add(id, new IntConfig(min, max), value, setter, def);
	}

	public LongConfig addLong(String id, long value, Consumer<Long> setter, long def, long min, long max) {
		return add(id, new LongConfig(min, max), value, setter, def);
	}

	public DoubleConfig addDouble(String id, double value, Consumer<Double> setter, double def, double min, double max) {
		return add(id, new DoubleConfig(min, max), value, setter, def);
	}

	public StringConfig addString(String id, String value, Consumer<String> setter, String def, @Nullable Pattern pattern) {
		return add(id, new StringConfig(pattern), value, setter, def);
	}

	public StringConfig addString(String id, String value, Consumer<String> setter, String def) {
		return addString(id, value, setter, def, null);
	}

	public <E> EnumConfig<E> addEnum(String id, E value, Consumer<E> setter, NameMap<E> nameMap, E def) {
		return add(id, new EnumConfig<>(nameMap), value, setter, def);
	}

	public <E> EnumConfig<E> addEnum(String id, E value, Consumer<E> setter, NameMap<E> nameMap) {
		return addEnum(id, value, setter, nameMap, nameMap.defaultValue);
	}

	public <E, CV extends ConfigValue<E>> ListConfig<E, CV> addList(String id, List<E> c, CV type, E def) {
		type.setDefaultValue(def);
		return add(id, new ListConfig<>(type), c, t -> {
			c.clear();
			c.addAll(t);
		}, Collections.emptyList());
	}

	public EnumConfig<Tristate> addTristate(String id, Tristate value, Consumer<Tristate> setter, Tristate def) {
		return addEnum(id, value, setter, Tristate.NAME_MAP, def);
	}

	public EnumConfig<Tristate> addTristate(String id, Tristate value, Consumer<Tristate> setter) {
		return addTristate(id, value, setter, Tristate.DEFAULT);
	}

	public ItemStackConfig addItemStack(String id, ItemStack value, Consumer<ItemStack> setter, ItemStack def, boolean singleItem, boolean allowEmpty) {
		return add(id, new ItemStackConfig(singleItem, allowEmpty), value, setter, def);
	}

	public ItemStackConfig addItemStack(String id, ItemStack value, Consumer<ItemStack> setter, ItemStack def, int fixedSize) {
		return add(id, new ItemStackConfig(fixedSize), value, setter, def);
	}

	public FluidConfig addFluidStack(String id, FluidStack value, Consumer<FluidStack> setter, FluidStack def, boolean allowEmpty) {
		return add(id, new FluidConfig(allowEmpty), value, setter, def);
	}

	public FluidConfig addFluidStack(String id, FluidStack value, Consumer<FluidStack> setter, FluidStack def, long fixedSize) {
		return add(id, new FluidConfig(fixedSize), value, setter, def);
	}

	public ImageResourceConfig addImage(String id, ResourceLocation value, Consumer<ResourceLocation> setter, ResourceLocation def) {
		return add(id, new ImageResourceConfig(), value, setter, def);
	}

	public ColorConfig addColor(String id, Color4I value, Consumer<Color4I> setter, Color4I def) {
		return add(id, new ColorConfig(), value, setter, def);
	}

	public final Collection<ConfigValue<?>> getValues() {
		return values.values();
	}

	public final Collection<ConfigGroup> getSubgroups() {
		return subgroups.values();
	}

	public String getPath() {
		return parent == null ? id : parent.getPath() + '.' + id;
	}

	public void save(boolean accepted) {
		if (accepted) {
			values.values().forEach(ConfigValue::applyValue);
		}

		for (var group : subgroups.values()) {
			group.save(accepted);
		}

		if (savedCallback != null) {
			savedCallback.save(accepted);
		}
	}

	@Override
	public int compareTo(@NotNull ConfigGroup o) {
		int i = Integer.compare(displayOrder, o.displayOrder);
		return i == 0 ? getPath().compareToIgnoreCase(o.getPath()) : i;
	}
}
