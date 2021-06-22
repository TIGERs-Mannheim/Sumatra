/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Locale;


/**
 * Parameters for ExtIdentifier
 */
@Data
@AllArgsConstructor
public class ExtIdentifierParams
{
	private double minValue;
	private double maxValue;
	private double chargeTime;


	/**
	 * @return
	 */
	public static ExtIdentifierParams createDefault()
	{
		return new ExtIdentifierParams(0, 0, 0);
	}


	/**
	 * @param str
	 * @return
	 */
	public static ExtIdentifierParams valueOf(final String str)
	{
		String[] strParts = str.split(",");
		if (strParts.length != 3)
		{
			throw new IllegalArgumentException("Invalid string: " + str);
		}
		return new ExtIdentifierParams(
				Double.parseDouble(strParts[0]),
				Double.parseDouble(strParts[1]),
				Double.parseDouble(strParts[2]));
	}


	/**
	 * Get a string that is parseable by {@link ExtIdentifierParams#valueOf(String)}
	 *
	 * @return
	 */
	public String getParseableString()
	{
		return String.format(Locale.ENGLISH, "%.4f,%.4f,%.4f", minValue, maxValue, chargeTime);
	}


	@Override
	public String toString()
	{
		return String.format(Locale.ENGLISH, "[%.2f;%.2f;%.2f]", minValue, maxValue, chargeTime);
	}
}
