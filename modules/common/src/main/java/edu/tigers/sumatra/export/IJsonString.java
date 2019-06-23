/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 22, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.export;

import org.json.simple.JSONObject;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IJsonString
{
	/**
	 * Return a valid JSON string
	 * 
	 * @return
	 */
	JSONObject toJSON();
}
