package com.feed_the_beast.mods.ftbguilibrary.icon;

import com.feed_the_beast.mods.ftbguilibrary.utils.IPixelBuffer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class Icon implements Drawable
{
	public static final Color4I EMPTY = new Color4I(255, 255, 255, 255)
	{
		@Override
		public boolean isEmpty()
		{
			return true;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void draw(int x, int y, int w, int h)
		{
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void draw3D(MatrixStack matrixStack)
		{
		}

		@Override
		public MutableColor4I mutable()
		{
			return new MutableColor4I.None();
		}

		@Override
		@Nullable
		public IPixelBuffer createPixelBuffer()
		{
			return null;
		}

		public int hashCode()
		{
			return 0;
		}

		public boolean equals(Object o)
		{
			return o == this;
		}
	};

	public static Icon getIcon(@Nullable JsonElement json)
	{
		if (json == null || json.isJsonNull())
		{
			return EMPTY;
		}
		else if (json.isJsonObject())
		{
			JsonObject o = json.getAsJsonObject();

			if (o.has("id"))
			{
				switch (o.get("id").getAsString())
				{
					case "color":
					{
						Color4I color = Color4I.fromJson(o.get("color"));
						return (o.has("mutable") && o.get("mutable").getAsBoolean()) ? color.mutable() : color;
					}
					case "padding":
						return getIcon(o.get("parent")).withPadding(o.has("padding") ? o.get("padding").getAsInt() : 0);
					case "tint":
						return getIcon(o.get("parent")).withTint(Color4I.fromJson(o.get("color")));
					case "animation":
					{
						List<Icon> icons = new ArrayList<>();

						for (JsonElement e : o.get("icons").getAsJsonArray())
						{
							icons.add(getIcon(e));
						}

						return IconAnimation.fromList(icons, true);
					}
					case "border":
					{
						Icon icon = EMPTY;
						Color4I outline = EMPTY;
						boolean roundEdges = false;

						if (o.has("icon"))
						{
							icon = getIcon(o.get("icon"));
						}

						if (o.has("color"))
						{
							outline = Color4I.fromJson(o.get("color"));
						}

						if (o.has("round_edges"))
						{
							roundEdges = o.get("round_edges").getAsBoolean();
						}

						return icon.withBorder(outline, roundEdges);
					}
					case "bullet":
					{
						return new BulletIcon().withColor(o.has("color") ? Color4I.fromJson(o.get("color")) : EMPTY);
					}
					case "part":
					{
						PartIcon partIcon = new PartIcon(getIcon(o.get("parent")));
						partIcon.posX = o.get("x").getAsInt();
						partIcon.posY = o.get("y").getAsInt();
						partIcon.width = o.get("width").getAsInt();
						partIcon.height = o.get("height").getAsInt();
						partIcon.corner = o.get("corner").getAsInt();
						partIcon.textureWidth = o.get("texture_width").getAsInt();
						partIcon.textureHeight = o.get("texture_height").getAsInt();
						return partIcon;
					}
				}
			}
		}
		else if (json.isJsonArray())
		{
			List<Icon> list = new ArrayList<>();

			for (JsonElement e : json.getAsJsonArray())
			{
				list.add(getIcon(e));
			}

			return CombinedIcon.getCombined(list);
		}

		String s = json.getAsString();

		if (s.isEmpty())
		{
			return EMPTY;
		}

		Icon icon = IconPresets.MAP.get(s);
		return icon == null ? getIcon(s) : icon;
	}

	public static Icon getIcon(ResourceLocation id)
	{
		return getIcon(id.toString());
	}

	public static Icon getIcon(String id)
	{
		if (id.isEmpty())
		{
			return EMPTY;
		}

		String[] comb = id.split(" \\+ ");

		if (comb.length > 1)
		{
			ArrayList<Icon> list = new ArrayList<>(comb.length);

			for (String s : comb)
			{
				list.add(getIcon(s));
			}

			return CombinedIcon.getCombined(list);
		}

		String[] ids = id.split("; ");

		for (int i = 0; i < ids.length; i++)
		{
			ids[i] = ids[i].trim();
		}

		Icon icon = getIcon0(ids[0]);

		if (ids.length > 1 && !icon.isEmpty())
		{
			IconProperties properties = new IconProperties();

			for (int i = 1; i < ids.length; i++)
			{
				String[] p = ids[i].split("=", 2);
				properties.set(p[0], p.length == 1 ? "1" : p[1]);
			}

			icon.setProperties(properties);

			int padding = properties.getInt("padding", 0);
			if (padding != 0)
			{
				icon = icon.withPadding(padding);
			}

			Color4I border = properties.getColor("border");
			if (border != null)
			{
				icon = icon.withBorder(border, properties.getBoolean("border_round_edges", false));
			}

			Color4I color = properties.getColor("color");
			if (color != null)
			{
				icon = icon.withColor(color);
			}

			Color4I tint = properties.getColor("tint");
			if (tint != null)
			{
				icon = icon.withTint(tint);
			}
		}

		return icon;
	}

	private static Icon getIcon0(String id)
	{
		if (id.isEmpty() || id.equals("none"))
		{
			return Icon.EMPTY;
		}

		Color4I col = Color4I.fromString(id);

		if (!col.isEmpty())
		{
			return col;
		}

		String[] ida = id.split(":", 2);

		if (ida.length == 2)
		{
			switch (ida[0])
			{
				case "color":
					return Color4I.fromString(ida[1]);
				case "item":
					return ItemIcon.getItemIcon(ida[1]);
				case "bullet":
					return new BulletIcon().withColor(Color4I.fromString(ida[1]));
				case "http":
				case "https":
				case "file":
					try
					{
						return new URLImageIcon(new URI(id));
					}
					catch (Exception ex)
					{
					}
				case "hollow_rectangle":
					return new HollowRectangleIcon(Color4I.fromString(ida[1]), false);
				case "part":
					return new PartIcon(getIcon(ida[1]));
			}
		}

		return (id.endsWith(".png") || id.endsWith(".jpg")) ? new ImageIcon(new ResourceLocation(id)) : new AtlasSpriteIcon(new ResourceLocation(id));
	}

	public boolean isEmpty()
	{
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public void bindTexture()
	{
	}

	public Icon copy()
	{
		return this;
	}

	public JsonElement getJson()
	{
		return new JsonPrimitive(toString());
	}

	public final Icon combineWith(Icon icon)
	{
		if (icon.isEmpty())
		{
			return this;
		}
		else if (isEmpty())
		{
			return icon;
		}

		return new CombinedIcon(this, icon);
	}

	public final Icon combineWith(Icon... icons)
	{
		if (icons.length == 0)
		{
			return this;
		}
		else if (icons.length == 1)
		{
			return combineWith(icons[0]);
		}

		List<Icon> list = new ArrayList<>(icons.length + 1);
		list.add(this);
		list.addAll(Arrays.asList(icons));
		return CombinedIcon.getCombined(list);
	}

	public Icon withColor(Color4I color)
	{
		return copy();
	}

	public final Icon withBorder(Color4I color, boolean roundEdges)
	{
		if (color.isEmpty())
		{
			return withPadding(1);
		}

		return new IconWithBorder(this, color, roundEdges);
	}

	public final Icon withPadding(int padding)
	{
		return padding == 0 ? this : new IconWithPadding(this, padding);
	}

	public Icon withTint(Color4I color)
	{
		return this;
	}

	public Icon withUV(float u0, float v0, float u1, float v1)
	{
		return this;
	}

	public Icon withUV(float x, float y, float w, float h, float tw, float th)
	{
		return withUV(x / tw, y / th, (x + w) / tw, (y + h) / th);
	}

	public int hashCode()
	{
		return getJson().hashCode();
	}

	public boolean equals(Object o)
	{
		return o == this || o instanceof Icon && getJson().equals(((Icon) o).getJson());
	}

	/**
	 * @return false if this should be queued for rendering
	 */
	public boolean hasPixelBuffer()
	{
		return false;
	}

	/**
	 * @return null if failed to load
	 */
	@Nullable
	public IPixelBuffer createPixelBuffer()
	{
		return null;
	}

	@Nullable
	public Object getIngredient()
	{
		return null;
	}

	protected void setProperties(IconProperties properties)
	{
	}
}