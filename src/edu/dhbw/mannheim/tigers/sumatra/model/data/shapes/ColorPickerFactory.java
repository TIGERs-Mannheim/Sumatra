/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 7, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;


/**
 * Create color pickers
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class ColorPickerFactory
{
	
	private ColorPickerFactory()
	{
	}
	
	
	/**
	 * Scale color between black and r,g or b.
	 * colorId chooses which color to scale. The r/g/b param for this color will be ignored
	 * 
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @param colorId one of 1(red),2(green),3(blue)
	 * @return
	 */
	public static IColorPicker scaledSingleBlack(final float r, final float g, final float b, final int colorId)
	{
		return new IColorPicker()
		{
			
			@Override
			public Color applyColor(final Graphics2D g2, final float relValue)
			{
				Color color;
				switch (colorId)
				{
					case 1:
						color = new Color(relValue, g, b);
						break;
					case 2:
						color = new Color(r, relValue, b);
						break;
					case 3:
						color = new Color(r, g, relValue);
						break;
					default:
						throw new IllegalArgumentException("Invalid colorId: " + colorId);
				}
				g2.setColor(color);
				return color;
			}
		};
	}
	
	
	/**
	 * Scale color between two given colors
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	public static IColorPicker scaledDouble(final Color color1, final Color color2)
	{
		return new IColorPicker()
		{
			
			@Override
			public Color applyColor(final Graphics2D g, final float relValue)
			{
				int red = (int) ((color2.getRed() * relValue) + (color1.getRed() * (1 - relValue)));
				int green = (int) ((color2.getGreen() * relValue) + (color1.getGreen() * (1 - relValue)));
				int blue = (int) ((color2.getBlue() * relValue) + (color1.getBlue() * (1 - relValue)));
				Color stepColor = new Color(red, green, blue);
				g.setColor(stepColor);
				return stepColor;
			}
		};
	}
	
	
	/**
	 * Scale single color by changing transparency
	 * 
	 * @param color
	 * @return
	 */
	public static IColorPicker scaledTransparent(final Color color)
	{
		return new IColorPicker()
		{
			final int	type	= AlphaComposite.SRC_OVER;
			
			
			@Override
			public Color applyColor(final Graphics2D g, final float relValue)
			{
				g.setColor(color);
				g.setComposite(AlphaComposite.getInstance(type, relValue));
				return color;
			}
		};
	}
}
