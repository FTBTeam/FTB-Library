package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;


public class Color4I extends Icon {
    static final Color4I EMPTY_ICON = new Color4I(255, 255, 255, 255) {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void draw3D(GuiGraphics graphics) {
        }

        @Override
        public MutableColor4I mutable() {
            return new MutableColor4I.None();
        }

        @Override
        @Nullable
        public PixelBuffer createPixelBuffer() {
            return null;
        }

        public int hashCode() {
            return 0;
        }

        public boolean equals(Object o) {
            return o == this;
        }
    };

    private static final Color4I[] BLACK_A = Util.make(new Color4I[256], array -> {
        for (var i = 0; i < 256; i++) {
            array[i] = new Color4I(0, 0, 0, i) {
                @Override
                public Color4I withAlpha(int a) {
                    return alpha == a ? this : BLACK_A[a & 255];
                }
            };
        }
    });

    private static final Color4I[] WHITE_A = Util.make(new Color4I[256], array -> {
        for (var i = 0; i < 256; i++) {
            array[i] = new Color4I(255, 255, 255, i) {
                @Override
                public Color4I withAlpha(int a) {
                    return alpha == a ? this : WHITE_A[a & 255];
                }
            };
        }
    });

    private static final Color4I[] CHAT_FORMATTING_COLORS = Util.make(new Color4I[256], array -> {
        for (var i = 0; i < 16; i++) {
            var j = (i >> 3 & 1) * 85;
            var r = (i >> 2 & 1) * 170 + j;
            var g = (i >> 1 & 1) * 170 + j;
            var b = (i & 1) * 170 + j;
            array[i] = rgb((i == 6) ? r + 85 : r, g, b);
        }
    });

