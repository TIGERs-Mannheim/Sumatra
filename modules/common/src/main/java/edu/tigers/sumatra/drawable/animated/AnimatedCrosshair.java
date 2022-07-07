/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;


/**
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class AnimatedCrosshair extends AnimatedCircle
{
	private INumberAnimator rotation;


	@SuppressWarnings("unused")
	private AnimatedCrosshair()
	{
		super();
	}


	/**
	 * @param center
	 * @param radius
	 */
	public AnimatedCrosshair(final IVector2 center, final INumberAnimator radius)
	{
		super(center, radius);
		rotation = new NumberAnimatorFixed(0.0f);
	}


	/**
	 * @param center
	 * @param radius
	 * @param lineColor
	 * @param fillColor
	 */
	public AnimatedCrosshair(final IVector2 center, final INumberAnimator radius, final IColorAnimator lineColor,
			final IColorAnimator fillColor)
	{
		super(center, radius, lineColor, fillColor);
		rotation = new NumberAnimatorFixed(0.0f);
	}


	/**
	 * @param center
	 * @param radius
	 * @param rotation
	 * @param lineColor
	 * @param fillColor
	 */
	public AnimatedCrosshair(final IVector2 center, final INumberAnimator radius, final INumberAnimator rotation,
			final IColorAnimator lineColor,
			final IColorAnimator fillColor)
	{
		super(center, radius, lineColor, fillColor);
		this.rotation = rotation;
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		final IVector2 guiCenter = tool.transformToGuiCoordinates(getCenter(), invert);
		final int guiRadius = (int) (tool.scaleGlobalToGui(getRadius().getNumber()) * 1.2);
		final int guiRadiusInner = (int) (tool.scaleGlobalToGui(getRadius().getNumber()) * 0.8);

		AffineTransform old = g.getTransform();
		g.translate(guiCenter.x(), guiCenter.y());
		g.rotate(rotation.getNumber());

		g.drawLine(-guiRadius, 0, -guiRadiusInner, 0);
		g.drawLine(guiRadius, 0, guiRadiusInner, 0);
		g.drawLine(0, -guiRadius, 0, -guiRadiusInner);
		g.drawLine(0, guiRadius, 0, guiRadiusInner);

		// restore old transform
		g.setTransform(old);
	}


	/**
	 * @param rotation the rotation to set
	 * @return
	 */
	public AnimatedCrosshair withRotation(final INumberAnimator rotation)
	{
		this.rotation = rotation;
		return this;
	}


	/**
	 * Continuously rotates the crosshair.
	 *
	 * @param period
	 * @return
	 */
	public AnimatedCrosshair withContinuousRotation(final float period)
	{
		rotation = new NumberAnimatorMinMax(0, (float) AngleMath.PI_TWO, new AnimationTimerUp(period));
		return this;
	}


	/**
	 * Rotates the crosshair continously back and forth.
	 *
	 * @param period
	 * @return
	 */
	public AnimatedCrosshair withBackAndForthRotation(final float period)
	{
		rotation = new NumberAnimatorMinMax(0, (float) AngleMath.PI_TWO, new AnimationTimerSine(period));
		return this;
	}


	/**
	 * Create a crosshair with continuously increasing/decreasing size.
	 *
	 * @param center
	 * @param minSize
	 * @param maxSize
	 * @param pulsePeriod
	 * @param lineColor
	 * @return
	 */
	public static AnimatedCrosshair aCrosshairWithPulsingSize(final IVector2 center, final float minSize,
			final float maxSize, final float pulsePeriod, final Color lineColor)
	{
		return new AnimatedCrosshair(center,
				new NumberAnimatorMinMax(minSize, maxSize, new AnimationTimerSine(pulsePeriod)),
				new ColorAnimatorFixed(lineColor), null);
	}


	/**
	 * Create a filled crosshair with continuously increasing/decreasing size.
	 *
	 * @param center
	 * @param minSize
	 * @param maxSize
	 * @param pulsePeriod
	 * @param lineColor
	 * @param fillColor
	 * @return
	 */
	public static AnimatedCrosshair aFilledCrosshairWithPulsingSize(final IVector2 center, final float minSize,
			final float maxSize, final float pulsePeriod, final Color lineColor, final Color fillColor)
	{
		return new AnimatedCrosshair(center,
				new NumberAnimatorMinMax(minSize, maxSize, new AnimationTimerSine(pulsePeriod)),
				new ColorAnimatorFixed(lineColor), new ColorAnimatorFixed(fillColor));
	}


	/**
	 * Create a fixed-size crosshair that rotates continuously 360°.
	 *
	 * @param circle
	 * @param period
	 * @param lineColor
	 * @return
	 */
	public static AnimatedCrosshair aCrosshairWithContinuousRotation(final ICircle circle,
			final float period, final Color lineColor)
	{
		return new AnimatedCrosshair(circle.center(), new NumberAnimatorFixed((float) circle.radius()),
				new NumberAnimatorMinMax(0, (float) AngleMath.PI_TWO, new AnimationTimerUp(period)),
				new ColorAnimatorFixed(lineColor), null);
	}


	/**
	 * Create a filled fixed-size crosshair that rotates continuously 360°.
	 *
	 * @param circle
	 * @param period
	 * @param lineColor
	 * @param fillColor
	 * @return
	 */
	public static AnimatedCrosshair aFilledCrosshairWithContinuousRotation(final ICircle circle,
			final float period, final Color lineColor, final Color fillColor)
	{
		return new AnimatedCrosshair(circle.center(), new NumberAnimatorFixed((float) circle.radius()),
				new NumberAnimatorMinMax(0, (float) AngleMath.PI_TWO, new AnimationTimerUp(period)),
				new ColorAnimatorFixed(lineColor), new ColorAnimatorFixed(fillColor));
	}


	/**
	 * Create a crazy crosshair that is surely over-animated and will draw too much attention.
	 *
	 * @param center
	 * @param minSize
	 * @param maxSize
	 * @param period
	 * @param lineColor
	 * @param firstFillColor
	 * @param secondFillColor
	 * @return
	 */
	public static AnimatedCrosshair aCrazyCrosshair(final IVector2 center, final float minSize,
			final float maxSize, final float period, final Color lineColor, final Color firstFillColor,
			final Color secondFillColor)
	{
		return new AnimatedCrosshair(center, new NumberAnimatorMinMax(minSize, maxSize, new AnimationTimerSine(period)),
				new NumberAnimatorMinMax(0, (float) AngleMath.PI_TWO, new AnimationTimerUp(period)),
				new ColorAnimatorFixed(lineColor),
				new ColorAnimatorMixTwo(firstFillColor, secondFillColor, new AnimationTimerSine(period)));
	}

}
