package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ui.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectableResource;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalLong;

public class ImageResourceConfig extends ResourceConfigValue<ResourceLocation> {
	public static final ResourceLocation NONE = new ResourceLocation(FTBLibrary.MOD_ID, "none");

	private boolean allowEmpty = true;

	@Override
	public void onClicked(Widget clicked, MouseButton button, ConfigCallback callback) {
		new SelectImageResourceScreen(this, callback).withGridSize(8, 12).openGui();
	}

	public void setAllowEmpty(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}

	@Override
	public boolean allowEmptyResource() {
		return allowEmpty;
	}

	@Override
	public boolean canHaveNBT() {
		return false;
	}

	@Override
	public OptionalLong fixedResourceSize() {
		return OptionalLong.of(1);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public SelectableResource<ResourceLocation> getResource() {
		return new SelectableResource.ImageResource(getValue());
	}

	@Override
	public boolean setResource(SelectableResource<ResourceLocation> selectedStack) {
		return setCurrentValue(selectedStack.stack());
	}

	@Override
	public void addInfo(TooltipList list) {
		if (value != null && !value.equals(defaultValue)) {
			list.add(Component.translatable("config.group.value").append(": ").withStyle(ChatFormatting.AQUA)
					.append(Component.literal(getValue().toString()).withStyle(ChatFormatting.WHITE)));
		}

		super.addInfo(list);

	}
}