    private static final Color4I[] COLORS_256 = Util.make(new Color4I[256], array -> {
        var colors256 = new int[]{
                0x000000, 0x252525, 0x343434, 0x4E4E4E, 0x686868, 0x757575, 0x8E8E8E, 0xA4A4A4,
                0xB8B8B8, 0xC5C5C5, 0xD0D0D0, 0xD7D7D7, 0xE1E1E1, 0xEAEAEA, 0xF4F4F4, 0xFFFFFF,
                0x412000, 0x542800, 0x763700, 0x9A5000, 0xC36806, 0xE47B07, 0xFF911A, 0xFFAB1D,
                0xFFC51F, 0xFFD03B, 0xFFD84C, 0xFFE651, 0xFFF456, 0xFFF970, 0xFFFF90, 0xFFFFAA,
                0x451904, 0x721E11, 0x9F241E, 0xB33A20, 0xC85120, 0xE36920, 0xFC8120, 0xFD8C25,
                0xFE982C, 0xFFAE38, 0xFFB946, 0xFFBF51, 0xFFC66D, 0xFFD587, 0xFFE498, 0xFFE6AB,
                0x5D1F0C, 0x7A240D, 0x982C0E, 0xB02F0F, 0xBF3624, 0xD34E2A, 0xE7623E, 0xF36E4A,
                0xFD7854, 0xFF8A6A, 0xFF987C, 0xFFA48B, 0xFFB39E, 0xFFC2B2, 0xFFD0C3, 0xFFDAD0,
                0x4A1700, 0x721F00, 0xA81300, 0xC8210A, 0xDF2512, 0xEC3B24, 0xFA5236, 0xFC6148,
                0xFF705F, 0xFF7E7E, 0xFF8F8F, 0xFF9D9E, 0xFFABAD, 0xFFB9BD, 0xFFC7CE, 0xFFCADE,
                0x490036, 0x66004B, 0x80035F, 0x950F74, 0xAA2288, 0xBA3D99, 0xCA4DA9, 0xD75AB6,
                0xE467C3, 0xEF72CE, 0xFB7EDA, 0xFF8DE1, 0xFF9DE5, 0xFFA5E7, 0xFFAFEA, 0xFFB8EC,
                0x48036C, 0x5C0488, 0x650D90, 0x7B23A7, 0x933BBF, 0x9D45C9, 0xA74FD3, 0xB25ADE,
                0xBD65E9, 0xC56DF1, 0xCE76FA, 0xD583FF, 0xDA90FF, 0xDE9CFF, 0xE2A9FF, 0xE6B6FF,
                0x051E81, 0x0626A5, 0x082FCA, 0x263DD4, 0x444CDE, 0x4F5AEC, 0x5A68FF, 0x6575FF,
                0x7183FF, 0x8091FF, 0x90A0FF, 0x97A9FF, 0x9FB2FF, 0xAFBEFF, 0xC0CBFF, 0xCDD3FF,
                0x0B0779, 0x201C8E, 0x3531A3, 0x4642B4, 0x5753C5, 0x615DCF, 0x6D69DB, 0x7B77E9,
                0x8985F7, 0x918DFF, 0x9C98FF, 0xA7A4FF, 0xB2AFFF, 0xBBB8FF, 0xC3C1FF, 0xD3D1FF,
                0x1D295A, 0x1D3876, 0x1D4892, 0x1D5CAC, 0x1D71C6, 0x3286CF, 0x489BD9, 0x4EA8EC,
                0x55B6FF, 0x69CAFF, 0x74CBFF, 0x82D3FF, 0x8DDAFF, 0x9FD4FF, 0xB4E2FF, 0xC0EBFF,
                0x004B59, 0x005D6E, 0x006F84, 0x00849C, 0x0099BF, 0x00ABCA, 0x00BCDE, 0x00D0F5,
                0x10DCFF, 0x3EE1FF, 0x64E7FF, 0x76EAFF, 0x8BEDFF, 0x9AEFFF, 0xB1F3FF, 0xC7F6FF,
                0x004800, 0x005400, 0x036B03, 0x0E760E, 0x188018, 0x279227, 0x36A436, 0x4EB94E,
                0x51CD51, 0x72DA72, 0x7CE47C, 0x85ED85, 0x99F299, 0xB3F7B3, 0xC3F9C3, 0xCDFCCD,
                0x164000, 0x1C5300, 0x236600, 0x287800, 0x2E8C00, 0x3A980C, 0x47A519, 0x51AF23,
                0x5CBA2E, 0x71CF43, 0x85E357, 0x8DEB5F, 0x97F569, 0xA0FE72, 0xB1FF8A, 0xBCFF9A,
                0x2C3500, 0x384400, 0x445200, 0x495600, 0x607100, 0x6C7F00, 0x798D0A, 0x8B9F1C,
                0x9EB22F, 0xABBF3C, 0xB8CC49, 0xC2D653, 0xCDE153, 0xDBEF6C, 0xE8FC79, 0xF2FFAB,
                0x463A09, 0x4D3F09, 0x544509, 0x6C5809, 0x907609, 0xAB8B0A, 0xC1A120, 0xD0B02F,
                0xDEBE3D, 0xE6C645, 0xEDCD4C, 0xF5D862, 0xFBE276, 0xFCEE98, 0xFDF3A9, 0xFDF3BE,
                0x401A02, 0x581F05, 0x702408, 0x8D3A13, 0xAB511F, 0xB56427, 0xBF7730, 0xD0853A,
                0xE19344, 0xEDA04E, 0xF9AD58, 0xFCB75C, 0xFFC160, 0xFFCA69, 0xFFCF7E, 0xFFDA96,
        };

        for (var i = 0; i < 256; i++) {
            array[i] = rgb(colors256[i]);
        }
    });

