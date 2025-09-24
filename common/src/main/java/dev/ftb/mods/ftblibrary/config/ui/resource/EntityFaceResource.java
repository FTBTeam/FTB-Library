package dev.ftb.mods.ftblibrary.config.ui.resource;

import dev.ftb.mods.ftblibrary.config.EntityFaceConfig;
import dev.ftb.mods.ftblibrary.icon.EntityIconLoader;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class EntityFaceResource implements SelectableResource<EntityType<?>>, Comparable<EntityFaceResource> {
    public static final EntityFaceResource NONE = new EntityFaceResource(EntityFaceConfig.NONE);

    private final EntityType<?> type;
    private final ResourceLocation location;
    private final Component name;
    private final Icon icon;

    public EntityFaceResource(EntityType<?> type) {
        this.type = type;

        location = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        name = type == EntityFaceConfig.NONE ? Component.translatable("gui.none").withStyle(ChatFormatting.GRAY) :
                Component.literal(location.getNamespace()).withStyle(ChatFormatting.GOLD).append(":")
                        .append(Component.literal(location.getPath()).withStyle(ChatFormatting.YELLOW));
        icon = type == EntityFaceConfig.NONE ? Icon.empty() : EntityIconLoader.getIcon(type);
    }

    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public EntityType<?> resource() {
        return type;
    }

    @Override
    public long getCount() {
        return 1;
    }

    @Override
    public void setCount(int count) {
    }

    @Override
    public boolean isEmpty() {
        return type == EntityFaceConfig.NONE;
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public SelectableResource<EntityType<?>> copyWithCount(long count) {
        return this;
    }

    @Override
    public int compareTo(@NotNull EntityFaceResource o) {
        return location.compareTo(o.location);
    }
}
