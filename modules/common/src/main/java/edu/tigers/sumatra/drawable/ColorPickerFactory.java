/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.SumatraMath;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;


/**
 * Create color pickers
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColorPickerFactory
{
	private static final List<Color> COLORS = List.of(
			new Color(0xba55d3),
			new Color(0x1e90ff),
			new Color(0xfa8072),
			new Color(0xdda0dd),
			new Color(0xff1493),
			new Color(0x98fb98),
			new Color(0x87cefa),
			new Color(0xffe4b5),
			new Color(0x2f4f4f),
			new Color(0x2e8b57),
			new Color(0x800000),
			new Color(0x808000),
			new Color(0x000080),
			new Color(0xffa500)
	);


	/**
	 * Get a color from a static color map with visually distinct colors.
	 *
	 * @param index
	 * @return
	 */
	public static Color getDistinctColor(final int index)
	{
		return COLORS.get(index % COLORS.size());
	}


	/**
	 * Scale color between black and r,g or b.
	 * colorId chooses which color to scale. The r/g/b param for this color will be ignored
	 *
	 * @param r       red
	 * @param g       green
	 * @param b       blue
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
	 * @param r       red
	 * @param g       green
	 * @param b       blue
	 * @param alpha   alpha channel
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
				float value = (float) SumatraMath.cap(relValue, 0, 1);

				float[] hsb1 = new float[3];
				float[] hsb2 = new float[3];
				Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), hsb1);
				Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), hsb2);

				float[] hsb = new float[3];
				for (int i = 0; i < 3; i++)
				{
					hsb[i] = (hsb2[i] * value) + (hsb1[i] * (1 - value));
				}
				return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
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
		return ColorPickerFactory.scaledDouble(new Color(0xE11408), new Color(0x00E52A));
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
