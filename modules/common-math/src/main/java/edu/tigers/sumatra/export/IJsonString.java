/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.export;

import org.json.simple.JSONObject;


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
	JSONObject toJSON();
}
