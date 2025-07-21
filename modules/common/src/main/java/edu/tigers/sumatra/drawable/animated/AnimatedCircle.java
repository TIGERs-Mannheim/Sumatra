/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;

import java.awt.Color;
import java.awt.Graphics2D;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class AnimatedCircle extends AAnimatedShape
{
	private INumberAnimator radius;


	protected AnimatedCircle()
	{
		super();
	}


	/**
	 * @param center
	 * @param radius
	 */
	public AnimatedCircle(final IVector2 center, final INumberAnimator radius)
	{
		super(center);
		this.radius = radius;
	}


	/**
	 * @param center
	 * @param radius
	 * @param lineColor
	 * @param fillColor
	 */
	public AnimatedCircle(final IVector2 center, final INumberAnimator radius, final IColorAnimator lineColor,
			final IColorAnimator fillColor)
	{
		super(center, lineColor, fillColor);
		this.radius = radius;
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 guiCenter = tool.transformToGuiCoordinates(getCenter(), invert);
		final double guiRadius = tool.scaleGlobalToGui(radius.getNumber());

		if (getFillColor() != null)
		{
			g.setColor(getFillColor().getColor());
			g.fillOval((int) (guiCenter.x() - guiRadius), (int) (guiCenter.y() - guiRadius), (int) guiRadius * 2,
					(int) guiRadius * 2);
		}

		g.setColor(getLineColor().getColor());
		g.drawOval((int) (guiCenter.x() - guiRadius), (int) (guiCenter.y() - guiRadius), (int) guiRadius * 2,
				(int) guiRadius * 2);
	}


	/**
	 * @param radius the radius to set
	 * @return
	 */
	public AnimatedCircle withRadius(final INumberAnimator radius)
	{
		this.radius = radius;
		return this;
	}


	/**
	 * @return the radius
	 */
	public INumberAnimator getRadius()
	{
		return radius;
	}


	/**
	 * Create a circle with continuously increasing/decreasing size.
	 *
	 * @param center
	 * @param minSize
	 * @param maxSize
	 * @param pulsePeriod
	 * @param lineColor
	 * @return
	 */
	public static AnimatedCircle aCircleWithPulsingSize(final IVector2 center, final double minSize, final float maxSize,
			final double pulsePeriod, final Color lineColor)
	{
		return new AnimatedCircle(center, new NumberAnimatorMinMax(minSize, maxSize, new AnimationTimerSine(pulsePeriod)),
				new ColorAnimatorFixed(lineColor), null);
	}


	/**
	 * Create a circle which grows from min to max size and then starts again at min size.
	 *
	 * @param center
	 * @param minSize
	 * @param maxSize
	 * @param pulsePeriod
	 * @param lineColor
	 * @return
	 */
	public static AnimatedCircle aCircleWithGrowingSize(final IVector2 center, final double minSize, final float maxSize,
			final double pulsePeriod, final Color lineColor)
	{
		return new AnimatedCircle(center, new NumberAnimatorMinMax(minSize, maxSize, new AnimationTimerUp(pulsePeriod)),
				new ColorAnimatorFixed(lineColor), null);
	}


	/**
	 * Create a filled circle which grows from min to max size and then starts again at min size.
	 *
	 * @param center
	 * @param minSize
	 * @param maxSize
	 * @param pulsePeriod
	 * @param lineColor
	 * @param fillColor
	 * @return
	 */
	public static AnimatedCircle aFilledCircleWithGrowingSize(final IVector2 center, final double minSize,
			final double maxSize,
			final double pulsePeriod, final Color lineColor, final Color fillColor)
	{
		return new AnimatedCircle(center, new NumberAnimatorMinMax(minSize, maxSize, new AnimationTimerUp(pulsePeriod)),
				new ColorAnimatorFixed(lineColor), new ColorAnimatorFixed(fillColor));
	}


	/**
	 * Create a circle which shrinks from max to min size and then starts again at max size.
	 *
	 * @param center
	 * @param minSize
	 * @param maxSize
	 * @param pulsePeriod
	 * @param lineColor
	 * @return
	 */
	public static AnimatedCircle aCircleWithShrinkingSize(final IVector2 center, final double minSize,
			final double maxSize, final float pulsePeriod, final Color lineColor)
	{
		return new AnimatedCircle(center, new NumberAnimatorMinMax(minSize, maxSize, new AnimationTimerDown(pulsePeriod)),
				new ColorAnimatorFixed(lineColor), null);
	}


	/**
	 * Create a filled circle which shrinks from max to min size and then starts again at max size.
	 *
	 * @param center
	 * @param minSize
	 * @param maxSize
	 * @param pulsePeriod
	 * @param lineColor
	 * @param fillColor
	 * @return
	 */
	public static AnimatedCircle aFilledCircleWithShrinkingSize(final IVector2 center, final double minSize,
			final double maxSize, final float pulsePeriod, final Color lineColor, final Color fillColor)
	{
		return new AnimatedCircle(center, new NumberAnimatorMinMax(minSize, maxSize, new AnimationTimerDown(pulsePeriod)),
				new ColorAnimatorFixed(lineColor), new ColorAnimatorFixed(fillColor));
	}


	/**
	 * Create a filled circle with continuously increasing/decreasing size.
	 *
	 * @param center
	 * @param minSize
	 * @param maxSize
	 * @param pulsePeriod
	 * @param lineColor
	 * @param fillColor
	 * @return
	 */
	public static AnimatedCircle aFilledCircleWithPulsingSize(final IVector2 center, final double minSize,
			final double maxSize, final float pulsePeriod, final Color lineColor, final Color fillColor)
	{
		return new AnimatedCircle(center, new NumberAnimatorMinMax(minSize, maxSize, new AnimationTimerSine(pulsePeriod)),
				new ColorAnimatorFixed(lineColor), new ColorAnimatorFixed(fillColor));
	}


	/**
	 * Create a fixed-size circle with varying line color.
	 *
	 * @param circle
	 * @param firstColor
	 * @param secondColor
	 * @param pulsePeriod
	 * @return
	 */
	public static AnimatedCircle aCircleWithPulsingLineColor(final ICircle circle, final Color firstColor,
			final Color secondColor, final double pulsePeriod)
	{
		return new AnimatedCircle(circle.center(), new NumberAnimatorFixed(circle.radius()),
				new ColorAnimatorMixTwo(firstColor, secondColor, new AnimationTimerSine(pulsePeriod)), null);
	}


	/**
	 * Create a filled fixed-size circle with varying color.
	 *
	 * @param circle
	 * @param firstColor
	 * @param secondColor
	 * @param pulsePeriod
	 * @return
	 */
	public static AnimatedCircle aFilledCircleWithPulsingColor(final ICircle circle, final Color firstColor,
			final Color secondColor, final double pulsePeriod)
	{
		IColorAnimator animator = new ColorAnimatorMixTwo(firstColor, secondColor, new AnimationTimerSine(pulsePeriod));

		return new AnimatedCircle(circle.center(), new NumberAnimatorFixed(circle.radius()),
				animator, animator);
	}
}
