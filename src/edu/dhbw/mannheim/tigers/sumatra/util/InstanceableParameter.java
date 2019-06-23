/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;


/**
 * Parameter of a {@link InstanceableClass}.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class InstanceableParameter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Class<?>	impl;
	private final String		description;
	private final String		defaultValue;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param impl
	 * @param description
	 * @param defaultValue
	 */
	public InstanceableParameter(Class<?> impl, String description, String defaultValue)
	{
		this.impl = impl;
		this.description = description;
		this.defaultValue = defaultValue;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Parse given String to value
	 * @param value
	 * @return
	 */
	public Object parseString(String value)
	{
		return String2ValueConverter.parseString(impl, value);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the impl
	 */
	public final Class<?> getImpl()
	{
		return impl;
	}
	
	
	/**
	 * @return the description
	 */
	public final String getDescription()
	{
		return description;
	}
	
	
	/**
	 * @return the defaultValue
	 */
	public final String getDefaultValue()
	{
		return defaultValue;
	}
}
