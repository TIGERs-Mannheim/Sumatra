/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;


/**
 * All animated shapes have a center position, line color, fill color, and stroke width.
 *
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class AAnimatedShape implements IDrawableShape
{
	private IVector2 center;
	private IColorAnimator lineColor;
	private IColorAnimator fillColor;
	private float strokeWidth = 10;
	private transient Stroke stroke;


	@SuppressWarnings("unused")
	protected AAnimatedShape()
	{
		center = Vector2f.ZERO_VECTOR;
		lineColor = new ColorAnimatorFixed(Color.BLACK);
	}


	protected AAnimatedShape(final IVector2 center)
	{
		this.center = center;
		lineColor = new ColorAnimatorFixed(Color.BLACK);
	}


	protected AAnimatedShape(final IVector2 center, final IColorAnimator lineColor, final IColorAnimator fillColor)
	{
		this.center = center;
		this.lineColor = lineColor;
		this.fillColor = fillColor;
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		if (stroke == null)
		{
			stroke = new BasicStroke(tool.scaleGlobalToGui(strokeWidth));
		}
		g.setStroke(stroke);

		g.setColor(lineColor.getColor());
	}


	@Override
	public AAnimatedShape setStrokeWidth(final double strokeWidth)
	{
		this.strokeWidth = (float) strokeWidth;
		return this;
	}


	@Override
	public AAnimatedShape setColor(final Color color)
	{
		lineColor = new ColorAnimatorFixed(color);
		return this;
	}


	/**
	 * @param center the center to set
	 * @return
	 */
	public AAnimatedShape withCenter(final IVector2 center)
	{
		this.center = center;
		return this;
	}


	/**
	 * @param lineColor the lineColor to set
	 * @return
	 */
	public AAnimatedShape withLineColor(final IColorAnimator lineColor)
	{
		this.lineColor = lineColor;
		return this;
	}


	/**
	 * @param fillColor the fillColor to set
	 * @return
	 */
	public AAnimatedShape withFillColor(final IColorAnimator fillColor)
	{
		this.fillColor = fillColor;
		return this;
	}


	/**
	 * @return the center
	 */
	public IVector2 getCenter()
	{
		return center;
	}


	/**
	 * @return the lineColor
	 */
	public IColorAnimator getLineColor()
	{
		return lineColor;
	}


	/**
	 * @return the fillColor
	 */
	public IColorAnimator getFillColor()
	{
		return fillColor;
	}


	/**
	 * @return the strokeWidth
	 */
	public float getStrokeWidth()
	{
		return strokeWidth;
	}
}
