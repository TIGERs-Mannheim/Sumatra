/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Locale;


/**
 * Identifier for components for RCM module with extended information
 */
@RequiredArgsConstructor
@Data
public class ExtIdentifier
{
	private final String identifier;
	private final ExtIdentifierParams params;


	/**
	 * @param str
	 * @return
	 */
	public static ExtIdentifier valueOf(final String str)
	{
		String[] strParts = str.split(";");
		if (strParts.length != 2)
		{
			throw new IllegalArgumentException("Invalid string: " + str);
		}
		String identifier = strParts[0];
		ExtIdentifierParams params = ExtIdentifierParams.valueOf(strParts[1]);
		return new ExtIdentifier(identifier, params);
	}


	@Override
	public String toString()
	{
		if ((Math.abs(params.getMinValue()) > 0.00001) || (Math.abs(params.getMaxValue()) > 0.00001))
		{
			return String.format(Locale.ENGLISH, "%s [%.1f;%.1f]", identifier, params.getMinValue(), params.getMaxValue());
		}
		return identifier;
	}


	/**
	 * @return
	 */
	public String getExtIdentifier()
	{
		return identifier + ";" + params.getParseableString();
	}
}
