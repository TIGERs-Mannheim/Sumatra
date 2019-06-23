/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;

import java.util.Locale;

import edu.tigers.sumatra.math.SumatraMath;


/**
 * Identifier for components for RCM module with extended information
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ExtIdentifier
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final ExtIdentifier UNDEFINED = new ExtIdentifier("", ExtIdentifierParams.createDefault());
	private ExtIdentifierParams params;
	private String identifier;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param identifier
	 * @param params
	 */
	public ExtIdentifier(final String identifier, final ExtIdentifierParams params)
	{
		this.identifier = identifier;
		this.params = params;
	}
	
	
	/**
	 * Create undefined identifier
	 *
	 * @return
	 */
	public static ExtIdentifier undefinedIdentifier()
	{
		return UNDEFINED;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((identifier == null) ? 0 : identifier.hashCode());
		result = (prime * result) + ((params == null) ? 0 : (int) (params.getMinValue() * 100));
		result = (prime * result) + ((params == null) ? 0 : (int) (params.getMaxValue() * 100));
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		ExtIdentifier other = (ExtIdentifier) obj;
		if (identifier == null)
		{
			if (other.identifier != null)
			{
				return false;
			}
		} else if (!identifier.equals(other.identifier))
		{
			return false;
		}
		if (params == null)
		{
			if (other.params != null)
			{
				return false;
			}
		} else if (!SumatraMath.isEqual(params.getMinValue(), other.getParams().getMinValue()) ||
				!SumatraMath.isEqual(params.getMaxValue(), other.getParams().getMaxValue()))
		{
			return false;
		}
		return true;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the params
	 */
	public ExtIdentifierParams getParams()
	{
		return params;
	}
	
	
	/**
	 * @return the identifier
	 */
	public String getIdentifier()
	{
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
