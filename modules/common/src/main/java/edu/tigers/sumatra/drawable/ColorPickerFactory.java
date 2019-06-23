/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;


/**
 * Create color pickers
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class ColorPickerFactory
{
	
	private static final List<Color> colors = new ArrayList<>();
	
	static
	{
		colors.add(new Color(0x00E52A));
		colors.add(new Color(0x00E422));
		colors.add(new Color(0x00E41A));
		colors.add(new Color(0x00E413));
		colors.add(new Color(0x00E40B));
		colors.add(new Color(0x00E404));
		colors.add(new Color(0x05E400));
		colors.add(new Color(0x0DE401));
		colors.add(new Color(0x14E401));
		colors.add(new Color(0x1CE401));
		colors.add(new Color(0x24E401));
		colors.add(new Color(0x2CE401));
		colors.add(new Color(0x33E401));
		colors.add(new Color(0x3BE401));
		colors.add(new Color(0x43E402));
		colors.add(new Color(0x4BE402));
		colors.add(new Color(0x52E302));
		colors.add(new Color(0x5AE302));
		colors.add(new Color(0x62E302));
		colors.add(new Color(0x69E302));
		colors.add(new Color(0x71E302));
		colors.add(new Color(0x79E303));
		colors.add(new Color(0x80E303));
		colors.add(new Color(0x88E303));
		colors.add(new Color(0x8FE303));
		colors.add(new Color(0x97E303));
		colors.add(new Color(0x9FE303));
		colors.add(new Color(0xA6E303));
		colors.add(new Color(0xAEE304));
		colors.add(new Color(0xB5E304));
		colors.add(new Color(0xBDE304));
		colors.add(new Color(0xC4E304));
		colors.add(new Color(0xCCE204));
		colors.add(new Color(0xD3E204));
		colors.add(new Color(0xDBE204));
		colors.add(new Color(0xE2E205));
		colors.add(new Color(0xE2DB05));
		colors.add(new Color(0xE2D305));
		colors.add(new Color(0xE2CC05));
		colors.add(new Color(0xE2C405));
		colors.add(new Color(0xE2BD05));
		colors.add(new Color(0xE2B505));
		colors.add(new Color(0xE2AE06));
		colors.add(new Color(0xE2A706));
		colors.add(new Color(0xE29F06));
		colors.add(new Color(0xE29806));
		colors.add(new Color(0xE29006));
		colors.add(new Color(0xE28906));
		colors.add(new Color(0xE18206));
		colors.add(new Color(0xE17A07));
		colors.add(new Color(0xE17307));
		colors.add(new Color(0xE16B07));
		colors.add(new Color(0xE16407));
		colors.add(new Color(0xE15D07));
		colors.add(new Color(0xE15607));
		colors.add(new Color(0xE14E07));
		colors.add(new Color(0xE14708));
		colors.add(new Color(0xE14008));
		colors.add(new Color(0xE13908));
		colors.add(new Color(0xE13108));
		colors.add(new Color(0xE12A08));
		colors.add(new Color(0xE12308));
		colors.add(new Color(0xE11C08));
		colors.add(new Color(0xE11408));
	}
	
	
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
	public static IColorPicker scaledSingleBlack(final double r, final double g, final double b, final int colorId)
	{
		return scaledSingleBlack(r, g, b, 1.0, colorId);
	}
	
	
	/**
	 * Scale color between black and r,g or b with respect to the alpha channel.
	 * colorId chooses which color to scale. The r/g/b param for this color will be ignored
	 * 
	 * @param r red
	 * @param g green
	 * @param b blue
	 * @param alpha alpha channel
	 * @param colorId one of 1(red),2(green),3(blue)
	 * @return
	 */
	public static IColorPicker scaledSingleBlack(final double r, final double g, final double b, final double alpha,
			final int colorId)
	{
		return new IColorPicker()
		{
			
			@Override
			public Color applyColor(final Graphics2D g2, final double relValue)
			{
				Color color = getColor(relValue);
				g2.setColor(color);
				return color;
			}
			
			
			@Override
			public Color getColor(final double relValue)
			{
				return colorIdToColor((float) relValue, colorId, (float) g, (float) b, (float) alpha, (float) r);
			}
		};
	}
	
	
	private static Color colorIdToColor(final float relValue, final int colorId, final float g, final float b,
			final float alpha, final float r)
	{
		switch (colorId)
		{
			case 1:
				return new Color(relValue, g, b, alpha);
			case 2:
				return new Color(r, relValue, b, alpha);
			case 3:
				return new Color(r, g, relValue, alpha);
			default:
				throw new IllegalArgumentException("Invalid colorId: " + colorId);
		}
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
			public Color applyColor(final Graphics2D g, final double relValue)
			{
				Color color = getColor(relValue);
				g.setColor(color);
				return color;
			}
			
			
			@Override
			public Color getColor(final double relValue)
			{
				Validate.isTrue(relValue >= 0);
				Validate.isTrue(relValue <= 1);
				int red = (int) ((color2.getRed() * relValue) + (color1.getRed() * (1 - relValue)));
				int green = (int) ((color2.getGreen() * relValue) + (color1.getGreen() * (1 - relValue)));
				int blue = (int) ((color2.getBlue() * relValue) + (color1.getBlue() * (1 - relValue)));
				return new Color(red, green, blue);
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
			@Override
			public Color applyColor(final Graphics2D g, final double relValue)
			{
				final int type = AlphaComposite.SRC_OVER;
				g.setColor(color);
				g.setComposite(AlphaComposite.getInstance(type, (float) relValue));
				return color;
			}
			
			
			@Override
			public Color getColor(final double relValue)
			{
				return color;
			}
		};
	}
	
	
	/**
	 * Get color from a static color map, scaled from red (zero) to green (one)
	 * 
	 * @return
	 */
	public static IColorPicker greenRedGradient()
	{
		return new IColorPicker()
		{
			
			@Override
			public Color getColor(final double relValue)
			{
				return gradientFromList(relValue);
			}
			
			
			@Override
			public Color applyColor(final Graphics2D g, final double relValue)
			{
				Color c = getColor(relValue);
				g.setColor(c);
				return c;
			}
		};
	}
	
	
	private static Color gradientFromList(final double relValue)
	{
		double step = 1.0 / colors.size();
		for (int i = 0; i < colors.size(); i++)
		{
			double val = (i + 1) * step;
			if (relValue <= val)
			{
				return colors.get(i);
			}
		}
		return Color.black;
	}
	
	
	public static IColorPicker invert(IColorPicker colorPicker)
	{
		return new IColorPicker()
		{
			@Override
			public Color applyColor(final Graphics2D g, final double relValue)
			{
				return colorPicker.applyColor(g, 1 - relValue);
			}
			
			
			@Override
			public Color getColor(final double relValue)
			{
				return colorPicker.getColor(1 - relValue);
			}
		};
	}
}
