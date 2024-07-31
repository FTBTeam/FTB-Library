package dev.ftb.mods.ftblibrary.sidebar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.misc.LoadingScreen;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


public record SidebarButton(
		ResourceLocation group,
		Icon icon,
		int x,
		boolean defaultEnabled,
		List<String> clickEvents,
		List<String> shiftClickEvent,
		boolean loadingScreen,
		List<String> tooltips,
		boolean requiresOp) implements Comparable<SidebarButton> {

	public static final Codec<SidebarButton> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        ResourceLocation.CODEC.fieldOf("group").forGetter(SidebarButton::group),
        Icon.CODEC.fieldOf("icon").forGetter(SidebarButton::icon),
        Codec.BOOL.fieldOf("requires_op").orElse(false).forGetter(SidebarButton::requiresOp),
        Codec.STRING.listOf(1, Integer.MAX_VALUE).fieldOf("click").orElse(List.of()).forGetter(SidebarButton::clickEvents),
        Codec.STRING.listOf().fieldOf("shift_click").orElse(List.of()).forGetter(SidebarButton::shiftClickEvent),
        Codec.BOOL.fieldOf("loading_screen").orElse(false).forGetter(SidebarButton::loadingScreen),
        Codec.BOOL.fieldOf("default_enabled").orElse(true).forGetter(SidebarButton::defaultEnabled),
        Codec.STRING.listOf().fieldOf("text").orElse(List.of()).forGetter(SidebarButton::tooltips)
    ).apply(builder, SidebarButton::of));
//
//
	public static SidebarButton of(ResourceLocation group,
								   Icon icon,
								   boolean requiresOp,
								   List<String> clickEvents,
								   List<String> shiftClickEvents,
								   boolean loadingScreen,
								   boolean defaultEnabled,
								   List<String> tooltips) {
		return new SidebarButton( group,  icon, 0, defaultEnabled, clickEvents, shiftClickEvents, loadingScreen, tooltips, requiresOp);
	}


//	public SidebarButton(ResourceLocation id, SidebarButtonGroup group, JsonObject json) {
//		this.group = group;
//		this.id = id;
//		this.json = json;
//
//		if (json.has("icon")) {
//			icon = Icon.getIcon(json.get("icon"));
//		}
//
//		if (icon.isEmpty()) {
//			icon = Icons.ACCEPT_GRAY;
//		}
//
//		if (json.has("click")) {
//			var j = json.get("click");
//			for (var e : j.isJsonArray() ? j.getAsJsonArray() : Collections.singleton(j)) {
//				if (e.isJsonPrimitive()) {
//					clickEvents.add(e.getAsString());
//				}
//			}
//		}
//		if (json.has("shift_click")) {
//			var j = json.get("shift_click");
//			for (var e : j.isJsonArray() ? j.getAsJsonArray() : Collections.singleton(j)) {
//				if (e.isJsonPrimitive()) {
//					shiftClickEvents.add(e.getAsString());
//				}
//			}
//		}
//		if (json.has("config")) {
//			defaultEnabled = configValue = json.get("config").getAsBoolean();
//		}
//
//		if (json.has("x")) {
//			x = json.get("x").getAsInt();
//		}
//
//		if (json.has("requires_op") && json.get("requires_op").getAsBoolean()) {
//			addVisibilityCondition(ClientUtils.IS_CLIENT_OP);
//		}
//
//		if (json.has("required_mods")) {
//			var requiredServerMods = new LinkedHashSet<String>();
//
//			for (var e : json.get("required_mods").getAsJsonArray()) {
//				requiredServerMods.add(e.getAsString());
//			}
//
//			addVisibilityCondition(() -> {
//				for (var s : requiredServerMods) {
//					if (!Platform.isModLoaded(s)) {
//						return false;
//					}
//				}
//
//				return true;
//			});
//		}
//
//		loadingScreen = json.has("loading_screen") && json.get("loading_screen").getAsBoolean();
//	}


//	public ResourceLocation getId() {
//		return id;
//	}
//
//	public SidebarButtonGroup getGroup() {
//		return group;
//	}
//
//	public JsonObject getJson() {
//		return json;
//	}
//
//	public void addVisibilityCondition(BooleanSupplier supplier) {
//		visible = visible.and(supplier);
//	}

	public String getLangKey() {
		//Todo -unreal
		return Util.makeDescriptionId("sidebar_button", ResourceLocation.fromNamespaceAndPath("ftbquests", "sidebar_button"));
	}

	public String getTooltipLangKey() {
		return getLangKey() + ".tooltip";
	}

	public void onClicked(boolean shift) {
		if (loadingScreen) {
			new LoadingScreen(Component.translatable(getLangKey())).openGui();
		}

		for (var event : (shift && !shiftClickEvent.isEmpty() ? shiftClickEvent : clickEvents)) {
			GuiHelper.BLANK_GUI.handleClick(event);
		}
	}

	public boolean isVisible() {
		return true;
	}


	@Nullable
	public Supplier<String> getCustomTextHandler() {
		return () -> "";
	}


	@Nullable
	public Consumer<List<String>> getTooltipHandler() {
		return s -> {};
	}



	@Override
	public int compareTo(SidebarButton button) {
		return x - button.x;
	}
}
