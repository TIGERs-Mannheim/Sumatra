/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.util;

import edu.tigers.sumatra.drawable.EFontSize;

import java.util.EnumMap;
import java.util.Map;


/**
 * Util class for getting default GUI dimensions.
 * Needed for the GUI to scale properly on high DPI screens with gui scaling.
 */
public class ScalingUtil
{
	private static final Map<EFontSize, Integer> FONT_SIZES = new EnumMap<>(EFontSize.class);
	private static int imageButtonSize;
	private static int tableRowHeight;
	private static double baselineSize = 10;

	// Size of various GUI elements relative to the default text size set by the selected LookAndFeel
	private static final double TABLE_ROW_FACTOR = 1.5;
	private static final double IMAGE_BUTTON_FACTOR = 2.25;

	static
	{
		// Make sure the sizes are initialized, even when running without a UI
		update();
	}

	private ScalingUtil()
	{
	}


	public static void updateBaselineSize(double baselineSize)
	{
		ScalingUtil.baselineSize = baselineSize;
		update();
	}


	public static int getFontSize(EFontSize fontSize)
	{
		return FONT_SIZES.get(fontSize);
	}


	public static int getImageButtonSize()
	{
		return imageButtonSize;
	}


	public static int getTableRowHeight()
	{
		return tableRowHeight;
	}


	public static int scale(double value)
	{
		return (int) Math.ceil(baselineSize * value);
	}


	/**
	 * Gets the default font size of the active LookAndFeel.
	 * If the LookAndFeel supports GUI scaling this value will depend on the scale factor.
	 * This function must only be called after the LookAndFeel has been set (in MainPresenter.onSelectLookAndFeel)
	 */
	private static void update()
	{
		for (EFontSize fontSize : EFontSize.values())
		{
			FONT_SIZES.put(fontSize, scale(fontSize.getScaleFactor()));
		}

		imageButtonSize = scale(IMAGE_BUTTON_FACTOR);
		tableRowHeight = scale(TABLE_ROW_FACTOR);
	}
}
