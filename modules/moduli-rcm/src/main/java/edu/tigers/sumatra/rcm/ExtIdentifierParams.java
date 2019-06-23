/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 2, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.rcm;

import java.util.Locale;


/**
 * Parameters for ExtIdentifier
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ExtIdentifierParams
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private double	minValue;
	private double	maxValue;
	private double	chargeTime;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param minValue
	 * @param maxValue
	 * @param chargeTime
	 */
	public ExtIdentifierParams(final double minValue, final double maxValue, final double chargeTime)
	{
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.chargeTime = chargeTime;
	}
	
	
	/**
	 * @return
	 */
	public static ExtIdentifierParams createDefault()
	{
		return new ExtIdentifierParams(0, 0, 0);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
		return new ExtIdentifierParams(Double.valueOf(strParts[0]), Double.valueOf(strParts[1]),
				Double.valueOf(strParts[2]));
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
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the minValue
	 */
	public final double getMinValue()
	{
		return minValue;
	}
	
	
	/**
	 * @return the maxValue
	 */
	public final double getMaxValue()
	{
		return maxValue;
	}
	
	
	/**
	 * @return the chargeTime
	 */
	public final double getChargeTime()
	{
		return chargeTime;
	}
	
	
	/**
	 * @param minValue the minValue to set
	 */
	public final void setMinValue(final double minValue)
	{
		this.minValue = minValue;
	}
	
	
	/**
	 * @param maxValue the maxValue to set
	 */
	public final void setMaxValue(final double maxValue)
	{
		this.maxValue = maxValue;
	}
	
	
	/**
	 * @param chargeTime the chargeTime to set
	 */
	public final void setChargeTime(final double chargeTime)
	{
		this.chargeTime = chargeTime;
	}
}
