package dev.ftb.mods.ftblibrary.icon;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftblibrary.client.icon.FaceIconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

public class FaceIcon extends Icon<FaceIcon> {
    public static final Logger LOGGER = LoggerFactory.getLogger(FaceIcon.class);

    private static final HashMap<UUID, FaceIcon> CACHE = new HashMap<>();

    public final GameProfile profile;
    private FaceData faceData = FaceData.EMPTY;

    private FaceIcon(GameProfile profile, boolean isClient) {
        this.profile = profile;

        if (isClient) {
            faceData = FaceIconRenderer.getDefaultFaceData(this);
            FaceIconRenderer.requestFaceFromSkinManager(this);
        }
    }

    public void setSkin(ImageIcon skin) {
        faceData = new FaceData(skin,
                skin.withUV(8F, 8F, 8F, 8F, 64F, 64F),
                skin.withUV(40F, 8F, 8F, 8F, 64F, 64F)
        );
    }

    public Icon<?> getHead() {
        return faceData.head;
    }

    public Icon<?> getHat() {
        return faceData.hat;
    }

    public static FaceIcon getFace(GameProfile profile, boolean isClient) {
        var icon = CACHE.get(profile.id());

        if (icon == null) {
            icon = new FaceIcon(profile, isClient);
            CACHE.put(profile.id(), icon);
        }

        return icon;
    }

    @Override
    public IconRenderer<FaceIcon> getRenderer() {
        return FaceIconRenderer.INSTANCE;
    }

    public record FaceData(Icon<?> face, Icon<?> head, Icon<?> hat) {
        public static final FaceData EMPTY = new FaceData(Icon.empty(), Icon.empty(), Icon.empty());
    }
}