    public static final Color4I BLACK = rgb(0x000000);
    public static final Color4I DARK_GRAY = rgb(0x212121);
    public static final Color4I GRAY = rgb(0x999999);
    public static final Color4I WHITE = rgb(0xFFFFFF);
    public static final Color4I RED = rgb(0xFF0000);
    public static final Color4I GREEN = rgb(0x00FF00);
    public static final Color4I BLUE = rgb(0x0000FF);
    public static final Color4I LIGHT_RED = rgb(0xFF5656);
    public static final Color4I LIGHT_GREEN = rgb(0x56FF56);
    public static final Color4I LIGHT_BLUE = rgb(0x5656FF);

//    private static final Color4I[] CHAT_FORMATTING_COLORS = new Color4I[16];
//    private static final Color4I[] COLORS_256 = new Color4I[256];

//    static {
//        for (var i = 0; i < 256; i++) {
//            BLACK_A[i] = new Color4I(0, 0, 0, i) {
//                @Override
//                public Color4I withAlpha(int a) {
//                    return alpha == a ? this : BLACK_A[a & 255];
//                }
//            };
//
//            WHITE_A[i] = new Color4I(255, 255, 255, i) {
//                @Override
//                public Color4I withAlpha(int a) {
//                    return alpha == a ? this : WHITE_A[a & 255];
//                }
//            };
//        }
//    }

//    static {
//        for (var i = 0; i < 16; i++) {
//            var j = (i >> 3 & 1) * 85;
//            var r = (i >> 2 & 1) * 170 + j;
//            var g = (i >> 1 & 1) * 170 + j;
//            var b = (i & 1) * 170 + j;
//            CHAT_FORMATTING_COLORS[i] = rgb((i == 6) ? r + 85 : r, g, b);
//        }
//
//        var colors256 = new int[]{
//                0x000000, 0x252525, 0x343434, 0x4E4E4E, 0x686868, 0x757575, 0x8E8E8E, 0xA4A4A4, 0xB8B8B8, 0xC5C5C5, 0xD0D0D0, 0xD7D7D7, 0xE1E1E1, 0xEAEAEA, 0xF4F4F4, 0xFFFFFF,
//                0x412000, 0x542800, 0x763700, 0x9A5000, 0xC36806, 0xE47B07, 0xFF911A, 0xFFAB1D, 0xFFC51F, 0xFFD03B, 0xFFD84C, 0xFFE651, 0xFFF456, 0xFFF970, 0xFFFF90, 0xFFFFAA,
//                0x451904, 0x721E11, 0x9F241E, 0xB33A20, 0xC85120, 0xE36920, 0xFC8120, 0xFD8C25, 0xFE982C, 0xFFAE38, 0xFFB946, 0xFFBF51, 0xFFC66D, 0xFFD587, 0xFFE498, 0xFFE6AB,
//                0x5D1F0C, 0x7A240D, 0x982C0E, 0xB02F0F, 0xBF3624, 0xD34E2A, 0xE7623E, 0xF36E4A, 0xFD7854, 0xFF8A6A, 0xFF987C, 0xFFA48B, 0xFFB39E, 0xFFC2B2, 0xFFD0C3, 0xFFDAD0,
//                0x4A1700, 0x721F00, 0xA81300, 0xC8210A, 0xDF2512, 0xEC3B24, 0xFA5236, 0xFC6148, 0xFF705F, 0xFF7E7E, 0xFF8F8F, 0xFF9D9E, 0xFFABAD, 0xFFB9BD, 0xFFC7CE, 0xFFCADE,
//                0x490036, 0x66004B, 0x80035F, 0x950F74, 0xAA2288, 0xBA3D99, 0xCA4DA9, 0xD75AB6, 0xE467C3, 0xEF72CE, 0xFB7EDA, 0xFF8DE1, 0xFF9DE5, 0xFFA5E7, 0xFFAFEA, 0xFFB8EC,
//                0x48036C, 0x5C0488, 0x650D90, 0x7B23A7, 0x933BBF, 0x9D45C9, 0xA74FD3, 0xB25ADE, 0xBD65E9, 0xC56DF1, 0xCE76FA, 0xD583FF, 0xDA90FF, 0xDE9CFF, 0xE2A9FF, 0xE6B6FF,
//                0x051E81, 0x0626A5, 0x082FCA, 0x263DD4, 0x444CDE, 0x4F5AEC, 0x5A68FF, 0x6575FF, 0x7183FF, 0x8091FF, 0x90A0FF, 0x97A9FF, 0x9FB2FF, 0xAFBEFF, 0xC0CBFF, 0xCDD3FF,
//                0x0B0779, 0x201C8E, 0x3531A3, 0x4642B4, 0x5753C5, 0x615DCF, 0x6D69DB, 0x7B77E9, 0x8985F7, 0x918DFF, 0x9C98FF, 0xA7A4FF, 0xB2AFFF, 0xBBB8FF, 0xC3C1FF, 0xD3D1FF,
//                0x1D295A, 0x1D3876, 0x1D4892, 0x1D5CAC, 0x1D71C6, 0x3286CF, 0x489BD9, 0x4EA8EC, 0x55B6FF, 0x69CAFF, 0x74CBFF, 0x82D3FF, 0x8DDAFF, 0x9FD4FF, 0xB4E2FF, 0xC0EBFF,
//                0x004B59, 0x005D6E, 0x006F84, 0x00849C, 0x0099BF, 0x00ABCA, 0x00BCDE, 0x00D0F5, 0x10DCFF, 0x3EE1FF, 0x64E7FF, 0x76EAFF, 0x8BEDFF, 0x9AEFFF, 0xB1F3FF, 0xC7F6FF,
//                0x004800, 0x005400, 0x036B03, 0x0E760E, 0x188018, 0x279227, 0x36A436, 0x4EB94E, 0x51CD51, 0x72DA72, 0x7CE47C, 0x85ED85, 0x99F299, 0xB3F7B3, 0xC3F9C3, 0xCDFCCD,
//                0x164000, 0x1C5300, 0x236600, 0x287800, 0x2E8C00, 0x3A980C, 0x47A519, 0x51AF23, 0x5CBA2E, 0x71CF43, 0x85E357, 0x8DEB5F, 0x97F569, 0xA0FE72, 0xB1FF8A, 0xBCFF9A,
//                0x2C3500, 0x384400, 0x445200, 0x495600, 0x607100, 0x6C7F00, 0x798D0A, 0x8B9F1C, 0x9EB22F, 0xABBF3C, 0xB8CC49, 0xC2D653, 0xCDE153, 0xDBEF6C, 0xE8FC79, 0xF2FFAB,
//                0x463A09, 0x4D3F09, 0x544509, 0x6C5809, 0x907609, 0xAB8B0A, 0xC1A120, 0xD0B02F, 0xDEBE3D, 0xE6C645, 0xEDCD4C, 0xF5D862, 0xFBE276, 0xFCEE98, 0xFDF3A9, 0xFDF3BE,
//                0x401A02, 0x581F05, 0x702408, 0x8D3A13, 0xAB511F, 0xB56427, 0xBF7730, 0xD0853A, 0xE19344, 0xEDA04E, 0xF9AD58, 0xFCB75C, 0xFFC160, 0xFFCA69, 0xFFCF7E, 0xFFDA96,
//        };
//
//        for (var i = 0; i < 256; i++) {
//            COLORS_256[i] = rgb(colors256[i]);
//        }
//    }

