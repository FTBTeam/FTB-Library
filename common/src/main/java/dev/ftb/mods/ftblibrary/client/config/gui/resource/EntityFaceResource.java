package dev.ftb.mods.ftblibrary.client.config.gui.resource;

import dev.ftb.mods.ftblibrary.client.config.editable.EditableEntityFace;
import dev.ftb.mods.ftblibrary.icon.EntityIconLoader;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public class EntityFaceResource implements SelectableResource<EntityType<?>>, Comparable<EntityFaceResource> {
    public static final EntityFaceResource NONE = new EntityFaceResource(EditableEntityFace.NONE) {
        @Override
        public long getCount() {
            return 0;
        }
    };

    private final EntityType<?> type;
    private final Identifier location;
    private final Component name;
    private final Icon<?> icon;

    public EntityFaceResource(EntityType<?> type) {
        this.type = type;

        location = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        name = type == EditableEntityFace.NONE ? Component.translatable("gui.none").withStyle(ChatFormatting.GRAY) :
                Component.literal(location.getNamespace()).withStyle(ChatFormatting.GOLD).append(":")
                        .append(Component.literal(location.getPath()).withStyle(ChatFormatting.YELLOW));
        icon = type == EditableEntityFace.NONE ? Icon.empty() : EntityIconLoader.getIcon(type);
    }

    public Identifier getLocation() {
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
    public Component getName() {
        return name;
    }

    @Override
    public Icon<?> getIcon() {
        return icon;
    }

    @Override
    public SelectableResource<EntityType<?>> copyWithCount(long count) {
        return this;
    }

    @Override
    public int compareTo(EntityFaceResource o) {
        return location.compareTo(o.location);
    }
}
