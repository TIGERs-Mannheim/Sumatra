/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.export;

import com.github.cliftonlabs.json_simple.JsonObject;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@FunctionalInterface
public interface IJsonString
{
	/**
	 * Return a valid JSON string
	 * 
	 * @return
	 */
	JsonObject toJSON();
}
