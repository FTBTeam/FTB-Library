package com.feed_the_beast.mods.ftbguilibrary.newui;

import com.feed_the_beast.mods.ftbguilibrary.newui.event.MousePressedEvent;
import com.feed_the_beast.mods.ftbguilibrary.newui.event.MouseReleasedEvent;
import com.feed_the_beast.mods.ftbguilibrary.newui.event.MouseScrolledEvent;
import com.feed_the_beast.mods.ftbguilibrary.newui.event.PositionUpdateEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class Panel extends Widget
{
	public final List<Widget> widgets;
	private boolean onlyRenderWidgetsInside = true;
	private boolean onlyInteractWithWidgetsInside = true;
	private double contentWidth = Double.NaN, contentHeight = Double.NaN;
	public int contentWidthExtra = 0, contentHeightExtra = 0;

	public Panel()
	{
		type = "panel";
		widgets = new ArrayList<>();
	}

	public Panel add(String id, Widget widget)
	{
		widget.id = id.isEmpty() ? (widget.type + "_" + (widgets.size() + 1)) : id;
		widget.ui = ui;
		widget.panel = this;
		widgets.add(widget);
		return this;
	}

	public Panel add(Widget widget)
	{
		return add("", widget);
	}

	public Panel addPanel(String id, Consumer<Panel> callback)
	{
		Panel panel = new Panel();
		panel.id = id;
		callback.accept(panel);
		return add(panel);
	}

	public Panel addPanel(Consumer<Panel> callback)
	{
		return addPanel("", callback);
	}

	public boolean getOnlyRenderWidgetsInside()
	{
		return onlyRenderWidgetsInside;
	}

	public void setOnlyRenderWidgetsInside(boolean value)
	{
		onlyRenderWidgetsInside = value;
	}

	public boolean getOnlyInteractWithWidgetsInside()
	{
		return onlyInteractWithWidgetsInside;
	}

	public void setOnlyInteractWithWidgetsInside(boolean value)
	{
		onlyInteractWithWidgetsInside = value;
	}

	public double getContentWidth()
	{
		if (Double.isNaN(contentWidth))
		{
			if (widgets.isEmpty())
			{
				return contentWidthExtra;
			}

			double minX = Double.POSITIVE_INFINITY;
			double maxX = Double.NEGATIVE_INFINITY;

			for (Widget w : widgets)
			{
				if (w.rx < minX)
				{
					minX = w.rx;
				}

				if (w.rx + w.width > maxX)
				{
					maxX = w.rx + w.width;
				}
			}

			contentWidth = maxX - minX + contentWidthExtra;
		}

		return contentWidth;
	}

	public double getContentHeight()
	{
		if (Double.isNaN(contentHeight))
		{
			if (widgets.isEmpty())
			{
				return contentHeightExtra;
			}

			double minY = Double.POSITIVE_INFINITY;
			double maxY = Double.NEGATIVE_INFINITY;

			for (Widget w : widgets)
			{
				if (w.ry < minY)
				{
					minY = w.ry;
				}

				if (w.ry + w.height > maxY)
				{
					maxY = w.ry + w.height;
				}
			}

			contentHeight = maxY - minY + contentHeightExtra;
		}

		return contentHeight;
	}

	@Override
	public void updatePosition(PositionUpdateEvent event)
	{
		super.updatePosition(event);

		for (int i = widgets.size() - 1; i >= 0; i--)
		{
			widgets.get(i).updatePosition(event);
		}
	}

	@Override
	public boolean mousePressed(MousePressedEvent event)
	{
		if (getOnlyInteractWithWidgetsInside() && !mouseOver)
		{
			return false;
		}

		for (int i = widgets.size() - 1; i >= 0; i--)
		{
			Widget widget = widgets.get(i);

			if (widget.isEnabled())
			{
				if (widget.mousePressed(event))
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void mouseReleased(MouseReleasedEvent event)
	{
		for (int i = widgets.size() - 1; i >= 0; i--)
		{
			Widget widget = widgets.get(i);

			if (widget.isEnabled())
			{
				widget.mouseReleased(event);
			}
		}
	}

	@Override
	public boolean mouseScrolled(MouseScrolledEvent event)
	{
		for (int i = widgets.size() - 1; i >= 0; i--)
		{
			Widget widget = widgets.get(i);

			if (widget.isEnabled())
			{
				if (widget.mouseScrolled(event))
				{
					return true;
				}
			}
		}

		return scrollPanel(event);
	}

	public boolean scrollPanel(MouseScrolledEvent event)
	{
		return false;
	}
}