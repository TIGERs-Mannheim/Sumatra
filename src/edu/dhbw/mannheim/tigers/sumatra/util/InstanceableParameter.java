/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import java.util.Arrays;
import java.util.List;


/**
 * Parameter of a {@link InstanceableClass}.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class InstanceableParameter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Class<?>			impl;
	private final String				description;
	private final String				defaultValue;
	private final List<Class<?>>	genericsImpls;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param impl
	 * @param description
	 * @param defaultValue
	 * @param genericsImpls
	 */
	public InstanceableParameter(final Class<?> impl, final String description, final String defaultValue,
			final Class<?>... genericsImpls)
	{
		this.impl = impl;
		this.description = description;
		this.defaultValue = defaultValue;
		this.genericsImpls = Arrays.asList(genericsImpls);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Parse given String to value
	 * 
	 * @param value
	 * @return
	 */
	public Object parseString(final String value)
	{
		if (genericsImpls.isEmpty())
		{
			return String2ValueConverter.parseString(impl, value);
		}
		return String2ValueConverter.parseString(impl, genericsImpls, value);
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
	
	
	/**
	 * @return the genericsImpls
	 */
	protected List<Class<?>> getGenericsImpls()
	{
		return genericsImpls;
	}
}
