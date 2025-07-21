/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package com.github.g3force.instanceables;

/**
 * Interface for instanceable parameters.
 */
public interface IInstanceableParameter
{
	/**
	 * Parse given String to value
	 *
	 * @param value the value to be parsed
	 * @return the instance of the parsed value
	 */
	Object parseString(String value);

	/**
	 * @return the implementation type
	 */
	Class<?> getImpl();

	/**
	 * @return a description of the parameter
	 */
	String getDescription();

	/**
	 * @return the default value
	 */
	String getDefaultValue();
}
