/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.instanceables;

/**
 * Implement this in an enum that provides {@link InstanceableClass}s
 */
public interface IInstanceableEnum
{
	/**
	 * @return the instance class of the enum value
	 */
	InstanceableClass<?> getInstanceableClass();


	/**
	 * @return the name of the enum value (implemented by {@link Enum})
	 */
	String name();


	/**
	 * Parse the given enum name to an enum instance of this enum type ({@link Enum#valueOf(Class, String)})
	 *
	 * @param name the name of the enum value
	 * @return the respective enum instance
	 */
	default IInstanceableEnum parse(String name)
	{
		return null;
	}
}