    int red, green, blue, alpha;

    Color4I(int r, int g, int b, int a) {
        red = r;
        green = g;
        blue = b;
        alpha = a;
    }

    public static Color4I getChatFormattingColor(int id) {
        return CHAT_FORMATTING_COLORS[id & 0xF];
    }

    public static Color4I getChatFormattingColor(@Nullable ChatFormatting formatting) {
        return formatting == null ? WHITE : getChatFormattingColor(formatting.ordinal());
    }

    public static Color4I get256(int id) {
        return COLORS_256[id & 255];
    }

    public static Color4I fromString(@Nullable String s) {
        if (s == null || s.isEmpty()) {
            return empty();
        } else if ((s.length() == 7 || s.length() == 9) && s.charAt(0) == '#') {
            var hex = s.substring(1);
            return hex.length() == 8 ? rgba((int) Long.parseLong(hex, 16)) : rgb((int) Long.parseLong(hex, 16));
        } else if (s.equalsIgnoreCase("transparent")) {
            return WHITE.withAlpha(0);
        } else if (s.equalsIgnoreCase("black")) {
            return BLACK;
        } else if (s.equalsIgnoreCase("dark_gray")) {
            return DARK_GRAY;
        } else if (s.equalsIgnoreCase("gray")) {
            return GRAY;
        } else if (s.equalsIgnoreCase("white")) {
            return WHITE;
        } else if (s.equalsIgnoreCase("red")) {
            return RED;
        } else if (s.equalsIgnoreCase("green")) {
            return GREEN;
        } else if (s.equalsIgnoreCase("blue")) {
            return BLUE;
        } else if (s.equalsIgnoreCase("light_red")) {
            return LIGHT_RED;
        } else if (s.equalsIgnoreCase("light_green")) {
            return LIGHT_GREEN;
        } else if (s.equalsIgnoreCase("light_blue")) {
            return LIGHT_BLUE;
        }

        return empty();
    }

