/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import java.awt.Color;

import com.sleepycat.persist.model.Persistent;


/**
 * Fade between two colors.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class ColorAnimatorMixTwo implements IColorAnimator
{
	private final Color colorA;
	private final Color colorB;
	private final IAnimationTimer timer;
	
	
	@SuppressWarnings("unused")
	private ColorAnimatorMixTwo()
	{
		colorA = Color.BLACK;
		colorB = Color.WHITE;
		timer = null;
	}
	
	
	/**
	 * @param colorA
	 * @param colorB
	 * @param timer
	 */
	public ColorAnimatorMixTwo(final Color colorA, final Color colorB, final IAnimationTimer timer)
	{
		this.colorA = colorA;
		this.colorB = colorB;
		this.timer = timer;
	}
	
	
	@Override
	public Color getColor()
	{
		float counterValue = timer.getTimerValue();
		
		int r = (int) (((1.0f - counterValue) * colorA.getRed()) + (counterValue * colorB.getRed()));
		int g = (int) (((1.0f - counterValue) * colorA.getGreen()) + (counterValue * colorB.getGreen()));
		int b = (int) (((1.0f - counterValue) * colorA.getBlue()) + (counterValue * colorB.getBlue()));
		int a = (int) (((1.0f - counterValue) * colorA.getAlpha()) + (counterValue * colorB.getAlpha()));
		
		return new Color(r, g, b, a);
	}
}
