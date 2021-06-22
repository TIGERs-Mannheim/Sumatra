/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


/**
 * Format a generic vector to a String.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VectorFormatter
{
	private static final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);

	static
	{
		decimalFormatSymbols.setDecimalSeparator('.');
		decimalFormatSymbols.setGroupingSeparator(',');
	}

	private static final DecimalFormat df = new DecimalFormat("0.000", decimalFormatSymbols);


	public static String format(AVector vector)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		if (vector.getNumDimensions() > 0)
		{
			sb.append(df.format(vector.get(0)));
			for (int d = 1; d < vector.getNumDimensions(); d++)
			{
				sb.append(',');
				sb.append(df.format(vector.get(d)));
			}
			sb.append("|l=");
			sb.append(df.format(vector.getLength()));
			if ((vector.getNumDimensions() > 1) && !vector.getXYVector().isZeroVector())
			{
				sb.append("|a=");
				sb.append(df.format(vector.getXYVector().getAngle()));
			}
		}
		sb.append(']');
		return sb.toString();
	}
}
