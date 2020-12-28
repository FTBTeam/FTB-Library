package com.feed_the_beast.mods.ftbguilibrary.sidebar;

import com.feed_the_beast.mods.ftbguilibrary.FTBGUILibrary;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiLoading;
import com.feed_the_beast.mods.ftbguilibrary.utils.ChainedBooleanSupplier;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.shedaniel.architectury.platform.Platform;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class SidebarButton implements Comparable<SidebarButton>
{
	private static final BooleanSupplier NEI_NOT_LOADED = () -> !Platform.isModLoaded("notenoughitems");

	public final ResourceLocation id;
	public final SidebarButtonGroup group;
	private Icon icon = Icon.EMPTY;
	private int x = 0;
	private boolean defaultConfig = true;
	private boolean configValue = true;
	private final List<String> clickEvents = new ArrayList<>();
	private final List<String> shiftClickEvents = new ArrayList<>();
	private final boolean loadingScreen;
	private ChainedBooleanSupplier visible = ChainedBooleanSupplier.TRUE;
	private Supplier<String> customTextHandler = null;
	private Consumer<List<String>> tooltipHandler = null;

	public SidebarButton(ResourceLocation _id, SidebarButtonGroup g, JsonObject json)
	{
		group = g;
		id = _id;

		if (json.has("icon"))
		{
			icon = Icon.getIcon(json.get("icon"));
		}

		if (icon.isEmpty())
		{
			icon = GuiIcons.ACCEPT_GRAY;
		}

		if (json.has("click"))
		{
			JsonElement j = json.get("click");
			for (JsonElement e : j.isJsonArray() ? j.getAsJsonArray() : Collections.singleton(j))
			{
				if (e.isJsonPrimitive())
				{
					clickEvents.add(e.getAsString());
				}
			}
		}
		if (json.has("shift_click"))
		{
			JsonElement j = json.get("shift_click");
			for (JsonElement e : j.isJsonArray() ? j.getAsJsonArray() : Collections.singleton(j))
			{
				if (e.isJsonPrimitive())
				{
					shiftClickEvents.add(e.getAsString());
				}
			}
		}
		if (json.has("config"))
		{
			defaultConfig = configValue = json.get("config").getAsBoolean();
		}

		if (json.has("x"))
		{
			x = json.get("x").getAsInt();
		}

		if (json.has("requires_op") && json.get("requires_op").getAsBoolean())
		{
			addVisibilityCondition(ClientUtils.IS_CLIENT_OP);
		}

		if (json.has("hide_with_nei") && json.get("hide_with_nei").getAsBoolean())
		{
			addVisibilityCondition(NEI_NOT_LOADED);
		}

		if (json.has("required_mods"))
		{
			LinkedHashSet<String> requiredServerMods = new LinkedHashSet<>();

			for (JsonElement e : json.get("required_mods").getAsJsonArray())
			{
				requiredServerMods.add(e.getAsString());
			}

			addVisibilityCondition(() -> {
				for (String s : requiredServerMods)
				{
					if (!Platform.isModLoaded(s))
					{
						return false;
					}
				}

				return true;
			});
		}

		loadingScreen = json.has("loading_screen") && json.get("loading_screen").getAsBoolean();
	}

	public void addVisibilityCondition(BooleanSupplier supplier)
	{
		visible = visible.and(supplier);
	}

	public String getLangKey()
	{
		return "sidebar_button." + id.getNamespace() + '.' + id.getPath();
	}

	public String getTooltipLangKey()
	{
		return getLangKey() + ".tooltip";
	}

	@Override
	public String toString()
	{
		return id.toString();
	}

	@Override
	public final int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public final boolean equals(Object o)
	{
		return o == this || o instanceof SidebarButton && id.equals(((SidebarButton) o).id);
	}

	public Icon getIcon()
	{
		return icon;
	}

	public int getX()
	{
		return x;
	}

	public boolean getDefaultConfig()
	{
		return defaultConfig;
	}

	public void onClicked(boolean shift)
	{
		if (loadingScreen)
		{
			new GuiLoading(new TranslatableComponent(getLangKey())).openGui();
		}

		for (String event : (shift && !shiftClickEvents.isEmpty() ? shiftClickEvents : clickEvents))
		{
			GuiHelper.BLANK_GUI.handleClick(event);
		}
	}

	public boolean isActuallyVisible()
	{
		return configValue && FTBGUILibrary.showButtons != 0 && isVisible();
	}

	public boolean isVisible()
	{
		return visible.getAsBoolean();
	}

	public boolean getConfig()
	{
		return configValue;
	}

	public void setConfig(boolean value)
	{
		configValue = value;
	}

	@Nullable
	public Supplier<String> getCustomTextHandler()
	{
		return customTextHandler;
	}

	public void setCustomTextHandler(Supplier<String> text)
	{
		customTextHandler = text;
	}

	@Nullable
	public Consumer<List<String>> getTooltipHandler()
	{
		return tooltipHandler;
	}

	public void setTooltipHandler(Consumer<List<String>> text)
	{
		tooltipHandler = text;
	}

	@Override
	public int compareTo(SidebarButton button)
	{
		return getX() - button.getX();
	}
}