    public static Color4I fromJson(@Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return empty();
        } else if (element.isJsonPrimitive()) {
            return fromString(element.getAsString());
        } else if (element.isJsonArray()) {
            var array = element.getAsJsonArray();

            if (array.size() >= 3) {
                var r = array.get(0).getAsInt();
                var g = array.get(1).getAsInt();
                var b = array.get(2).getAsInt();
                var a = 255;

                if (array.size() >= 3) {
                    a = array.get(3).getAsInt();
                }

                return rgba(r, g, b, a);
            }
        }

        var object = element.getAsJsonObject();

        if (object.has("red") && object.has("green") && object.has("blue")) {
            var r = object.get("red").getAsInt();
            var g = object.get("green").getAsInt();
            var b = object.get("blue").getAsInt();
            var a = 255;

            if (object.has("alpha")) {
                a = object.get("alpha").getAsInt();
            }

            return rgba(r, g, b, a);
        }

        return empty();
    }

    public static Color4I rgba(int r, int g, int b, int a) {
        r = r & 255;
        g = g & 255;
        b = b & 255;
        a = a & 255;

        if (a == 0) {
            return empty();
        } else if (r == 0 && g == 0 && b == 0) {
            return BLACK_A[a];
        } else if (r == 255 && g == 255 && b == 255) {
            return WHITE_A[a];
        }

        return new Color4I(r, g, b, a);
    }

    public static Color4I rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    public static Color4I hsb(float h, float s, float b) {
        return rgb(HSBtoRGB(h, s, b));
    }

    public static Color4I rgba(int col) {
        return rgba(col >> 16, col >> 8, col, col >> 24);
    }

    public static Color4I rgb(int col) {
        return rgb(col >> 16, col >> 8, col);
    }

    public static Color4I rgb(Vec3 color) {
        return rgb((int) (color.x * 255D), (int) (color.y * 255D), (int) (color.z * 255D));
    }

    public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        float hue, saturation, brightness;
        if (hsbvals == null) {
            hsbvals = new float[3];
        }
        int cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        int cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;

        brightness = ((float) cmax) / 255.0f;
        if (cmax != 0)
            saturation = ((float) (cmax - cmin)) / ((float) cmax);
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                    break;
            }
        }
        return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
    }

    @Override
    public Color4I copy() {
        return this;
    }

    public boolean isMutable() {
        return false;
    }

    public MutableColor4I mutable() {
        return new MutableColor4I(redi(), greeni(), bluei(), alphai());
    }

    public Color4I whiteIfEmpty() {
        return isEmpty() ? WHITE : this;
    }

    public int redi() {
        return red;
    }

    public int greeni() {
        return green;
    }

    public int bluei() {
        return blue;
    }

    public int alphai() {
        return alpha;
    }

    public float redf() {
        return redi() / 255F;
    }

    public float greenf() {
        return greeni() / 255F;
    }

    public float bluef() {
        return bluei() / 255F;
    }

    public float alphaf() {
        return alphai() / 255F;
    }

    public int rgba() {
        return (alphai() << 24) | (redi() << 16) | (greeni() << 8) | bluei();
    }

    public int rgb() {
        return (redi() << 16) | (greeni() << 8) | bluei();
    }

    public int hashCode() {
        return rgba();
    }

    public boolean equals(Object o) {
        return o == this || (o instanceof Color4I && o.hashCode() == rgba());
    }

    public String toString() {
        var a = alphai();
        char[] chars;

        if (a < 255) {
            chars = new char[9];
            chars[1] = StringUtils.HEX[(a & 0xF0) >> 4];
            chars[2] = StringUtils.HEX[a & 0xF];
            var r = redi();
            chars[3] = StringUtils.HEX[(r & 0xF0) >> 4];
            chars[4] = StringUtils.HEX[r & 0xF];
            var g = greeni();
            chars[5] = StringUtils.HEX[(g & 0xF0) >> 4];
            chars[6] = StringUtils.HEX[g & 0xF];
            var b = bluei();
            chars[7] = StringUtils.HEX[(b & 0xF0) >> 4];
            chars[8] = StringUtils.HEX[b & 0xF];
        } else {
            chars = new char[7];
            var r = redi();
            chars[1] = StringUtils.HEX[(r & 0xF0) >> 4];
            chars[2] = StringUtils.HEX[r & 0xF];
            var g = greeni();
            chars[3] = StringUtils.HEX[(g & 0xF0) >> 4];
            chars[4] = StringUtils.HEX[g & 0xF];
            var b = bluei();
            chars[5] = StringUtils.HEX[(b & 0xF0) >> 4];
            chars[6] = StringUtils.HEX[b & 0xF];
        }

        chars[0] = '#';
        return new String(chars);
    }

    @Override
    public JsonElement getJson() {
        return isEmpty() ? JsonNull.INSTANCE : new JsonPrimitive(toString());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);

        var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        GuiHelper.addRectToBuffer(graphics, buffer, x, y, w, h, this);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    @Override
    public Icon withColor(Color4I color) {
        return color;
    }

    @Override
    public Color4I withTint(Color4I col) {
        if (isEmpty()) {
            return this;
        } else if (col.isEmpty()) {
            return empty();
        } else if (col.redi() == 255 && col.greeni() == 255 && col.bluei() == 255) {
            return this;
        }

        double a = col.alphaf();
        var r = MathUtils.lerp(redi(), col.redi(), a);
        var g = MathUtils.lerp(greeni(), col.greeni(), a);
        var b = MathUtils.lerp(bluei(), col.bluei(), a);
        return rgba((int) r, (int) g, (int) b, alpha);
    }

    public Color4I withAlpha(int a) {
        return alpha == a ? this : rgba(redi(), greeni(), bluei(), a);
    }

    public final Color4I withAlphaf(float alpha) {
        return withAlpha((int) (alpha * 255F));
    }

    public Color4I lerp(Color4I col, float m) {
        m = Mth.clamp(m, 0F, 1F);
        var r = MathUtils.lerp(redf(), col.redf(), m);
        var g = MathUtils.lerp(greenf(), col.greenf(), m);
        var b = MathUtils.lerp(bluef(), col.bluef(), m);
        var a = MathUtils.lerp(alphaf(), col.alphaf(), m);
        return rgba((int) (r * 255F), (int) (g * 255F), (int) (b * 255F), (int) (a * 255F));
    }

    public Color4I addBrightness(float percent) {
        var hsb = new float[3];
        RGBtoHSB(redi(), greeni(), bluei(), hsb);
        return rgb(HSBtoRGB(hsb[0], hsb[1], Mth.clamp(hsb[2] + percent, 0F, 1F))).withAlpha(alphai());
    }

    @Override
    public boolean hasPixelBuffer() {
        return true;
    }

    @Override
    @Nullable
    public PixelBuffer createPixelBuffer() {
        var buffer = new PixelBuffer(1, 1);
        buffer.setRGB(0, 0, rgba());
        return buffer;
    }

    public Style toStyle() {
        return Style.EMPTY.withColor(TextColor.fromRgb(rgb()));
    }
}
